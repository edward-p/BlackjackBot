package xyz.edward_p.blackjackbot.context;

import xyz.edward_p.blackjackbot.entity.UserData;
import xyz.edward_p.blackjackbot.entity.UserDataV2;

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author edward <br/>
 * Date: 4/22/21 5:18 PM <br/>
 * Description:
 */
public class UserContext implements Serializable {
    public static ConcurrentHashMap<Long, UserDataV2> holder;

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
            if (((ConcurrentHashMap<?, ?>) o).keySet().iterator().next() instanceof Long) {
                holder = (ConcurrentHashMap<Long, UserDataV2>) o;
            } else {
                // load old user data
                ConcurrentHashMap<Integer, UserData> legacyHolder = (ConcurrentHashMap<Integer, UserData>) o;
                holder = new ConcurrentHashMap<>();
                legacyHolder.forEach((k, v) -> {
                    Long key = Long.valueOf(k);
                    UserDataV2 userDataV2 = new UserDataV2(v.getId());
                    userDataV2.setUsername(v.getUsername());
                    userDataV2.setBalance(v.getBalance());
                    holder.put(Long.valueOf(k), userDataV2);
                });
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            holder = new ConcurrentHashMap<>();
        }
    }
}
