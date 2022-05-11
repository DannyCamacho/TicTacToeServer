package com.tictactoe.tictactoeserver;

import javafx.application.Platform;
import java.io.IOException;
import java.net.*;
import java.util.*;

public class TicTacToeServer {
    private final int port;
    private final ServerController controller;
    private final Set<UserThread> players;
    private ServerSocket serverSocket;
    private UserThread gameController;
    private UserThread gameManager;
    private UserThread minimax;

    public TicTacToeServer(int port, ServerController controller) {
        this.port = port;
        this.controller = controller;
        players = new HashSet<>();
    }

    public void execute() {
        new Thread( () -> {
            try {
                serverSocket = new ServerSocket(port);
                print("Tic-Tac-Toe Server started at " + new Date() + '\n');
                while (true) {
                    Socket socket = serverSocket.accept();
                    UserThread newUser = new UserThread(socket, this);
                    addUserThread(newUser);
                    newUser.start();
                }
            } catch(IOException ex) {
                print(ex.getMessage());
            }
        }).start();
    }

    void print(String message) {
        Platform.runLater( () -> controller.update(message));
    }

    void updatePlayer(Object message, String userName) {
        for (UserThread player : players) {
            System.out.println(player + " " + player.getUserName());
            if (Objects.equals(player.getUserName(), userName)) {
                player.sendMessage(message);
            }
        }
    }

    void updateController(Object message) {
        gameController.sendMessage(message);
    }

    void updateManager(Object message) {
        gameManager.sendMessage(message);
    }

    void updateMinimax(Object message) {
        minimax.sendMessage(message);
    }

    void addUserThread(UserThread userThread) {
        players.add(userThread);
    }

    void addGameController(UserThread gameController) {
        this.gameController = gameController;
        players.remove(gameController);
    }

    void addGameManager(UserThread gameManager) {
        this.gameManager = gameManager;
        players.remove(gameManager);
    }

    void addMinimax(UserThread minimax) {
        this.minimax = minimax;
        players.remove(minimax);
    }

    void removeUserThread(UserThread user) {
        players.remove(user);
    }

    void removeGameController() {
        gameController = null;
    }

    void removeGameManager() {
        gameManager = null;
    }

    void removeMinimax() {
        minimax = null;
    }
}