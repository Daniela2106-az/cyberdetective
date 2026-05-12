package cyberdetective.network;

import java.io.Serializable;

public class GameMessage implements Serializable {
    public enum Type {
        CONNECT,        // Cliente -> Servidor (Nombre)
        WAITING,        // Servidor -> Cliente (Esperando oponente)
        START,          // Servidor -> Cliente (Sospechoso, Nivel 1)
        SYNC_STATE,     // Servidor -> Cliente (Estado completo)
        ACTION,         // Cliente -> Servidor (Revelar pista, Responder)
        REJECT_ACTION,  // Servidor -> Cliente (Alguien más ya respondió)
        UPDATE_SCORE,   // Servidor -> Cliente (Puntos actualizados)
        CHAT            // Chat opcional
    }

    private Type type;
    private Object data;
    private String sender;

    public GameMessage(Type type, Object data, String sender) {
        this.type = type;
        this.data = data;
        this.sender = sender;
    }

    public Type getType() { return type; }
    public Object getData() { return data; }
    public String getSender() { return sender; }
}
