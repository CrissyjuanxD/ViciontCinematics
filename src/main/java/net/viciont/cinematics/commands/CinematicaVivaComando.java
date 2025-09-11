package net.viciont.cinematics.commands;

import net.viciont.cinematics.ViciontCinematics;
import net.viciont.cinematics.core.GestorCinematicasVivas;
import net.viciont.cinematics.objects.CinematicaViva;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Comandos para manejar cinemáticas en vivo
 *
 * @author CrissyjuanxD
 */
public class CinematicaVivaComando implements CommandExecutor {

    private final ViciontCinematics plugin;
    private final GestorCinematicasVivas gestorVivas;

    public CinematicaVivaComando(ViciontCinematics plugin) {
        this.plugin = plugin;
        this.gestorVivas = new GestorCinematicasVivas(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Verificar permisos
        if (!sender.hasPermission("viciont.cinematics.live")) {
            plugin.enviarMensaje((Player) sender, "<red>No tienes permisos para usar cinemáticas en vivo.");
            return true;
        }

        if (!(sender instanceof Player jugador)) {
            sender.sendMessage("§cEste comando solo puede ser usado por jugadores.");
            return true;
        }

        if (args.length == 0) {
            mostrarAyuda(jugador);
            return true;
        }

        String subcomando = args[0].toLowerCase();

        switch (subcomando) {
            case "help" -> mostrarAyuda(jugador);
            case "start" -> iniciarCinematicaViva(jugador);
            case "add" -> {
                if (args.length < 2) {
                    plugin.enviarMensaje(jugador, "<red>Uso: /cinematica-viva add <jugador> [force]");
                    return true;
                }
                boolean forzar = args.length > 2 && args[2].equalsIgnoreCase("true");
                agregarJugador(jugador, args[1], forzar);
            }
            case "remove" -> {
                if (args.length < 2) {
                    plugin.enviarMensaje(jugador, "<red>Uso: /cinematica-viva remove <jugador>");
                    return true;
                }
                removerJugador(jugador, args[1]);
            }
            case "range" -> {
                if (args.length < 2) {
                    plugin.enviarMensaje(jugador, "<red>Uso: /cinematica-viva range <radio> [force]");
                    return true;
                }
                try {
                    int radio = Integer.parseInt(args[1]);
                    boolean forzar = args.length > 2 && args[2].equalsIgnoreCase("true");
                    agregarPorRango(jugador, radio, forzar);
                } catch (NumberFormatException e) {
                    plugin.enviarMensaje(jugador, "<red>El radio debe ser un número válido.");
                }
            }
            case "stop" -> pararCinematicaViva(jugador);
            case "status" -> mostrarEstado(jugador);
            case "list" -> {
                if (!sender.hasPermission("viciont.cinematics.admin")) {
                    plugin.enviarMensaje(jugador, "<red>No tienes permisos de administrador.");
                    return true;
                }
                listarCinematicasVivas(jugador);
            }
            default -> plugin.enviarMensaje(jugador, "<red>Subcomando desconocido. Usa <white>/cinematica-viva help<red> para ver los comandos disponibles.");
        }

        return true;
    }

    public void mostrarAyuda(Player jugador) {
        plugin.enviarMensaje(jugador, "<gold>§l=== Cinemáticas en Vivo - Ayuda ===");
        plugin.enviarMensaje(jugador, "<yellow>/cinematica-viva start <gray>- Inicia una cinemática en vivo");
        plugin.enviarMensaje(jugador, "<yellow>/cinematica-viva add <jugador> <gray>- Añade un jugador");
        plugin.enviarMensaje(jugador, "<yellow>/cinematica-viva remove <jugador> <gray>- Remueve un jugador");
        plugin.enviarMensaje(jugador, "<yellow>/cinematica-viva range <radio> <gray>- Añade jugadores en un radio");
        plugin.enviarMensaje(jugador, "<yellow>/cinematica-viva stop <gray>- Detiene la cinemática en vivo");
        plugin.enviarMensaje(jugador, "<yellow>/cinematica-viva status <gray>- Muestra el estado actual");
        plugin.enviarMensaje(jugador, "<gray>Las cinemáticas en vivo permiten que otros jugadores te sigan en tiempo real");
    }

    public void iniciarCinematicaViva(Player jugador) {
        CinematicaViva cinematicaViva = gestorVivas.crearCinematicaViva(jugador);

        if (cinematicaViva != null) {
            plugin.enviarMensaje(jugador, "<green>¡Cinemática en vivo iniciada!");
            plugin.enviarMensaje(jugador, "<yellow>Otros jugadores pueden unirse usando:");
            plugin.enviarMensaje(jugador, "<white>/cinematica-viva add <jugador>");
            plugin.enviarMensaje(jugador, "<gray>Muévete y otros jugadores te seguirán automáticamente");
        } else {
            plugin.enviarMensaje(jugador, "<red>No se pudo iniciar la cinemática en vivo.");
            plugin.enviarMensaje(jugador, "<red>Verifica que no tengas ya una activa o que no estés en otra cinemática.");
        }
    }

    public void agregarJugador(Player anfitrion, String nombreJugador, Boolean forzar) {
        CinematicaViva cinematicaViva = gestorVivas.obtenerCinematicaViva(anfitrion);

        if (cinematicaViva == null) {
            plugin.enviarMensaje(anfitrion, "<red>No tienes una cinemática en vivo activa.");
            plugin.enviarMensaje(anfitrion, "<yellow>Usa <white>/cinematica-viva start<yellow> para comenzar una.");
            return;
        }

        Player objetivo = Bukkit.getPlayer(nombreJugador);
        if (objetivo == null) {
            plugin.enviarMensaje(anfitrion, "<red>El jugador '<white>" + nombreJugador + "<red>' no está conectado.");
            return;
        }

        if (objetivo.equals(anfitrion)) {
            plugin.enviarMensaje(anfitrion, "<red>No puedes agregarte a ti mismo.");
            return;
        }

        if (cinematicaViva.agregarEspectador(objetivo, forzar)) {
            plugin.enviarMensaje(anfitrion, "<green>¡Jugador <white>" + objetivo.getName() + "<green> agregado a la cinemática!");
            plugin.enviarMensaje(objetivo, "<purple>¡Te has unido a la cinemática en vivo de <white>" + anfitrion.getName() + "<purple>!");
            plugin.enviarMensaje(objetivo, "<gray>Ahora seguirás automáticamente sus movimientos");
        } else {
            plugin.enviarMensaje(anfitrion, "<red>No se pudo agregar al jugador. Puede que ya esté en una cinemática.");
        }
    }

    public void removerJugador(Player anfitrion, String nombreJugador) {
        CinematicaViva cinematicaViva = gestorVivas.obtenerCinematicaViva(anfitrion);

        if (cinematicaViva == null) {
            plugin.enviarMensaje(anfitrion, "<red>No tienes una cinemática en vivo activa.");
            return;
        }

        Player objetivo = Bukkit.getPlayer(nombreJugador);
        if (objetivo == null) {
            plugin.enviarMensaje(anfitrion, "<red>El jugador '<white>" + nombreJugador + "<red>' no está conectado.");
            return;
        }

        if (cinematicaViva.removerEspectador(objetivo)) {
            plugin.enviarMensaje(anfitrion, "<yellow>Jugador <white>" + objetivo.getName() + "<yellow> removido de la cinemática.");
            plugin.enviarMensaje(objetivo, "<yellow>Has salido de la cinemática en vivo de <white>" + anfitrion.getName());
        } else {
            plugin.enviarMensaje(anfitrion, "<red>El jugador no estaba en tu cinemática en vivo.");
        }
    }

    public void agregarPorRango(Player anfitrion, int radio, Boolean forzar) {
        if (radio <= 0 || radio > 100) {
            plugin.enviarMensaje(anfitrion, "<red>El radio debe ser entre 1 y 100 bloques.");
            return;
        }

        CinematicaViva cinematicaViva = gestorVivas.obtenerCinematicaViva(anfitrion);

        if (cinematicaViva == null) {
            plugin.enviarMensaje(anfitrion, "<red>No tienes una cinemática en vivo activa.");
            return;
        }

        List<Player> jugadoresCercanos = anfitrion.getNearbyEntities(radio, radio, radio)
                .stream()
                .filter(entidad -> entidad instanceof Player)
                .map(entidad -> (Player) entidad)
                .filter(jugador -> !jugador.equals(anfitrion))
                .toList();

        if (jugadoresCercanos.isEmpty()) {
            plugin.enviarMensaje(anfitrion, "<yellow>No hay jugadores en un radio de " + radio + " bloques.");
            return;
        }

        int agregados = 0;
        for (Player jugador : jugadoresCercanos) {
            if (cinematicaViva.agregarEspectador(jugador, forzar)) {
                agregados++;
                plugin.enviarMensaje(jugador, "<purple>¡Te has unido a la cinemática en vivo de <white>" + anfitrion.getName() + "<purple>!");
            }
        }

        plugin.enviarMensaje(anfitrion, "<green>Se agregaron <white>" + agregados + "<green> jugadores de <white>" + jugadoresCercanos.size() + "<green> encontrados.");
    }

    public void pararCinematicaViva(Player jugador) {
        if (gestorVivas.detenerCinematicaViva(jugador)) {
            plugin.enviarMensaje(jugador, "<green>¡Cinemática en vivo finalizada!");
        } else {
            plugin.enviarMensaje(jugador, "<red>No tienes una cinemática en vivo activa.");
        }
    }

    public void mostrarEstado(Player jugador) {
        CinematicaViva cinematicaViva = gestorVivas.obtenerCinematicaViva(jugador);

        if (cinematicaViva == null) {
            plugin.enviarMensaje(jugador, "<yellow>No tienes una cinemática en vivo activa.");
            return;
        }

        List<Player> espectadores = cinematicaViva.obtenerEspectadores();
        long duracion = cinematicaViva.obtenerDuracion();

        plugin.enviarMensaje(jugador, "<gold>§l=== Estado de Cinemática en Vivo ===");
        plugin.enviarMensaje(jugador, "<yellow>Anfitrión: <white>" + jugador.getName());
        plugin.enviarMensaje(jugador, "<yellow>Espectadores: <white>" + espectadores.size());
        plugin.enviarMensaje(jugador, "<yellow>Duración: <white>" + (duracion / 1000) + " segundos");
        plugin.enviarMensaje(jugador, "<yellow>Estado: <green>Activa");

        if (!espectadores.isEmpty()) {
            plugin.enviarMensaje(jugador, "<yellow>Jugadores siguiéndote:");
            for (Player espectador : espectadores) {
                plugin.enviarMensaje(jugador, "<gray>  • <white>" + espectador.getName());
            }
        }
    }

    public void listarCinematicasVivas(Player jugador) {
        List<CinematicaViva> cinematicasActivas = gestorVivas.obtenerCinematicasActivas();

        if (cinematicasActivas.isEmpty()) {
            plugin.enviarMensaje(jugador, "<yellow>No hay cinemáticas en vivo activas.");
            return;
        }

        plugin.enviarMensaje(jugador, "<gold>§l=== Cinemáticas en Vivo Activas ===");
        for (CinematicaViva cinematica : cinematicasActivas) {
            Player anfitrion = cinematica.obtenerAnfitrion();
            if (anfitrion != null) {
                plugin.enviarMensaje(jugador, "<yellow>• <white>" + anfitrion.getName() +
                        " <gray>(" + cinematica.obtenerEspectadores().size() + " espectadores)");
            }
        }
    }
}