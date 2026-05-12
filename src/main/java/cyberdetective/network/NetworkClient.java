package cyberdetective.network;

import javafx.application.Platform;
import java.io.*;
import java.net.*;
import java.util.function.Consumer;

public class NetworkClient {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private boolean running = false;
    private Consumer<GameMessage> onMessageReceived;
    
    public void setOnMessageReceived(Consumer<GameMessage> onMessageReceived) {
        this.onMessageReceived = onMessageReceived;
    }

    public void connect(String host, int port, String playerName, Consumer<GameMessage> onMessageReceived) {
        this.onMessageReceived = onMessageReceived;
        
        new Thread(() -> {
            try {
                socket = new Socket(host, port);
                out = new ObjectOutputStream(socket.getOutputStream());
                out.flush(); // Enviar cabecera inmediatamente
                in = new ObjectInputStream(socket.getInputStream());
                running = true;

                // Enviar mensaje inicial de conexión
                send(new GameMessage(GameMessage.Type.CONNECT, playerName, playerName));

                // Hilo para escuchar mensajes
                new Thread(this::listen).start();
            } catch (IOException e) {
                Platform.runLater(() -> {
                    onMessageReceived.accept(new GameMessage(GameMessage.Type.CHAT, "❌ ERROR: " + e.getMessage(), "SYSTEM"));
                });
            }
        }).start();
    }

    private void listen() {
        try {
            while (running) {
                GameMessage msg = (GameMessage) in.readObject();
                Platform.runLater(() -> onMessageReceived.accept(msg));
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Conexión perdida con el servidor.");
            running = false;
        }
    }

    public void send(GameMessage msg) {
        try {
            out.writeObject(msg);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        running = false;
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
