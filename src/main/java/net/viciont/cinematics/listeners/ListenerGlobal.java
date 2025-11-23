package net.viciont.cinematics.listeners;

import net.viciont.cinematics.ViciontCinematics;
import net.viciont.cinematics.events.CinematicaFinEvent;
import net.viciont.cinematics.events.CinematicaInicioEvent;
import net.viciont.cinematics.events.CinematicaTickEvent;
import net.viciont.cinematics.objects.InfoJugador;
import net.viciont.cinematics.objects.ProgresoCinematica;
import net.viciont.cinematics.objects.Cinematica;
import net.viciont.cinematics.objects.Frame;
import net.viciont.cinematics.core.GestorPackets;
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

        // NO ocultar jugadores - permitir visibilidad durante cinemáticas
        // En su lugar, mantener visibles usando packets
        GestorPackets gestorPackets = plugin.getGestorPackets();

        // Cambiar a modo espectador pero mantener visibilidad
        for (UUID uuid : espectadores) {
            Player jugador = Bukkit.getPlayer(uuid);
            if (jugador != null) {
                jugador.setGameMode(GameMode.SPECTATOR);
            }
        }

        // Mantener visibles a todos los jugadores entre ellos usando packets
        if (gestorPackets.isDisponible()) {
            gestorPackets.mantenerVisiblesTodos(espectadores);

            // Ejecutar periódicamente para asegurar visibilidad
            Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                if (progreso.isActiva()) {
                    gestorPackets.mantenerVisiblesTodos(espectadores);
                }
            }, 20L, 20L);
        } else {
            // Sistema básico - mostrar a todos
            Bukkit.getScheduler().runTask(plugin, () -> {
                for (UUID uuid1 : espectadores) {
                    Player j1 = Bukkit.getPlayer(uuid1);
                    if (j1 == null || !j1.isOnline()) continue;

                    for (UUID uuid2 : espectadores) {
                        if (uuid1.equals(uuid2)) continue;
                        Player j2 = Bukkit.getPlayer(uuid2);
                        if (j2 == null || !j2.isOnline()) continue;

                        j1.showPlayer(plugin, j2);
                    }
                }
            });
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

        // Restaurar visibilidad normal
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (UUID uuid1 : espectadores) {
                Player j1 = Bukkit.getPlayer(uuid1);
                if (j1 == null || !j1.isOnline()) continue;

                for (Player j2 : Bukkit.getOnlinePlayers()) {
                    if (!j1.equals(j2)) {
                        j1.showPlayer(plugin, j2);
                        j2.showPlayer(plugin, j1);
                    }
                }
            }
        });

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

}