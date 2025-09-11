package net.viciont.cinematics.commands;

import net.viciont.cinematics.ViciontCinematics;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Auto-completado para el comando principal de cinemáticas
 *
 * @author CrissyjuanxD
 */
public class CinematicaTabCompleter implements TabCompleter {

    private final ViciontCinematics plugin;

    public CinematicaTabCompleter(ViciontCinematics plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Subcomandos principales
            List<String> subcomandos = Arrays.asList(
                    "help", "gui", "record", "stop", "cut", "playcut", "static",
                    "play", "list", "delete", "clone", "config", "reload", "forcestop"
            );

            for (String subcomando : subcomandos) {
                if (subcomando.toLowerCase().startsWith(args[0].toLowerCase())) {
                    // Verificar permisos
                    if (subcomando.equals("delete") || subcomando.equals("clone") ||
                            subcomando.equals("config") || subcomando.equals("reload")) {
                        if (sender.hasPermission("viciont.cinematics.admin")) {
                            completions.add(subcomando);
                        }
                    } else if (subcomando.equals("gui")) {
                        if (sender.hasPermission("viciont.cinematics.gui")) {
                            completions.add(subcomando);
                        }
                        if (sender.hasPermission("viciont.cinematics.admin")) {
                            completions.add(subcomando);
                        }
                    } else if (subcomando.equals("forcestop")) {
                    } else if (sender.hasPermission("viciont.cinematics.use")) {
                        completions.add(subcomando);
                    }
                }
            }
        } else if (args.length == 2) {
            String subcomando = args[0].toLowerCase();

            switch (subcomando) {
                case "play" -> {
                    // Opciones de alcance
                    if ("all".startsWith(args[1].toLowerCase())) {
                        completions.add("all");
                    }
                    if ("me".startsWith(args[1].toLowerCase())) {
                        completions.add("me");
                    }
                    // Nombres de jugadores
                    for (Player jugador : Bukkit.getOnlinePlayers()) {
                        if (jugador.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                            completions.add(jugador.getName());
                        }
                    }
                }
                // Nombres de jugadores
                case "delete", "clone" -> {
                    // Nombres de cinemáticas existentes
                    if (sender.hasPermission("viciont.cinematics.admin")) {
                        for (String nombre : plugin.getGestorCinematicas().getCinematicas().keySet()) {
                            if (nombre.toLowerCase().startsWith(args[1].toLowerCase())) {
                                completions.add(nombre);
                            }
                        }
                    }
                }
                case "record" -> {
                    // Sugerencia de nombre
                    completions.add("<nombre>");
                }
                case "static" -> {
                    // Sugerencia de nombre
                    completions.add("<nombre>");
                }
                case "forcestop" -> {
                    if ("all".startsWith(args[1].toLowerCase())) {
                        completions.add("all");
                    }
                    // Nombres de jugadores
                    for (org.bukkit.entity.Player jugador : org.bukkit.Bukkit.getOnlinePlayers()) {
                        if (jugador.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                            completions.add(jugador.getName());
                        }
                    }
                }
                case "addsound" -> {
                    if (args.length == 2) {
                        // Nombres de cinemáticas
                        for (String nombre : plugin.getGestorCinematicas().getCinematicas().keySet()) {
                            if (nombre.toLowerCase().startsWith(args[1].toLowerCase())) {
                                completions.add(nombre);
                            }
                        }
                    } else if (args.length == 3) {
                        // Sugerencias de tick
                        completions.addAll(Arrays.asList("0", "20", "40", "60", "100", "200"));
                    } else if (args.length == 4) {
                        // Sugerencias de sonidos (presets del config)
                        completions.addAll(Arrays.asList(
                                "pling", "bell", "chime", "harp", "bass",
                                "transition_tp", "transition_whoosh",
                                "ambient_cave", "custom_wait1", "custom_transition",
                                "minecraft:block.note_block.pling", "dtools3:tools.transitions.tp"
                        ));
                    } else if (args.length == 5) {
                        // Volumen
                        completions.addAll(Arrays.asList("0.5", "1.0", "1.5"));
                    } else if (args.length == 6) {
                        // Pitch
                        completions.addAll(Arrays.asList("0.5", "1.0", "1.5", "2.0"));
                    }
                }
            }
        } else if (args.length == 3) {
            String subcomando = args[0].toLowerCase();

            switch (subcomando) {
                case "record" -> {
                    // Tipo de grabación
                    if ("normal".startsWith(args[2].toLowerCase())) {
                        completions.add("normal");
                    }
                    if ("parts".startsWith(args[2].toLowerCase())) {
                        completions.add("parts");
                    }
                }
                case "play" -> {
                    // Nombres de cinemáticas para reproducir
                    for (String nombre : plugin.getGestorCinematicas().getCinematicas().keySet()) {
                        if (nombre.toLowerCase().startsWith(args[2].toLowerCase())) {
                            completions.add(nombre);
                        }
                    }
                }
                case "clone" -> {
                    // Nuevo nombre para el clon
                    if (sender.hasPermission("viciont.cinematics.admin")) {
                        completions.add("<nuevo_nombre>");
                    }
                }
                case "static" -> {
                    // Duración en ticks
                    completions.add("<ticks>");
                }
            }
        } else if (args.length > 3 && args[0].equalsIgnoreCase("play")) {
            // Más cinemáticas para reproducir
            for (String nombre : plugin.getGestorCinematicas().getCinematicas().keySet()) {
                if (nombre.toLowerCase().startsWith(args[args.length - 1].toLowerCase())) {
                    completions.add(nombre);
                }
            }
        }

        return completions;
    }
}