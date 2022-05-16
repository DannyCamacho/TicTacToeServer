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
                ServerSocket serverSocket = new ServerSocket(port);
                print("Tic-Tac-Toe Server started at " + new Date() + '\n');
                while (true) {
                    Socket socket = serverSocket.accept();
                    new UserThread(socket, this).start();
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
        if (message instanceof ServerConnection) {
            for (UserThread user : userThreads) {
                if (Objects.equals(user.getUserName(), ((ServerConnection)message).userName())) {
                    user.sendMessage(message);
                }
            }
        } else if (message instanceof GameListResult) {
            for (UserThread user : userThreads) {
                if (Objects.equals(user.getUserName(), ((GameListResult)message).userName())) {
                    user.sendMessage(message);
                }
            }
        } else if (message instanceof UpdateGame) {
            for (UserThread user : userThreads) {
                if (Objects.equals(user.getUserName(), ((UpdateGame)message).userName())) {
                    user.sendMessage(message);
                }
            }
        } else if (message instanceof ConnectToGame) {
            for (UserThread user : userThreads) {
                if (Objects.equals(user.getUserName(), ((ConnectToGame)message).userName())) {
                    user.sendMessage(message);
                }
            }
        } else if (message instanceof UpdateGameHistory) {
            for (UserThread user : userThreads) {
                if (Objects.equals(user.getUserName(), ((UpdateGameHistory)message).userName())) {
                    user.sendMessage(message);
                }
            }
        } else if (message instanceof ChatMessage) {
            for (UserThread user : userThreads) {
                if (Objects.equals(user.getUserName(), ((ChatMessage)message).userName())) {
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

    void removeUserThread(UserThread userThread) {
        userThreads.remove(userThread);
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