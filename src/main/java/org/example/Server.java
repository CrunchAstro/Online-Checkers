package org.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.google.gson.Gson;

public class Server {
    private final ServerSocket serverSocket;

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void startServer() {

        try{
            while (!serverSocket.isClosed()) {
                Socket socket1 = serverSocket.accept();
                System.out.println("Player 1 has connected. Waiting for an opponent to join.");
                ClientHandler player1 = new ClientHandler(socket1);

                Socket socket2 = serverSocket.accept();
                System.out.println("Player 1 has connected. Waiting for an opponent to join.");
                ClientHandler player2 = new ClientHandler(socket2);

                GameSession match = new GameSession(player1, player2);

                Thread matchThread = new Thread(match);
                matchThread.start();

                System.out.println("Two players paired. GameSession started on new thread.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeServerSocket() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = new ServerSocket(1234);
        Server server = new Server(serverSocket);
        server.startServer();

        GameMessage testMove = new GameMessage("MOVE", "Player1", 2, 3,3, 4);

        Gson gson = new Gson();
        String json = gson.toJson(testMove);

        System.out.println(json);
    }
}