package net.viciont.cinematics.objects;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Almacena información de un jugador antes de una cinemática
 * para poder restaurarla después
 * 
 * @author CrissyjuanxD
 */
public class InfoJugador {
    
    private final UUID uuid;
    private final GameMode modoJuegoOriginal;
    private final Location ubicacionOriginal;
    private final boolean estabaVolando;
    private final float velocidadVuelo;
    private final float velocidadCaminata;
    private final long tiempoGuardado;
    
    public InfoJugador(Player jugador) {
        this.uuid = jugador.getUniqueId();
        this.modoJuegoOriginal = jugador.getGameMode();
        this.ubicacionOriginal = jugador.getLocation().clone();
        this.estabaVolando = jugador.isFlying();
        this.velocidadVuelo = jugador.getFlySpeed();
        this.velocidadCaminata = jugador.getWalkSpeed();
        this.tiempoGuardado = System.currentTimeMillis();
    }
    
    /**
     * Restaura el estado original del jugador
     */
    public void restaurarJugador(Player jugador) {
        if (!jugador.getUniqueId().equals(this.uuid)) {
            throw new IllegalArgumentException("El jugador no coincide con la información guardada");
        }
        
        try {
            // Restaurar modo de juego
            jugador.setGameMode(modoJuegoOriginal);
            
            // Restaurar ubicación
            jugador.teleport(ubicacionOriginal);
            
            // Restaurar estado de vuelo
            if (modoJuegoOriginal == GameMode.CREATIVE || modoJuegoOriginal == GameMode.SPECTATOR) {
                jugador.setFlying(estabaVolando);
            }
            
            // Restaurar velocidades
            jugador.setFlySpeed(velocidadVuelo);
            jugador.setWalkSpeed(velocidadCaminata);
            
        } catch (Exception e) {
            // Log del error pero no lanzar excepción para no interrumpir el flujo
            System.err.println("Error al restaurar jugador " + jugador.getName() + ": " + e.getMessage());
        }
    }
    
    /**
     * Verifica si la información es del jugador especificado
     */
    public boolean esDelJugador(UUID uuidJugador) {
        return this.uuid.equals(uuidJugador);
    }
    
    /**
     * Obtiene la edad de la información en milisegundos
     */
    public long obtenerEdad() {
        return System.currentTimeMillis() - tiempoGuardado;
    }
    
    @Override
    public String toString() {
        return String.format("InfoJugador{uuid=%s, modo=%s, ubicacion=%s, edad=%dms}", 
            uuid.toString().substring(0, 8), modoJuegoOriginal, 
            ubicacionOriginal.getWorld().getName(), obtenerEdad());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        InfoJugador that = (InfoJugador) obj;
        return uuid.equals(that.uuid);
    }
    
    @Override
    public int hashCode() {
        return uuid.hashCode();
    }
    
    // Getters
    public UUID getUuid() {
        return uuid;
    }
    
    public GameMode getModoJuegoOriginal() {
        return modoJuegoOriginal;
    }
    
    public Location getUbicacionOriginal() {
        return ubicacionOriginal.clone();
    }
    
    public boolean isEstabaVolando() {
        return estabaVolando;
    }
    
    public float getVelocidadVuelo() {
        return velocidadVuelo;
    }
    
    public float getVelocidadCaminata() {
        return velocidadCaminata;
    }
    
    public long getTiempoGuardado() {
        return tiempoGuardado;
    }
}