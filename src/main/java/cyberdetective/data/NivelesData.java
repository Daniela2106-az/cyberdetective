package cyberdetective.data;

import cyberdetective.model.Caso;
import java.util.*;

/**
 * Datos de los niveles del juego.
 * Las evidencias de cada nivel incluyen rastros del sospechoso
 * para que el jugador pueda identificarlo al final.
 */
public class NivelesData {

    private static final Random random = new Random();

    // Perfiles completos de cada sospechoso
    // Cada sospechoso tiene: nombre, usuario de red social, IP, dispositivo, patrón
    public static final String[][] PERFILES_SOSPECHOSOS = {
            {
                    "Mateo R. – compañero de clase de Valeria",
                    "@mateo_r99",
                    "192.168.1.47",
                    "iPhone 13 – iOS 16",
                    "Escribe en minúsculas y usa muchos puntos suspensivos..."
            },
            {
                    "Sebastián L. – exnovio de Valeria",
                    "@seba_leal_2005",
                    "10.0.0.83",
                    "Samsung Galaxy A54 – Android 13",
                    "Usa mayúsculas al inicio de cada insulto y emojis agresivos"
            },
            {
                    "Camila V. – rival académica de Valeria",
                    "@cami.v_real",
                    "172.16.254.9",
                    "MacBook Pro – Safari",
                    "Publica exactamente a las 10pm, siempre desde la misma red WiFi"
            },
            {
                    "Usuario anónimo conocido como 'ShadowNet_21'",
                    "@sh4d0w_n3t21",
                    "45.33.32.156",
                    "Dispositivo con VPN activa – Windows 11",
                    "Alterna entre mayúsculas y minúsculas, siempre desde IP enmascarada"
            }
    };

    // Índice del sospechoso activo en esta partida
    private static int indiceSospechoso = -1;

    /**
     * Se llama al iniciar una nueva partida para fijar el sospechoso.
     * El índice debe coincidir con el sospechoso elegido en JuegoController.
     */
    public static void setSospechoso(int indice) {
        indiceSospechoso = indice;
    }

    public static String getNombreSospechoso() {
        if (indiceSospechoso < 0) return PERFILES_SOSPECHOSOS[0][0];
        return PERFILES_SOSPECHOSOS[indiceSospechoso][0];
    }

    private static String usuario()    { return PERFILES_SOSPECHOSOS[indiceSospechoso][1]; }
    private static String ip()         { return PERFILES_SOSPECHOSOS[indiceSospechoso][2]; }
    private static String dispositivo(){ return PERFILES_SOSPECHOSOS[indiceSospechoso][3]; }
    private static String patron()     { return PERFILES_SOSPECHOSOS[indiceSospechoso][4]; }

    // ── Nivel 1 – Las primeras señales ─────────────────────────────────

    public static Caso getCasoNivel1() {
        // Las evidencias del nivel 1 revelan el nombre de usuario del agresor
        String[] todasEvidencias = {
                "REPORTE INICIAL: Primeros mensajes detectados hace 3 meses por " + usuario(),
                "Perfil público: La cuenta " + usuario() + " fue creada hace 90 días exactamente.",
                "Historial antiguo: Registro de actividad sospechosa desde el inicio del semestre.",
                "Registro de la plataforma: Cuenta " + usuario() + " vinculada al dispositivo " + dispositivo(),
                "Testimonio: 'Los problemas con " + usuario() + " empezaron hace meses, en las primeras clases.'"
        };

        return new Caso(
                1,
                "Injuria en redes sociales",
                "Valeria recibe mensajes ofensivos de forma repetida.",
                elegirEvidencias(todasEvidencias, 3),
                "Artículo 220 del Código Penal – Injuria",
                "Multa de 1 a 3 salarios mínimos.",
                3, 1 // 1 es el inicio de la cronología
        );
    }

    // ── Nivel 2 – El rumor viral ────────────────────────────────────────

    public static Caso getCasoNivel2() {
        String[] todasEvidencias = {
                "Publicación del RUMOR: Subida por " + usuario() + " hace 1 mes (mediados del mes pasado).",
                "Metadatos: El post viral fue creado desde " + dispositivo() + " durante las vacaciones pasadas.",
                "Captura: El rumor empezó a circular masivamente 30 días después de los primeros mensajes.",
                "Registro: La publicación fue reportada por primera vez el mes anterior.",
                "Análisis: El patrón " + patron() + " se identificó en publicaciones de hace 4 semanas."
        };

        return new Caso(
                2,
                "Calumnia – Difusión de rumores falsos",
                "Circulan publicaciones falsas sobre Valeria. " +
                        "Varios estudiantes comparten información inventada " +
                        "que daña su reputación.",
                elegirEvidencias(todasEvidencias, 3),
                "Artículo 221 del Código Penal Colombiano – Calumnia",
                "Multa o sanciones penales por difundir acusaciones falsas " +
                        "que afectan la reputación de una persona.",
                5, 2
        );
    }

    // ── Nivel 3 – La cuenta fantasma ───────────────────────────────────

    public static Caso getCasoNivel3() {
        String[] todasEvidencias = {
                "ALERTA: Perfil falso creado hace apenas 3 días usando la IP " + ip(),
                "Evidencia: La foto de Valeria fue descargada por " + usuario() + " hace solo 72 horas.",
                "Conexión: La cuenta suplantadora se activó el pasado fin de semana.",
                "Análisis forense: El acceso desde " + dispositivo() + " ocurrió hace 3 días.",
                "Reporte: El perfil falso empezó a atacar a otros hace apenas unas jornadas."
        };

        return new Caso(
                3,
                "Suplantación de identidad digital",
                "Aparece un perfil falso usando la foto de Valeria. " +
                        "Desde esa cuenta se publican contenidos ofensivos " +
                        "y se atacan otros usuarios en su nombre.",
                elegirEvidencias(todasEvidencias, 3),
                "Ley 1273 de 2009 – Delitos Informáticos en Colombia",
                "Sanciones penales de 48 a 96 meses de prisión y multas " +
                        "de 100 a 1000 salarios mínimos según el daño causado.",
                8, 3
        );
    }

    // ── Nivel 4 – El ataque coordinado ─────────────────────────────────

    public static Caso getCasoNivel4() {
        String[] todasEvidencias = {
                "ATAQUE FINAL: Coordinación de 4 cuentas detectada HOY mismo.",
                "Análisis de tiempo: Todos los ataques ocurrieron en las últimas 2 horas.",
                "Patrón actual: El estilo " + patron() + " se está usando en este momento.",
                "Metadatos recientes: Conexión activa ahora desde " + dispositivo() + " con IP " + ip(),
                "Registro: Las cuentas secundarias se activaron hoy para el ataque final."
        };

        return new Caso(
                4,
                "Acoso y hostigamiento digital coordinado",
                "Múltiples cuentas atacan a Valeria simultáneamente. " +
                        "El análisis forense revela que todas las cuentas " +
                        "pertenecen a la misma persona.",
                elegirEvidencias(todasEvidencias, 3),
                "Ley 1273 de 2009 – Delitos Informáticos / " +
                        "Hostigamiento reiterado",
                "Sanciones penales agravadas por reincidencia y uso " +
                        "de múltiples identidades digitales para cometer el delito.",
                10, 4
        );
    }

    // ── Pistas del detective ────────────────────────────────────────────

    public static String getPistaAleatoria(int nivel) {
        String[][] pistas = {
                {
                        "Alex dice: Guarda el nombre de usuario, lo vamos a necesitar " +
                                "en los siguientes niveles.",
                        "Alex dice: Revisa cuándo fue creada la cuenta — las cuentas " +
                                "nuevas suelen ser las que más atacan.",
                        "Alex dice: El historial de mensajes es evidencia directa. " +
                                "No lo pierdas de vista."
                },
                {
                        "Alex dice: Quien publica primero es quien inicia el daño. " +
                                "Fíjate en las marcas de tiempo.",
                        "Alex dice: El estilo de escritura no miente — " +
                                "es una firma digital involuntaria.",
                        "Alex dice: Ya tenemos el usuario del nivel anterior. " +
                                "¿Coincide con quien inició el rumor?"
                },
                {
                        "Alex dice: La IP no miente. Si coincide con lo que ya " +
                                "tenemos, el caso se cierra solo.",
                        "Alex dice: El dispositivo registrado en los metadatos " +
                                "es el mismo que vimos en el nivel 1.",
                        "Alex dice: Descargar la foto de Valeria antes de crear " +
                                "el perfil falso no fue una coincidencia."
                },
                {
                        "Alex dice: Una persona, cuatro cuentas, una sola IP. " +
                                "El árbol ya tiene suficientes nodos para probarlo.",
                        "Alex dice: El patrón de horarios es la evidencia más " +
                                "sólida de todas. Nadie más ataca a esa hora.",
                        "Alex dice: Con esto tenemos suficiente. " +
                                "Cerremos el árbol y presentemos el caso."
                }
        };

        if (nivel < 1 || nivel > 4) {
            return "Alex dice: Sigue el rastro. La verdad está en el árbol.";
        }
        String[] pistasNivel = pistas[nivel - 1];
        return pistasNivel[random.nextInt(pistasNivel.length)];
    }

    // ── Preguntas por nivel ─────────────────────────────────────────────

    public static String[][] getPreguntasNivel(int nivel) {
        switch (nivel) {
            case 1: return new String[][]{
                    {
                            "¿Qué artículo del Código Penal colombiano aplica " +
                                    "cuando alguien envía mensajes ofensivos que " +
                                    "dañan el buen nombre de una persona?",
                            "Artículo 220 – Injuria",
                            "Artículo 239 – Hurto",
                            "Artículo 356 – Terrorismo",
                            "0"
                    },
                    {
                            "¿Cuál es la evidencia más importante para identificar " +
                                    "al usuario que envía los mensajes ofensivos?",
                            "El número de seguidores de la cuenta",
                            "Las capturas de pantalla con el nombre de usuario visible",
                            "La foto de perfil del agresor",
                            "1"
                    }
            };
            case 2: return new String[][]{
                    {
                            "¿Qué delito se comete al publicar información falsa " +
                                    "que daña la reputación de una persona?",
                            "Injuria",
                            "Calumnia",
                            "Hurto de identidad",
                            "1"
                    },
                    {
                            "Para rastrear el origen de un rumor viral, " +
                                    "¿qué dato es más relevante?",
                            "El número de likes de la publicación",
                            "La fecha y hora de la publicación original y quién la creó",
                            "Los comentarios más recientes",
                            "1"
                    }
            };
            case 3: return new String[][]{
                    {
                            "¿Qué ley colombiana regula los delitos informáticos " +
                                    "como la suplantación de identidad digital?",
                            "Ley 100 de 1993 - Sistema de seguridad social.",
                            "Ley 1273 de 2009 – Delitos informáticos en Colombia.",
                            "Ley 1010 de 2006 - Acoso laboral.",
                            "1"
                    },
                    {
                            "¿Qué evidencia técnica es clave para rastrear " +
                                    "quién creó un perfil falso?",
                            "El número de publicaciones del perfil falso",
                            "La dirección IP registrada al momento de crear la cuenta",
                            "Los seguidores del perfil falso",
                            "1"
                    }
            };
            case 4: return new String[][]{
                    {
                            "¿Qué patrón técnico confirma que varias cuentas " +
                                    "pertenecen a la misma persona?",
                            "Que todas tienen la misma foto de perfil",
                            "Misma dirección IP, mismo dispositivo y " +
                                    "horarios idénticos de actividad",
                            "Que siguen a las mismas personas",
                            "1"
                    },
                    {
                            "Usar múltiples cuentas para atacar a una persona, " +
                                    "¿cómo afecta la sanción legal?",
                            "No cambia la pena",
                            "Reduce la pena porque se distribuye la responsabilidad",
                            "Agrava la pena por reincidencia y uso de " +
                                    "múltiples identidades digitales",
                            "2"
                    }
            };
            default: return new String[][]{};
        }
    }

    // ── Info de niveles ─────────────────────────────────────────────────

    public static String getTituloNivel(int nivel) {
        switch (nivel) {
            case 1: return "Nivel 1 – Las primeras señales";
            case 2: return "Nivel 2 – El rumor viral";
            case 3: return "Nivel 3 – La cuenta fantasma";
            case 4: return "Nivel 4 – El ataque coordinado";
            default: return "Nivel Final – La verdad detrás del acoso";
        }
    }

    public static String getDescripcionNivel(int nivel) {
        switch (nivel) {
            case 1:
                return "Valeria comienza a recibir mensajes ofensivos en redes sociales. " +
                        "Recolecta las capturas de pantalla e identifica el usuario " +
                        "que está detrás de los ataques.";
            case 2:
                return "Circulan publicaciones falsas sobre Valeria. " +
                        "Rastrea quién inició el rumor y determina si la " +
                        "información publicada es falsa.";
            case 3:
                return "Aparece un perfil falso usando la foto de Valeria. " +
                        "Analiza la información del perfil y rastrea la dirección IP " +
                        "de creación de la cuenta.";
            case 4:
                return "Varias cuentas atacan a Valeria al mismo tiempo. " +
                        "Identifica qué cuentas pertenecen a la misma persona " +
                        "y encuentra los patrones de comportamiento.";
            default:
                return "El detective reúne todas las evidencias para cerrar el caso.";
        }
    }

    // ── Utilidad ────────────────────────────────────────────────────────

    private static String[] elegirEvidencias(String[] todas, int cantidad) {
        List<String> lista = new ArrayList<>(Arrays.asList(todas));
        Collections.shuffle(lista, random);
        String[] sel = new String[cantidad];
        for (int i = 0; i < cantidad; i++) sel[i] = lista.get(i);
        return sel;
    }
}