package xyz.edward_p.blackjackbot.handler.impl;


import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.AnswerCallbackQuery;
import xyz.edward_p.blackjackbot.context.BotContext;
import xyz.edward_p.blackjackbot.context.GameContext;
import xyz.edward_p.blackjackbot.game.Game;
import xyz.edward_p.blackjackbot.handler.UpdateHandler;

/**
 * @author edward <br/>
 * Date: 4/22/21 2:41 PM <br/>
 * Description:
 */
public class CallbackHandler implements UpdateHandler {

    @Override
    public void handle(Update update) {
        CallbackQuery callbackQuery = update.callbackQuery();
        if (callbackQuery == null) {
            return;
        }


        Game game = GameContext.get(callbackQuery.message().chat().id());
        if (game == null || game.isExpired()) {
            BotContext.bot.execute(new AnswerCallbackQuery(callbackQuery.id())
                    .text("Game expired!"));
            return;
        }

        game.handleCallback(callbackQuery);

    }
}
