package net.viciont.cinematics.events;

import net.viciont.cinematics.objects.ProgresoCinematica;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Evento que se dispara cuando termina una cinemática
 * 
 * @author CrissyjuanxD
 */
public class CinematicaFinEvent extends Event {
    
    private static final HandlerList HANDLERS = new HandlerList();
    private final ProgresoCinematica progresoCinematica;
    
    public CinematicaFinEvent(ProgresoCinematica progresoCinematica) {
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