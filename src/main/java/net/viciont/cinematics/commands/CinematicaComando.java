package net.viciont.cinematics.commands;

import net.viciont.cinematics.ViciontCinematics;
import net.viciont.cinematics.objects.Cinematica.TipoCinematica;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Comando principal para manejar cinemáticas
 *
 * @author CrissyjuanxD
 */
public class CinematicaComando implements CommandExecutor {

    private final ViciontCinematics plugin;

    public CinematicaComando(ViciontCinematics plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Verificar permisos básicos
        if (!sender.hasPermission("viciont.cinematics.use")) {
            if (sender instanceof Player) {
                plugin.enviarMensaje((Player) sender, "<red>No tienes permisos para usar este comando.");
            } else {
                sender.sendMessage("§cNo tienes permisos para usar este comando.");
            }
            return true;
        }

        if (args.length == 0) {
            mostrarAyuda(sender);
            return true;
        }

        String subcomando = args[0].toLowerCase();

        switch (subcomando) {
            case "help" -> mostrarAyuda(sender);
            case "record" -> {
                if (sender instanceof Player jugador) {
                    if (args.length < 2 || args.length > 3) {
                        plugin.enviarMensaje(jugador, "<red>Uso: /cinematica record <nombre> [normal/parts]");
                        return true;
                    }

                    TipoCinematica tipo = TipoCinematica.NORMAL;
                    if (args.length == 3) {
                        if (args[2].equalsIgnoreCase("parts")) {
                            tipo = TipoCinematica.PARTES;
                        } else if (!args[2].equalsIgnoreCase("normal")) {
                            plugin.enviarMensaje(jugador, "<red>Tipo debe ser 'normal' o 'parts'");
                            return true;
                        }
                    }

                    iniciarGrabacion(jugador, args[1], tipo);
                } else {
                    sender.sendMessage("§cEste comando solo puede ser usado por jugadores.");
                }
            }
            case "stop" -> {
                if (sender instanceof Player jugador) {
                    detenerGrabacion(jugador);
                } else {
                    sender.sendMessage("§cEste comando solo puede ser usado por jugadores.");
                }
            }
            case "static" -> {
                if (sender instanceof Player jugador) {
                    if (args.length < 3) {
                        plugin.enviarMensaje(jugador, "<red>Uso: /cinematica static <nombre> <ticks>");
                        return true;
                    }
                    try {
                        int ticks = Integer.parseInt(args[2]);
                        grabarEstatica(jugador, args[1], ticks);
                    } catch (NumberFormatException e) {
                        plugin.enviarMensaje(jugador, "<red>El número de ticks debe ser un número válido.");
                    }
                } else {
                    sender.sendMessage("§cEste comando solo puede ser usado por jugadores.");
                }
            }
            case "cut" -> {
                if (sender instanceof Player jugador) {
                    cortarGrabacion(jugador);
                } else {
                    sender.sendMessage("§cEste comando solo puede ser usado por jugadores.");
                }
            }
            case "playcut" -> {
                if (sender instanceof Player jugador) {
                    continuarGrabacion(jugador);
                } else {
                    sender.sendMessage("§cEste comando solo puede ser usado por jugadores.");
                }
            }
            case "play" -> {
                if (args.length < 2) {
                    if (sender instanceof Player) {
                        plugin.enviarMensaje((Player) sender, "<red>Uso: /cinematica play <all/me/jugador> [nombres...]");
                    } else {
                        sender.sendMessage("§cUso: /cinematica play <all/me/jugador> [nombres...]");
                    }
                    return true;
                }

                if (args.length < 3 && !args[1].equalsIgnoreCase("all") && !args[1].equalsIgnoreCase("me")) {
                    // Si no es all/me, debe ser un jugador específico
                    Player objetivo = Bukkit.getPlayer(args[1]);
                    if (objetivo == null) {
                        if (sender instanceof Player) {
                            plugin.enviarMensaje((Player) sender, "<red>Jugador no encontrado: " + args[1]);
                        } else {
                            sender.sendMessage("§cJugador no encontrado: " + args[1]);
                        }
                        return true;
                    }
                    if (sender instanceof Player) {
                        plugin.enviarMensaje((Player) sender, "<red>Especifica las cinemáticas a reproducir");
                    } else {
                        sender.sendMessage("§cEspecifica las cinemáticas a reproducir");
                    }
                    return true;
                }

                String[] nombres;
                if (args.length == 2 && (args[1].equalsIgnoreCase("all") || args[1].equalsIgnoreCase("me"))) {
                    // Sin nombres específicos, mostrar error
                    if (sender instanceof Player) {
                        plugin.enviarMensaje((Player) sender, "<red>Especifica las cinemáticas a reproducir");
                    } else {
                        sender.sendMessage("§cEspecifica las cinemáticas a reproducir");
                    }
                    return true;
                } else {
                    nombres = new String[args.length - 2];
                    System.arraycopy(args, 2, nombres, 0, nombres.length);
                }

                reproducir(sender, args[1], nombres);
            }
            case "forcestop" -> {
                if (!sender.hasPermission("viciont.cinematics.admin")) {
                    if (sender instanceof Player) {
                        plugin.enviarMensaje((Player) sender, "<red>No tienes permisos de administrador.");
                    } else {
                        sender.sendMessage("§cNo tienes permisos de administrador.");
                    }
                    return true;
                }

                if (args.length < 2) {
                    if (sender instanceof Player) {
                        plugin.enviarMensaje((Player) sender, "<red>Uso: /cinematica forcestop <all/jugador>");
                    } else {
                        sender.sendMessage("§cUso: /cinematica forcestop <all/jugador>");
                    }
                    return true;
                }

                forzarDetenerCinematica(sender, args[1]);
            }
            case "list" -> listarCinematicas(sender);
            case "delete" -> {
                if (!sender.hasPermission("viciont.cinematics.admin")) {
                    if (sender instanceof Player) {
                        plugin.enviarMensaje((Player) sender, "<red>No tienes permisos de administrador.");
                    } else {
                        sender.sendMessage("§cNo tienes permisos de administrador.");
                    }
                    return true;
                }
                if (args.length < 2) {
                    if (sender instanceof Player) {
                        plugin.enviarMensaje((Player) sender, "<red>Uso: /cinematica delete <nombre>");
                    } else {
                        sender.sendMessage("§cUso: /cinematica delete <nombre>");
                    }
                    return true;
                }
                eliminarCinematica(sender, args[1]);
            }
            case "clone" -> {
                if (!sender.hasPermission("viciont.cinematics.admin")) {
                    if (sender instanceof Player) {
                        plugin.enviarMensaje((Player) sender, "<red>No tienes permisos de administrador.");
                    } else {
                        sender.sendMessage("§cNo tienes permisos de administrador.");
                    }
                    return true;
                }
                if (args.length < 3) {
                    if (sender instanceof Player) {
                        plugin.enviarMensaje((Player) sender, "<red>Uso: /cinematica clone <original> <nuevo>");
                    } else {
                        sender.sendMessage("§cUso: /cinematica clone <original> <nuevo>");
                    }
                    return true;
                }
                clonarCinematica(sender, args[1], args[2]);
            }
            case "reload" -> {
                if (!sender.hasPermission("viciont.cinematics.admin")) {
                    if (sender instanceof Player) {
                        plugin.enviarMensaje((Player) sender, "<red>No tienes permisos de administrador.");
                    } else {
                        sender.sendMessage("§cNo tienes permisos de administrador.");
                    }
                    return true;
                }
                recargarPlugin(sender);
            }
            default -> {
                if (sender instanceof Player) {
                    plugin.enviarMensaje((Player) sender, "<red>Subcomando desconocido. Usa <white>/cinematica help<red> para ver los comandos disponibles.");
                } else {
                    sender.sendMessage("§cSubcomando desconocido. Usa /cinematica help para ver los comandos disponibles.");
                }
            }
        }

        return true;
    }

    private void mostrarAyuda(CommandSender sender) {
        sender.sendMessage("§6§l=== Viciont Cinematics - Ayuda ===");
        sender.sendMessage("§e/cinematica record <nombre> [normal/parts] §7- Inicia grabación");
        sender.sendMessage("§e/cinematica stop §7- Para la grabación actual");
        sender.sendMessage("§e/cinematica static <nombre> <ticks> §7- Graba estática");
        sender.sendMessage("§e/cinematica play <all/me/jugador> <nombres...> §7- Reproduce");
        sender.sendMessage("§e/cinematica cut §7- Corta grabación por partes");
        sender.sendMessage("§e/cinematica playcut §7- Continúa grabación por partes");
        sender.sendMessage("§e/cinematica list §7- Lista cinemáticas");
        sender.sendMessage("§e/cinematica delete <nombre> §7- Elimina cinemática");
        sender.sendMessage("§e/cinematica clone <original> <nuevo> §7- Clona cinemática");
        sender.sendMessage("§e/cinematica forcestop <all/jugador> §7- Fuerza fin");
        sender.sendMessage("§e/cinematica reload §7- Recarga configuración");
        sender.sendMessage("§a§lOptimizado para 60 FPS y 100+ jugadores");
    }

    private void iniciarGrabacion(Player jugador, String nombre, TipoCinematica tipo) {
        var gestor = plugin.getGestorCinematicas();

        if (gestor.iniciarGrabacion(jugador, nombre, tipo)) {
            String tipoTexto = tipo == TipoCinematica.PARTES ? "por partes" : "normal";
            plugin.enviarMensaje(jugador, "<green>¡Grabación " + tipoTexto + " iniciada para '<white>" + nombre + "<green>'!");

            if (tipo == TipoCinematica.PARTES) {
                plugin.enviarMensaje(jugador, "<yellow>Grabando parte 1. Usa <white>/cinematica cut<yellow> para cortar y <white>/cinematica playcut<yellow> para continuar.");
            } else {
                plugin.enviarMensaje(jugador, "<yellow>Muévete para grabar la cinemática. Usa <white>/cinematica stop<yellow> para finalizar.");
            }
        } else {
            plugin.enviarMensaje(jugador, "<red>No se pudo iniciar la grabación. Verifica que no estés grabando ya o que el nombre no exista.");
        }
    }

    private void detenerGrabacion(Player jugador) {
        var gestor = plugin.getGestorCinematicas();

        if (gestor.detenerGrabacion(jugador)) {
            plugin.enviarMensaje(jugador, "<green>¡Grabación finalizada y guardada correctamente!");
        } else {
            plugin.enviarMensaje(jugador, "<red>No tienes ninguna grabación activa.");
        }
    }

    private void cortarGrabacion(Player jugador) {
        var gestor = plugin.getGestorCinematicas();

        if (gestor.cortarGrabacion(jugador)) {
            plugin.enviarMensaje(jugador, "<yellow>Parte cortada. Muévete a otra ubicación y usa <white>/cinematica playcut<yellow> para continuar.");
        } else {
            plugin.enviarMensaje(jugador, "<red>No estás grabando una cinemática por partes o ya está cortada.");
        }
    }

    private void continuarGrabacion(Player jugador) {
        var gestor = plugin.getGestorCinematicas();

        if (gestor.continuarGrabacion(jugador)) {
            plugin.enviarMensaje(jugador, "<green>Continuando grabación de nueva parte. Muévete para grabar.");
        } else {
            plugin.enviarMensaje(jugador, "<red>No puedes continuar la grabación. Verifica que hayas cortado una parte primero.");
        }
    }

    private void grabarEstatica(Player jugador, String nombre, int duracionTicks) {
        var gestor = plugin.getGestorCinematicas();

        if (duracionTicks <= 0 || duracionTicks > 12000) { // Máximo 10 minutos
            plugin.enviarMensaje(jugador, "<red>La duración debe ser entre 1 y 12000 ticks (10 minutos).");
            return;
        }

        if (gestor.grabarEstatica(jugador, nombre, duracionTicks)) {
            plugin.enviarMensaje(jugador, "<green>¡Cinemática estática '<white>" + nombre + "<green>' creada con duración de <white>" + duracionTicks + "<green> ticks!");
        } else {
            plugin.enviarMensaje(jugador, "<red>No se pudo crear la cinemática. Verifica que el nombre no exista.");
        }
    }

    private void reproducir(CommandSender sender, String alcance, String... nombres) {
        if (nombres.length == 0) {
            if (sender instanceof Player) {
                plugin.enviarMensaje((Player) sender, "<red>Debes especificar al menos una cinemática para reproducir.");
            } else {
                sender.sendMessage("§cDebes especificar al menos una cinemática para reproducir.");
            }
            return;
        }

        var gestor = plugin.getGestorCinematicas();
        List<UUID> jugadores;

        if (alcance.equalsIgnoreCase("all")) {
            jugadores = Bukkit.getOnlinePlayers().stream()
                    .map(Player::getUniqueId)
                    .toList();
        } else if (alcance.equalsIgnoreCase("me") && sender instanceof Player) {
            jugadores = List.of(((Player) sender).getUniqueId());
        } else {
            // Buscar jugador específico
            Player objetivo = Bukkit.getPlayer(alcance);
            if (objetivo == null) {
                if (sender instanceof Player) {
                    plugin.enviarMensaje((Player) sender, "<red>Jugador no encontrado: " + alcance);
                } else {
                    sender.sendMessage("§cJugador no encontrado: " + alcance);
                }
                return;
            }
            jugadores = List.of(objetivo.getUniqueId());
        }

        if (gestor.reproducir(jugadores, nombres)) {
            String mensaje;
            if (alcance.equalsIgnoreCase("all")) {
                mensaje = "Reproduciendo cinemáticas para todos los jugadores";
            } else if (alcance.equalsIgnoreCase("me")) {
                mensaje = "Reproduciendo cinemáticas para ti";
            } else {
                mensaje = "Reproduciendo cinemáticas para " + alcance;
            }

            if (sender instanceof Player) {
                plugin.enviarMensaje((Player) sender, "<green>" + mensaje + ": <white>" + String.join(", ", nombres));
            } else {
                sender.sendMessage("§a" + mensaje + ": §f" + String.join(", ", nombres));
            }
        } else {
            if (sender instanceof Player) {
                plugin.enviarMensaje((Player) sender, "<red>Error al reproducir las cinemáticas. Verifica que todas existan.");
            } else {
                sender.sendMessage("§cError al reproducir las cinemáticas. Verifica que todas existan.");
            }
        }
    }

    private void forzarDetenerCinematica(CommandSender sender, String objetivo) {
        var gestor = plugin.getGestorCinematicas();
        List<UUID> jugadores;

        if (objetivo.equalsIgnoreCase("all")) {
            jugadores = Bukkit.getOnlinePlayers().stream()
                    .map(Player::getUniqueId)
                    .collect(java.util.stream.Collectors.toList());
        } else {
            Player jugador = Bukkit.getPlayer(objetivo);
            if (jugador == null) {
                if (sender instanceof Player) {
                    plugin.enviarMensaje((Player) sender, "<red>Jugador no encontrado: " + objetivo);
                } else {
                    sender.sendMessage("§cJugador no encontrado: " + objetivo);
                }
                return;
            }
            jugadores = new ArrayList<>();
            jugadores.add(jugador.getUniqueId());
        }

        if (gestor.forzarFinCinematica(jugadores)) {
            String mensaje = objetivo.equalsIgnoreCase("all") ?
                    "Cinemáticas detenidas para todos los jugadores" :
                    "Cinemática detenida para " + objetivo;
            if (sender instanceof Player) {
                plugin.enviarMensaje((Player) sender, "<green>" + mensaje);
            } else {
                sender.sendMessage("§a" + mensaje);
            }
        } else {
            String mensaje = objetivo.equalsIgnoreCase("all") ?
                    "No hay cinemáticas activas para detener" :
                    "El jugador " + objetivo + " no tiene cinemáticas activas";
            if (sender instanceof Player) {
                plugin.enviarMensaje((Player) sender, "<yellow>" + mensaje);
            } else {
                sender.sendMessage("§e" + mensaje);
            }
        }
    }

    private void listarCinematicas(CommandSender sender) {
        var cinematicas = plugin.getGestorCinematicas().getCinematicas();

        if (cinematicas.isEmpty()) {
            if (sender instanceof Player) {
                plugin.enviarMensaje((Player) sender, "<yellow>No hay cinemáticas creadas aún.");
            } else {
                sender.sendMessage("§eNo hay cinemáticas creadas aún.");
            }
            return;
        }

        if (sender instanceof Player) {
            plugin.enviarMensaje((Player) sender, "<gold>§l=== Cinemáticas Disponibles ===");
        } else {
            sender.sendMessage("§6§l=== Cinemáticas Disponibles ===");
        }
        cinematicas.forEach((nombre, cinematica) -> {
            String info = "• " + nombre + " (" +
                    String.format("%.1f", cinematica.getDuracionSegundos()) + "s, " +
                    cinematica.getFrames().size() + " frames)";
            if (sender instanceof Player) {
                plugin.enviarMensaje((Player) sender, "<yellow>" + info);
            } else {
                sender.sendMessage("§e" + info);
            }
        });
    }

    private void eliminarCinematica(CommandSender sender, String nombre) {
        var gestor = plugin.getGestorCinematicas();
        var cinematicas = gestor.getCinematicas();

        if (cinematicas.containsKey(nombre)) {
            // Remover de la instancia real del gestor
            gestor.eliminarCinematica(nombre);
            plugin.guardarConfiguracion();
            if (sender instanceof Player) {
                plugin.enviarMensaje((Player) sender, "<green>Cinemática '<white>" + nombre + "<green>' eliminada correctamente.");
            } else {
                sender.sendMessage("§aCinemática '" + nombre + "' eliminada correctamente.");
            }
        } else {
            if (sender instanceof Player) {
                plugin.enviarMensaje((Player) sender, "<red>La cinemática '<white>" + nombre + "<red>' no existe.");
            } else {
                sender.sendMessage("§cLa cinemática '" + nombre + "' no existe.");
            }
        }
    }

    private void clonarCinematica(CommandSender sender, String original, String nuevo) {
        var gestor = plugin.getGestorCinematicas();
        var cinematicas = gestor.getCinematicas();

        if (!cinematicas.containsKey(original)) {
            if (sender instanceof Player) {
                plugin.enviarMensaje((Player) sender, "<red>La cinemática original '<white>" + original + "<red>' no existe.");
            } else {
                sender.sendMessage("§cLa cinemática original '" + original + "' no existe.");
            }
            return;
        }

        if (cinematicas.containsKey(nuevo)) {
            if (sender instanceof Player) {
                plugin.enviarMensaje((Player) sender, "<red>Ya existe una cinemática con el nombre '<white>" + nuevo + "<red>'.");
            } else {
                sender.sendMessage("§cYa existe una cinemática con el nombre '" + nuevo + "'.");
            }
            return;
        }

        var cinematicaOriginal = cinematicas.get(original);
        var cinematicaClonada = new net.viciont.cinematics.objects.Cinematica(
                nuevo,
                new java.util.ArrayList<>(cinematicaOriginal.getFrames()),
                new java.util.HashMap<>(cinematicaOriginal.getEventosProgramados())
        );

        // Añadir al gestor real
        gestor.agregarCinematica(nuevo, cinematicaClonada);
        plugin.guardarConfiguracion();

        if (sender instanceof Player) {
            plugin.enviarMensaje((Player) sender, "<green>Cinemática '<white>" + original + "<green>' clonada como '<white>" + nuevo + "<green>'.");
        } else {
            sender.sendMessage("§aCinemática '" + original + "' clonada como '" + nuevo + "'.");
        }
    }

    private void recargarPlugin(CommandSender sender) {
        try {
            plugin.cargarConfiguracion();
            plugin.getGestorCinematicas().recargarConfiguracionInterpolacion();
            if (sender instanceof Player) {
                plugin.enviarMensaje((Player) sender, "<green>¡Configuración recargada correctamente!");
            } else {
                sender.sendMessage("§a¡Configuración recargada correctamente!");
            }
        } catch (Exception e) {
            if (sender instanceof Player) {
                plugin.enviarMensaje((Player) sender, "<red>Error al recargar la configuración: " + e.getMessage());
            } else {
                sender.sendMessage("§cError al recargar la configuración: " + e.getMessage());
            }
        }
    }

    private void mostrarInfoInterpolacion(Player jugador) {
        var gestor = plugin.getGestorCinematicas();
        var interpolador = gestor.getInterpolador();

        plugin.enviarMensaje(jugador, "<gold>§l=== Sistema de Interpolación ===");
        plugin.enviarMensaje(jugador, "<yellow>Estado: " + (gestor.isInterpolacionHabilitada() ? "<green>Habilitado" : "<red>Deshabilitado"));
        plugin.enviarMensaje(jugador, "<yellow>FPS Objetivo: <white>" + gestor.getFpsObjetivo());
        plugin.enviarMensaje(jugador, "<yellow>Tipo: <white>" + interpolador.getTipo());
        plugin.enviarMensaje(jugador, "<yellow>Suavizado de Rotación: " + (interpolador.isSuavizadoRotacion() ? "<green>Sí" : "<red>No"));
        plugin.enviarMensaje(jugador, "<yellow>Factor de Suavizado: <white>" + String.format("%.2f", interpolador.getFactorSuavizado()));
        plugin.enviarMensaje(jugador, "<yellow>Sistema Legacy: " + (gestor.isUsarSistemaLegacy() ? "<red>Activo" : "<green>Inactivo"));
        plugin.enviarMensaje(jugador, "");
        plugin.enviarMensaje(jugador, "<gray>Configura en config.yml y usa <white>/cinematica reload<gray> para aplicar");
    }

    private void mostrarInfoInterpolacionConsola(CommandSender sender) {
        var gestor = plugin.getGestorCinematicas();
        var interpolador = gestor.getInterpolador();

        sender.sendMessage("§6§l=== Sistema de Interpolación ===");
        sender.sendMessage("§eEstado: " + (gestor.isInterpolacionHabilitada() ? "§aHabilitado" : "§cDeshabilitado"));
        sender.sendMessage("§eFPS Objetivo: §f" + gestor.getFpsObjetivo());
        sender.sendMessage("§eTipo: §f" + interpolador.getTipo());
        sender.sendMessage("§eSuavizado de Rotación: " + (interpolador.isSuavizadoRotacion() ? "§aSí" : "§cNo"));
        sender.sendMessage("§eFactor de Suavizado: §f" + String.format("%.2f", interpolador.getFactorSuavizado()));
        sender.sendMessage("§eSistema Legacy: " + (gestor.isUsarSistemaLegacy() ? "§cActivo" : "§aInactivo"));
        sender.sendMessage("");
        sender.sendMessage("§7Configura en config.yml y usa /cinematica reload para aplicar");
    }
}