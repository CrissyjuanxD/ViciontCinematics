package net.viciont.cinematics.utils;

/**
 * Constantes para mensajes del plugin
 *
 * @author CrissyjuanxD
 */
public class Mensajes {

    // Prefijo del plugin
    public static final String PREFIJO = "<gradient:#ff6b6b:#4ecdc4>[Viciont Cinematics]</gradient> ";

    // Mensajes de éxito
    public static final String GRABACION_INICIADA = PREFIJO + "<green>¡Grabación iniciada correctamente!";
    public static final String GRABACION_DETENIDA = PREFIJO + "<green>¡Grabación finalizada y guardada!";
    public static final String CINEMATICA_CREADA = PREFIJO + "<green>¡Cinemática creada exitosamente!";
    public static final String CINEMATICA_REPRODUCIDA = PREFIJO + "<green>¡Reproduciendo cinemática!";

    // Mensajes de error
    public static final String ERROR_GRABANDO = PREFIJO + "<red>Ya estás grabando una cinemática.";
    public static final String ERROR_NO_GRABANDO = PREFIJO + "<red>No tienes ninguna grabación activa.";
    public static final String ERROR_CINEMATICA_EXISTE = PREFIJO + "<red>Ya existe una cinemática con ese nombre.";
    public static final String ERROR_CINEMATICA_NO_EXISTE = PREFIJO + "<red>La cinemática especificada no existe.";
    public static final String ERROR_PERMISOS = PREFIJO + "<red>No tienes permisos para usar este comando.";

    // Mensajes informativos
    public static final String INFO_AYUDA_GUI = PREFIJO + "<yellow>Usa <white>/cinematica gui<yellow> para una experiencia más fácil.";
    public static final String INFO_GRABANDO = PREFIJO + "<yellow>Grabando... Muévete para crear la cinemática.";
    public static final String INFO_CINEMATICA_VACIA = PREFIJO + "<yellow>No hay cinemáticas creadas aún.";

    // Mensajes de configuración
    public static final String CONFIG_SILENCIO_ON = PREFIJO + "<green>Silencio global activado.";
    public static final String CONFIG_SILENCIO_OFF = PREFIJO + "<red>Silencio global desactivado.";
    public static final String CONFIG_FADE_ON = PREFIJO + "<green>Efecto fade activado.";
    public static final String CONFIG_FADE_OFF = PREFIJO + "<red>Efecto fade desactivado.";

    // Mensajes de cinemáticas en vivo
    public static final String LIVE_INICIADA = PREFIJO + "<purple>¡Cinemática en vivo iniciada!";
    public static final String LIVE_DETENIDA = PREFIJO + "<purple>Cinemática en vivo finalizada.";
    public static final String LIVE_JUGADOR_AGREGADO = PREFIJO + "<purple>Jugador agregado a la cinemática en vivo.";
    public static final String LIVE_JUGADOR_REMOVIDO = PREFIJO + "<purple>Jugador removido de la cinemática en vivo.";

    // Mensajes de la GUI
    public static final String GUI_TITULO_PRINCIPAL = "§6§lViciont Cinematics - Menú Principal";
    public static final String GUI_TITULO_LISTA = "§6§lCinemáticas Disponibles";
    public static final String GUI_TITULO_CONFIG = "§6§lConfiguración de Cinemáticas";

    /**
     * Formatea un mensaje con parámetros
     */
    public static String formatear(String mensaje, Object... parametros) {
        return String.format(mensaje, parametros);
    }

    /**
     * Añade el prefijo a un mensaje
     */
    public static String conPrefijo(String mensaje) {
        return PREFIJO + mensaje;
    }
}