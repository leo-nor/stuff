package com.example.assignmentdistributed.user;

import com.example.assignmentdistributed.Game;
import com.example.assignmentdistributed.server.GameServer;
import com.example.assignmentdistributed.SocketMessage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class UserAplication extends Application {
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private String username;

    private int mode;

    public UserAplication() {}

    public void connectToServer() {
        String hostname = "localhost";

        try {
            InetAddress ip = InetAddress.getByName(hostname);
            Socket userSocket = new Socket(ip, 12345);
            output = new ObjectOutputStream(userSocket.getOutputStream());
            input = new ObjectInputStream(userSocket.getInputStream());
            userSocket.setSoTimeout(200);
            System.out.println("USER - CONNECTION WAS SUCCESSFULLY SET UP");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void authentication() {
        launch();
    }

    @Override
    public void start(Stage primaryStage) {
        connectToServer();

        drawLoginMenu(primaryStage);
    }

    public void drawLoginMenu(Stage stage) {
        // Create UI controls
        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();
        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();
        Button loginButton = new Button("Login");
        Button registerButton = new Button("Register");

        // Create layout panes and add controls
        GridPane root = new GridPane();
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(10));
        root.setHgap(10);
        root.setVgap(10);
        root.add(usernameLabel, 0, 0);
        root.add(usernameField, 1, 0);
        root.add(passwordLabel, 0, 1);
        root.add(passwordField, 1, 1);

        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setSpacing(10);
        buttonBox.getChildren().addAll(loginButton, registerButton);
        root.add(buttonBox, 0, 2, 2, 1);

        // Create scene and add layout pane
        Scene scene = new Scene(root, 300, 200);

        // Set stage properties and show
        stage.setScene(scene);
        stage.setTitle("Authentication Menu");
        stage.show();

        loginButton.setOnAction(event -> {
            sendLoginRequest(usernameField.getText(), passwordField.getText());

            SocketMessage response = getServerMessageResponse();
            if (response.getCode() == SocketMessage.LOGIN_SUCCESSFUL) {
                stage.close();
                username = usernameField.getText();
                Object[] arguments = response.getArguments();
                String game = (String) arguments[0];

                if (game.equals("game"))
                    draw_success_message("Login succeded! Choose Game Mode.", stage, "startGame");
                else
                    draw_success_message("Login succeded! Choose Game Mode.", stage, "login");
            } else {
                // show error
                stage.close();
                draw_error("Invalid username or password.", stage);
            }
        });

        registerButton.setOnAction(event -> {
            sendRegistrationRequest(usernameField.getText(), passwordField.getText());

            SocketMessage response = getServerMessageResponse();
            if (response.getCode() == SocketMessage.REGISTER_SUCCESSFUL) {
                stage.close();
                draw_success_message("Register Succeeded! Please Login.", stage, "register");
            } else {
                // show error
                stage.close();
                draw_error("Username already exists.", stage);
            }
        });
    }

    public void draw_error(String error, Stage stage) {
        // Create UI controls
        Label errorLabel = new Label(error);
        Button okayButton = new Button("I understand");

        GridPane root = new GridPane();
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(10));
        root.setHgap(10);
        root.setVgap(10);
        root.add(errorLabel, 0, 0);

        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setSpacing(10);
        buttonBox.getChildren().addAll(okayButton);
        root.add(buttonBox, 0, 2, 2, 1);

        // Create scene and add layout pane
        Scene scene = new Scene(root, 300, 200);

        // Set stage properties and show
        Stage errorStage = new Stage();
        errorStage.setScene(scene);
        errorStage.setTitle("Error");
        errorStage.show();

        okayButton.setOnAction(event -> {
            errorStage.close();
            drawLoginMenu(stage);
        });
    }

    public void draw_success_message(String msg, Stage stage, String etapa) {
        // Create UI controls
        Label messageLabel = new Label(msg);
        Button nextButton = new Button("Next");

        GridPane root = new GridPane();
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(10));
        root.setHgap(10);
        root.setVgap(10);
        root.add(messageLabel, 0, 0);

        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setSpacing(10);
        buttonBox.getChildren().addAll(nextButton);
        root.add(buttonBox, 0, 2, 2, 1);

        // Create scene and add layout pane
        Scene scene = new Scene(root, 300, 200);
        Stage successStage = new Stage();
        successStage.setScene(scene);
        successStage.setTitle("Success");
        successStage.show();


        if (etapa.equals("gameMode")){
            stage.setOnCloseRequest(event -> {
                // code to run when the window is closed
                System.out.println("Window closed - exiting queue");
                sendExitQueueRequest(username);
                // any other cleanup code here
            });
        }

        nextButton.setOnAction(event -> {
            successStage.close();

            switch (etapa) {
                case "login" -> askGameMode(stage);
                case "register" -> drawLoginMenu(stage);
                case "gameMode" -> {
                    sendGameMode(mode);
                    inQueue(stage);
                }
                case "startGame" -> {
                    SocketMessage response = getServerMessageResponse();
                    Object[] arguments = response.getArguments();
                    Game game = (Game) arguments[0];

                    startGame(stage, game);
                }
                default -> {
                }
            }
        });
    }

    public SocketMessage getServerMessageResponse(){
        SocketMessage response = null;
        try {
            response = (SocketMessage) input.readObject();
        } catch (IOException | ClassNotFoundException e) {
            //throw new RuntimeException(e);
        }
        return response;
    }

    public void sendLoginRequest(String name, String pass) {
        try {
            SocketMessage loginInfo = new SocketMessage(SocketMessage.LOGIN_REQUEST, name, pass);

            output.writeObject(loginInfo);
            output.flush();
            System.out.println("USER - WRITING login info");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendRegistrationRequest(String name, String pass) {
        try {
            SocketMessage loginInfo = new SocketMessage(SocketMessage.REGISTER_REQUEST, name, pass);

            output.writeObject(loginInfo);
            output.flush();
            System.out.println("USER - WRITING registration info");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void askGameMode(Stage stage) {
        System.out.println("ASKING GAME MODE");
        GameModeView.run(stage, mode -> {
            setMode(mode);
            draw_success_message("Simple Mode chosen Successfully!", stage, "gameMode");
        });
    }

    public void sendGameMode(int mode) {
        try {
            SocketMessage gameMode = new SocketMessage(SocketMessage.GAME_MODE, mode);

            output.writeObject(gameMode);
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public User getNextUserFromRankedQueueWithSimilarRank(User currentUser) {
        // Get the users currently in the queue
        List<User> usersInQueue = server.getRankedQueue().getUsers();
    
        // Sort the users by their rank difference from the current user
        usersInQueue.sort((u1, u2) -> {
            int rankDiff1 = Math.abs(u1.getRank().ordinal() - currentUser.getRank().ordinal());
            int rankDiff2 = Math.abs(u2.getRank().ordinal() - currentUser.getRank().ordinal());
            return Integer.compare(rankDiff1, rankDiff2);
        });
    
        // Find the first user in the queue with a rank difference of 1 or less
        for (User user : usersInQueue) {
            if (Math.abs(user.getRank().ordinal() - currentUser.getRank().ordinal()) <= 1) {
                return user;
            }
        }
    
        // If there are no users in the queue with a rank difference of 1 or less, return null
        return null;
    }

    public void inQueue(Stage stage){
        System.out.println("IN QUEUE");
        SocketMessage posGameMode = getServerMessageResponse();
        Object[] arguments = posGameMode.getArguments();

        if (mode == 1) {
            Integer queueLength = (Integer) arguments[0];
            QueueView.run(stage, username, queueLength);
        }
        else { // ranking
            server.addUserToRankedQueue(this);
            int queue_length = server.getRankedQueueLength();
            output.writeObject(new SocketMessage(SocketMessage.GAME_MODE, queue_length));

            shouldStartGame = false;
            while (!shouldStartGame) {
                if (server.getRankedQueueLength() != queue_length){
                    System.out.println("A ENVIAR SOCKET UPDATE QUEUE");
                    try {
                        output.writeObject(new SocketMessage(SocketMessage.UPDATE_QUEUE_LENGTH, server.getRankedQueueLength()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    queue_length = server.getRankedQueueLength();
                }

                SocketMessage user_message = getUserMessageResponse();
                if (user_message != null && user_message.getCode() == SocketMessage.EXIT_QUEUE) {
                    server.removeUserFromRankedQueue(this);
                }
        
                if (server.getRankedQueueLength() >= 2) {
                    User user1 = server.getNextUserFromRankedQueue();
                    User user2 = server.getNextUserFromRankedQueueWithSimilarRank(user1.getRank());
                    if (user2 == null) {
                        continue;
                    }
                    List<User> users = Arrays.asList(user1, user2);
                    server.createRankedGame(users);
                    shouldStartGame = true;
                    try {
                        user1.output.writeObject(new SocketMessage(SocketMessage.START_GAME, server.getGameByUsers(users)));
                        user2.output.writeObject(new SocketMessage(SocketMessage.START_GAME, server.getGameByUsers(users)));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        Timeline timeline = null;
        Timeline finalTimeline = timeline;
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            SocketMessage response = getServerMessageResponse();
            if (response != null && response.getCode() == SocketMessage.UPDATE_QUEUE_LENGTH) {
                System.out.println("SUCCESS");
                Object[] arguments2 = response.getArguments();
                Integer queueLength2 = (Integer) arguments2[0];
                QueueView.updateView(stage, queueLength2);
            }
            if (response != null && response.getCode() == SocketMessage.START_GAME) {
                System.out.println("Received START_GAME");
                Object[] arguments2 = response.getArguments();
                Game game = (Game) arguments2[0];

                stage.close();
                if (finalTimeline != null) {  //IGNORE WARNING, NEEDED DUE TO MULTITHREADED TIMELINE NATURE
                    finalTimeline.stop();
                }
                startGame(stage, game);
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    public void sendExitQueueRequest(String username) {
        try {
            SocketMessage exitRequest = new SocketMessage(SocketMessage.EXIT_QUEUE, username);

            output.writeObject(exitRequest);
            output.flush();
            System.out.println("USER - Sending Queue Exit request");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startGame(Stage stage, Game game) {
        System.out.println("Game started!");
        GameView.run(stage, game, mode);

        /*boolean hasGameStarted = false;
        Timeline timeline = null;
        Timeline finalTimeline = timeline;
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            SocketMessage response = getServerMessageResponse();
            if (response != null && response.getCode() == SocketMessage.PLAYER_ATTACK) {
                Object[] arguments2 = response.getArguments();
                Integer queueLength2 = (Integer) arguments2[0];
                QueueView.updateView(stage, queueLength2);
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();*/
    }

    private void setMode(int m) {
        mode = m;
    }
}
