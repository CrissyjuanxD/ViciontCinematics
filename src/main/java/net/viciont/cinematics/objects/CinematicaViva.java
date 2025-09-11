package net.viciont.cinematics.objects;

import net.viciont.cinematics.ViciontCinematics;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Representa una cinemática en vivo donde otros jugadores siguen al anfitrión
 *
 * @author CrissyjuanxD
 */
public class CinematicaViva {

    private final ViciontCinematics plugin;
    private final UUID uuidAnfitrion;
    private final long tiempoInicio;

    private final Set<UUID> espectadores = ConcurrentHashMap.newKeySet();
    private final Map<UUID, InfoJugador> informacionOriginal = new ConcurrentHashMap<>();

    private boolean activa = true;

    public CinematicaViva(Player anfitrion, ViciontCinematics plugin) {
        this.plugin = plugin;
        this.uuidAnfitrion = anfitrion.getUniqueId();
        this.tiempoInicio = System.currentTimeMillis();

        // Configurar al anfitrión
        configurarAnfitrion(anfitrion);
    }

    /**
     * Configura al anfitrión para la cinemática en vivo
     */
    private void configurarAnfitrion(Player anfitrion) {
        // Guardar información original
        informacionOriginal.put(anfitrion.getUniqueId(), new InfoJugador(anfitrion));

        // Cambiar a modo espectador para mejor control
        anfitrion.setGameMode(GameMode.SPECTATOR);

        plugin.enviarMensaje(anfitrion, "<purple>Ahora estás en modo cinemática en vivo");
        plugin.enviarMensaje(anfitrion, "<gray>Muévete y otros jugadores te seguirán automáticamente");
    }

    /**
     * Añade un espectador a la cinemática
     */
    public boolean agregarEspectador(Player jugador, boolean forzar) {
        if (!activa) return false;

        UUID uuidJugador = jugador.getUniqueId();

        // No puede agregarse el anfitrión
        if (uuidJugador.equals(uuidAnfitrion)) return false;

        // Verificar si ya está en la cinemática
        if (espectadores.contains(uuidJugador)) return false;

        // Verificar si está en otra cinemática (si no es forzado)
        if (!forzar && plugin.getGestorCinematicas().obtenerProgresoCinematica(jugador) != null) {
            return false;
        }

        // Guardar información original
        informacionOriginal.put(uuidJugador, new InfoJugador(jugador));

        // Configurar jugador para seguir
        configurarEspectador(jugador);

        // Añadir a la lista
        espectadores.add(uuidJugador);

        plugin.getLogger().info("Espectador agregado a cinemática en vivo: " + jugador.getName());
        return true;
    }

    /**
     * Configura un espectador para seguir al anfitrión
     */
    private void configurarEspectador(Player espectador) {
        // Cambiar a modo espectador
        espectador.setGameMode(GameMode.SPECTATOR);

        // Aplicar efecto de transición (implementar método en GestorCinematicas)
        // plugin.getGestorCinematicas().aplicarPantallaNegra(List.of(espectador.getUniqueId()));

        // Programar la sincronización después del fade
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Player anfitrion = obtenerAnfitrion();
            if (anfitrion != null && espectador.isOnline()) {
                espectador.teleport(anfitrion.getLocation());
            }
        }, 110L); // 5.5 segundos
    }

    /**
     * Remueve un espectador de la cinemática
     */
    public boolean removerEspectador(Player jugador) {
        UUID uuidJugador = jugador.getUniqueId();

        if (!espectadores.remove(uuidJugador)) {
            return false; // No estaba en la cinemática
        }

        // Aplicar transición de salida
        // plugin.getGestorCinematicas().aplicarPantallaNegra(List.of(uuidJugador));

        // Restaurar información original después del fade
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (jugador.isOnline()) {
                InfoJugador info = informacionOriginal.remove(uuidJugador);
                if (info != null) {
                    info.restaurarJugador(jugador);
                }
            }
        }, 110L);

        plugin.getLogger().info("Espectador removido de cinemática en vivo: " + jugador.getName());
        return true;
    }

    /**
     * Sincroniza la posición de todos los espectadores con el anfitrión
     */
    public void sincronizarEspectadores() {
        if (!activa) return;

        Player anfitrion = obtenerAnfitrion();
        if (anfitrion == null) {
            finalizar();
            return;
        }

        // Sincronizar posición de cada espectador
        espectadores.removeIf(uuidEspectador -> {
            Player espectador = Bukkit.getPlayer(uuidEspectador);

            if (espectador == null || !espectador.isOnline()) {
                // Limpiar información del jugador desconectado
                informacionOriginal.remove(uuidEspectador);
                return true;
            }

            // Teleportar al espectador a la posición del anfitrión
            try {
                espectador.teleport(anfitrion.getLocation());
            } catch (Exception e) {
                plugin.getLogger().warning("Error al sincronizar espectador " + espectador.getName() + ": " + e.getMessage());
            }

            return false;
        });
    }

    /**
     * Finaliza la cinemática en vivo
     */
    public void finalizar() {
        if (!activa) return;

        activa = false;

        // Restaurar anfitrión
        Player anfitrion = obtenerAnfitrion();
        if (anfitrion != null) {
            InfoJugador infoAnfitrion = informacionOriginal.get(uuidAnfitrion);
            if (infoAnfitrion != null) {
                // plugin.getGestorCinematicas().aplicarPantallaNegra(List.of(uuidAnfitrion));
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (anfitrion.isOnline()) {
                        infoAnfitrion.restaurarJugador(anfitrion);
                    }
                }, 110L);
            }
        }

        // Restaurar todos los espectadores
        for (UUID uuidEspectador : new ArrayList<>(espectadores)) {
            Player espectador = Bukkit.getPlayer(uuidEspectador);
            if (espectador != null) {
                removerEspectador(espectador);
            }
        }

        espectadores.clear();
        informacionOriginal.clear();

        plugin.getLogger().info("Cinemática en vivo finalizada");
    }

    /**
     * Verifica si un jugador es participante (anfitrión o espectador)
     */
    public boolean esParticipante(Player jugador) {
        UUID uuidJugador = jugador.getUniqueId();
        return uuidJugador.equals(uuidAnfitrion) || espectadores.contains(uuidJugador);
    }

    /**
     * Obtiene el anfitrión de la cinemática
     */
    public Player obtenerAnfitrion() {
        return Bukkit.getPlayer(uuidAnfitrion);
    }

    /**
     * Obtiene la lista de espectadores conectados
     */
    public List<Player> obtenerEspectadores() {
        return espectadores.stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .filter(Player::isOnline)
                .toList();
    }

    /**
     * Obtiene la duración de la cinemática en milisegundos
     */
    public long obtenerDuracion() {
        return System.currentTimeMillis() - tiempoInicio;
    }

    /**
     * Obtiene información de estado para debugging
     */
    public String obtenerEstadoDebug() {
        Player anfitrion = obtenerAnfitrion();
        String nombreAnfitrion = anfitrion != null ? anfitrion.getName() : "DESCONECTADO";

        return String.format("CinematicaViva{anfitrion=%s, espectadores=%d, duracion=%ds, activa=%s}",
                nombreAnfitrion, espectadores.size(), obtenerDuracion() / 1000, activa);
    }

    // Getters
    public UUID getUuidAnfitrion() {
        return uuidAnfitrion;
    }

    public boolean estaActiva() {
        return activa;
    }

    public long getTiempoInicio() {
        return tiempoInicio;
    }

    public int getCantidadEspectadores() {
        return espectadores.size();
    }
}