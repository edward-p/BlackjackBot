package xyz.edward_p.blackjackbot.context;

import xyz.edward_p.blackjackbot.game.Game;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author edward <br/>
 * Date: 4/22/21 4:20 PM <br/>
 * Description:
 */
public class GameContext {
    /**
     * K: chatId
     * V: Game
     */
    private final static ConcurrentHashMap<Long, Game> holder = new ConcurrentHashMap<>();

    public static Game get(Long chatId) {
        return holder.get(chatId);
    }

    public static Game put(Long chatId, Game game) {
        return holder.put(chatId, game);
    }

    public static Game remove(Long chatId) {
        return holder.remove(chatId);
    }

    public static ConcurrentHashMap<Long, Game> getHolder() {
        return holder;
    }


}
