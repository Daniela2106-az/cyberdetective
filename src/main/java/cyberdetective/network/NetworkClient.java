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
                System.out.println("Intentando conectar a " + host + ":" + port + "...");
                socket = new java.net.Socket();
                socket.connect(new java.net.InetSocketAddress(host, port), 5000);
                System.out.println("Socket conectado.");
                
                out = new ObjectOutputStream(socket.getOutputStream());
                out.flush(); 
                System.out.println("ObjectOutputStream flusheado.");
                
                in = new ObjectInputStream(socket.getInputStream());
                System.out.println("ObjectInputStream listo.");
                
                running = true;

                // Enviar mensaje inicial de conexión
                send(new GameMessage(GameMessage.Type.CONNECT, playerName, playerName));
                System.out.println("Mensaje CONNECT enviado para: " + playerName);

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
