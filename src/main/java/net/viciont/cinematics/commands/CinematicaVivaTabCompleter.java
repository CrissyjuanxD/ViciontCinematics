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
 * Auto-completado para el comando de cinemáticas en vivo
 *
 * @author CrissyjuanxD
 */
public class CinematicaVivaTabCompleter implements TabCompleter {

    private final ViciontCinematics plugin;

    public CinematicaVivaTabCompleter(ViciontCinematics plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (!sender.hasPermission("viciont.cinematics.live")) {
            return completions;
        }

        if (args.length == 1) {
            // Subcomandos principales
            List<String> subcomandos = Arrays.asList(
                    "help", "start", "add", "remove", "range", "stop", "status"
            );

            // Añadir lista solo si tiene permisos de admin
            if (sender.hasPermission("viciont.cinematics.admin")) {
                subcomandos = new ArrayList<>(subcomandos);
                subcomandos.add("list");
            }

            for (String subcomando : subcomandos) {
                if (subcomando.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(subcomando);
                }
            }
        } else if (args.length == 2) {
            String subcomando = args[0].toLowerCase();

            switch (subcomando) {
                case "add", "remove" -> {
                    // Nombres de jugadores conectados
                    for (Player jugador : Bukkit.getOnlinePlayers()) {
                        if (jugador.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                            completions.add(jugador.getName());
                        }
                    }
                }
                case "range" -> {
                    // Sugerencias de radio
                    List<String> radios = Arrays.asList("5", "10", "15", "20", "30", "50");
                    for (String radio : radios) {
                        if (radio.startsWith(args[1])) {
                            completions.add(radio);
                        }
                    }
                }
            }
        } else if (args.length == 3) {
            String subcomando = args[0].toLowerCase();

            if (subcomando.equals("add") || subcomando.equals("range")) {
                // Opción forzar
                if ("true".startsWith(args[2].toLowerCase())) {
                    completions.add("true");
                }
                if ("false".startsWith(args[2].toLowerCase())) {
                    completions.add("false");
                }
            }
        }

        return completions;
    }
}