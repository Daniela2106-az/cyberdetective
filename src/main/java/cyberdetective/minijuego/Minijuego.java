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

    public void setOnJuegoTerminado(LongConsumer callback) {
        this.onJuegoTerminado = callback;
    }

    /** Llama a este método para arrancar el temporizador y mostrar el minijuego. */
    public abstract void iniciar();

    /** Limpieza al cerrar el popup antes de tiempo (si aplica). */
    public abstract void finalizar();

    /** Llama internamente cuando el jugador completa el reto. */
    protected void completar() {
        long elapsed = (tiempoInicio >= 0) ? System.currentTimeMillis() - tiempoInicio : 0;
        if (onJuegoTerminado != null) {
            onJuegoTerminado.accept(elapsed);
        }
    }
}
