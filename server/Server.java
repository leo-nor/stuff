package com.example.assignmentdistributed.server;

import com.example.assignmentdistributed.Database;
import com.example.assignmentdistributed.Game;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class Server {
    private ServerSocket serverSocket;
    private ReentrantLock lock;
    private volatile Queue<UserHandler> simpleModeQueue;
    private Map<Integer, Queue<UserHandler>> rankedModeQueues;
    private static List<GameServer> activeGameServers = new ArrayList<>();

    public Server(int port) {
        try {
            serverSocket = new ServerSocket(port);
            lock = new ReentrantLock();
            simpleModeQueue = new LinkedList<>();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() throws IOException, ClassNotFoundException{
        // start the simple mode matchmaking thread
        new Thread(this::simpleModeMatchmaking).start();
        // start the ranked mode matchmaking thread
        new Thread(this::rankedModeMatchmaking).start();

        while (true) {
            try {
                System.out.println("Server running...");
                Socket userSocket = serverSocket.accept();
                Thread userThread = new UserHandler(userSocket, this);
                userThread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void addUserToSimpleQueue(UserHandler userHandler){
        lock.lock();
        try {
            simpleModeQueue.add(userHandler);
            System.out.println("SERVER - User added to queue - user: " + userHandler.user.getUsername() + "  level: " + userHandler.user.getLevel());
        } finally {
            lock.unlock();
        }
    }

    public void removeUserFromSimpleQueue(UserHandler userHandler){
        lock.lock();
        try {
            boolean removed = simpleModeQueue.remove(userHandler);
            if (removed) {
                System.out.println("REMOVED USER " + userHandler.user.getUsername() + " FROM QUEUE");
            }
        } finally {
            lock.unlock();
        }
    }

    public int getSimpleQueueLength(){
        return simpleModeQueue.size();
    }

    // Method to manage the matchmaking queue and start games when two players are ready
    public void simpleModeMatchmaking() {
        while (true) {
            // Wait for the queue to have at least 2 players
            while (simpleModeQueue.size() < 2) {
                try {
                    Thread.sleep(1000); // Sleep for 1 second
                } catch (InterruptedException e) {
                    // Handle the exception
                    Thread.currentThread().interrupt();
                }
            }

            // Remove the first two players from the queue
            UserHandler player1 = simpleModeQueue.remove();
            UserHandler player2 = simpleModeQueue.remove();

            // Start a new game with these two players
            GameServer gameServer = new GameServer(player1, player2);
            activeGameServers.add(gameServer);

            String gameToken = gameServer.getToken();

            player1.setGame(gameServer.getGame());
            player2.setGame(gameServer.getGame());
            player1.user.setGameToken(gameToken);
            player2.user.setGameToken(gameToken);
            Database.updateGameTokens(player1.user, player2.user);

            player1.shouldStartGame();
            player2.shouldStartGame();

            Thread gameThread = new Thread(gameServer::start);
            gameThread.start();
        }
    }

    public void rankedModeMatchmaking() { //ranking
        while (true) {
            // Wait for the queue to have at least 2 players
            while (true) {
                boolean canMatch = false;
                for (int i = 0; i < Rank.values().length; i++) {
                    int queueLength = getRankedQueueLength(i);
                    if (queueLength >= 2) {
                        canMatch = true;
                        break;
                    }
                }
                if (canMatch) {
                    break;
                }
                try {
                    Thread.sleep(5000);     // Sleep for 5 seconds before checking again
                } catch (InterruptedException e) {
                    // Handle the exception
                    Thread.currentThread().interrupt();
                }
            }
    
            // Try to find two players of the same rank
            Rank rank = null;
            UserHandler player1 = null;
            UserHandler player2 = null;
            for (int i = 0; i < Rank.values().length; i++) {
                int queueLength = getRankedQueueLength(i);
                if (queueLength >= 2) {
                    rank = Rank.values()[i];
                    Queue<UserHandler> queue = rankedModeQueues.get(i);
                    player1 = queue.remove();
                    player2 = queue.remove();
                    break;
                }
            }
            
            // If no players of the same rank were found, match players with the closest rank
            if (rank == null) {
                int minDiff = Integer.MAX_VALUE;
                for (int i = 0; i < Rank.values().length; i++) {
                    int queueLength = getRankedQueueLength(i);
                    if (queueLength > 0) {
                        int diff = Math.abs(i - Rank.SILVER.ordinal()); // Calculate the difference in rank
                        if (diff < minDiff) {
                            minDiff = diff;
                            rank = Rank.values()[i];
                            Queue<UserHandler> queue = rankedModeQueues.get(i);
                            player1 = queue.remove();
                            break;
                        }
                    }
                }
                // Try to find a second player of the same rank as the first player
                if (player1 != null) {
                    for (int i = 0; i < Rank.values().length; i++) {
                        if (i == rank.ordinal()) {
                            continue;
                        }
                        int queueLength = getRankedQueueLength(i);
                        if (queueLength > 0) {
                            Queue<UserHandler> queue = rankedModeQueues.get(i);
                            player2 = queue.remove();
                            break;
                        }
                    }
                }
            }
    
            // If players were found, start a new game with them
            if (player1 != null && player2 != null) {
                GameServer gameServer = new GameServer(player1, player2);
                activeGameServers.add(gameServer);
    
                String gameToken = gameServer.getToken();
    
                player1.setGame(gameServer.getGame());
                player2.setGame(gameServer.getGame());
                player1.user.setGameToken(gameToken);
                player2.user.setGameToken(gameToken);
                Database.updateGameTokens(player1.user, player2.user);
    
                player1.shouldStartGame();
                player2.shouldStartGame();
    
                Thread gameThread = new Thread(gameServer::start);
                gameThread.start();
            }
        }
        
    }

    public static Game getCorrespondentGame(String gameToken) {
        System.out.println(activeGameServers);
        for (GameServer gameServer : activeGameServers) {
            if (gameServer.getToken().equals(gameToken))
                return gameServer.getGame();
        }
        return null;
    }
}
