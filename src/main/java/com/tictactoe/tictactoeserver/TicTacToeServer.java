package com.tictactoe.tictactoeserver;

import com.tictactoe.message.*;
import javafx.application.Platform;
import java.io.IOException;
import java.net.*;
import java.util.*;

public class TicTacToeServer {
    private final int port;
    private final ServerController controller;
    private final Set<UserThread> userThreads;
    private ServerSocket serverSocket;
    private UserThread gameController;
    private UserThread gameManager;
    private UserThread minimax;

    public TicTacToeServer(int port, ServerController controller) {
        this.port = port;
        this.controller = controller;
        userThreads = new HashSet<>();
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

    void updatePlayer(Object message) {
        if (message instanceof GameListResult) {
            for (UserThread user : userThreads) {
                if (Objects.equals(user.getUserName(), ((GameListResult) message).userName())) {
                    user.sendMessage(message);
                }
            }
        } else if (message instanceof UpdateGame) {
            for (String player : ((UpdateGame)message).users().keySet()) {
                for (UserThread user : userThreads) {
                    if (Objects.equals(user.getUserName(), player)) {
                        user.sendMessage(message);
                    }
                }
            }
        } else if (message instanceof ConnectToGame) {
            for (UserThread user : userThreads) {
                if (Objects.equals(user.getUserName(), ((ConnectToGame) message).userName())) {
                    user.sendMessage(message);
                }
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
        userThreads.add(userThread);
    }

    void addGameController(UserThread gameController) {
        this.gameController = gameController;
    }

    void addGameManager(UserThread gameManager) {
        this.gameManager = gameManager;
    }

    void addMinimax(UserThread minimax) {
        this.minimax = minimax;
    }

    void removeUserThread(UserThread aUser) {
        userThreads.remove(aUser);
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