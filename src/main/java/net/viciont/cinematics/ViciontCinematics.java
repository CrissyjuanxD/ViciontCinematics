package net.viciont.cinematics;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.viciont.cinematics.commands.CinematicaComando;
import net.viciont.cinematics.commands.CinematicaVivaComando;
import net.viciont.cinematics.commands.CinematicaTabCompleter;
import net.viciont.cinematics.commands.CinematicaVivaTabCompleter;
import net.viciont.cinematics.core.GestorCinematicas;
import net.viciont.cinematics.gui.GestorGUI;
import net.viciont.cinematics.listeners.ListenerGlobal;
import net.viciont.cinematics.utils.ConfiguracionJSON;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/**
 * Clase principal del plugin Viciont Cinematics
 *
 * @author CrissyjuanxD
 * @version 1.0.0
 */
public class ViciontCinematics extends JavaPlugin {

    private static ViciontCinematics instancia;
    private GestorCinematicas gestorCinematicas;
    private GestorGUI gestorGUI;
    private FileConfiguration config;
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public void onEnable() {
        instancia = this;

        // Verificar compatibilidad de versión
        String version = getServer().getBukkitVersion();
        if (!version.contains("1.21")) {
            getLogger().warning("¡ADVERTENCIA! Este plugin está diseñado para Minecraft 1.21.x");
            getLogger().warning("Versión detectada: " + version);
            getLogger().warning("Algunas funciones podrían no funcionar correctamente.");
        }

        // Inicializar componentes principales
        inicializarComponentes();

        // Registrar listeners
        registrarListeners();

        // Registrar comandos
        registrarComandos();

        // Cargar configuración
        cargarConfiguracion();

        // Guardar config.yml por defecto
        saveDefaultConfig();
        this.config = getConfig();

        getLogger().info("§a¡Viciont Cinematics ha sido habilitado correctamente!");
        getLogger().info("§7Versión: " + getDescription().getVersion());
        getLogger().info("§7Autor: " + getDescription().getAuthors().get(0));
    }

    @Override
    public void onDisable() {
        // Guardar configuración antes de desactivar
        if (gestorCinematicas != null) {
            guardarConfiguracion();
        }

        getLogger().info("§c¡Viciont Cinematics ha sido deshabilitado!");
    }

    /**
     * Inicializa todos los componentes principales del plugin
     */
    private void inicializarComponentes() {
        try {
            this.gestorCinematicas = new GestorCinematicas(this);
            this.gestorGUI = new GestorGUI(this);

            getLogger().info("§aComponentes inicializados correctamente");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error al inicializar componentes", e);
        }
    }

    /**
     * Registra todos los listeners del plugin
     */
    private void registrarListeners() {
        try {
            getServer().getPluginManager().registerEvents(new ListenerGlobal(this), this);
            getLogger().info("§aListeners registrados correctamente");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error al registrar listeners", e);
        }
    }

    /**
     * Registra todos los comandos del plugin
     */
    private void registrarComandos() {
        try {
            // Registrar comandos tradicionales
            this.getCommand("cinematica").setExecutor(new CinematicaComando(this));
            this.getCommand("cinematica").setTabCompleter(new CinematicaTabCompleter(this));

            this.getCommand("cinematica-viva").setExecutor(new CinematicaVivaComando(this));
            this.getCommand("cinematica-viva").setTabCompleter(new CinematicaVivaTabCompleter(this));

            getLogger().info("§aComandos registrados correctamente");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error al registrar comandos", e);
        }
    }

    /**
     * Carga la configuración desde archivos JSON
     */
    public void cargarConfiguracion() {
        try {
            reloadConfig();
            this.config = getConfig();

            ConfiguracionJSON configJson = new ConfiguracionJSON("cinematicas.json");
            gestorCinematicas.cargarCinematicas(configJson);

            getLogger().info("§aConfiguración cargada correctamente");
        } catch (Exception e) {
            getLogger().log(Level.WARNING, "No se pudo cargar la configuración, usando valores por defecto", e);
        }
    }

    /**
     * Guarda la configuración actual en archivos JSON
     */
    public void guardarConfiguracion() {
        try {
            ConfiguracionJSON config = new ConfiguracionJSON("cinematicas.json");
            gestorCinematicas.guardarCinematicas(config);
            config.guardar();
            getLogger().info("§aConfiguración guardada correctamente");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error al guardar la configuración", e);
        }
    }

    /**
     * Convierte un string con formato MiniMessage a Component
     */
    public Component parsearTexto(String texto) {
        return miniMessage.deserialize(texto);
    }

    /**
     * Envía un mensaje formateado a un jugador
     */
    public void enviarMensaje(org.bukkit.entity.Player jugador, String mensaje) {
        jugador.sendMessage(parsearTexto(mensaje));
    }

    // Getters estáticos y de instancia
    public static ViciontCinematics getInstancia() {
        return instancia;
    }

    public GestorCinematicas getGestorCinematicas() {
        return gestorCinematicas;
    }

    public GestorGUI getGestorGUI() {
        return gestorGUI;
    }

    public static MiniMessage getMiniMessage() {
        return miniMessage;
    }

    public static Gson getGson() {
        return gson;
    }

    @Override
    public FileConfiguration getConfig() {
        return super.getConfig();
    }
}