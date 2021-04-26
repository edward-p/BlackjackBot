package xyz.edward_p.blackjackbot.game;

import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;

/**
 * @author edward <br/>
 * Date: 4/22/21 4:21 PM <br/>
 * Description:
 */
public interface Game {
    int TIMEOUT = 5 * 60 * 1000;

    boolean isExpired();

    void handleCallback(CallbackQuery callbackQuery);

    Message getGameMessage();

    void shutdown();
}
