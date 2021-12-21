package xyz.edward_p.blackjackbot.handler.impl;

import com.pengrad.telegrambot.model.InlineQuery;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineQueryResultArticle;
import com.pengrad.telegrambot.request.AnswerInlineQuery;
import xyz.edward_p.blackjackbot.context.BotContext;
import xyz.edward_p.blackjackbot.context.UserContext;
import xyz.edward_p.blackjackbot.entity.UserDataV2;
import xyz.edward_p.blackjackbot.handler.UpdateHandler;

/**
 * @author edward <br/>
 * Date: 4/25/21 11:09 PM <br/>
 * Description:
 */
public class InlineQueryHandler implements UpdateHandler {
    @Override
    public void handle(Update update) {
        InlineQuery inlineQuery = update.inlineQuery();
        if (inlineQuery == null) {
            return;
        }

        Long userId = inlineQuery.from().id();
        UserContext.holder.computeIfAbsent(userId, UserDataV2::new);
        UserDataV2 userData = UserContext.holder.get(userId);
        assert userData != null;

        BotContext.bot.execute(new AnswerInlineQuery(inlineQuery.id(),
                new InlineQueryResultArticle("bl-" + userId, "Get balance",
                        "Balance: " + userData.getBalance())).cacheTime(1));
    }
}
