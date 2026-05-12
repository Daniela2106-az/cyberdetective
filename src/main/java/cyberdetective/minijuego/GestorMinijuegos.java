package cyberdetective.minijuego;

import java.util.Random;

/**
 * Gestiona la selección de minijuegos por pregunta.
 *
 * Mapa de IDs (1-9):
 *   Pregunta 0 (Hallazgo de evidencia):   IDs 1, 2, 3
 *   Pregunta 1 (Identidad del agresor):   IDs 4, 5, 6
 *   Pregunta 2 (Clasificación del delito): IDs 7, 8, 9
 *
 * Por ahora solo el ID 1 (Escombros) está implementado.
 * Los demás minijuegos se añadirán iterativamente.
 */
public class GestorMinijuegos {

    private static final Random RND = new Random();

    /**
     * Devuelve un ID aleatorio válido para la pregunta indicada (0-indexed).
     * El servidor llama a este método y envía el ID a ambos clientes.
     */
    public static int seleccionarIdAleatorio(int numeroPregunta) {
        int base = (numeroPregunta % 3) * 3 + 1;   // 1, 4 o 7
        return base + RND.nextInt(3);               // +0, +1 o +2
    }

    /**
     * Instancia el minijuego correspondiente al ID recibido del servidor.
     * Si el ID no está implementado aún, devuelve Escombros como fallback.
     */
    public static Minijuego crearMinijuego(int id) {
        switch (id) {
            case 1: return new EscombroMinijuego();
            // case 2: return new OtroMinijuego1();
            // case 3: return new OtroMinijuego2();
            // case 4: return new MinijuegoIdentidad1();  ... etc.
            default: return new EscombroMinijuego();
        }
    }
}
