package net.viciont.cinematics.core;

import net.viciont.cinematics.ViciontCinematics;
import net.viciont.cinematics.objects.CinematicaViva;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestor para cinemáticas en vivo (donde otros jugadores siguen al anfitrión)
 * 
 * @author CrissyjuanxD
 */
public class GestorCinematicasVivas implements Listener {
    
    private final ViciontCinematics plugin;
    private final Map<UUID, CinematicaViva> cinematicasActivas = new ConcurrentHashMap<>();
    
    public GestorCinematicasVivas(ViciontCinematics plugin) {
        this.plugin = plugin;
        
        // Registrar eventos
        Bukkit.getPluginManager().registerEvents(this, plugin);
        
        // Tarea para sincronizar posiciones
        Bukkit.getScheduler().runTaskTimer(plugin, this::sincronizarPosiciones, 0L, 1L);
        
        // Tarea de limpieza
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::limpiarCinematicasInactivas, 0L, 200L);
    }
    
    /**
     * Crea una nueva cinemática en vivo
     */
    public CinematicaViva crearCinematicaViva(Player anfitrion) {
        UUID uuidAnfitrion = anfitrion.getUniqueId();
        
        // Verificar que no tenga ya una cinemática activa
        if (cinematicasActivas.containsKey(uuidAnfitrion)) {
            return null;
        }
        
        // Verificar que no esté participando en otra cinemática
        if (estaEnCinematicaViva(anfitrion)) {
            return null;
        }
        
        CinematicaViva nuevaCinematica = new CinematicaViva(anfitrion, plugin);
        cinematicasActivas.put(uuidAnfitrion, nuevaCinematica);
        
        plugin.getLogger().info("Cinemática en vivo creada por: " + anfitrion.getName());
        return nuevaCinematica;
    }
    
    /**
     * Detiene una cinemática en vivo
     */
    public boolean detenerCinematicaViva(Player anfitrion) {
        CinematicaViva cinematica = cinematicasActivas.remove(anfitrion.getUniqueId());
        
        if (cinematica != null) {
            cinematica.finalizar();
            plugin.getLogger().info("Cinemática en vivo finalizada por: " + anfitrion.getName());
            return true;
        }
        
        return false;
    }
    
    /**
     * Obtiene la cinemática en vivo de un anfitrión
     */
    public CinematicaViva obtenerCinematicaViva(Player anfitrion) {
        return cinematicasActivas.get(anfitrion.getUniqueId());
    }
    
    /**
     * Verifica si un jugador está participando en alguna cinemática en vivo
     */
    public boolean estaEnCinematicaViva(Player jugador) {
        return cinematicasActivas.values().stream()
            .anyMatch(cinematica -> cinematica.esParticipante(jugador));
    }
    
    /**
     * Obtiene la cinemática en vivo donde participa un jugador
     */
    public CinematicaViva obtenerCinematicaVivaDeJugador(Player jugador) {
        return cinematicasActivas.values().stream()
            .filter(cinematica -> cinematica.esParticipante(jugador))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Obtiene todas las cinemáticas en vivo activas
     */
    public List<CinematicaViva> obtenerCinematicasActivas() {
        return new ArrayList<>(cinematicasActivas.values());
    }
    
    /**
     * Sincroniza las posiciones de todos los espectadores con sus anfitriones
     */
    private void sincronizarPosiciones() {
        for (CinematicaViva cinematica : cinematicasActivas.values()) {
            cinematica.sincronizarEspectadores();
        }
    }
    
    /**
     * Limpia cinemáticas inactivas o con anfitriones desconectados
     */
    private void limpiarCinematicasInactivas() {
        cinematicasActivas.entrySet().removeIf(entrada -> {
            CinematicaViva cinematica = entrada.getValue();
            Player anfitrion = cinematica.obtenerAnfitrion();
            
            if (anfitrion == null || !anfitrion.isOnline() || !cinematica.estaActiva()) {
                cinematica.finalizar();
                return true;
            }
            
            return false;
        });
    }
    
    @EventHandler
    public void alSalirJugador(PlayerQuitEvent evento) {
        Player jugador = evento.getPlayer();
        UUID uuidJugador = jugador.getUniqueId();
        
        // Si es anfitrión, finalizar su cinemática
        CinematicaViva cinematicaAnfitrion = cinematicasActivas.remove(uuidJugador);
        if (cinematicaAnfitrion != null) {
            cinematicaAnfitrion.finalizar();
            plugin.getLogger().info("Cinemática en vivo finalizada por desconexión del anfitrión: " + jugador.getName());
        }
        
        // Si es espectador, removerlo de todas las cinemáticas
        cinematicasActivas.values().forEach(cinematica -> {
            cinematica.removerEspectador(jugador);
        });
    }
    
    /**
     * Obtiene estadísticas del sistema
     */
    public String obtenerEstadisticas() {
        int totalCinematicas = cinematicasActivas.size();
        int totalEspectadores = cinematicasActivas.values().stream()
            .mapToInt(c -> c.obtenerEspectadores().size())
            .sum();
        
        return String.format("Cinemáticas activas: %d, Total espectadores: %d", 
            totalCinematicas, totalEspectadores);
    }
}