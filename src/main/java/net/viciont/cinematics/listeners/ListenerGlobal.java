package net.viciont.cinematics.listeners;

import net.viciont.cinematics.ViciontCinematics;
import net.viciont.cinematics.events.CinematicaFinEvent;
import net.viciont.cinematics.events.CinematicaInicioEvent;
import net.viciont.cinematics.events.CinematicaTickEvent;
import net.viciont.cinematics.objects.InfoJugador;
import net.viciont.cinematics.objects.ProgresoCinematica;
import net.viciont.cinematics.objects.Cinematica;
import net.viciont.cinematics.objects.Frame;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.UUID;

/**
 * Listener global para manejar eventos del plugin
 *
 * @author CrissyjuanxD
 */
public class ListenerGlobal implements Listener {

    private final ViciontCinematics plugin;

    public ListenerGlobal(ViciontCinematics plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void alIniciarCinematica(CinematicaInicioEvent evento) {
        var gestor = plugin.getGestorCinematicas();
        var progreso = evento.getProgresoCinematica();
        var espectadores = progreso.getEspectadores();

        // Guardar información de jugadores ANTES de cualquier cambio
        if (gestor.isRestaurarModoJuego() || gestor.isRestaurarUbicacion()) {
            for (UUID uuid : espectadores) {
                Player jugador = Bukkit.getPlayer(uuid);
                if (jugador != null) {
                    progreso.getInformacionJugadores().put(uuid, new InfoJugador(jugador));
                }
            }
        }

        // Aplicar fade de salida
        // TODO: Implementar método aplicarPantallaNegraConFadeOut en GestorCinematicas

        // Ocultar jugadores si está configurado
        if (gestor.isOcultarJugadoresAuto()) {
            gestor.alternarVisibilidadJugadores(true);
        }

        // Cambiar a modo espectador
        for (UUID uuid : espectadores) {
            Player jugador = Bukkit.getPlayer(uuid);
            if (jugador != null) {
                jugador.setGameMode(GameMode.SPECTATOR);

                // Ocultar nombres y cabezas de otros jugadores en modo espectador
                ocultarJugadoresEspectador(jugador, espectadores);
            }
        }

        plugin.getLogger().info("Cinemática iniciada con " + espectadores.size() + " espectadores");
    }

    @EventHandler
    public void alFinalizarCinematica(CinematicaFinEvent evento) {
        var gestor = plugin.getGestorCinematicas();
        var progreso = evento.getProgresoCinematica();
        var espectadores = progreso.getEspectadores();
        var informacionJugadores = progreso.getInformacionJugadores();

        // Restaurar modo de juego
        if (gestor.isRestaurarModoJuego()) {
            for (UUID uuid : espectadores) {
                Player jugador = Bukkit.getPlayer(uuid);
                if (jugador != null) {
                    InfoJugador info = informacionJugadores.get(uuid);
                    if (info != null) {
                        jugador.setGameMode(info.getModoJuegoOriginal());
                    }
                }
            }
        }

        // Restaurar ubicación
        if (gestor.isRestaurarUbicacion()) {
            for (UUID uuid : espectadores) {
                Player jugador = Bukkit.getPlayer(uuid);
                if (jugador != null) {
                    InfoJugador info = informacionJugadores.get(uuid);
                    if (info != null) {
                        jugador.teleport(info.getUbicacionOriginal());
                    }
                }
            }
        }

        // Aplicar efecto de ceguera temporal para suavizar la transición
        if (gestor.isRestaurarModoJuego() || gestor.isRestaurarUbicacion()) {
            for (UUID uuid : espectadores) {
                Player jugador = Bukkit.getPlayer(uuid);
                if (jugador != null) {
                    jugador.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20, 1, false, false, false));
                }
            }
        }

        // Mostrar jugadores si estaban ocultos
        if (gestor.isOcultarJugadoresAuto()) {
            gestor.alternarVisibilidadJugadores(false);
        }

        // Mostrar jugadores que estaban ocultos por el modo espectador
        for (UUID uuid : espectadores) {
            Player jugador = Bukkit.getPlayer(uuid);
            if (jugador != null) {
                mostrarJugadoresEspectador(jugador);
            }
        }

        plugin.getLogger().info("Cinemática finalizada");
    }

    @EventHandler
    public void alTickCinematica(CinematicaTickEvent evento) {
        // Verificar eventos programados
        Bukkit.getScheduler().runTask(plugin, () -> {
            evento.getProgresoCinematica().verificarEventos();
        });
    }

    @EventHandler
    public void alMoverseJugador(PlayerMoveEvent evento) {
        var gestor = plugin.getGestorCinematicas();
        var grabando = gestor.getGrabando();
        var estadosGrabacion = gestor.getEstadosGrabacion();
        UUID uuid = evento.getPlayer().getUniqueId();

        // Si el jugador está grabando, añadir frame
        if (grabando.containsKey(uuid)) {
            var cinematica = grabando.get(uuid);
            var ubicacion = evento.getTo();

            if (ubicacion != null) {
                var frame = new Frame(
                        ubicacion.getWorld().getName(),
                        ubicacion.getX(), ubicacion.getY(), ubicacion.getZ(),
                        ubicacion.getYaw(), ubicacion.getPitch()
                );

                // Manejar grabación por partes
                if (cinematica.getTipo() == Cinematica.TipoCinematica.PARTES) {
                    var estado = estadosGrabacion.get(uuid);
                    // Solo grabar si está activamente grabando una parte
                    if (estado != null && estado.grabandoParte) {
                        // Añadir a la parte actual si está grabando
                        if (!cinematica.getPartes().isEmpty()) {
                            var parteActual = cinematica.getPartes().get(cinematica.getPartes().size() - 1);
                            parteActual.getFrames().add(frame);
                        }
                    }
                } else {
                    // Grabación normal
                    cinematica.getFrames().add(frame);
                }
            }
        }
    }

    @EventHandler
    public void alSalirJugador(PlayerQuitEvent evento) {
        var gestor = plugin.getGestorCinematicas();
        var grabando = gestor.getGrabando();
        UUID uuid = evento.getPlayer().getUniqueId();

        // Si estaba grabando, cancelar la grabación
        if (grabando.containsKey(uuid)) {
            grabando.remove(uuid);
            plugin.getLogger().info("Grabación cancelada por desconexión: " + evento.getPlayer().getName());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void alHablarEnChat(AsyncPlayerChatEvent evento) {
        var gestor = plugin.getGestorCinematicas();

        // Silenciar chat si está habilitado y el jugador no tiene permisos
        if (gestor.isSilencioGlobal() && !evento.getPlayer().hasPermission("viciont.cinematics.chat")) {
            evento.setCancelled(true);
            plugin.enviarMensaje(evento.getPlayer(), "<red>El chat está silenciado durante las cinemáticas.");
        }
    }

    /**
     * Oculta otros jugadores para evitar ver cabezas y nombres en modo espectador
     */
    private void ocultarJugadoresEspectador(Player jugador, List<UUID> espectadores) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (Player otroJugador : Bukkit.getOnlinePlayers()) {
                // Solo ocultar jugadores que NO están en la cinemática
                if (!espectadores.contains(otroJugador.getUniqueId()) && !otroJugador.equals(jugador)) {
                    jugador.hidePlayer(plugin, otroJugador);
                }
            }
        });
    }

    /**
     * Muestra todos los jugadores de nuevo después de la cinemática
     */
    private void mostrarJugadoresEspectador(Player jugador) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (Player otroJugador : Bukkit.getOnlinePlayers()) {
                if (!otroJugador.equals(jugador)) {
                    jugador.showPlayer(plugin, otroJugador);
                }
            }
        });
    }
}