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
    private ObjectOutputStream output;
    private String userName;

    public UserThread(Socket socket, TicTacToeServer server) {
        this.socket = socket;
        this.server = server;
    }

    public void run() {
        try {
            output = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
            InetAddress inetAddress = socket.getInetAddress();
            Object message = input.readObject();

            if (Objects.equals(((ServerConnection)message).connectType(), "Player")) {
                userName = ((ServerConnection)message).userName();
                server.addUserThread(this);
                server.print("Starting thread for Client " + inetAddress.getHostName() + " at " + new Date() + '\n');
                server.print("Client's username is " + userName + " (IP Address: " + inetAddress.getHostAddress() + ")\n");
                server.updatePlayer(message);
                server.updateManager(message);
                while (true) {
                    message = input.readObject();
                    if (message instanceof GameListRequest) {
                        server.updateManager(message);
                    } else if (message instanceof ConnectToGame) {
                        server.updateManager(message);
                    } else if (message instanceof UpdateGame) {
                        server.updateManager(message);
                    } else if (message instanceof ChatMessage) {
                        server.updateManager(message);
                    } else if (message instanceof PlayerMoveSend) {
                        server.updateController(message);
                    } else if (message instanceof ServerConnection && !((ServerConnection)message).connection()) {
                        sendMessage(new ServerConnection("Player", userName, false));
                        server.updateManager(message);
                        server.removeUserThread(this);
                        socket.close();
                        server.print("User " + userName + " has quit\n");
                        break;
                    }
                }
            } else if (Objects.equals(((ServerConnection)message).connectType(), "Manager")) {
                server.addGameManager(this);
                server.print("Starting thread for Game Manager " + inetAddress.getHostName() + " at " + new Date() + '\n');
                while (true) {
                    message = input.readObject();
                    if (message instanceof UpdateGame) {
                        server.updatePlayer(message);
                    } else if (message instanceof GameListResult) {
                        server.updatePlayer(message);
                    } else if (message instanceof ConnectToGame) {
                        server.updatePlayer(message);
                    } else if (message instanceof ChatMessage) {
                        server.updatePlayer(message);
                    } else if (message instanceof MinimaxMoveSend) {
                        server.updateMinimax(message);
                    } else if (message instanceof UpdateGameHistory) {
                        server.updatePlayer(message);
                    } else if (message instanceof ServerConnection && !((ServerConnection)message).connection()) {
                        server.removeGameManager();
                        socket.close();
                        server.print("Game Manager Removed\n");
                        break;
                    }
                }
            } else if (Objects.equals(((ServerConnection)message).connectType(), "Controller")) {
                server.addGameController(this);
                server.print("Starting thread for Game Controller " + inetAddress.getHostName() + " at " + new Date() + '\n');
                while (true) {
                    message = input.readObject();
                    if (message instanceof PlayerMoveResult) {
                        server.updateManager(message);
                    } else if (message instanceof ServerConnection && !((ServerConnection)message).connection()) {
                        server.removeGameController();
                        socket.close();
                        server.print("Game Controller removed\n");
                        break;
                    }
                }
            } else if (Objects.equals(((ServerConnection)message).connectType(), "Minimax")) {
                server.addMinimax(this);
                server.print("Starting thread for Minimax AI " + inetAddress.getHostName() + " at " + new Date() + '\n');
                while (true) {
                    message = input.readObject();
                    if (message instanceof PlayerMoveSend) {
                        server.updateController(message);
                    } else if (message instanceof ServerConnection && !((ServerConnection)message).connection()) {
                        server.removeMinimax();
                        socket.close();
                        server.print("Minimax AI Removed\n");
                        break;
                    }
                }
            }
        } catch (IOException | ClassNotFoundException ex) {
            server.print("Connection Error: " + ex.getMessage());
        }
    }

    public void sendMessage(Object message) {
        try {
            output.writeObject(message);
            output.flush();
        } catch(IOException ex) {
            server.print("Error Sending Message: " + ex.getMessage());
        }
    }

    public String getUserName() {
        return userName;
    }
}