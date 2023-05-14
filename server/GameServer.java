package com.example.assignmentdistributed.server;

import com.example.assignmentdistributed.Game;
import com.example.assignmentdistributed.server.Champion;
import com.example.assignmentdistributed.server.UserHandler;

import java.io.Serializable;
import java.util.Random;
import java.util.UUID;

public class GameServer implements Serializable {
    private final UserHandler user1;
    private final UserHandler user2;
    private final String token;

    private final Game game;

    public GameServer(UserHandler user1, UserHandler user2) {
        this.user1 = user1;
        this.user2 = user2;
        this.game = new Game(user1.user, user2.user);

        // initialize token
        UUID uuid = UUID.randomUUID();
        this.token = uuid.toString();
    }

    public UserHandler getUser1() {
        return user1;
    }

    public UserHandler getUser2() {
        return user2;
    }

    public String getToken() {
        return token;
    }

    public void start() {
        System.out.println("GameServer - Starting game with players " + user1.user.getUsername() + " and " + user2.user.getUsername());

        System.out.println(game.tooltip);
        while (!game.gameOver) {
            game.nextTurn();
            System.out.println(game.tooltip);
        }
    }

    public Game getGame() {
        return game;
    }
}
