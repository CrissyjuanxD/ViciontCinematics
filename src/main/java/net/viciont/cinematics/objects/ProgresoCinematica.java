package net.viciont.cinematics.objects;

import net.viciont.cinematics.ViciontCinematics;
import net.viciont.cinematics.utils.CadenaTareas;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Representa el progreso de una cinemática en reproducción
 *
 * @author CrissyjuanxD
 */
public class ProgresoCinematica {

    private final ViciontCinematics plugin;
    private final List<Cinematica> secuencia;
    private final List<UUID> espectadores;
    private final CadenaTareas cadenaTareas;

    private final Map<UUID, InfoJugador> informacionJugadores = new HashMap<>();

    private boolean activa = true;
    private int cinematicaActual = 0;

    public ProgresoCinematica(List<Cinematica> secuencia, List<UUID> espectadores,
                              CadenaTareas cadenaTareas, ViciontCinematics plugin) {
        this.plugin = plugin;
        this.secuencia = secuencia;
        this.espectadores = espectadores;
        this.cadenaTareas = cadenaTareas;
    }

    /**
     * Verifica si un jugador es espectador de esta cinemática
     */
    public boolean esEspectador(UUID uuidJugador) {
        return espectadores.contains(uuidJugador);
    }

    /**
     * Verifica los eventos programados en el tick actual
     */
    public void verificarEventos() {
        if (!activa || secuencia.isEmpty()) return;

        try {
            int tickActual = cadenaTareas.getCurrentTask();
            Cinematica cinematicaActual = secuencia.get(this.cinematicaActual);

            Map<Integer, List<String>> eventos = cinematicaActual.getEventosProgramados();
            if (eventos.containsKey(tickActual)) {
                List<String> comandos = eventos.get(tickActual);

                for (String comando : comandos) {
                    try {
                        plugin.getLogger().info("Ejecutando evento: " + comando);
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), comando);
                    } catch (Exception e) {
                        plugin.getLogger().warning("Error al ejecutar comando: " + comando + " - " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error al verificar eventos: " + e.getMessage());
        }
    }

    /**
     * Finaliza la cinemática
     */
    public void finalizar() {
        this.activa = false;

        plugin.getLogger().info("Cinemática finalizada con " + espectadores.size() + " espectadores");
    }

    /**
     * Obtiene el progreso actual como porcentaje
     */
    public double obtenerPorcentajeProgreso() {
        if (cadenaTareas.getTasksLeft() == 0) return 100.0;

        int tareasCompletadas = cadenaTareas.getCurrentTask();
        int tareasTotal = tareasCompletadas + cadenaTareas.getTasksLeft();

        return (double) tareasCompletadas / tareasTotal * 100.0;
    }

    /**
     * Obtiene información de estado para debugging
     */
    public String obtenerEstadoDebug() {
        return String.format("ProgresoCinematica{activa=%s, espectadores=%d, cinematica=%d/%d, progreso=%.1f%%}",
                activa, espectadores.size(), cinematicaActual + 1, secuencia.size(), obtenerPorcentajeProgreso());
    }

    // Getters y Setters
    public List<Cinematica> getSecuencia() {
        return secuencia;
    }

    public List<UUID> getEspectadores() {
        return espectadores;
    }

    public CadenaTareas getCadenaTareas() {
        return cadenaTareas;
    }

    public Map<UUID, InfoJugador> getInformacionJugadores() {
        return informacionJugadores;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }

    public int getCinematicaActual() {
        return cinematicaActual;
    }

    public void setCinematicaActual(int cinematicaActual) {
        this.cinematicaActual = cinematicaActual;
    }
}