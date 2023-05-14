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

    public void setUserPoints(String username, int points) {
        // Find the user with the given username
        User user = getUserByUsername(username);
        
        // Set the user's points
        user.setPoints(points);
        
        // Update the user's rank based on their new points
        updateRank(user);
    }
    // TODO: 5/4/2023
    public static void updateUsersLevels(String username1, String level1, String username2, String level2) {
        
    
        if (username1.equals(winner)) {
            if (level2 > 20) {
                level2 -= 20;
            } else {
                level2 = 1;
            }
            level1 += 20;
        } else {
            if (level1 > 20) {
                level1 -= 20;
            } else {
                level1 = 1;
            }
            level2 += 20;
        }
    
        Rank rank = Rank.getRankForPoints(level1);
        rank.setUserPoints(username1, level1);
    
        rank = Rank.getRankForPoints(level2);
        rank.setUserPoints(username2, level2);
    }
}
