package net.viciont.cinematics.core;

import net.viciont.cinematics.ViciontCinematics;
import net.viciont.cinematics.objects.Cinematica;
import net.viciont.cinematics.objects.InfoJugador;
import net.viciont.cinematics.objects.ProgresoCinematica;
import net.viciont.cinematics.objects.Frame;
import net.viciont.cinematics.objects.Cinematica.TipoCinematica;
import net.viciont.cinematics.objects.Cinematica.ParteCinematica;
import net.viciont.cinematics.utils.ConfiguracionJSON;
import net.viciont.cinematics.utils.CadenaTareas;
import net.viciont.cinematics.events.CinematicaInicioEvent;
import net.viciont.cinematics.events.CinematicaFinEvent;
import net.viciont.cinematics.core.InterpoladorFrames;
import net.viciont.cinematics.core.InterpoladorFrames.TipoInterpolacion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestor principal para manejar todas las cinemáticas del servidor
 *
 * @author CrissyjuanxD
 */
public class GestorCinematicas {

    private final ViciontCinematics plugin;

    // Sistema de interpolación
    private InterpoladorFrames interpolador;
    private boolean interpolacionHabilitada;
    private int fpsObjetivo;
    private boolean usarSistemaLegacy;

    // Configuraciones del sistema
    private boolean silencioGlobal = false;
    private boolean efectoFade = true;
    private boolean ocultarJugadoresAuto = false;
    private boolean restaurarUbicacion = true;
    private boolean restaurarModoJuego = true;

    // Almacenamiento de datos
    private final Map<String, Cinematica> cinematicas = new ConcurrentHashMap<>();
    private final Map<UUID, Cinematica> grabando = new ConcurrentHashMap<>();
    private final Map<UUID, EstadoGrabacion> estadosGrabacion = new ConcurrentHashMap<>();
    private final List<ProgresoCinematica> cinematicasEnProgreso = Collections.synchronizedList(new ArrayList<>());

    public GestorCinematicas(ViciontCinematics plugin) {
        this.plugin = plugin;
        inicializarSistemaInterpolacion();
    }

    /**
     * Inicializa el sistema de interpolación basado en la configuración
     */
    private void inicializarSistemaInterpolacion() {
        var config = plugin.getConfig();

        // Cargar configuración de interpolación
        this.interpolacionHabilitada = config.getBoolean("playback.interpolation.enabled", true);
        this.fpsObjetivo = Math.max(24, Math.min(60, config.getInt("playback.interpolation.target_fps", 24)));
        this.usarSistemaLegacy = config.getBoolean("playback.legacy.use_legacy_system", false);

        // Configurar interpolador
        String tipoStr = config.getString("playback.interpolation.type", "SMOOTH").toUpperCase();
        TipoInterpolacion tipo;
        try {
            tipo = TipoInterpolacion.valueOf(tipoStr);
        } catch (IllegalArgumentException e) {
            tipo = TipoInterpolacion.SMOOTH;
            plugin.getLogger().warning("Tipo de interpolación inválido: " + tipoStr + ", usando SMOOTH");
        }

        boolean suavizadoRotacion = config.getBoolean("playback.interpolation.rotation_smoothing", true);
        double factorSuavizado = config.getDouble("playback.interpolation.smoothing_factor", 0.3);

        this.interpolador = new InterpoladorFrames(tipo, suavizadoRotacion, factorSuavizado);

        plugin.getLogger().info("Sistema de interpolación inicializado: " +
                (interpolacionHabilitada ? "HABILITADO" : "DESHABILITADO") +
                " - FPS: " + fpsObjetivo + " - Tipo: " + tipo);
    }

    /**
     * Inicia la grabación de una nueva cinemática
     */
    public boolean iniciarGrabacion(Player jugador, String nombre, TipoCinematica tipo) {
        if (grabando.containsKey(jugador.getUniqueId())) {
            return false; // Ya está grabando
        }

        if (cinematicas.containsKey(nombre)) {
            return false; // La cinemática ya existe
        }

        Cinematica nuevaCinematica = new Cinematica(nombre);
        nuevaCinematica.setTipo(tipo);
        grabando.put(jugador.getUniqueId(), nuevaCinematica);

        if (tipo == TipoCinematica.PARTES) {
            EstadoGrabacion estado = new EstadoGrabacion();
            estado.parteActual = 1;
            estado.grabandoParte = true;
            estadosGrabacion.put(jugador.getUniqueId(), estado);

            // Crear primera parte
            ParteCinematica primeraParte = new ParteCinematica(1);
            nuevaCinematica.agregarParte(primeraParte);
        }

        plugin.getLogger().info("Grabación iniciada por " + jugador.getName() + " para cinemática: " + nombre);
        return true;
    }

    /**
     * Detiene la grabación actual del jugador
     */
    public boolean detenerGrabacion(Player jugador) {
        Cinematica cinematica = grabando.remove(jugador.getUniqueId());
        if (cinematica == null) {
            return false;
        }

        // Para cinemáticas por partes, unir todas las partes
        if (cinematica.getTipo() == TipoCinematica.PARTES) {
            List<Frame> todosFrames = new ArrayList<>();
            for (ParteCinematica parte : cinematica.getPartes()) {
                todosFrames.addAll(parte.getFrames());
            }
            cinematica.setFrames(todosFrames);
        }

        if (cinematica.getFrames().isEmpty()) {
            plugin.getLogger().warning("Cinemática " + cinematica.getNombre() + " no tiene frames, no se guardará");
            return false;
        }

        cinematicas.put(cinematica.getNombre(), cinematica);
        plugin.guardarConfiguracion();

        // Limpiar estado de grabación si existe
        estadosGrabacion.remove(jugador.getUniqueId());

        plugin.getLogger().info("Grabación finalizada: " + cinematica.getNombre() + " con " + cinematica.getFrames().size() + " frames");
        return true;
    }

    /**
     * Graba una cinemática estática (sin movimiento)
     */
    public boolean grabarEstatica(Player jugador, String nombre, int duracionTicks) {
        if (cinematicas.containsKey(nombre)) {
            return false;
        }

        Location loc = jugador.getLocation();
        List<Frame> frames = new ArrayList<>();

        for (int i = 0; i < duracionTicks; i++) {
            Frame frame = new Frame(
                    loc.getWorld().getName(),
                    loc.getX(), loc.getY(), loc.getZ(),
                    loc.getYaw(), loc.getPitch()
            );
            frames.add(frame);
        }

        Cinematica cinematica = new Cinematica(nombre);
        cinematica.setFrames(frames);
        cinematicas.put(nombre, cinematica);

        plugin.guardarConfiguracion();
        plugin.getLogger().info("Cinemática estática creada: " + nombre + " con " + duracionTicks + " ticks");
        return true;
    }

    /**
     * Corta la grabación actual (para cinemáticas por partes)
     */
    public boolean cortarGrabacion(Player jugador) {
        EstadoGrabacion estado = estadosGrabacion.get(jugador.getUniqueId());
        Cinematica cinematica = grabando.get(jugador.getUniqueId());

        if (estado == null || cinematica == null || !estado.grabandoParte) {
            return false;
        }

        // Detener la grabación de la parte actual
        estado.grabandoParte = false;

        // Asegurar que la parte actual tenga al menos un frame
        if (!cinematica.getPartes().isEmpty()) {
            ParteCinematica parteActual = cinematica.getPartes().get(cinematica.getPartes().size() - 1);
            if (parteActual.getFrames().isEmpty()) {
                // Si no tiene frames, añadir el frame actual del jugador
                Location loc = jugador.getLocation();
                Frame frame = new Frame(
                        loc.getWorld().getName(),
                        loc.getX(), loc.getY(), loc.getZ(),
                        loc.getYaw(), loc.getPitch()
                );
                parteActual.getFrames().add(frame);
            }
        }

        plugin.getLogger().info("Parte " + estado.parteActual + " cortada para " + jugador.getName());
        return true;
    }

    /**
     * Continúa la grabación (nueva parte)
     */
    public boolean continuarGrabacion(Player jugador) {
        EstadoGrabacion estado = estadosGrabacion.get(jugador.getUniqueId());
        Cinematica cinematica = grabando.get(jugador.getUniqueId());

        if (estado == null || cinematica == null || estado.grabandoParte) {
            return false;
        }

        estado.parteActual++;
        estado.grabandoParte = true;

        // Crear nueva parte
        ParteCinematica nuevaParte = new ParteCinematica(estado.parteActual);

        // Añadir frame inicial de la nueva posición
        Location loc = jugador.getLocation();
        Frame frameInicial = new Frame(
                loc.getWorld().getName(),
                loc.getX(), loc.getY(), loc.getZ(),
                loc.getYaw(), loc.getPitch()
        );
        nuevaParte.getFrames().add(frameInicial);

        cinematica.agregarParte(nuevaParte);

        plugin.getLogger().info("Iniciando parte " + estado.parteActual + " para " + jugador.getName());
        return true;
    }

    /**
     * Reproduce una o varias cinemáticas
     */
    public boolean reproducir(List<UUID> jugadores, String... nombresCinematicas) {
        // Verificar que todas las cinemáticas existen
        for (String nombre : nombresCinematicas) {
            if (!cinematicas.containsKey(nombre)) {
                plugin.getLogger().warning("Cinemática no encontrada: " + nombre);
                return false;
            }
        }

        if (jugadores.isEmpty()) {
            plugin.getLogger().warning("No hay jugadores para reproducir la cinemática");
            return false;
        }

        // Crear cadena de tareas
        CadenaTareas cadena = CadenaTareas.crear();
        List<Cinematica> secuencia = Arrays.stream(nombresCinematicas)
                .map(cinematicas::get)
                .toList();

        ProgresoCinematica progreso = new ProgresoCinematica(secuencia, jugadores, cadena, plugin);
        cinematicasEnProgreso.add(progreso);

        // IMPORTANTE: Guardar información ANTES de cualquier cambio
        if (restaurarUbicacion || restaurarModoJuego) {
            for (UUID uuid : jugadores) {
                Player jugador = Bukkit.getPlayer(uuid);
                if (jugador != null) {
                    progreso.getInformacionJugadores().put(uuid, new InfoJugador(jugador));
                }
            }
        }

        // Aplicar fade inicial si está habilitado
        if (efectoFade) {
            // Obtener configuración de fade inicial
            int fadeInInicial = plugin.getConfig().getInt("effects.initial.fade_in_duration", 20);
            int fadeStayInicial = plugin.getConfig().getInt("effects.initial.fade_stay_duration", 60);
            int fadeOutInicial = plugin.getConfig().getInt("effects.initial.fade_out_duration", 40);

            // Aplicar fade inicial
            cadena.add(new BukkitRunnable() {
                @Override
                public void run() {
                    aplicarFadeInicial(jugadores, fadeInInicial, fadeStayInicial, fadeOutInicial);
                }
            });

            // Esperar solo el fade in, la cinemática empezará durante el stay
            cadena.retraso(fadeInInicial * 50L);
        }

        // Evento de inicio
        cadena.add(new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    Bukkit.getPluginManager().callEvent(new CinematicaInicioEvent(progreso));
                });
            }
        });

        // Procesar cada cinemática
        for (String nombre : nombresCinematicas) {
            Cinematica cinematica = cinematicas.get(nombre);

            List<Frame> frames = procesarFramesCinematica(cinematica);
            long delayMs = calcularDelayEntreFrames();

            // Empezar desde el primer frame siempre
            for (int i = 0; i < frames.size(); i++) {
                Frame frame = frames.get(i);
                final int frameIndex = i;

                cadena.add(new BukkitRunnable() {
                    @Override
                    public void run() {
                        Location loc = new Location(
                                Bukkit.getWorld(frame.getMundo()),
                                frame.getX(), frame.getY(), frame.getZ(),
                                frame.getYaw(), frame.getPitch()
                        );

                        Bukkit.getScheduler().runTask(plugin, () -> {
                            jugadores.forEach(uuid -> {
                                Player jugador = Bukkit.getPlayer(uuid);
                                if (jugador != null && jugador.isOnline()) {
                                    jugador.teleport(loc);
                                }
                            });
                        });

                        // Reproducir sonidos si existen
                        try {
                            Map<Integer, Cinematica.SonidoCinematica> sonidos = cinematica.getSonidos();
                            if (sonidos != null && !sonidos.isEmpty()) {
                                var sonidoCinematica = sonidos.get(frameIndex);
                                if (sonidoCinematica != null) {
                                    Bukkit.getScheduler().runTask(plugin, () -> {
                                        jugadores.forEach(uuid -> {
                                            Player jugador = Bukkit.getPlayer(uuid);
                                            if (jugador != null && jugador.isOnline()) {
                                                reproducirSonido(jugador, sonidoCinematica);
                                            }
                                        });
                                    });
                                }
                            }
                        } catch (Exception e) {
                            plugin.getLogger().warning("Error al reproducir sonido en frame " + frameIndex + ": " + e.getMessage());
                        }
                    }
                });

                // Añadir delay después de cada frame
                if (i < frames.size() - 1) { // No añadir delay después del último frame
                    cadena.retraso(delayMs);
                }
            }
        }

        // Aplicar fade final (antes del último frame para ocultar teleport)
        if (efectoFade) {
            // Obtener configuración de fade final
            int fadeInFinal = plugin.getConfig().getInt("effects.final.fade_in_duration", 20);
            int fadeStayFinal = plugin.getConfig().getInt("effects.final.fade_stay_duration", 60);
            int fadeOutFinal = plugin.getConfig().getInt("effects.final.fade_out_duration", 40);

            // Aplicar fade final
            cadena.add(new BukkitRunnable() {
                @Override
                public void run() {
                    aplicarFadeFinal(jugadores, fadeInFinal, fadeStayFinal, fadeOutFinal);
                }
            });

            // TP de finalización (durante el fade stay)
            cadena.retraso((fadeInFinal + 10) * 50L); // 10 ticks después del fade in
            cadena.add(new BukkitRunnable() {
                @Override
                public void run() {
                    // Restaurar jugadores
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        for (UUID uuid : jugadores) {
                            Player jugador = Bukkit.getPlayer(uuid);
                            if (jugador != null) {
                                var info = progreso.getInformacionJugadores().get(uuid);
                                if (info != null) {
                                    if (restaurarUbicacion) {
                                        jugador.teleport(info.getUbicacionOriginal());
                                    }
                                    if (restaurarModoJuego) {
                                        jugador.setGameMode(info.getModoJuegoOriginal());
                                    }
                                }
                            }
                        }
                    });
                }
            });
        }

        // Ejecutar la cadena
        cadena.execute().thenAccept(resultado -> {
            cinematicasEnProgreso.remove(progreso);
            Bukkit.getScheduler().runTask(plugin, () -> {
                Bukkit.getPluginManager().callEvent(new CinematicaFinEvent(progreso));
            });
        });

        plugin.getLogger().info("Reproduciendo cinemáticas: " + Arrays.toString(nombresCinematicas) + " para " + jugadores.size() + " jugadores");
        return true;
    }

    /**
     * Procesa los frames de una cinemática aplicando interpolación si está habilitada
     */
    private List<Frame> procesarFramesCinematica(Cinematica cinematica) {
        List<Frame> framesOriginales = cinematica.getFrames();

        if (usarSistemaLegacy || !interpolacionHabilitada) {
            // Sistema legacy
            boolean usarProlongados = plugin.getConfig().getBoolean("playback.legacy.use_prolonged_frames", false);
            return usarProlongados ? cinematica.getFramesProlongados() : framesOriginales;
        }

        // Sistema de interpolación moderno
        if (framesOriginales.size() < 2) {
            return framesOriginales;
        }

        // Optimizar frames antes de interpolar (remover redundantes)
        List<Frame> framesOptimizados = interpolador.optimizar(framesOriginales);

        // Aplicar interpolación
        List<Frame> framesInterpolados = interpolador.interpolar(framesOptimizados, fpsObjetivo);

        plugin.getLogger().info("Frames procesados: " + framesOriginales.size() +
                " → " + framesOptimizados.size() + " (optimizados) → " +
                framesInterpolados.size() + " (interpolados)");

        return framesInterpolados;
    }

    /**
     * Calcula el delay entre frames basado en el sistema configurado
     */
    private long calcularDelayEntreFrames() {
        if (usarSistemaLegacy || !interpolacionHabilitada) {
            // Sistema legacy
            int fps = plugin.getConfig().getInt("playback.legacy.fps", 20);
            fps = Math.max(10, Math.min(50, fps)); // Validar rango
            int delayTicks = Math.max(1, 20 / fps);
            return delayTicks * 50L; // 50ms por tick
        }

        // Sistema de interpolación - delay más preciso
        // Calcular delay en milisegundos directamente
        return Math.max(16L, 1000L / fpsObjetivo); // Mínimo 16ms (60 FPS máximo real)
    }

    /**
     * Recarga la configuración del sistema de interpolación
     */
    public void recargarConfiguracionInterpolacion() {
        inicializarSistemaInterpolacion();
        plugin.getLogger().info("Configuración de interpolación recargada");
    }

    /**
     * Reproduce un sonido de cinemática
     */
    private void reproducirSonido(Player jugador, Cinematica.SonidoCinematica sonidoCinematica) {
        try {
            if (sonidoCinematica.isEsCustom()) {
                String sonidoFinal = sonidoCinematica.getSonidoCustom();

                // Si es un preset, obtener el sonido real del config
                if (sonidoCinematica.isEsPreset()) {
                    sonidoFinal = plugin.getConfig().getString("sounds.presets." + sonidoCinematica.getSonidoCustom(),
                            sonidoCinematica.getSonidoCustom());
                }

                // Reproducir sonido personalizado
                jugador.playSound(jugador.getLocation(), sonidoFinal,
                        sonidoCinematica.getVolumen(), sonidoCinematica.getPitch());
            } else {
                // Reproducir sonido nativo
                jugador.playSound(jugador.getLocation(), sonidoCinematica.getSonido(),
                        sonidoCinematica.getVolumen(), sonidoCinematica.getPitch());
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error al reproducir sonido: " + e.getMessage());
        }
    }

    /**
     * Fuerza el fin de una cinemática para jugadores específicos
     */
    public boolean forzarFinCinematica(List<UUID> jugadores) {
        boolean algunoDetenido = false;

        for (ProgresoCinematica progreso : new ArrayList<>(cinematicasEnProgreso)) {
            // Crear una lista mutable de espectadores afectados
            List<UUID> espectadoresAfectados = new ArrayList<>(progreso.getEspectadores().stream()
                    .filter(jugadores::contains)
                    .toList());

            if (!espectadoresAfectados.isEmpty()) {
                // Crear nueva lista sin los jugadores afectados
                List<UUID> nuevosEspectadores = new ArrayList<>(progreso.getEspectadores());
                nuevosEspectadores.removeAll(espectadoresAfectados);

                // Actualizar la lista de espectadores
                progreso.getEspectadores().clear();
                progreso.getEspectadores().addAll(nuevosEspectadores);

                // Si no quedan espectadores, finalizar completamente
                if (progreso.getEspectadores().isEmpty()) {
                    progreso.finalizar();
                    cinematicasEnProgreso.remove(progreso);
                }

                // Restaurar jugadores afectados
                for (UUID uuid : espectadoresAfectados) {
                    Player jugador = Bukkit.getPlayer(uuid);
                    if (jugador != null) {
                        var info = progreso.getInformacionJugadores().get(uuid);
                        if (info != null) {
                            info.restaurarJugador(jugador);
                        }
                    }
                }

                algunoDetenido = true;
            }
        }

        return algunoDetenido;
    }

    /**
     * Añade un sonido a una cinemática
     */
    public boolean agregarSonido(String nombreCinematica, int tick, String sonido, float volumen, float pitch) {
        Cinematica cinematica = cinematicas.get(nombreCinematica);
        if (cinematica == null) {
            return false;
        }

        // Validar rangos
        if (volumen < 0.0f || volumen > 1.0f) {
            return false;
        }
        if (pitch < 0.5f || pitch > 2.0f) {
            return false;
        }

        try {
            // Intentar como preset primero
            String presetSonido = plugin.getConfig().getString("sounds.presets." + sonido);
            if (presetSonido != null) {
                cinematica.agregarSonidoPreset(tick, sonido, volumen, pitch);
                plugin.guardarConfiguracion();
                return true;
            }

            // Intentar como sonido nativo
            try {
                Sound soundEnum = Sound.valueOf(sonido.toUpperCase());
                cinematica.agregarSonido(tick, soundEnum, volumen, pitch);
                plugin.guardarConfiguracion();
                return true;
            } catch (IllegalArgumentException e) {
                // No es un sonido nativo, usar como custom
                cinematica.agregarSonidoCustom(tick, sonido, volumen, pitch);
                plugin.guardarConfiguracion();
                return true;
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error al agregar sonido: " + e.getMessage());
            return false;
        }
    }

    /**
     * Aplica fade inicial cinematográfico
     */
    private void aplicarFadeInicial(List<UUID> jugadores, int fadeIn, int stay, int fadeOut) {
        String fadeChar = plugin.getConfig().getString("effects.fade_unicode", "\uEAA8");

        Bukkit.getScheduler().runTask(plugin, () -> {
            jugadores.forEach(uuid -> {
                Player jugador = Bukkit.getPlayer(uuid);
                if (jugador != null && jugador.isOnline()) {
                    // Fade in -> Stay (TP cerca del final) -> Fade out
                    jugador.sendTitle(fadeChar, "", fadeIn, stay, fadeOut);
                }
            });
        });
    }

    /**
     * Aplica fade final cinematográfico
     */
    private void aplicarFadeFinal(List<UUID> jugadores, int fadeIn, int stay, int fadeOut) {
        String fadeChar = plugin.getConfig().getString("effects.fade_unicode", "\uEAA8");

        Bukkit.getScheduler().runTask(plugin, () -> {
            jugadores.forEach(uuid -> {
                Player jugador = Bukkit.getPlayer(uuid);
                if (jugador != null && jugador.isOnline()) {
                    // Fade in -> Stay (TP durante) -> Fade out
                    jugador.sendTitle(fadeChar, "", fadeIn, stay, fadeOut);
                }
            });
        });
    }

    /**
     * Oculta o muestra todos los jugadores
     */
    public void alternarVisibilidadJugadores(boolean ocultar) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (ocultar) {
                Bukkit.getOnlinePlayers().forEach(j1 -> {
                    Bukkit.getOnlinePlayers().forEach(j2 -> {
                        if (!j1.equals(j2)) {
                            j1.hidePlayer(plugin, j2);
                        }
                    });
                });
            } else {
                Bukkit.getOnlinePlayers().forEach(j1 -> {
                    Bukkit.getOnlinePlayers().forEach(j2 -> {
                        if (!j1.equals(j2)) {
                            j1.showPlayer(plugin, j2);
                        }
                    });
                });
            }
        });
    }

    /**
     * Obtiene el progreso de cinemática de un jugador
     */
    public ProgresoCinematica obtenerProgresoCinematica(Player jugador) {
        return cinematicasEnProgreso.stream()
                .filter(progreso -> progreso.esEspectador(jugador.getUniqueId()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Verifica si un jugador está grabando
     */
    public boolean estaGrabando(Player jugador) {
        return grabando.containsKey(jugador.getUniqueId());
    }

    /**
     * Verifica si un jugador está en una cinemática
     */
    public boolean estaEnCinematica(Player jugador) {
        return obtenerProgresoCinematica(jugador) != null;
    }

    /**
     * Elimina una cinemática
     */
    public boolean eliminarCinematica(String nombre) {
        return cinematicas.remove(nombre) != null;
    }

    /**
     * Añade una cinemática
     */
    public void agregarCinematica(String nombre, Cinematica cinematica) {
        cinematicas.put(nombre, cinematica);
    }

    /**
     * Carga las cinemáticas desde la configuración
     */
    public void cargarCinematicas(ConfiguracionJSON config) {
        try {
            var objetoJson = config.getObjetoJson();
            cinematicas.clear();

            for (var entrada : objetoJson.entrySet()) {
                String nombre = entrada.getKey();
                try {
                    Cinematica cinematica = ViciontCinematics.getGson().fromJson(entrada.getValue(), Cinematica.class);
                    if (cinematica != null && cinematica.getFrames() != null) {
                        // Asegurar que los mapas no sean null
                        if (cinematica.getEventosProgramados() == null) {
                            cinematica.setEventosProgramados(new HashMap<>());
                        }
                        if (cinematica.getSonidos() == null) {
                            cinematica.setSonidos(new HashMap<>());
                        }
                        cinematicas.put(nombre, cinematica);
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Error al cargar cinemática " + nombre + ": " + e.getMessage());
                }
            }

            plugin.getLogger().info("Cargadas " + cinematicas.size() + " cinemáticas");
        } catch (Exception e) {
            plugin.getLogger().warning("Error al cargar cinemáticas: " + e.getMessage());
        }
    }

    /**
     * Guarda las cinemáticas en la configuración
     */
    public void guardarCinematicas(ConfiguracionJSON config) {
        try {
            String json = ViciontCinematics.getGson().toJson(cinematicas);
            var objeto = ViciontCinematics.getGson().fromJson(json, com.google.gson.JsonObject.class);
            config.setObjetoJson(objeto);

            plugin.getLogger().info("Guardadas " + cinematicas.size() + " cinemáticas");
        } catch (Exception e) {
            plugin.getLogger().severe("Error al guardar cinemáticas: " + e.getMessage());
        }
    }

    /**
     * Obtiene estadísticas del sistema
     */
    public String obtenerEstadisticas() {
        return String.format("Cinemáticas: %d, Grabando: %d, Reproduciendo: %d",
                cinematicas.size(), grabando.size(), cinematicasEnProgreso.size());
    }

    // Getters y Setters
    public Map<String, Cinematica> getCinematicas() {
        return new HashMap<>(cinematicas);
    }

    public Map<UUID, Cinematica> getGrabando() {
        return new HashMap<>(grabando);
    }

    public Map<UUID, EstadoGrabacion> getEstadosGrabacion() {
        return estadosGrabacion;
    }

    public List<ProgresoCinematica> getCinematicasEnProgreso() {
        return new ArrayList<>(cinematicasEnProgreso);
    }

    public boolean isSilencioGlobal() {
        return silencioGlobal;
    }

    public void setSilencioGlobal(boolean silencioGlobal) {
        this.silencioGlobal = silencioGlobal;
    }

    public boolean isEfectoFade() {
        return efectoFade;
    }

    public void setEfectoFade(boolean efectoFade) {
        this.efectoFade = efectoFade;
    }

    public boolean isOcultarJugadoresAuto() {
        return ocultarJugadoresAuto;
    }

    public void setOcultarJugadoresAuto(boolean ocultarJugadoresAuto) {
        this.ocultarJugadoresAuto = ocultarJugadoresAuto;
    }

    public boolean isRestaurarUbicacion() {
        return restaurarUbicacion;
    }

    public void setRestaurarUbicacion(boolean restaurarUbicacion) {
        this.restaurarUbicacion = restaurarUbicacion;
    }

    public boolean isRestaurarModoJuego() {
        return restaurarModoJuego;
    }

    public void setRestaurarModoJuego(boolean restaurarModoJuego) {
        this.restaurarModoJuego = restaurarModoJuego;
    }

    // Getters para el sistema de interpolación
    public InterpoladorFrames getInterpolador() {
        return interpolador;
    }

    public boolean isInterpolacionHabilitada() {
        return interpolacionHabilitada;
    }

    public int getFpsObjetivo() {
        return fpsObjetivo;
    }

    public boolean isUsarSistemaLegacy() {
        return usarSistemaLegacy;
    }

    /**
     * Clase para manejar el estado de grabación por partes
     */
    public static class EstadoGrabacion {
        public int parteActual = 1;
        public boolean grabandoParte = true;
    }
}