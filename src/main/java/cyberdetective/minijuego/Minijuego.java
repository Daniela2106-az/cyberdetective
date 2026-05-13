package cyberdetective.minijuego;

import javafx.scene.layout.Pane;
import java.util.function.LongConsumer;

/**
 * Clase base abstracta para todos los minijuegos del interrogatorio.
 * Cada minijuego ocupa un Pane y notifica su finalización con el tiempo transcurrido (ms).
 */
public abstract class Minijuego extends Pane {

    protected LongConsumer onJuegoTerminado;
    protected long tiempoInicio = -1;
    protected javafx.scene.Node evidenciaVirtual;
    protected String datosExtra;
    protected Runnable onVerEvidenciaRequest;

    public void setOnVerEvidenciaRequest(Runnable callback) {
        this.onVerEvidenciaRequest = callback;
    }

    public void setOnJuegoTerminado(LongConsumer callback) {
        this.onJuegoTerminado = callback;
    }

    public void setEvidenciaNode(javafx.scene.Node nodo) {
        this.evidenciaVirtual = nodo;
    }

    public void setDatosExtra(String data) {
        this.datosExtra = data;
    }

    /** Llama a este método para arrancar el temporizador y mostrar el minijuego. */
    public abstract void iniciar();

    /** Llama a este método cuando el minijuego se completa internamente. */
    public abstract void finalizar();

    protected void completar() {
        if (tiempoInicio == -1) return;
        long t = System.currentTimeMillis() - tiempoInicio;
        if (onJuegoTerminado != null) {
            onJuegoTerminado.accept(t);
        }
    }
}
