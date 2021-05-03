package xyz.edward_p.blackjackbot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.request.GetMe;
import com.pengrad.telegrambot.response.GetMeResponse;
import xyz.edward_p.blackjackbot.context.BotContext;
import xyz.edward_p.blackjackbot.context.GameContext;
import xyz.edward_p.blackjackbot.context.UserContext;
import xyz.edward_p.blackjackbot.handler.UpdateHandler;
import xyz.edward_p.blackjackbot.handler.impl.CallbackHandler;
import xyz.edward_p.blackjackbot.handler.impl.CommandHandler;
import xyz.edward_p.blackjackbot.handler.impl.InlineQueryHandler;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;

/**
 * @author edward <br/>
 * Date: 4/22/21 10:10 AM <br/>
 * Description:
 */

public class Main {
    private static void printUsage() {
        System.out.print("Usage:\n\t");
        System.out.println("java -jar <PACKAGE-NAME> <TOKEN> <DATA_FILE_PATH>");
    }

    public static void main(String[] args) {

        // need 2 args, token & data file
        if (args.length < 2) {
            printUsage();
            return;
        }
        String token = args[0];
        // check if token is a valid bot token
        if (!token.matches("[0-9]{9,10}:[a-zA-Z0-9_-]{35}")) {
            System.err.println("Invalid token: " + token);
            return;
        }

        String dataPath = args[1];

        // Load User Data
        UserContext.load(dataPath);
        // Create your bot passing the token received from @BotFather
        TelegramBot bot = new TelegramBot(token);
        BotContext.bot = bot;
        GetMeResponse getMeResponse = bot.execute(new GetMe());
        BotContext.name = getMeResponse.user().username();
        assert BotContext.name != null;

        UpdateHandler inlineQueryHandler = new InlineQueryHandler();
        UpdateHandler commandHandler = new CommandHandler();
        UpdateHandler callbackHandler = new CallbackHandler();
        ExecutorService executorService = Executors.newFixedThreadPool(5);

        BlockingQueue<Future<?>> futureBlockingQueue = new LinkedBlockingQueue<>();
        // Register for updates
        bot.setUpdatesListener(updates -> {
            // ... process updates
            // Send messages
            updates.forEach(update -> {
                Future<?> future = executorService.submit(() -> {
                    inlineQueryHandler.handle(update);
                    commandHandler.handle(update);
                    callbackHandler.handle(update);
                });
                futureBlockingQueue.offer(future);
            });
            // return id of last processed update or confirm them all
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });

        /*
         * Clean expired games.
         */
        new Timer("Game Cleaner", true).schedule(new TimerTask() {
            @Override
            public void run() {
                GameContext.getHolder().forEach((k, v) -> {
                    if (v.isExpired()) {
                        v.shutdown();
                    }
                });
            }
        }, 60 * 1000L, 60 * 1000L);

        /*
         * Print exceptions here.
         */
        new Timer("Future consumer", true).schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        while (futureBlockingQueue.size() != 0) {
                            Future<?> x = futureBlockingQueue.poll();
                            try {
                                x.get();
                            } catch (InterruptedException | ExecutionException e) {
                                // Print Exceptions here.
                                e.printStackTrace();
                            }
                        }
                    }
                }, 1000L, 1000L);

        /*
         * Shutdown hook.
         */
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            executorService.shutdown();
            try {
                boolean b = executorService.awaitTermination(5, TimeUnit.SECONDS);
                assert b;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            UserContext.persist(dataPath);
        }));
    }
}
