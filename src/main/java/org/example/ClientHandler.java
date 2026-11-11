package org.example;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import com.google.gson.Gson;

public class ClientHandler {

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private Gson gson;

    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.gson = new Gson();
            clientHandlers.add(this);
        } catch (IOException e) {
            closeEverything();
        }
    }

    public void sendMessage(GameMessage message) {
        try {
            String jsonPayload = gson.toJson(message);
            bufferedWriter.write(jsonPayload);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            closeEverything();
        }
    }

    public GameMessage receiveMessage(String messageToSend) {
        try {
            String incomingJson = bufferedReader.readLine();
            if (incomingJson != null) {
                return gson.fromJson(incomingJson, GameMessage.class);
            }
        } catch (IOException e) {
            closeEverything();
        }
        return null;
    }

    public void closeEverything() {
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
