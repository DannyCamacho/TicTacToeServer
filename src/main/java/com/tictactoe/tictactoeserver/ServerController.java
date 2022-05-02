package com.tictactoe.tictactoeserver;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

public class ServerController {
    @FXML
    public Button startButton;
    @FXML
    public TextArea ta;

    public void update(String message) {
        ta.appendText(message);
    }

    public void onStartButtonClicked() {
        TicTacToeServer server = new TicTacToeServer(8000, this);
        server.execute();
        startButton.setVisible(false);
    }
}