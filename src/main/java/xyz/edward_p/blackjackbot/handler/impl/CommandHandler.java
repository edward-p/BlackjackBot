package xyz.edward_p.blackjackbot.handler.impl;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import xyz.edward_p.blackjackbot.context.BotContext;
import xyz.edward_p.blackjackbot.context.GameContext;
import xyz.edward_p.blackjackbot.context.UserContext;
import xyz.edward_p.blackjackbot.entity.UserData;
import xyz.edward_p.blackjackbot.game.BlackJack;
import xyz.edward_p.blackjackbot.game.Game;
import xyz.edward_p.blackjackbot.handler.UpdateHandler;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author edward <br/>
 * Date: 4/22/21 2:29 PM <br/>
 * Description:
 */
public class CommandHandler implements UpdateHandler {
    private static final String BASE_COMMAND_REGEX = "^/.*";
    private static final String COMMAND_REGEX = "^/.*@" + BotContext.name + ".*";

    @Override
    public void handle(Update update) {
        Message message = update.message();
        if (message == null
                || (message.chat().type() != Chat.Type.Private && !message.text().matches(COMMAND_REGEX))
                || !message.text().matches(BASE_COMMAND_REGEX)) {
            return;
        }

        String text = message.text();
        String[] args = text.replace("@" + BotContext.name, "").split(" ");

        switch (args[0]) {
            case "/blackjack":
                sendGame(message);
                break;
            case "/transfer":
                if (args.length != 2) {
                    answerMessage(message, "Wrong number of arguments!");
                    return;
                }
                transfer(message, args[1]);
                break;
            case "/rank":
                handleRank(message);
                break;
        }
    }

    private void handleRank(Message message) {
        List<UserData> collect = UserContext.holder.values().stream()
                .sorted((o1, o2) -> (int) (o2.getBalance() - o1.getBalance()))
                .collect(Collectors.toList());
        StringBuilder sb = new StringBuilder("Rank:\n");
        for (int i = 1; i <= 10 && i - 1 < collect.size(); i++) {
            UserData userData = collect.get(i - 1);
            sb.append(i).append(". ").append(userData.getUsername())
                    .append(" - ").append(userData.getBalance()).append("\n");
        }
        answerMessage(message, sb.toString());
    }

    private void sendGame(Message message) {
        Long chatId = message.chat().id();
        Game game = GameContext.get(chatId);
        if (game != null) {
            Message gameMessage = game.getGameMessage();
            String url = "https://t.me";
            String channelId = gameMessage.chat().id().toString().replace("-100", "");
            url += "/c/" + channelId + "/" + gameMessage.messageId();
            BotContext.bot.execute(new SendMessage(chatId, "A running game is not finished yet!")
                    .replyToMessageId(message.messageId())
                    .replyMarkup(new InlineKeyboardMarkup(
                            new InlineKeyboardButton("To the game").url(url)
                    )));
            return;
        }
        GameContext.put(chatId, new BlackJack(chatId));
    }

    private void transfer(Message message, String arg) {
        long amount = Long.parseLong(arg);
        if (amount <= 0) {
            answerMessage(message, "Transfer must be positive!");
            return;
        }
        if (message.replyToMessage() == null) {
            answerMessage(message, "Reply to the message to transfer!");
            return;
        }
        Integer fromId = message.from().id();
        Integer toId = message.replyToMessage().from().id();
        assert fromId != null;
        assert toId != null;
        UserContext.holder.computeIfAbsent(fromId, UserData::new);
        UserContext.holder.computeIfAbsent(toId, UserData::new);

        UserData fromUser = UserContext.holder.get(fromId);
        UserData toUser = UserContext.holder.get(toId);

        long transfer = fromUser.transfer(toUser, amount);
        if (transfer == -1L) {
            answerMessage(message, "Insufficient chips!");
        } else {
            answerMessage(message, "Transferred out: " + amount + "\n" + "Balance: " + fromUser.getBalance());
        }
    }


    private void answerMessage(Message message, String text) {
        BotContext.bot.execute(new SendMessage(message.chat().id(), text).replyToMessageId(message.messageId()));
    }
}
