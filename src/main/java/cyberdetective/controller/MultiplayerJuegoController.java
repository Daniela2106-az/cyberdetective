package cyberdetective.controller;

import cyberdetective.network.GameMessage;
import cyberdetective.network.NetworkClient;
import javafx.application.Platform;

public class MultiplayerJuegoController extends JuegoController {
    private NetworkClient client;
    private int oponentePuntaje = 0;

    public MultiplayerJuegoController(String nombre, NetworkClient client) {
        super(nombre);
        this.client = client;
        this.client.setOnMessageReceived(this::handleMessage);
    }

    private void handleMessage(GameMessage msg) {
        switch (msg.getType()) {
            case UPDATE_SCORE:
                handleScoreUpdate((String) msg.getData());
                break;
            case ACTION:
                handleActionUpdate((String) msg.getData());
                break;
            case CHAT:
                handleChatUpdate((String) msg.getData());
                break;
        }
    }

    // ── Interrogatorio: mensajes UPDATE_SCORE ──────────────────────────────
    private void handleScoreUpdate(String data) {
        if (data.startsWith("RESOLVED:")) {
            // parts: RESOLVED : sender : correct : option : totalPoints
            String[] parts = data.split(":");
            String sender     = parts[1];
            // En RESOLVED, la respuesta siempre es correcta (por lógica del servidor)
            boolean correct   = true; 
            int option        = Integer.parseInt(parts[3]);
            int totalPoints   = Integer.parseInt(parts[4]);
            procesarRespuestaServidor(option, correct, sender, totalPoints);

        } else if (data.startsWith("PLAYER_FAILED:")) {
            // parts: PLAYER_FAILED : failedPlayer : qIdx : option : newScore
            String[] parts      = data.split(":");
            String failedPlayer = parts[1];
            int option          = Integer.parseInt(parts[3]);
            int newScore        = Integer.parseInt(parts[4]);
            boolean isMe        = failedPlayer.equals(getNombreJugador());

            if (isMe) {
                super.setPuntaje(newScore);
            } else {
                this.oponentePuntaje = newScore;
                Platform.runLater(() -> {
                    for (JuegoListener l : listeners) l.onPuntajeOponenteActualizado(newScore);
                });
            }
            Platform.runLater(() -> {
                for (JuegoListener l : listeners) l.onRespuestaIncorrecta(failedPlayer, option, isMe);
            });

        } else if (data.startsWith("BOTH_FAILED:")) {
            int correcta = getOpcionCorrectaNivelActual();
            super.avanzarPreguntaSinPuntaje();
            Platform.runLater(() -> {
                for (JuegoListener l : listeners) l.onAmbosFallaron(correcta);
            });
        }
    }

    // ── Investigación: mensajes ACTION ────────────────────────────────────
    private void handleActionUpdate(String data) {
        if ("OPPONENT_REVEALED".equals(data)) {
            // El oponente reveló una evidencia: avanzamos estado local SIN puntaje
            revelarSiguienteEvidenciaLocal();

        } else if (data.startsWith("ACTION_RESOLVED:")) {
            // parts: ACTION_RESOLVED : sender : accionIdx : totalPoints
            String[] parts  = data.split(":");
            String sender   = parts[1];
            int accionIdx   = Integer.parseInt(parts[2]);
            int puntos      = Integer.parseInt(parts[3]);
            boolean isMe    = sender.equals(getNombreJugador());

            if (isMe) {
                // Puntaje autoritativo del servidor para este jugador
                super.setPuntaje(puntos);
            } else {
                // Actualizar puntaje del oponente en la UI de este jugador
                this.oponentePuntaje = puntos;
                Platform.runLater(() -> {
                    for (JuegoListener l : listeners) l.onPuntajeOponenteActualizado(puntos);
                });
            }
            Platform.runLater(() -> {
                for (JuegoListener l : listeners) l.onAccionResuelta(accionIdx, sender, isMe);
            });

        } else if (data.startsWith("ACTION_PLAYER_FAILED:")) {
            // parts: ACTION_PLAYER_FAILED : sender : accionIdx : newScore
            String[] parts = data.split(":");
            String sender  = parts[1];
            int accionIdx  = Integer.parseInt(parts[2]);
            int newScore   = Integer.parseInt(parts[3]);
            boolean isMe   = sender.equals(getNombreJugador());

            if (isMe) {
                super.setPuntaje(newScore);
            } else {
                this.oponentePuntaje = newScore;
                Platform.runLater(() -> {
                    for (JuegoListener l : listeners) l.onPuntajeOponenteActualizado(newScore);
                });
            }
            Platform.runLater(() -> {
                for (JuegoListener l : listeners) l.onAccionFallida(accionIdx, sender, isMe);
            });

        } else if (data.startsWith("ACTION_BOTH_FAILED:")) {
            int accionIdx = Integer.parseInt(data.split(":")[1]);
            Platform.runLater(() -> {
                for (JuegoListener l : listeners) l.onAccionAmbosFallaron(accionIdx);
            });
        } else if (data.startsWith("MINIGAME_START:")) {
            String[] parts = data.split(":");
            int gameId = Integer.parseInt(parts[1]);
            int qIdx   = Integer.parseInt(parts[2]);
            Platform.runLater(() -> {
                for (JuegoListener l : listeners) l.onMinijuegoIniciado(gameId, qIdx);
            });
        } else if (data.startsWith("MINIGAME_RESOLVED:")) {
            String[] parts  = data.split(":");
            String ganador  = parts[1];
            int qIdx        = Integer.parseInt(parts[2]);
            int nuevosPuntos = Integer.parseInt(parts[3]);
            boolean soyYo  = ganador.equals(getNombreJugador());
            if (soyYo) {
                super.setPuntaje(nuevosPuntos);
            } else {
                this.oponentePuntaje = nuevosPuntos;
                Platform.runLater(() -> {
                    for (JuegoListener l : listeners) l.onPuntajeOponenteActualizado(nuevosPuntos);
                });
            }
            // La sincronización visual se hace en onMinijuegoResuelto de AccionesNivel
            Platform.runLater(() -> {
                for (JuegoListener l : listeners) l.onMinijuegoResuelto(ganador, qIdx, soyYo);
            });
        } else {
            procesarAccionServidor(data);
        }
    }

    private void handleChatUpdate(String data) {
        if (data.startsWith("DISCONNECTED:")) {
            String name = data.substring("DISCONNECTED:".length());
            Platform.runLater(() -> {
                for (JuegoListener l : listeners) l.onOponenteDesconectado(name);
            });
        }
    }

    // ── Overrides ─────────────────────────────────────────────────────────

    @Override
    public boolean revelarSiguienteEvidencia() {
        // Notificamos al servidor. NO sumamos puntos localmente;
        // el servidor responderá con ACTION_RESOLVED con el total correcto.
        client.send(new GameMessage(GameMessage.Type.ACTION, "REVEAL_EVIDENCE", getNombreJugador()));
        return super.revelarSiguienteEvidenciaSinPuntos();
    }

    @Override
    public boolean responderPregunta(int opcionElegida) {
        // En multijugador, no actualizamos el puntaje localmente de inmediato.
        // Enviamos el intento al servidor y esperamos el mensaje RESOLVED o PLAYER_FAILED.
        String[] dataPregunta = getPreguntasNivelActual()[getPreguntaActual()];
        int correctaIdx = Integer.parseInt(dataPregunta[dataPregunta.length - 1]);
        boolean esCorrecta = (opcionElegida == correctaIdx);

        client.send(new GameMessage(GameMessage.Type.ACTION,
            "ANSWER:" + getPreguntaActual() + ":" + opcionElegida + ":" + esCorrecta, getNombreJugador()));
        
        return esCorrecta; 
    }

    // Cuando el oponente revela una evidencia: avanzar estado SIN puntaje local
    public boolean revelarSiguienteEvidenciaLocal() {
        return super.revelarSiguienteEvidenciaSinPuntos();
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    public void procesarRespuestaServidor(int opcionElegida, boolean esCorrecta,
                                          String respondioPrimero, int puntosActualizados) {
        Platform.runLater(() -> {
            boolean soyYo = respondioPrimero.equals(getNombreJugador());

            // Notificamos a la UI para que pinte el resultado (colores, pausa, etc.)
            for (JuegoListener l : listeners) {
                l.onRespuestaRecibida(opcionElegida, esCorrecta, respondioPrimero);
            }

            if (soyYo) {
                // El servidor confirma mi puntaje total
                super.setPuntaje(puntosActualizados);
            } else {
                // El oponente respondió primero
                this.oponentePuntaje = puntosActualizados;
                for (JuegoListener l : listeners) {
                    l.onPuntajeOponenteActualizado(puntosActualizados);
                }
            }
            
            // Avanzamos el índice de pregunta para que mostrarSiguientePregunta() vea la nueva
            super.avanzarPreguntaSinPuntaje();
        });
    }

    public void procesarAccionServidor(String data) {
        Platform.runLater(() -> {
            if (data.startsWith("START_LEVEL:")) {
                for (JuegoListener l : listeners) l.onNivelIniciado();
            } else if (data.startsWith("INTERROGATION_START:")) {
                for (JuegoListener l : listeners) l.onInterrogatorioListo();
            } else if (data.startsWith("NIVEL5_CHRONO_START:")) {
                for (JuegoListener l : listeners) l.onNivel5CronologiaListo();
            } else if (data.startsWith("NIVEL5_REPORT_START:")) {
                for (JuegoListener l : listeners) l.onNivel5ReporteFinalListo();
            } else if (data.startsWith("AVL_START:")) {
                System.out.println("Mensaje AVL_START recibido. Notificando a los listeners...");
                for (JuegoListener l : listeners) l.onArbolListoParaInsertar();
            } else if (data.startsWith("OPPONENT_SCORE_P2:")) {
                String[] parts = data.split(":");
                this.oponentePuntaje = Integer.parseInt(parts[1]);
                for (JuegoListener l : listeners) {
                    l.onPuntajeOponenteActualizado(this.oponentePuntaje);
                }
            } else if (data.startsWith("OPPONENT_ACTION:")) {
                String content = data.substring("OPPONENT_ACTION:".length());
                int idx = Integer.parseInt(content.split(":")[0]);
                for (JuegoListener l : listeners) l.onAccionSincronizada(idx, content);
            }
        });
    }

    public void enviarListoParaArbol() {
        client.send(new GameMessage(GameMessage.Type.ACTION, "AVL_READY", getNombreJugador()));
    }

    public void enviarListoParaInterrogatorio() {
        client.send(new GameMessage(GameMessage.Type.ACTION, "INTERROGATION_READY", getNombreJugador()));
    }

    public void notificarAccionInvestigacion(int accionIdx, String extraData) {
        client.send(new GameMessage(GameMessage.Type.ACTION,
            "INVESTIGATION_COMPLETE:" + accionIdx + ":" + extraData, getNombreJugador()));
    }

    public void intentarAccionInvestigacion(int accionIdx, boolean correcta) {
        client.send(new GameMessage(GameMessage.Type.ACTION,
            "ACTION_ATTEMPT:" + accionIdx + ":" + correcta, getNombreJugador()));
    }

    public void notificarListoParaNivel(int nivel) {
        client.send(new GameMessage(GameMessage.Type.ACTION,
            "LEVEL_READY:" + nivel, getNombreJugador()));
    }

    /** Notifica al servidor que este jugador está listo para iniciar el minijuego de la pregunta qIdx. */
    public void enviarListoParaMinijuego(int qIdx) {
        client.send(new GameMessage(GameMessage.Type.ACTION,
            "MINIGAME_READY:" + qIdx, getNombreJugador()));
    }

    /** Notifica al servidor que este jugador completó el minijuego de la pregunta qIdx. */
    public void enviarMinijuegoCompletado(int qIdx, long tiempoMs) {
        client.send(new GameMessage(GameMessage.Type.ACTION,
            "MINIGAME_DONE:" + qIdx + ":" + tiempoMs, getNombreJugador()));
    }

    public void enviarListoCronologiaNivel5() {
        client.send(new GameMessage(GameMessage.Type.ACTION, "NIVEL5_CHRONO_READY", getNombreJugador()));
    }

    public void enviarListoReporteFinalNivel5() {
        client.send(new GameMessage(GameMessage.Type.ACTION, "NIVEL5_REPORT_READY", getNombreJugador()));
    }
}
