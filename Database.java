package com.example.assignmentdistributed;

import com.example.assignmentdistributed.user.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;




public class Database {
    private static List<User> users;

    public static List<User> getUsers() {
        Gson gson = new Gson();
        users = new ArrayList<>();

        try {
            String data = new String(Files.readAllBytes(Paths.get("src/users.json")));

            Type listType = new TypeToken<List<User>>() {}.getType();
            users = gson.fromJson(data, listType);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return users;
    }

    public static User authenticate(String username, String password) {
        for (User user : getUsers()) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }

    public static boolean register(String username, String password) {
        for (User user: getUsers()) {
            if (user.getUsername().equals(username)) {
                System.out.println(user);
                return false;
            }
        }

        User newUser = new User(username, password);

        users.add(newUser);
        writeUsersToFile();
        return true;
    }

    public static void writeUsersToFile() {
        Gson gson = new Gson();
        String json = gson.toJson(users);

        try (FileWriter writer = new FileWriter("src/users.json", false)) {
            writer.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void updateGameTokens(User user1, User user2) {
        for (User user: getUsers()) {
            if(user.getUsername().equals(user1.getUsername()) || user.getUsername().equals(user2.getUsername())) {
                user.setGameToken(user1.getGameToken());
            }
        }

        writeUsersToFile();
    }

    public static void cleanGameTokens(String gameToken) {
        for (User user: getUsers()) {
            if(user.getGameToken().equals(gameToken))
                user.setGameToken("");
        }

        writeUsersToFile();
    }

    public static boolean checkUserIsInGame(User user1) {
        for (User user: getUsers()) {
            if (user.getUsername().equals(user1.getUsername()) && !user.getGameToken().equals(""))
                return true;
        }
        return false;
    }


    public static User getUserByUsername(String username) {
        for (User user : getUsers()) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null; // Return null if no user with the specified username is found
    }


    public static void updateUsersRanks(String winner, String loser) {


        User w = getUserByUsername(winner);
        User l = getUserByUsername(loser);


        double K_FACTOR = 20; //ELO constant, determines the magnitude of the rank adjustment. between 10 and 32

        double wExpected = 1 / (1 + Math.pow(10, (l.getRank() - w.getRank()) / 400.0));
        double lExpected = 1 / (1 + Math.pow(10, (w.getRank() - l.getRank()) / 400.0));

        int wNewRank = (int) (w.getRank() + K_FACTOR * (1 - wExpected));
        int lNewRank = (int) (l.getRank() + K_FACTOR * (0 - lExpected));

        w.setRank(wNewRank);
        l.setRank(lNewRank); 
    }

}
