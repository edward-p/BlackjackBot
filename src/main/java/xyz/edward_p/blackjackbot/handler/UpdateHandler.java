package xyz.edward_p.blackjackbot.handler;

import com.pengrad.telegrambot.model.Update;

/**
 * @author edward <br/>
 * Date: 4/22/21 2:18 PM <br/>
 * Description:
 */
public interface UpdateHandler {
    void handle(Update update);
}
