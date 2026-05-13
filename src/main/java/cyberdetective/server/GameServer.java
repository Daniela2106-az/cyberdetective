package cyberdetective.server;

import cyberdetective.data.NivelesData;
import cyberdetective.network.GameMessage;
import java.io.*;
import java.net.*;
import java.util.*;

public class GameServer {
    private static final int PORT = 12345;
    private List<ClientHandler> clients = new ArrayList<>();
    private int suspectIndex;
    private int currentQuestionIndex = -1;
    private boolean questionAnswered = false;
    private int failedCount = 0;
    private Set<String> respondedPlayers = new HashSet<>();
    private Map<String, Integer> playerScores = new HashMap<>();
    private ServerSocket serverSocket;
    private boolean running = false;

    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            running = true;
            System.out.println("Servidor de CyberDetective iniciado en el puerto " + PORT);
            
            suspectIndex = (int) (Math.random() * NivelesData.PERFILES_SOSPECHOSOS.length);

            while (running) {
                try {
                    Socket socket = serverSocket.accept();
                    ClientHandler handler = new ClientHandler(socket, this);
                    new Thread(handler).start();
                } catch (IOException e) {
                    if (!running) break;
                }
            }
            
        } catch (IOException e) {
            if (running) e.printStackTrace();
        }
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void startInBackground() {
        new Thread(() -> new GameServer().start()).start();
    }

    public synchronized void addClient(ClientHandler client) {
        clients.add(client);
        playerScores.put(client.getPlayerName(), 0);
        if (clients.size() == 1) {
            client.send(new GameMessage(GameMessage.Type.WAITING, "Esperando al segundo detective...", "SERVER"));
        } else if (clients.size() == 2) {
            broadcast(new GameMessage(GameMessage.Type.START, suspectIndex, "SERVER"));
        }
    }

    public synchronized void removeClient(ClientHandler client) {
        clients.remove(client);
        if (client.getPlayerName() != null) {
            playerScores.remove(client.getPlayerName());
            broadcast(new GameMessage(GameMessage.Type.CHAT, "DISCONNECTED:" + client.getPlayerName(), "SERVER"));
        }
        System.out.println("Cliente desconectado: " + client.getPlayerName());
    }

    public synchronized void broadcast(GameMessage msg) {
        for (ClientHandler client : clients) {
            client.send(msg);
        }
    }

    /** Envía el mensaje a todos los clientes EXCEPTO al emisor (para no rebotar el propio mensaje). */
    public synchronized void sendToOther(GameMessage msg, String senderName) {
        for (ClientHandler client : clients) {
            if (!senderName.equals(client.getPlayerName())) {
                client.send(msg);
            }
        }
    }

    public synchronized void handleAction(GameMessage msg) {
        String data = (String) msg.getData();
        if (data != null && data.startsWith("ANSWER:")) {
            String[] parts = data.split(":");
            int qIdx = Integer.parseInt(parts[1]);
            int option = Integer.parseInt(parts[2]);
            boolean correct = Boolean.parseBoolean(parts[3]);

            if (currentQuestionIndex != qIdx) {
                currentQuestionIndex = qIdx;
                questionAnswered = false;
                failedCount = 0;
                respondedPlayers.clear();
            }

            if (!questionAnswered && !respondedPlayers.contains(msg.getSender())) {
                respondedPlayers.add(msg.getSender());
                if (correct) {
                    questionAnswered = true;
                    String player = msg.getSender();
                    playerScores.put(player, playerScores.getOrDefault(player, 0) + 50);
                    broadcast(new GameMessage(GameMessage.Type.UPDATE_SCORE, 
                        "RESOLVED:" + player + ":" + qIdx + ":" + option + ":" + playerScores.get(player), "SERVER"));
                } else {
                    failedCount++;
                    String player = msg.getSender();
                    int newScore = Math.max(0, playerScores.getOrDefault(player, 0) - 25);
                    playerScores.put(player, newScore);
                    
                    if (failedCount >= 2) {
                        broadcast(new GameMessage(GameMessage.Type.UPDATE_SCORE, 
                            "BOTH_FAILED:" + qIdx + ":" + option, "SERVER"));
                    } else {
                        broadcast(new GameMessage(GameMessage.Type.UPDATE_SCORE, 
                            "PLAYER_FAILED:" + player + ":" + qIdx + ":" + option + ":" + newScore, "SERVER"));
                    }
                }
            }
        } else if (data != null && data.startsWith("ACTION_ATTEMPT:")) {
            // ACTION_ATTEMPT:accionIdx:correct
            String[] parts = data.split(":");
            int aIdx = Integer.parseInt(parts[1]);
            boolean correct = Boolean.parseBoolean(parts[2]);

            if (currentActionIndex != aIdx) {
                currentActionIndex = aIdx;
                actionAnswered = false;
                actionFailedCount = 0;
                actionRespondedPlayers.clear();
            }

            if (!actionAnswered && !actionRespondedPlayers.contains(msg.getSender())) {
                actionRespondedPlayers.add(msg.getSender());
                if (correct) {
                    actionAnswered = true;
                    String player = msg.getSender();
                    playerScores.put(player, playerScores.getOrDefault(player, 0) + 50);
                    broadcast(new GameMessage(GameMessage.Type.ACTION, 
                        "ACTION_RESOLVED:" + player + ":" + aIdx + ":" + playerScores.get(player), "SERVER"));
                } else {
                    actionFailedCount++;
                    String player = msg.getSender();
                    int newScore = Math.max(0, playerScores.getOrDefault(player, 0) - 25);
                    playerScores.put(player, newScore);
                    
                    if (actionFailedCount >= 2) {
                        broadcast(new GameMessage(GameMessage.Type.ACTION, 
                            "ACTION_BOTH_FAILED:" + aIdx, "SERVER"));
                    } else {
                        broadcast(new GameMessage(GameMessage.Type.ACTION, 
                            "ACTION_PLAYER_FAILED:" + player + ":" + aIdx + ":" + newScore, "SERVER"));
                    }
                }
            }
        } else if (data != null && data.startsWith("PHASE2_SCORE_UPDATE:")) {
            String score = data.split(":")[1];
            sendToOther(new GameMessage(GameMessage.Type.ACTION, "OPPONENT_SCORE_P2:" + score, msg.getSender()), msg.getSender());
        } else if ("REVEAL_EVIDENCE".equals(data)) {
            broadcast(new GameMessage(GameMessage.Type.ACTION, "OPPONENT_REVEALED", msg.getSender()));
        } else if (data != null && data.startsWith("LEVEL_READY:")) {
            synchronized(this) {
                readyPlayers.add(msg.getSender());
                System.out.println("Detective " + msg.getSender() + " listo para el nivel.");
                if (readyPlayers.size() >= 2) {
                    String levelIdx = data.split(":")[1];
                    broadcast(new GameMessage(GameMessage.Type.ACTION, "START_LEVEL:" + levelIdx, "SERVER"));
                    readyPlayers.clear();
                    // Limpiar estados de sincronización previos para evitar bloqueos
                    avlReadyPlayers.clear();
                    interrogationReadyPlayers.clear();
                    nivel5ChronoReadyPlayers.clear();
                    nivel5ReportReadyPlayers.clear();
                    System.out.println("Nivel " + levelIdx + " iniciado. Estados de sincronización limpiados.");
                }
            }
        } else if ("AVL_READY".equals(data)) {
            synchronized(this) {
                avlReadyPlayers.add(msg.getSender());
                System.out.println("Detective " + msg.getSender() + " listo para insertar en AVL. (Total: " + avlReadyPlayers.size() + "/2)");
                if (avlReadyPlayers.size() >= 2) {
                    broadcast(new GameMessage(GameMessage.Type.ACTION, "AVL_START:go", "SERVER"));
                    avlReadyPlayers.clear();
                    System.out.println("Sincronización AVL completa. Mensaje START enviado.");
                }
            }
        } else if ("INTERROGATION_READY".equals(data)) {
            synchronized(this) {
                interrogationReadyPlayers.add(msg.getSender());
                System.out.println("Detective " + msg.getSender() + " listo para el interrogatorio.");
                if (interrogationReadyPlayers.size() >= 2) {
                    broadcast(new GameMessage(GameMessage.Type.ACTION, "INTERROGATION_START:go", "SERVER"));
                    interrogationReadyPlayers.clear();
                }
            }
        } else if ("NIVEL5_CHRONO_READY".equals(data)) {
            synchronized(this) {
                nivel5ChronoReadyPlayers.add(msg.getSender());
                System.out.println("Detective " + msg.getSender() + " listo para la cronología.");
                if (nivel5ChronoReadyPlayers.size() >= 2) {
                    broadcast(new GameMessage(GameMessage.Type.ACTION, "NIVEL5_CHRONO_START:go", "SERVER"));
                    nivel5ChronoReadyPlayers.clear();
                }
            }
        } else if ("NIVEL5_REPORT_READY".equals(data)) {
            synchronized(this) {
                nivel5ReportReadyPlayers.add(msg.getSender());
                System.out.println("Detective " + msg.getSender() + " listo para el reporte final.");
                if (nivel5ReportReadyPlayers.size() >= 2) {
                    broadcast(new GameMessage(GameMessage.Type.ACTION, "NIVEL5_REPORT_START:go", "SERVER"));
                    nivel5ReportReadyPlayers.clear();
                }
            }
        } else if (data != null && data.startsWith("MINIGAME_READY:")) {
            int qIdx = Integer.parseInt(data.split(":")[1]);
            synchronized(this) {
                minigameReadyPlayers.add(msg.getSender());
                if (minigameReadyPlayers.size() >= 2) {
                    currentMinigameQIdx = qIdx;
                    currentMinigameWinner = null;
                    int gameId = cyberdetective.minijuego.GestorMinijuegos.seleccionarIdAleatorio(qIdx);
                    broadcast(new GameMessage(GameMessage.Type.ACTION,
                        "MINIGAME_START:" + gameId + ":" + qIdx, "SERVER"));
                    minigameReadyPlayers.clear();
                }
            }
        } else if (data != null && data.startsWith("MINIGAME_DONE:")) {
            String[] parts = data.split(":");
            int qIdx = Integer.parseInt(parts[1]);
            synchronized(this) {
                if (currentMinigameWinner == null && qIdx == currentMinigameQIdx) {
                    String winner = msg.getSender();
                    currentMinigameWinner = winner;
                    playerScores.put(winner, playerScores.getOrDefault(winner, 0) + 50);
                    broadcast(new GameMessage(GameMessage.Type.ACTION,
                        "MINIGAME_RESOLVED:" + winner + ":" + qIdx + ":" + playerScores.get(winner), "SERVER"));
                }
                // El segundo en terminar no recibe penalización (solo 0 puntos)
            }
        } else if (data != null && data.startsWith("INVESTIGATION_COMPLETE:")) {
            broadcast(new GameMessage(GameMessage.Type.ACTION, "OPPONENT_ACTION:" + data.split(":")[1], msg.getSender()));
        }
    }
    
    private int currentActionIndex = -1;
    private boolean actionAnswered = false;
    private int actionFailedCount = 0;
    private Set<String> actionRespondedPlayers = new HashSet<>();
    private Set<String> readyPlayers = new HashSet<>();
    private Set<String> avlReadyPlayers = new HashSet<>();
    private Set<String> interrogationReadyPlayers = new HashSet<>();
    private Set<String> nivel5ChronoReadyPlayers = new HashSet<>();
    private Set<String> nivel5ReportReadyPlayers = new HashSet<>();
    // Minijuegos
    private Set<String> minigameReadyPlayers = new HashSet<>();
    private int currentMinigameQIdx = -1;
    private String currentMinigameWinner = null;

    public static void main(String[] args) {
        new GameServer().start();
    }
}

class ClientHandler implements Runnable {
    private Socket socket;
    private GameServer server;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String playerName;

    public ClientHandler(Socket socket, GameServer server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush(); // Enviar cabecera inmediatamente
            in = new ObjectInputStream(socket.getInputStream());

            while (true) {
                GameMessage msg = (GameMessage) in.readObject();
                if (msg.getType() == GameMessage.Type.CONNECT) {
                    this.playerName = (String) msg.getData();
                    server.addClient(this);
                } else {
                    server.handleAction(msg);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            // Manejo de desconexión
        } finally {
            server.removeClient(this);
            try {
                socket.close();
            } catch (IOException e) {
                // Ya cerrado
            }
        }
    }

    public String getPlayerName() {
        return playerName;
    }

    public void send(GameMessage msg) {
        try {
            out.writeObject(msg);
            out.flush();
        } catch (IOException e) {
            // Error de envío
        }
    }
}
