package com.example.assignmentdistributed.user;

import com.example.assignmentdistributed.Game;
import com.example.assignmentdistributed.server.GameServer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class GameView {
    private static Label leftPlayerNameLabel;
    private static Label leftPlayerDamageLabel;
    private static Label rightPlayerNameLabel;
    private static Label rightPlayerDamageLabel;
    private static Label actionLabel;

    public static void run(Stage stage, Game game, int mode) {
        String leftPlayerName = game.P1;
        String rightPlayerName = game.P2;

        leftPlayerNameLabel = new Label(leftPlayerName);
        leftPlayerNameLabel.setAlignment(Pos.TOP_CENTER);
        leftPlayerDamageLabel = new Label(Integer.toString(game.P1_currentChampHealth));
        leftPlayerDamageLabel.setAlignment(Pos.CENTER);
        rightPlayerNameLabel = new Label(rightPlayerName);
        rightPlayerNameLabel.setAlignment(Pos.TOP_CENTER);
        rightPlayerDamageLabel = new Label(Integer.toString(game.P2_currentChampHealth));
        rightPlayerDamageLabel.setAlignment(Pos.CENTER);
        actionLabel = new Label(game.tooltip);
        actionLabel.setAlignment(Pos.CENTER);

        // Create a layout for the labels
        GridPane layout = new GridPane();
        layout.setPadding(new Insets(20));
        layout.setHgap(100);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #F5F5F5;");

        layout.add(leftPlayerNameLabel, 0, 0);
        layout.add(leftPlayerDamageLabel, 0, 2);
        layout.add(new ImageView(), 0, 1);
        layout.add(rightPlayerNameLabel, 2, 0);
        layout.add(rightPlayerDamageLabel, 2, 2);
        layout.add(new ImageView(), 2, 1);
        layout.add(actionLabel, 1, 2);

        Scene scene = new Scene(layout, 700, 500);
        if(mode == 1)
            stage.setTitle("Simple Game Window");
        else
            stage.setTitle("Ranked Game Window");
        stage.setScene(scene);

        stage.show();
    }

    public static void updateView(Game game) {
        // Update the action label with the attacker name and damage value
        actionLabel.setText(game.tooltip);
    }
}