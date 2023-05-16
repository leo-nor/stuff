package com.example.assignmentdistributed.server;

import com.example.assignmentdistributed.Database;
import com.example.assignmentdistributed.Game;
import com.example.assignmentdistributed.SocketMessage;
import com.example.assignmentdistributed.user.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;

public class UserHandler extends Thread implements Serializable {
    private transient final Socket userSocket;
    private transient final Server server;
    public transient ObjectInputStream input;
    public transient ObjectOutputStream output;

    public User user;
    private int mode;
    private volatile boolean shouldStartGame = false;
    private Game game;

    private static final long serialVersionUID = 1L;

    public UserHandler(Socket socket, Server mainServer) {
        userSocket = socket;
        server = mainServer;

        try {
            System.out.println("SERVER trying to setup connection");
            output = new ObjectOutputStream(userSocket.getOutputStream());
            input = new ObjectInputStream(userSocket.getInputStream());
            this.userSocket.setSoTimeout(200);
        } catch (IOException e) {
            System.out.println("Failed");
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            boolean isAuthenticated = false;
            while(!isAuthenticated) {
                SocketMessage authenticationInfo = getUserMessageResponse();
                if (authenticationInfo != null && authenticationInfo.getCode() == SocketMessage.LOGIN_REQUEST) {
                    System.out.println("SERVER - login request received");
                    if (dealWithLogin(authenticationInfo)) {
                        isAuthenticated = true;
                    } else
                        output.writeObject(new SocketMessage(SocketMessage.LOGIN_REJECTED));
                }
                 else if (authenticationInfo != null && authenticationInfo.getCode() == SocketMessage.REGISTER_REQUEST) {
                    System.out.println("SERVER - Register request received");
                    if (dealWithRegister(authenticationInfo)) {
                        output.writeObject(new SocketMessage(SocketMessage.REGISTER_SUCCESSFUL));
                    } else
                        output.writeObject(new SocketMessage(SocketMessage.REGISTER_REJECTED));
                }
            }

            System.out.println("user gameToken: " + user.getGameToken());
            // check if user has a game going
            if (!user.getGameToken().equals("")) {
                System.out.println("Game already in progress for this user");
                output.writeObject(new SocketMessage(SocketMessage.LOGIN_SUCCESSFUL, "game"));

                game = Server.getCorrespondentGame(user.getGameToken());
                startGame();
            } else { //askGameMode and then queue
                output.writeObject(new SocketMessage(SocketMessage.LOGIN_SUCCESSFUL, ""));

                askGameMode();
                inQueue();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                userSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public SocketMessage getUserMessageResponse(){
        SocketMessage response = null;
        try {
            response = (SocketMessage) input.readObject();
        } catch (IOException | ClassNotFoundException e) {
            //throw new RuntimeException(e);
        }
        return response;
    }

    public boolean dealWithLogin(SocketMessage loginInfo) {
        Object[] arguments = loginInfo.getArguments();
        String username = (String) arguments[0];
        String password = (String) arguments[1];
        System.out.println(username);
        System.out.println(password);
        user = Database.authenticate(username, password);
        return (user != null);
    }

    public boolean dealWithRegister(SocketMessage loginInfo) {
        Object[] arguments = loginInfo.getArguments();
        String username = (String) arguments[0];
        String password = (String) arguments[1];
        User user = new User(username, password);
        System.out.println(username);
        System.out.println(password);
        return Database.register(user.getUsername(), user.getPassword());
    }

    public void askGameMode() {
        System.out.println("ASKING GAME MODE!");
        SocketMessage user_message = null;
        while (user_message == null) {
            user_message = getUserMessageResponse();
            if (user_message != null && user_message.getCode() == SocketMessage.GAME_MODE) {
                Object[] arguments = user_message.getArguments();
                mode = (Integer) arguments[0];
            } else
                user_message = null;
        }
        System.out.println("MODE: " + mode);
    }

    public void inQueue() throws IOException, ClassNotFoundException {
        if (mode == 1) {
            server.addUserToSimpleQueue(this);
            int queue_length = server.getSimpleQueueLength();
            output.writeObject(new SocketMessage(SocketMessage.GAME_MODE, queue_length));

            shouldStartGame = false;
            while (!shouldStartGame) {
                if (server.getSimpleQueueLength() != queue_length){
                    System.out.println("A ENVIAR SOCKET UPDATE QUEUE");
                    try {
                        output.writeObject(new SocketMessage(SocketMessage.UPDATE_QUEUE_LENGTH, server.getSimpleQueueLength()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    queue_length = server.getSimpleQueueLength();
                }

                SocketMessage user_message = getUserMessageResponse();
                if (user_message != null && user_message.getCode() == SocketMessage.EXIT_QUEUE) {
                    server.removeUserFromSimpleQueue(this);
                }
            }
        }
        else { //ranking
            Rank userRank = Rank.values()[rank];
            server.addUserToRankedQueue(this);
    
            // Wait for a short time to allow for better matchmaking
            try {
                Thread.sleep(5000); // wait for 5 seconds
            } catch (InterruptedException e) {
                // handle exception
            }
    
            shouldStartGame = false;
            while (!shouldStartGame) {
                List<ClientThread> queue = server.getRankedQueueForRank(userRank);
    
                // check if there are enough players in the queue
                if (queue.size() >= 2) {
                    // remove the first two players from the queue
                    ClientThread player1 = queue.remove(0);
                    ClientThread player2 = queue.remove(0);
    
                    // create a new game with the two players
                    server.createNewGame(player1, player2);
    
                    // notify the players that the game is starting
                    player1.startGame();
                    player2.startGame();
                } else {
                    try {
                        Thread.sleep(1000); // wait for 1 second before checking again
                    } catch (InterruptedException e) {
                        // handle exception
                    }
                }
    
                SocketMessage user_message = getUserMessageResponse();
                if (user_message != null && user_message.getCode() == SocketMessage.EXIT_QUEUE) {
                    server.removeUserFromRankedQueue(this);
                }
            }
        }
        

        startGame();
    }

    public void shouldStartGame() {
        shouldStartGame = true;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public void startGame() {
        try {
            output.writeObject(new SocketMessage(SocketMessage.START_GAME, game));
        } catch (Exception e){
            e.printStackTrace();
        }
        /*try {
            output.writeObject(new SocketMessage(SocketMessage.START_GAME, user.getUsername()));
            boolean hasWinner = false;
            Champion player1Champion = Champion.CHAMPION_1;
            Champion player2Champion = Champion.CHAMPION_1;
            int player1Health = player1Champion.getHealth(user.getLevel());
            int player2Health = player2Champion.getHealth(user.getLevel());

            while (!hasWinner) {
                SocketMessage user_message = getUserMessageResponse();
                if (user_message != null && user_message.getCode() == SocketMessage.PLAYER_ATTACK) {
                    if (user_message.getArguments()[0].equals(user.getUsername())) {
                        int damage = (int) user_message.getArguments()[1];
                        player2Health -= damage;
                        //display
                    } else {
                        int damage = (int) user_message.getArguments()[1];
                        player1Health -= damage;
                        //display
                    }

                    // Check if either player has won

                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

}
