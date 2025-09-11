package net.viciont.cinematics.core;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Pool para reutilizar objetos Location y reducir la creación de instancias
 */
public class LocationPool {
    private final Map<String, Location> locationCache = new ConcurrentHashMap<>();

    public Location getLocation(World world, double x, double y, double z, float yaw, float pitch) {
        if (world == null) {
            return new Location(null, x, y, z, yaw, pitch);
        }

        String key = world.getName() + ":" + x + ":" + y + ":" + z + ":" + yaw + ":" + pitch;
        return locationCache.computeIfAbsent(key, k ->
                new Location(world, x, y, z, yaw, pitch)
        );
    }

    public void clear() {
        locationCache.clear();
    }
}