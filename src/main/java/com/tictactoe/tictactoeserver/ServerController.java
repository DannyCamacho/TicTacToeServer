package com.tictactoe.tictactoeserver;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;

public class ServerController {
    private TicTacToeServer server;
    @FXML
    public Button startButton;
    @FXML
    public ListView<String> clientListView, gameListView;
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

    public void addClient(String user) {
        clientListView.getItems().add(user);
    }

    public void removeClient(String user) {
        clientListView.getItems().remove(user);
    }

    public void addGame(String game) {
        gameListView.getItems().add(game);
    }

    public void removeGame(String game) {
        gameListView.getItems().remove(game);
    }
}