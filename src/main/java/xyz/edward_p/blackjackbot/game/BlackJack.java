package xyz.edward_p.blackjackbot.game;

import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.AnswerCallbackQuery;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import xyz.edward_p.blackjackbot.card.Card;
import xyz.edward_p.blackjackbot.card.Deck;
import xyz.edward_p.blackjackbot.card.Suit;
import xyz.edward_p.blackjackbot.context.BotContext;
import xyz.edward_p.blackjackbot.context.GameContext;
import xyz.edward_p.blackjackbot.context.UserContext;
import xyz.edward_p.blackjackbot.entity.UserData;

import java.util.*;

/**
 * @author edward <br/>
 * Date: 4/22/21 4:19 PM <br/>
 * Description:
 */
public class BlackJack implements Game {
    private static final List<Card> A_DECK_OF_CARDS;
    private static final InlineKeyboardMarkup BET_KEYBOARD;

    static {
        A_DECK_OF_CARDS = new ArrayList<>(52);
        Arrays.stream(Suit.values()).forEach(x ->
                Arrays.stream(Deck.values())
                        .forEach(y -> A_DECK_OF_CARDS.add(new Card(x, y))));
        BET_KEYBOARD = new InlineKeyboardMarkup(
                new InlineKeyboardButton("1000").callbackData("BET_1000"),
                new InlineKeyboardButton("5000").callbackData("BET_5000"),
                new InlineKeyboardButton("10000").callbackData("BET_10000")
        ).addRow(
                new InlineKeyboardButton("50000").callbackData("BET_50000"),
                new InlineKeyboardButton("100000").callbackData("BET_100000"),
                new InlineKeyboardButton("ALL_IN").callbackData("BET_ALL")
        ).addRow(
                new InlineKeyboardButton("CHECK_IN").callbackData("CHECK_IN"),
                new InlineKeyboardButton("START").callbackData("START"));
    }

    private volatile long expireTime;
    private final Message gameMessage;
    private final LinkedList<Card> cards;
    private final List<Card> dealerCards;
    private final List<UserData> players;
    private volatile Integer currentPlayerIndex;
    private volatile long canStartUntil;
    private volatile int dealerCardSum;

    public BlackJack(long chatId) {
        this.dealerCards = new ArrayList<>();
        this.cards = new LinkedList<>();
        // Prepare 4 decks of cards
        for (int i = 0; i < 4; i++) {
            this.cards.addAll(A_DECK_OF_CARDS);
        }
        // Shuffle
        Collections.shuffle(cards);
        this.players = new ArrayList<>(208);
        // Send game
        SendMessage sendMessage = new SendMessage(chatId, "Okay, bets in:")
                .replyMarkup(BET_KEYBOARD);
        SendResponse response = BotContext.bot.execute(sendMessage);
        assert response != null;
        this.gameMessage = response.message();
        // Set expire
        this.expireTime = System.currentTimeMillis() + TIMEOUT;
    }

    private void answerCallback(String queryId, String text) {
        answerCallback(queryId, text, true);
    }

    private void answerCallback(String queryId, String text, boolean showAlert) {
        BotContext.bot.execute(new AnswerCallbackQuery(queryId)
                .showAlert(showAlert)
                .text(text));
    }

    private void handleCheckIn(CallbackQuery callbackQuery) {
        User from = callbackQuery.from();
        UserContext.holder.computeIfAbsent(from.id(), UserData::new);
        UserData userData = UserContext.holder.get(from.id());
        assert userData != null;

        long checkTime = userData.getCdUntil() - System.currentTimeMillis();
        if (checkTime > 0) {
            answerCallback(callbackQuery.id(), String.format("CD - %ds", checkTime / 1000));
            return;
        }
        userData.addBalance(5000);
        // add 3-minute CD
        userData.addCd(3 * 60 * 1000L);
        answerCallback(callbackQuery.id(), "Get Bonus: 5000\n" +
                "Current Balance: " + userData.getBalance());
    }

    private void handleBets(CallbackQuery callbackQuery) {
        String data = callbackQuery.data();
        Integer userId = callbackQuery.from().id();
        UserContext.holder.computeIfAbsent(userId, UserData::new);
        UserData userData = UserContext.holder.get(userId);
        assert userData != null;

        long chatId = gameMessage.chat().id();
        if (userData.isInGame() && chatId != userData.getChatId()) {
            answerCallback(callbackQuery.id(), "You are currently in another game!");
            return;
        } else {
            userData.setInGame(true);
            userData.setChatId(chatId);
        }
        long bets;
        if (data.equals("BET_ALL")) {
            bets = userData.bet();
        } else {
            long bet = Long.parseLong(data.replace("BET_", ""));
            bets = userData.bet(bet);
        }
        if (bets == -1) {
            answerCallback(callbackQuery.id(), "Insufficient chips!");
            return;
        }
        if (!players.contains(userData)) {
            players.add(userData);
        }

        userData.setUsername(callbackQuery.from().username());

        updateBets();
        canStartUntil = System.currentTimeMillis() + 5 * 1000;

    }

    private void updateBets() {
        StringBuilder sb = new StringBuilder();
        sb.append(gameMessage.text()).append("\n");
        players.forEach(e -> sb.append(e.getUsername()).append(": ")
                .append(e.getBets()).append("\n"));
        BotContext.bot.execute(new EditMessageText(gameMessage.chat().id(), gameMessage.messageId(), sb.toString())
                .replyMarkup(
                        gameMessage.replyMarkup()
                ));
    }

    public Message getGameMessage() {
        return gameMessage;
    }

    @Override
    public void shutdown() {
        this.players.forEach(UserData::leave);
        GameContext.remove(gameMessage.chat().id());
    }

    @Override
    public boolean isExpired() {
        return System.currentTimeMillis() > expireTime;
    }

    @Override
    public void handleCallback(CallbackQuery callbackQuery) {
        // Refresh expire
        this.expireTime = System.currentTimeMillis() + TIMEOUT;

        String data = callbackQuery.data();
        if (data.equals("CHECK_IN")) {
            handleCheckIn(callbackQuery);
        } else if (data.contains("BET_")) {
            handleBets(callbackQuery);
        } else if (data.equals("START")) {
            handleStart(callbackQuery);
        } else if (data.equals("HIT")) {
            handleHit(callbackQuery);
        } else if (data.equals("STAND")) {
            handleStand(callbackQuery);
        } else if (data.equals("DOUBLE_DOWN")) {
            handleDoubleDown(callbackQuery);
        } else if (data.equals("SPLIT")) {
            handleSplit(callbackQuery);
        }
    }

    private void handleSplit(CallbackQuery callbackQuery) {
        UserData currentPlayer = players.get(currentPlayerIndex);
        if (isNotCurrentPlayer(callbackQuery)) {
            answerCallback(callbackQuery.id(), "Current player is: " +
                    currentPlayer.getUsername(), false);
            return;
        }

//        currentPlayer.split(cards.removeFirst(), cards.removeFirst());
        currentPlayer.split(cards.removeFirst(), A_DECK_OF_CARDS.get(11));
        skipBlackJackPlayers();
//        seekNextPlayer();
        resultOrUpdate();
    }

    private synchronized void seekNextPlayer() {
        for (int i = currentPlayerIndex; i < players.size(); i++) {
            if (players.get(i).getCurrentHand() == null) {
                currentPlayerIndex++;
            } else {
                break;
            }
        }
    }

    private synchronized void handleDoubleDown(CallbackQuery callbackQuery) {
        UserData currentPlayer = players.get(currentPlayerIndex);
        if (isNotCurrentPlayer(callbackQuery)) {
            answerCallback(callbackQuery.id(), "Current player is: " +
                    currentPlayer.getUsername(), false);
            return;
        }
        currentPlayer.doubleDown();
        currentPlayer.draw(cards.removeFirst());
        currentPlayer.stand();
        if (currentPlayer.getCurrentHand() != null) {
            // check right Hand
            if (currentPlayer.getSumOfRight() >= 21) {
                currentPlayer.stand();
            }
        }

        seekNextPlayer();
        resultOrUpdate();
    }

    private synchronized void handleStand(CallbackQuery callbackQuery) {
        UserData currentPlayer = players.get(currentPlayerIndex);
        if (isNotCurrentPlayer(callbackQuery)) {
            answerCallback(callbackQuery.id(), "Current player is: " +
                    currentPlayer.getUsername(), false);
            return;
        }

        currentPlayer.stand();
        if (currentPlayer.getCurrentHand() != null) {
            // check right Hand
            if (currentPlayer.getSumOfRight() >= 21) {
                currentPlayer.stand();
            }
        }

        seekNextPlayer();
        resultOrUpdate();
    }

    private boolean isNotCurrentPlayer(CallbackQuery callbackQuery) {
        return callbackQuery.from().id() != players.get(currentPlayerIndex).getId();
    }

    private synchronized void handleHit(CallbackQuery callbackQuery) {
        UserData currentPlayer = players.get(currentPlayerIndex);
        if (isNotCurrentPlayer(callbackQuery)) {
            answerCallback(callbackQuery.id(), "Current player is: " +
                    currentPlayer.getUsername(), false);
            return;
        }
        currentPlayer.draw(cards.removeFirst());

        seekNextPlayer();
        resultOrUpdate();
    }

    private void resultOrUpdate() {
        if (this.currentPlayerIndex >= players.size()) {
            handleResult();
        } else {
            updateGame();
        }
    }


    private void handleResult() {
        dealerCardSum = Card.sumOfCards(dealerCards);
        while (dealerCardSum < 17) {
            dealerCards.add(cards.removeFirst());
            dealerCardSum = Card.sumOfCards(dealerCards);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("```\n");

        sb.append("--️Dealer: ");
        dealerCards.forEach(c -> sb.append(c.getText()));
        sb.append("<").append(dealerCardSum).append(">").append("\n");

        for (UserData p : players) {
            sb.append("--")
                    .append(p.getUsername())
                    .append(": ");
            p.getLeftHand().forEach(c -> sb.append(c.getText()));
            sb.append("<").append(p.getSumOfLeft()).append("> ");
            if (p.isLeftHandBust() || (p.getSumOfLeft() < dealerCardSum && dealerCardSum <= 21)) {
                sb.append("[LOSE]");
            } else if (!p.isLeftHandBust() && (dealerCardSum > 21 ||
                    (p.getSumOfLeft() > dealerCardSum && p.getSumOfLeft() != 21))) {
                sb.append("[WIN]");
            } else if ((p.getLeftHand().size() == 2 && p.getSumOfLeft() == 21)
                    && !(dealerCardSum == 21 && dealerCards.size() == 2)) {
                sb.append("[BLACKJACK]");
            } else {
                sb.append("[DRAW]");
            }

            sb.append("\n");
            if (p.getRightHand() != null) {
                sb.append("--")
                        .append(p.getUsername())
                        .append(": ");
                p.getRightHand().forEach(c -> sb.append(c.getText()));
                sb.append("<").append(p.getSumOfRight()).append("> ");
                if (p.isRightHandBust() || (p.getSumOfRight() < dealerCardSum && dealerCardSum <= 21)) {
                    sb.append("[LOSE]");
                } else if (!p.isRightHandBust() && (dealerCardSum > 21 ||
                        (p.getSumOfRight() > dealerCardSum && p.getSumOfRight() != 21))) {
                    sb.append("[WIN]");
                } else if ((p.getRightHand().size() == 2 && p.getSumOfRight() == 21)
                        && !(dealerCardSum == 21 && dealerCards.size() == 2)) {
                    sb.append("[BLACKJACK]");
                } else {
                    sb.append("[DRAW]");
                }

                sb.append("\n");
            }
        }

        calcResult(sb);

        sb.append("```\n");
        updateGame(sb.toString());
        shutdown();
    }

    private synchronized void calcResult(StringBuilder sb) {
        sb.append("-----------------------------------\n");
        for (UserData p : players) {
            int win = 0;

            if (p.getLeftHand().size() == 2 && p.getSumOfLeft() == 21
                    && (dealerCards.size() != 2 || dealerCardSum < 21)) {
                // Left hand black jack
                win += p.getInitBets() * 2.5;
            } else if (!p.isLeftHandBust() && (p.getSumOfLeft() > dealerCardSum || dealerCardSum > 21)) {
                // Left hand Win
                win += p.isLeftHandDouble() ? p.getInitBets() * 4 : p.getInitBets() * 2;
            } else if ((!p.isLeftHandBust() && p.getSumOfLeft() == dealerCardSum) ||
                    (p.getLeftHand().size() == 2 && p.getSumOfLeft() == 21 && dealerCardSum == 21)) {
                // Left hand draw
                win += p.isLeftHandDouble() ? p.getInitBets() * 2 : p.getInitBets();
            }

            if (p.getRightHand() != null) {
                if (p.getRightHand().size() == 2 && p.getSumOfRight() == 21
                        && (dealerCards.size() != 2 || dealerCardSum < 21)) {
                    // Left hand black jack
                    win += p.getInitBets() * 2.5;
                } else if (!p.isRightHandBust()
                        && (p.getSumOfRight() > dealerCardSum || dealerCardSum > 21)) {
                    // Right hand Win
                    win += p.isRightHandDouble() ? p.getInitBets() * 4 : p.getInitBets() * 2;
                } else if ((!p.isRightHandBust() && p.getSumOfRight() == dealerCardSum) ||
                        (p.getRightHand().size() == 2 && p.getSumOfRight() == 21 && dealerCardSum == 21)) {
                    // Right hand draw
                    win += p.isRightHandDouble() ? p.getInitBets() * 2 : p.getInitBets();
                }
            }
            p.addBalance(win);
            sb.append(p.getUsername()).append(": ").append(win - p.getBets()).append("\n");
            p.clearBets();
            p.leave();

        }
    }

    private void handleStart(CallbackQuery callbackQuery) {
        if (players.size() == 0) {
            answerCallback(callbackQuery.id(), "No enough players!", false);
            return;
        }
        if (canStartUntil > System.currentTimeMillis()) {
            answerCallback(callbackQuery.id(), "Can start only if inactive for 5s!", false);
            return;
        }
        this.currentPlayerIndex = 0;
        initGame();
    }

    private synchronized void initGame() {
        dealerCards.add(cards.removeFirst());
        dealerCards.add(cards.removeFirst());

        for (UserData p : players) {
            p.draw(cards.removeFirst());
            p.draw(cards.removeFirst());
        }

        skipBlackJackPlayers();


        if (Card.sumOfCards(dealerCards) == 21 && currentPlayerIndex < players.size()) {
            // Dealer's black jack
            // all player stand
            for (; currentPlayerIndex < players.size(); currentPlayerIndex++) {
                players.get(currentPlayerIndex).stand();
            }
        }

        resultOrUpdate();
    }

    private synchronized void skipBlackJackPlayers() {
        for (UserData player : players) {
            if (player.getCurrentHand() == null) {
                // Blackjack!
                currentPlayerIndex++;
            } else {
                break;
            }
        }
    }


    private void updateGame() {

        StringBuilder sb = new StringBuilder();
        sb.append("```\n");
        sb.append("--️Dealer: ")
                .append(dealerCards.get(0).getText()).append(Card.CARD_BACK).append("<")
                .append(dealerCards.get(0).getValue()).append(">\n");

        for (int i = 0; i < players.size(); i++) {
            UserData p = players.get(i);
            sb.append(i == currentPlayerIndex && p.getCurrentHand() == p.getLeftHand() ? "->" : "--")
                    .append(p.getUsername())
                    .append(": ");
            p.getLeftHand().forEach(c -> sb.append(c.getText()));
            sb.append("<").append(p.getSumOfLeft()).append("> ");
            if (p.getCurrentHand() != p.getLeftHand()) {
                if (p.isLeftHandBust()) {
                    sb.append("[BUST]");
                } else if (p.getLeftHand().size() == 2 && p.getSumOfLeft() == 21) {
                    sb.append("[BLACKJACK]");
                } else {
                    sb.append("[STAND]");
                }
            }
            sb.append("\n");
            if (p.getRightHand() != null) {
                sb.append(i == currentPlayerIndex && p.getCurrentHand() == p.getRightHand() ? "->" : "--")
                        .append(p.getUsername())
                        .append(": ");
                p.getRightHand().forEach(c -> sb.append(c.getText()));
                sb.append("<").append(p.getSumOfRight()).append("> ");
                if (p.getCurrentHand() != p.getRightHand()) {
                    if (p.isRightHandBust()) {
                        sb.append("[BUST]");
                    } else if (p.getRightHand().size() == 2 && p.getSumOfRight() == 21) {
                        sb.append("[BLACKJACK]");
                    } else if (p.getCurrentHand() == null) {
                        sb.append("[STAND]");
                    }
                }
                sb.append("\n");
            }
        }

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup(
                new InlineKeyboardButton("HIT").callbackData("HIT"),
                new InlineKeyboardButton("STAND").callbackData("STAND"));

        UserData currentPlayer = players.get(currentPlayerIndex);
        List<Card> currentHand = currentPlayer.getCurrentHand();
        if (currentPlayer.getBalance() >= currentPlayer.getInitBets()
                && currentHand != null && currentHand.size() == 2) {
            keyboard.addRow(new InlineKeyboardButton("DOUBLE_DOWN").callbackData("DOUBLE_DOWN"));
            if (currentPlayer.getRightHand() == null
                    && currentHand.get(0).getDeck() == currentHand.get(1).getDeck()) {
                keyboard.addRow(new InlineKeyboardButton("SPLIT").callbackData("SPLIT"));
            }
        }
        sb.append("```\n");
        updateGame(sb.toString(), keyboard);
    }


    private void updateGame(String text, InlineKeyboardMarkup replyMarkup) {
        BotContext.bot.execute(new EditMessageText(gameMessage.chat().id(), gameMessage.messageId(), text)
                .replyMarkup(replyMarkup).parseMode(ParseMode.Markdown));
    }

    private void updateGame(String text) {
        BotContext.bot.execute(new EditMessageText(gameMessage.chat().id(), gameMessage.messageId(), text)
                .parseMode(ParseMode.Markdown));
    }


}
