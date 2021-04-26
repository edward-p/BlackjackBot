package xyz.edward_p.blackjackbot.context;

import xyz.edward_p.blackjackbot.entity.UserData;

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author edward <br/>
 * Date: 4/22/21 5:18 PM <br/>
 * Description:
 */
public class UserContext implements Serializable {
    public static ConcurrentHashMap<Integer, UserData> holder;

    public static void persist(String path) {
        File file = new File(path);
        boolean mkdirs = file.getParentFile().mkdirs();
        assert mkdirs;

        try (FileOutputStream fout = new FileOutputStream(file);
             ObjectOutputStream oos = new ObjectOutputStream(fout)) {
            oos.writeObject(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public static void load(String path) {
        try (FileInputStream fin = new FileInputStream(path);
             ObjectInputStream ois = new ObjectInputStream(fin)) {
            Object o = ois.readObject();
            holder = (ConcurrentHashMap<Integer, UserData>) o;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            holder = new ConcurrentHashMap<>();
        }
    }
}
