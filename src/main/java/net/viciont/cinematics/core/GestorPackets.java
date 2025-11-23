package net.viciont.cinematics.core;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import net.viciont.cinematics.ViciontCinematics;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.UUID;

/**
 * Gestor de packets usando ProtocolLib para optimizaciones avanzadas
 * Permite que los jugadores se vean entre ellos durante cinemáticas
 *
 * @author CrissyjuanxD
 */
public class GestorPackets {

    private final ViciontCinematics plugin;
    private final ProtocolManager protocolManager;
    private final boolean disponible;

    public GestorPackets(ViciontCinematics plugin) {
        this.plugin = plugin;

        if (plugin.isProtocolLibDisponible()) {
            this.protocolManager = ProtocolLibrary.getProtocolManager();
            this.disponible = true;
            plugin.getLogger().info("GestorPackets inicializado con ProtocolLib");
        } else {
            this.protocolManager = null;
            this.disponible = false;
            plugin.getLogger().warning("GestorPackets no disponible - ProtocolLib no encontrado");
        }
    }

    /**
     * Cambia el gamemode del jugador solo visualmente (lado cliente)
     */
    public void cambiarGamemodeVisual(Player jugador, GameMode gameMode) {
        if (!disponible) return;

        try {
            PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.GAME_STATE_CHANGE);
            packet.getGameStateIDs().write(0, 3);
            packet.getFloat().write(0, (float) gameMode.getValue());

            protocolManager.sendServerPacket(jugador, packet);
        } catch (Exception e) {
            plugin.getLogger().warning("Error al cambiar gamemode visual: " + e.getMessage());
        }
    }

    /**
     * Mantiene visible a un jugador para otro (evita que se oculte en espectador)
     */
    public void mantenerVisible(Player observador, Player objetivo) {
        if (!disponible) return;

        try {
            observador.showPlayer(plugin, objetivo);
        } catch (Exception e) {
            plugin.getLogger().warning("Error al mantener visible: " + e.getMessage());
        }
    }

    /**
     * Mantiene visibles a todos los jugadores entre ellos
     */
    public void mantenerVisiblesTodos(List<UUID> jugadores) {
        if (!disponible) return;

        org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
            for (UUID uuid1 : jugadores) {
                Player j1 = org.bukkit.Bukkit.getPlayer(uuid1);
                if (j1 == null || !j1.isOnline()) continue;

                for (UUID uuid2 : jugadores) {
                    if (uuid1.equals(uuid2)) continue;

                    Player j2 = org.bukkit.Bukkit.getPlayer(uuid2);
                    if (j2 == null || !j2.isOnline()) continue;

                    mantenerVisible(j1, j2);
                }
            }
        });
    }

    /**
     * Envía un paquete de teleport optimizado
     */
    public void teleportOptimizado(Player jugador, double x, double y, double z, float yaw, float pitch) {
        if (!disponible) {
            org.bukkit.Location loc = new org.bukkit.Location(jugador.getWorld(), x, y, z, yaw, pitch);
            jugador.teleport(loc);
            return;
        }

        try {
            PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.POSITION);

            packet.getDoubles().write(0, x);
            packet.getDoubles().write(1, y);
            packet.getDoubles().write(2, z);

            packet.getFloat().write(0, yaw);
            packet.getFloat().write(1, pitch);

            packet.getIntegers().write(0, 0);

            protocolManager.sendServerPacket(jugador, packet);

            org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
                jugador.teleport(new org.bukkit.Location(jugador.getWorld(), x, y, z, yaw, pitch));
            });

        } catch (Exception e) {
            org.bukkit.Location loc = new org.bukkit.Location(jugador.getWorld(), x, y, z, yaw, pitch);
            jugador.teleport(loc);
        }
    }

    /**
     * Oculta la UI del jugador (hotbar, salud, etc)
     */
    public void ocultarUI(Player jugador, boolean ocultar) {
        if (!disponible) return;

        try {
            jugador.setScoreboard(ocultar ?
                    org.bukkit.Bukkit.getScoreboardManager().getNewScoreboard() :
                    org.bukkit.Bukkit.getScoreboardManager().getMainScoreboard());
        } catch (Exception e) {
            plugin.getLogger().warning("Error al ocultar UI: " + e.getMessage());
        }
    }

    public boolean isDisponible() {
        return disponible;
    }
}
