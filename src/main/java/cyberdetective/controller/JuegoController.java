package cyberdetective.controller;

import cyberdetective.data.NivelesData;
import cyberdetective.model.ArbolAVL;
import cyberdetective.model.Caso;

import java.util.ArrayList;
import java.util.List;

/**
 * Controlador principal del juego. Maneja las tres fases de cada nivel:
 * investigación de evidencias, interrogatorio y cierre del caso en el árbol.
 */
public class JuegoController {

    // ---------------- Fases del nivel ----------------
    public enum FaseNivel {
        INVESTIGANDO,   // El jugador descubre evidencias
        INTERROGATORIO, // Responde preguntas sin ver la ley
        CASO_CERRADO    // El caso fue insertado en el árbol
    }

    // ---------------- Estado del juego ----------------
    private ArbolAVL arbol;
    private int nivelActual;
    private int puntaje;
    private boolean juegoTerminado;
    private List<String> logEventos;
    private FaseNivel faseActual;
    private Caso casoDelNivelActual;


    // Evidencias descubiertas en el nivel actual (se revelan de a una)
    private List<String> evidenciasDescubiertas;
    private int evidenciasReveladas;

    // Preguntas del nivel actual
    private int preguntaActual;
    private int respuestasCorrectasNivel;

    // Sospechosos posibles — aleatorio por partida
    private static final String[] SOSPECHOSOS = {
            "Mateo R. – compañero de clase de Valeria",
            "Sebastián L. – exnovio de Valeria",
            "Camila V. – rival académica de Valeria",
            "Usuario anónimo conocido como 'ShadowNet_21'"
    };
    private String sospechosoDeLaPartida;

    // Oyentes de la vista
    protected List<JuegoListener> listeners;

    // ---------------- Constructor ----------------
    private String nombreJugador;

    public JuegoController(String nombreJugador) {
        this.nombreJugador = nombreJugador;
        this.arbol = new ArbolAVL();
        this.nivelActual = 1;
        this.puntaje = 0;
        this.juegoTerminado = false;
        this.logEventos = new ArrayList<>();
        this.listeners = new ArrayList<>();
        this.faseActual = FaseNivel.INVESTIGANDO;
        this.evidenciasDescubiertas = new ArrayList<>();
        this.evidenciasReveladas = 0;
        this.preguntaActual = 0;
        this.respuestasCorrectasNivel = 0;

        // Inicialmente vacío, se sincronizará vía setSospechoso
        this.sospechosoDeLaPartida = "";

        logEventos.add("Investigación iniciada. El detective Alex toma el caso.");
        logEventos.add("Nivel 1 activo. Empieza a recolectar evidencias.");
    }

    public void setSospechoso(int idx) {
        if (idx >= 0 && idx < NivelesData.PERFILES_SOSPECHOSOS.length) {
            this.sospechosoDeLaPartida = NivelesData.PERFILES_SOSPECHOSOS[idx][0];
            NivelesData.setSospechoso(idx);
            logEventos.add("✓ Sospechoso sincronizado con el servidor: " + sospechosoDeLaPartida);
        }
    }

    // ---------------- Listener ----------------
    public interface JuegoListener {
        void onNivelCompletado(int nivel, Caso casoInsertado);
        void onArbolActualizado();
        void onJuegoTerminado(String reporte);
        void onPuntajeActualizado(int nuevoPuntaje);
        void onMensajeDetective(String mensaje);
        void onFaseCambiada(FaseNivel nuevaFase);
        void onEvidenciaRevelada(String evidencia, int totalReveladas, int total);
        default void onRespuestaRecibida(int opcion, boolean correcta, String jugador) {}
        default void onRespuestaOponente(String oponente, int opcion, boolean correcta) {}
        default void onRespuestaIncorrecta(String jugador, int opcion, boolean soyYo) {}
        default void onAmbosFallaron(int correcta) {}
        
        default void onAccionResuelta(int accionIdx, String jugador, boolean soyYo) {}
        default void onAccionFallida(int accionIdx, String jugador, boolean soyYo) {}
        default void onAccionAmbosFallaron(int accionIdx) {}
        default void onNivelIniciado() {}
        default void onAccionSincronizada(int accionIdx, String data) {}
        default void onPuntajeOponenteActualizado(int puntaje) {}
        default void onOponenteDesconectado(String nombre) {}
        default void onArbolListoParaInsertar() {}
        default void onInterrogatorioListo() {}
        default void onNivel5CronologiaListo() {}
        default void onNivel5ReporteFinalListo() {}
        /** El servidor confirmó el ID del minijuego a usar en la pregunta qIdx. */
        default void onMinijuegoIniciado(int gameId, int qIdx) {}
        /** El servidor confirmó al ganador del minijuego de la pregunta qIdx. */
        default void onMinijuegoResuelto(String ganador, int qIdx, boolean soyYo) {}
    }

    public void agregarListener(JuegoListener listener) {
        listeners.add(listener);
    }

    public void removerListener(JuegoListener listener) {
        listeners.remove(listener);
    }

    // ---------------- Fase: Investigando ----------------

    /**
     * Inicializa las evidencias del nivel actual para revelarlas de a una.
     * Se llama al entrar a un nuevo nivel.
     */
    public void iniciarInvestigacion() {
        casoDelNivelActual = getCasoNivelActual();
        if (casoDelNivelActual == null) return;
        Caso caso = casoDelNivelActual;

        evidenciasDescubiertas.clear();
        evidenciasReveladas = 0;
        faseActual = FaseNivel.INVESTIGANDO;

        // Cargamos todas las evidencias disponibles del caso
        for (String ev : caso.getEvidencias()) {
            evidenciasDescubiertas.add(ev);
        }

        notificarMensajeDetective(
                "Alex dice: Hay " + evidenciasDescubiertas.size() +
                        " evidencias en este caso. Examínalas todas antes de interrogar."
        );
        logEventos.add("— " + NivelesData.getTituloNivel(nivelActual) + " iniciado.");
        notificarFaseCambiada(faseActual);
    }

    /**
     * Revela la siguiente evidencia disponible.
     * Devuelve true si hay más evidencias por revelar.
     */
    public boolean revelarSiguienteEvidencia() {
        if (evidenciasReveladas >= evidenciasDescubiertas.size()) return false;

        String ev = evidenciasDescubiertas.get(evidenciasReveladas);
        evidenciasReveladas++;

        puntaje += 50;
        notificarPuntaje();

        logEventos.add("Evidencia " + evidenciasReveladas + " recolectada: " + ev);

        String pista = NivelesData.getPistaAleatoria(nivelActual);
        notificarMensajeDetective(pista);

        notificarEvidenciaRevelada(ev, evidenciasReveladas, evidenciasDescubiertas.size());

        // Cuando se revelaron todas, se puede pasar al interrogatorio
        if (evidenciasReveladas == evidenciasDescubiertas.size()) {
            notificarMensajeDetective(
                    "Alex dice: Bien. Ya tienes todas las evidencias. " +
                            "Ahora responde el interrogatorio para cerrar el caso."
            );
        }

        return evidenciasReveladas < evidenciasDescubiertas.size();
    }

    /**
     * Revela la siguiente evidencia SIN sumar puntos localmente.
     * Para uso en multijugador donde el servidor es la fuente de verdad de los puntos.
     */
    public boolean revelarSiguienteEvidenciaSinPuntos() {
        if (evidenciasReveladas >= evidenciasDescubiertas.size()) return false;

        String ev = evidenciasDescubiertas.get(evidenciasReveladas);
        evidenciasReveladas++;

        // NO sumamos puntaje — el servidor lo calculará y enviará

        logEventos.add("Evidencia " + evidenciasReveladas + " recolectada: " + ev);

        String pista = NivelesData.getPistaAleatoria(nivelActual);
        notificarMensajeDetective(pista);

        notificarEvidenciaRevelada(ev, evidenciasReveladas, evidenciasDescubiertas.size());

        if (evidenciasReveladas == evidenciasDescubiertas.size()) {
            notificarMensajeDetective(
                    "Alex dice: Bien. Ya tienes todas las evidencias. " +
                            "Ahora responde el interrogatorio para cerrar el caso."
            );
        }

        return evidenciasReveladas < evidenciasDescubiertas.size();
    }

    public boolean todasLasEvidenciasReveladas() {
        return evidenciasReveladas >= evidenciasDescubiertas.size();
    }

    public List<String> getEvidenciasReveladas() {
        return evidenciasDescubiertas.subList(0, evidenciasReveladas);
    }

    /**
     * Pasa a la fase de interrogatorio.
     * Solo se permite si ya se revelaron todas las evidencias.
     */
    public boolean pasarAInterrogatorio() {
        if (!todasLasEvidenciasReveladas()) {
            notificarMensajeDetective(
                    "Alex dice: Aún faltan evidencias por examinar. " +
                            "No puedes cerrar el caso todavía."
            );
            return false;
        }

        faseActual = FaseNivel.INTERROGATORIO;
        preguntaActual = 0;
        respuestasCorrectasNivel = 0;

        logEventos.add("Interrogatorio iniciado en Nivel " + nivelActual + ".");
        notificarMensajeDetective(
                "Alex dice: Ahora pon a prueba lo que investigaste. " +
                        "Responde correctamente para poder insertar el caso en el árbol."
        );
        notificarFaseCambiada(faseActual);
        return true;
    }

    // ---------------- Fase: Interrogatorio ----------------

    public String[][] getPreguntasNivelActual() {
        return NivelesData.getPreguntasNivel(nivelActual);
    }

    public int getPreguntaActual() {
        return preguntaActual;
    }

    /** Verifica si la opción es correcta sin avanzar el estado. */
    public boolean esOpcionCorrecta(int opcionElegida) {
        return opcionElegida == getOpcionCorrectaNivelActual();
    }

    public int getOpcionCorrectaNivelActual() {
        String[][] preguntas = NivelesData.getPreguntasNivel(nivelActual);
        if (preguntaActual >= preguntas.length) return -1;
        String[] pregunta = preguntas[preguntaActual];
        return Integer.parseInt(pregunta[pregunta.length - 1]);
    }

    /**
     * Evalúa la respuesta del jugador.
     * La ley y el delito NO se muestran antes de esta fase.
     */
    public boolean responderPregunta(int opcionElegida) {
        String[][] preguntas = NivelesData.getPreguntasNivel(nivelActual);
        if (preguntaActual >= preguntas.length) return false;

        String[] pregunta = preguntas[preguntaActual];
        int correcta = Integer.parseInt(pregunta[pregunta.length - 1]);
        boolean esCorrecta = opcionElegida == correcta;

        if (esCorrecta) {
            respuestasCorrectasNivel++;
            puntaje += 50;
            logEventos.add("✓ Respuesta correcta – pregunta " + (preguntaActual + 1)
                    + " del Nivel " + nivelActual + ". +50 pts.");
            notificarMensajeDetective(
                    "Alex dice: Correcto. Eso encaja con las evidencias recolectadas."
            );
        } else {
            puntaje = Math.max(0, puntaje - 25);
            logEventos.add("✗ Respuesta incorrecta – pregunta " + (preguntaActual + 1)
                    + ". -25 pts.");
            notificarMensajeDetective(
                    "Alex dice: Eso no coincide. Revisa las evidencias y piénsalo mejor."
            );
        }

        notificarPuntaje();
        
        // Notificamos a la vista para que pueda pintar el resultado
        for (JuegoListener l : listeners) {
            l.onRespuestaRecibida(opcionElegida, esCorrecta, nombreJugador);
        }
        
        preguntaActual++;
        return esCorrecta;
    }

    public void avanzarPreguntaSinPuntaje() {
        preguntaActual++;
    }

    public boolean hayMasPreguntas() {
        String[][] preguntas = NivelesData.getPreguntasNivel(nivelActual);
        return preguntaActual < preguntas.length;
    }

    // ---------------- Fase: Cerrar caso ----------------

    /**
     * Inserta el caso en el árbol AVL y avanza al siguiente nivel.
     * Solo se permite después del interrogatorio.
     */
    public boolean cerrarCaso() {
        if (faseActual != FaseNivel.INTERROGATORIO) {
            notificarMensajeDetective(
                    "Alex dice: Primero debes completar el interrogatorio."
            );
            return false;
        }
        if (hayMasPreguntas()) {
            notificarMensajeDetective(
                    "Alex dice: Aún faltan preguntas por responder."
            );
            return false;
        }

        Caso caso = casoDelNivelActual;
        if (caso == null) return false;

        arbol.insertar(caso);
        faseActual = FaseNivel.CASO_CERRADO;

        logEventos.add("✔ Caso #" + caso.getId() + " insertado en el árbol AVL.");
        logEventos.add("  Tipo   : " + caso.getTipoAcoso());
        logEventos.add("  Ley    : " + caso.getLeyColombia());
        logEventos.add("  Pena   : " + caso.getPenaAplicable());
        logEventos.add("  Gravedad: " + caso.getGravedad() + "/10");

        // Bonus si no tuvo errores en el interrogatorio
        String[][] preguntas = NivelesData.getPreguntasNivel(nivelActual);
        if (respuestasCorrectasNivel == preguntas.length) {
            puntaje += 300;
            logEventos.add("★ Nivel " + nivelActual + " perfecto. +300 pts bonus.");
            notificarMensajeDetective(
                    "Alex dice: Investigación impecable. Cerramos el caso sin dudas."
            );
        } else {
            notificarMensajeDetective(
                    "Alex dice: Caso cerrado. Seguimos con la siguiente capa."
            );
        }

        notificarArbolActualizado();
        notificarNivelCompletado(nivelActual, caso);
        notificarPuntaje();

        // Avanzar nivel
        nivelActual++;
        evidenciasReveladas = 0;
        evidenciasDescubiertas.clear();
        preguntaActual = 0;
        respuestasCorrectasNivel = 0;

        if (nivelActual > 4) {
            finalizarJuego();
        } else {
            faseActual = FaseNivel.INVESTIGANDO;
            logEventos.add("─────────────────────────────────");
            logEventos.add(NivelesData.getTituloNivel(nivelActual) + " desbloqueado.");
            notificarFaseCambiada(faseActual);
        }

        return true;
    }

    // ---------------- Nivel final ----------------

    private void finalizarJuego() {
        juegoTerminado = true;
        puntaje += 500;
        notificarPuntaje();

        logEventos.add("═════════════════════════════════");
        logEventos.add("CASO CERRADO.");
        logEventos.add("Agresor identificado: " + sospechosoDeLaPartida);
        logEventos.add("Puntaje final: " + puntaje + " pts.");

        String reporte = arbol.generarReporte()
                + "\n\nAgresor identificado: " + sospechosoDeLaPartida
                + "\nPuntaje final: " + puntaje + " puntos.";

        notificarJuegoTerminado(reporte);
    }

    // ---------------- Getters ----------------

    public int getNivelActual()                  { return nivelActual; }
    public int getPuntaje()                      { return puntaje; }
    public boolean isJuegoTerminado()            { return juegoTerminado; }
    public ArbolAVL getArbol()                   { return arbol; }
    public List<String> getLogEventos()          { return logEventos; }
    public String getSospechoso()                { return sospechosoDeLaPartida; }
    public FaseNivel getFaseActual()             { return faseActual; }
    public int getCantidadEvidenciasReveladas()  { return evidenciasReveladas; }
    public int getTotalEvidencias()              { return evidenciasDescubiertas.size(); }
    public String getNombreJugador() { return nombreJugador; }


    public String getTituloNivelActual() {
        return NivelesData.getTituloNivel(nivelActual);
    }

    public String getDescripcionNivelActual() {
        return NivelesData.getDescripcionNivel(nivelActual);
    }

    public Caso getCasoNivelActual() {
        switch (nivelActual) {
            case 1: return NivelesData.getCasoNivel1();
            case 2: return NivelesData.getCasoNivel2();
            case 3: return NivelesData.getCasoNivel3();
            case 4: return NivelesData.getCasoNivel4();
            default: return null;
        }
    }

    public Caso getCasoActualFijo() {
        return casoDelNivelActual;
    }

    // ---------------- Reinicio ----------------

    public void reiniciarJuego() {
        this.arbol = new ArbolAVL();
        this.nivelActual = 1;
        this.puntaje = 0;
        this.juegoTerminado = false;
        this.logEventos.clear();
        this.faseActual = FaseNivel.INVESTIGANDO;
        this.evidenciasDescubiertas.clear();
        this.evidenciasReveladas = 0;
        this.preguntaActual = 0;
        this.respuestasCorrectasNivel = 0;

        int idx = (int)(Math.random() * SOSPECHOSOS.length);
        this.sospechosoDeLaPartida = SOSPECHOSOS[idx];
        NivelesData.setSospechoso(idx);

        logEventos.add("Nueva investigación iniciada.");
        notificarArbolActualizado();
        notificarPuntaje();
        notificarFaseCambiada(faseActual);
    }

    public void setPuntaje(int p) {
        this.puntaje = p;
        notificarPuntaje();
    }

    // ---------------- Notificaciones ----------------

    private void notificarNivelCompletado(int nivel, Caso caso) {
        for (JuegoListener l : listeners) l.onNivelCompletado(nivel, caso);
    }
    private void notificarArbolActualizado() {
        for (JuegoListener l : listeners) l.onArbolActualizado();
    }
    private void notificarJuegoTerminado(String reporte) {
        for (JuegoListener l : listeners) l.onJuegoTerminado(reporte);
    }
    private void notificarPuntaje() {
        for (JuegoListener l : listeners) l.onPuntajeActualizado(puntaje);
    }
    private void notificarMensajeDetective(String msg) {
        for (JuegoListener l : listeners) l.onMensajeDetective(msg);
    }
    private void notificarFaseCambiada(FaseNivel fase) {
        for (JuegoListener l : listeners) l.onFaseCambiada(fase);
    }
    private void notificarEvidenciaRevelada(String ev, int reveladas, int total) {
        for (JuegoListener l : listeners) l.onEvidenciaRevelada(ev, reveladas, total);
    }
}