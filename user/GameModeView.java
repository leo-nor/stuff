package com.example.assignmentdistributed.user;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.function.Consumer;

public class GameModeView {
    public static void run(Stage stage, Consumer<Integer> modeCallback) {
        Label simpleModeLabel = new Label("Simple Mode: 1");
        Label rankingModeLabel = new Label("Ranking Mode: 2");
        TextField modeField = new TextField();
        Button nextButton = new Button("Next");

        // Create a layout for the labels
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #F5F5F5;");

        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setSpacing(10);
        buttonBox.getChildren().addAll(nextButton);

        layout.getChildren().addAll(simpleModeLabel, rankingModeLabel, modeField, buttonBox);

        // Create a scene with the layout and set the window title
        Scene scene = new Scene(layout, 400, 400);
        stage.setTitle("GameMode Window");
        stage.setScene(scene);

        // Show the window
        stage.show();

        nextButton.setOnAction(event -> {
            int mode = Integer.parseInt(modeField.getText());
            modeCallback.accept(mode);
            stage.close();
        });
    }
}
