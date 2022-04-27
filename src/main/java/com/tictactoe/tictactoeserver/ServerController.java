package com.tictactoe.tictactoeserver;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;

public class ServerController {
    private TicTacToeServer server;
    @FXML
    public Button startButton;
    @FXML
    public TextArea ta;

    public void update(String message) {
        ta.appendText(message);
    }

    public void onStartButtonClicked(MouseEvent mouseEvent) {
        server = new TicTacToeServer(8000, this);
        server.execute();
        startButton.setVisible(false);
    }
}