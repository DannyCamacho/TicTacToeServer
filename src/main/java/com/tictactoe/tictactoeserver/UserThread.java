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
                server.print("Starting thread for Client " + inetAddress.getHostName() + " at " + new Date() + '\n');
                server.print("Client's username is " + userName + " (IP Address: " + inetAddress.getHostAddress() + ")\n");
                server.updateManager(message);
                sendMessage(new ServerConnection("Player", userName, true));
                while (true) {
                    message = input.readObject();
                    if (message instanceof GameListRequest) {
                        server.updateManager(message);
                    } else if (message instanceof ConnectToGame) {
                        server.updateManager(message);
                    } else if (message instanceof PlayerMoveSend) {
                        server.updateController(message);
                    } else if (message instanceof UpdateGame) {
                        server.updateManager(message);
                    } else if (message instanceof ChatMessage) {
                        server.updateManager(message);
                    } else if (message instanceof ServerConnection) {
                        if (!((ServerConnection)message).connection()) {
                            sendMessage(new ServerConnection("Player", userName, false));
                            server.updateManager(message);
                            server.removeUserThread(this);
                            socket.close();
                            server.print("User " + userName + " has quit\n");
                            break;
                        }
                    }
                }
            } else if (Objects.equals(((ServerConnection)message).connectType(), "Controller")) {
                server.addGameController(this);
                server.print("Starting thread for Game Controller " + inetAddress.getHostName() + " at " + new Date() + '\n');
                char [] boardState = { 'X', '\0', 'X', 'O', '\0', 'O', '\0', '\0', '\0', '\0' };
                output.writeObject(new PlayerMoveSend("Test", 'X', 1, boardState));
                output.flush();
                PlayerMoveResult testMove = (PlayerMoveResult)input.readObject();
                if (Objects.equals(testMove.result(), "X0")) server.print("Test move result from Game Controller successful. " +  "\n");
                while (true) {
                    message = input.readObject();
                    if (message instanceof PlayerMoveResult) {
                        server.updateManager(message);
                    } else if (message instanceof ServerConnection) {
                        if (!((ServerConnection)message).connection()) {
                            server.removeGameController();
                            socket.close();
                            server.print("Game Controller removed\n");
                            break;
                        }
                    }
                }
            } else if (Objects.equals(((ServerConnection)message).connectType(), "Manager")) {
                server.addGameManager(this);
                server.print("Starting thread for Game Manager " + inetAddress.getHostName() + " at " + new Date() + '\n');
                while (true) {
                    message = input.readObject();
                    if (message instanceof UpdateGame) {
                        server.updatePlayer(message, getUserName());
                    } else if (message instanceof GameListResult) {
                        server.updatePlayer(message, getUserName());
                    } else if (message instanceof ConnectToGame) {
                        server.updatePlayer(message, getUserName());
                    } else if (message instanceof ChatMessage) {
                        server.updatePlayer(message, getUserName());
                    } else if (message instanceof MinimaxMoveSend) {
                        server.updateMinimax(message);
                    } else if (message instanceof ServerConnection) {
                        if (!((ServerConnection)message).connection()) {
                            server.removeGameManager();
                            socket.close();
                            server.print("Game Manager Removed\n");
                            break;
                        }
                    }
                }
            } else if (Objects.equals(((ServerConnection)message).connectType(), "Minimax")) {
                server.addMinimax(this);
                server.print("Starting thread for Minimax AI " + inetAddress.getHostName() + " at " + new Date() + '\n');
                while (true) {
                    message = input.readObject();
                    if (message instanceof PlayerMoveSend) {
                        server.updateController(message);
                    } else if (message instanceof ServerConnection) {
                        if (!((ServerConnection)message).connection()) {
                            server.removeMinimax();
                            socket.close();
                            server.print("Minimax AI Removed\n");
                            break;
                        }
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