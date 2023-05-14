package com.example.assignmentdistributed.user;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;



public class QueueView {
    private static Label positionLabel;

    public static void run(Stage stage, String username, int position) {
        // Create labels for the username and queue position
        Label usernameLabel = new Label("Username: " + username);
        positionLabel = new Label("Queue position: " + position);

        // Create a label for the subtitle
        Label subtitleLabel = new Label("You are in the queue");
        subtitleLabel.setStyle("-fx-font-size: 24px;");

        // Create a layout for the labels
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #F5F5F5;");
        layout.getChildren().addAll(usernameLabel, subtitleLabel, positionLabel);

        // Create a scene with the layout and set the window title
        Scene scene = new Scene(layout, 500, 200);
        stage.setTitle("Queue Window");
        stage.setScene(scene);

        // Show the window
        stage.show();
    }

    public static void updateView(Stage stage, Integer queueLength){
        positionLabel.setText("Queue position: " + queueLength); // update the positionLabel's text
    }

}

