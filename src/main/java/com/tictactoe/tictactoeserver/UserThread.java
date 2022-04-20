package com.tictactoe.tictactoeserver;

import com.tictactoe.message.*;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Date;
import java.util.Objects;

public class UserThread extends Thread {
    private final Socket socket;
    private final TicTacToeServer server;
    private String userName;
    private ObjectInputStream inputFromClient;
    private ObjectOutputStream outputToClient;

    public UserThread(Socket socket, TicTacToeServer server) {
        this.socket = socket;
        this.server = server;
    }

    public void run() {
        try {
            outputToClient = new ObjectOutputStream(socket.getOutputStream());
            inputFromClient = new ObjectInputStream(socket.getInputStream());

            InetAddress inetAddress = socket.getInetAddress();
            Object message = inputFromClient.readObject();

            if (message instanceof ServerConnection) {
                userName = ((ServerConnection) message).userName();
                server.addUserName(userName);
                server.print("Starting thread for Client " + inetAddress.getHostName() + " at " + new Date() + '\n');
                server.print("Client's username is " + userName + " (IP Address: " + inetAddress.getHostAddress() + ")\n");
                sendMessage(new ServerConnection(null, true));

                while (true) {
                    message = inputFromClient.readObject();
                    if (message instanceof GameListRequest) {
                        server.print("User " + userName + " Game List refresh\n");
                        server.updateGameList(this);
                    } else if (message instanceof ConnectToGame) {
                        String gameName = ((ConnectToGame) message).gameName();
                        if (server.addGame(gameName)) server.print(userName + " created new game " + gameName + " at " + new Date() + "\n");
                        else server.print(userName + " connected to existing game " + gameName + " at " + new Date() + "\n");
                        int gameCount = server.addPlayerToGame(gameName, this);
                        switch (gameCount) {
                            case 1 -> sendMessage(new ConnectToGame(gameName, null, 'O'));
                            case 2 -> sendMessage(new ConnectToGame(gameName, null, 'X'));
                            default -> sendMessage(new ConnectToGame(gameName, null, 'S'));
                        }
                    } else if (message instanceof ServerConnection) {
                        if (!((ServerConnection) message).connection()) {
                            server.removeUser(userName, this);
                            socket.close();
                            server.print("User " + userName + " has quit\n");
                            break;
                        }
                    } else if (message instanceof PlayerMoveSend) {
                        UserThread controller = server.getGameController();
                        controller.sendMessage(message);
                    }
                }
            } else if (message instanceof GameControllerConnection) {
                server.addGameController(this);
                server.print("Starting thread for Game Controller " + inetAddress.getHostName() + " at " + new Date() + '\n');
                char [] boardState = { 'X', '\0', 'X', 'O', '\0', 'O', '\0', '\0', '\0', '\0',};
                outputToClient.writeObject(new PlayerMoveSend("Test", 'X', 1, boardState));
                outputToClient.flush();
                PlayerMoveResult testMove = (PlayerMoveResult) inputFromClient.readObject();
                if (Objects.equals(testMove.result(), "X")) server.print("Test move result from Game Controller successful. " +  "\n");

                while (true) {
                    message = inputFromClient.readObject();
                    if (message instanceof PlayerMoveSend) {
                        server.print("Returning move to " + ((PlayerMoveResult) message).GameName() + "\n");
                        server.updateGameList(this);
                    } else if (message instanceof GameControllerConnection) {
                        if (!((GameControllerConnection) message).connection()) {
                            break;
                        }
                    } else if (message instanceof PlayerMoveResult) {
                        System.out.println("IN PLAYERMOVERESULT");
                        server.broadcastMove(((PlayerMoveResult) message).GameName(), (PlayerMoveResult) message);
                        System.out.println(((PlayerMoveResult) message).GameName());
                    }
                }
            }
        } catch (IOException | ClassNotFoundException ex) {
            server.print(ex.getMessage());
        }
    }

    public void sendMessage(Object message) {
        try {
            outputToClient.writeObject(message);
            outputToClient.flush();
        } catch(IOException ex) {
            server.print(ex.getMessage());
        }
    }
}