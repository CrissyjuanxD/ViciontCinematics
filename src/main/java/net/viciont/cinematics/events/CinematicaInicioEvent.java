package net.viciont.cinematics.events;

import net.viciont.cinematics.objects.ProgresoCinematica;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Evento que se dispara cuando inicia una cinemática
 * 
 * @author CrissyjuanxD
 */
public class CinematicaInicioEvent extends Event {
    
    private static final HandlerList HANDLERS = new HandlerList();
    private final ProgresoCinematica progresoCinematica;
    
    public CinematicaInicioEvent(ProgresoCinematica progresoCinematica) {
        this.progresoCinematica = progresoCinematica;
    }
    
    public ProgresoCinematica getProgresoCinematica() {
        return progresoCinematica;
    }
    
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
    
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}