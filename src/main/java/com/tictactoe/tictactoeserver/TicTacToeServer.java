package com.tictactoe.tictactoeserver;

import com.tictactoe.message.*;
import javafx.application.Platform;
import java.io.IOException;
import java.net.*;
import java.util.*;

public class TicTacToeServer {
    private final int port;
    private ServerSocket serverSocket;
    private final ServerController controller;
    private UserThread gameController;
    private Set<String> userNames = new HashSet<>();
    private Set<String> games = new HashSet<>();
    private Map<String, List<UserThread>> gameMap = new HashMap<>();
    private Set<UserThread> userThreads = new HashSet<>();

    public TicTacToeServer(int port, ServerController controller) {
        this.port = port;
        this.controller = controller;
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

    void updateGameList(UserThread user) {
        user.sendMessage(new GameListResult(games));
    }

    void broadcastMove(String gameName, PlayerMoveResult move) throws IOException {
        List<UserThread> current = gameMap.get(gameName);
        for (UserThread aUser : current) {
            aUser.sendMessage(move);
        }
    }

    void print(String message) {
        Platform.runLater( () -> controller.update(message));
    }

    void addUserName(String userName) {
        userNames.add(userName);
        Platform.runLater( () -> controller.addClient(userName));
    }

    void addUserThread(UserThread userThread) {
        userThreads.add(userThread);
    }

    void addGameController(UserThread gameController) {
        this.gameController = gameController;
    }

    Boolean addGame(String game) {
        for (String gameExists : games) {
            if (Objects.equals(gameExists, game))
                return false;
        }
        Platform.runLater( () -> controller.addGame(game));
        games.add(game);
        return true;
    }

    int addPlayerToGame(String game, UserThread user) {
        List<UserThread> current = gameMap.computeIfAbsent(game, k -> new ArrayList<UserThread>());
        current.add(user);
        return current.size();
    }

    void removeUser(String userName, UserThread aUser) {
        boolean removed = userNames.remove(userName);
        if (removed) userThreads.remove(aUser);
        Platform.runLater( () -> controller.removeClient(userName));
    }

    void removeGame(String game) {
        games.remove(game);
        Platform.runLater( () -> controller.removeGame(game));
    }

    Set<String> getUserNames() {
        return this.userNames;
    }

    boolean hasUsers() {
        return !this.userNames.isEmpty();
    }

    boolean hasGames() {
        return !this.games.isEmpty();
    }

    UserThread getGameController() {
        return gameController;
    }
}
