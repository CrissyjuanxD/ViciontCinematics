package net.viciont.cinematics.gui;

import net.viciont.cinematics.ViciontCinematics;
import net.viciont.cinematics.objects.Cinematica;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * Gestor de interfaces gráficas para el plugin
 *
 * @author CrissyjuanxD
 */
public class GestorGUI implements Listener {

    private final ViciontCinematics plugin;
    private final Map<UUID, String> inventariosAbiertos = new HashMap<>();
    private final Map<UUID, String> esperandoInput = new HashMap<>();
    private final Map<UUID, String> contextosInput = new HashMap<>();

    public GestorGUI(ViciontCinematics plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Abre el menú principal de cinemáticas
     */
    public void abrirMenuPrincipal(Player jugador) {
        Inventory inventario = Bukkit.createInventory(null, 54, "§6§lViciont Cinematics - Menú Principal");

        // Botón para crear nueva cinemática
        ItemStack crearItem = new ItemStack(Material.EMERALD);
        ItemMeta crearMeta = crearItem.getItemMeta();
        crearMeta.setDisplayName("§a§lCrear Nueva Cinemática");
        crearMeta.setLore(Arrays.asList(
                "§7Haz clic para crear una nueva",
                "§7cinemática grabando tu movimiento",
                "",
                "§e¡Clic para continuar!"
        ));
        crearItem.setItemMeta(crearMeta);
        inventario.setItem(10, crearItem);

        // Botón para listar cinemáticas
        ItemStack listarItem = new ItemStack(Material.BOOK);
        ItemMeta listarMeta = listarItem.getItemMeta();
        listarMeta.setDisplayName("§b§lListar Cinemáticas");
        listarMeta.setLore(Arrays.asList(
                "§7Ver todas las cinemáticas",
                "§7disponibles en el servidor",
                "",
                "§e¡Clic para ver!"
        ));
        listarItem.setItemMeta(listarMeta);
        inventario.setItem(12, listarItem);

        // Botón para reproducir cinemática
        ItemStack reproducirItem = new ItemStack(Material.COMPASS);
        ItemMeta reproducirMeta = reproducirItem.getItemMeta();
        reproducirMeta.setDisplayName("§d§lReproducir Cinemática");
        reproducirMeta.setLore(Arrays.asList(
                "§7Reproduce una cinemática",
                "§7para ti o para todos",
                "",
                "§e¡Clic para seleccionar!"
        ));
        reproducirItem.setItemMeta(reproducirMeta);
        inventario.setItem(14, reproducirItem);

        // Botón de configuración
        ItemStack configItem = new ItemStack(Material.REDSTONE);
        ItemMeta configMeta = configItem.getItemMeta();
        configMeta.setDisplayName("§c§lConfiguración");
        configMeta.setLore(Arrays.asList(
                "§7Ajusta las configuraciones",
                "§7del sistema de cinemáticas",
                "",
                "§e¡Clic para configurar!"
        ));
        configItem.setItemMeta(configMeta);
        inventario.setItem(16, configItem);

        // Botón de cinemáticas en vivo
        ItemStack vivoItem = new ItemStack(Material.ENDER_EYE);
        ItemMeta vivoMeta = vivoItem.getItemMeta();
        vivoMeta.setDisplayName("§5§lCinemáticas en Vivo");
        vivoMeta.setLore(Arrays.asList(
                "§7Crea cinemáticas en tiempo real",
                "§7donde otros jugadores te siguen",
                "",
                "§e¡Clic para iniciar!"
        ));
        vivoItem.setItemMeta(vivoMeta);
        inventario.setItem(28, vivoItem);

        // Información del plugin
        ItemStack infoItem = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName("§f§lInformación");
        infoMeta.setLore(Arrays.asList(
                "§7Plugin: §bViciont Cinematics",
                "§7Versión: §a" + plugin.getDescription().getVersion(),
                "§7Autor: §e" + plugin.getDescription().getAuthors().get(0),
                "",
                "§7¡Gracias por usar nuestro plugin!"
        ));
        infoItem.setItemMeta(infoMeta);
        inventario.setItem(49, infoItem);

        // Llenar espacios vacíos
        ItemStack relleno = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta rellenoMeta = relleno.getItemMeta();
        rellenoMeta.setDisplayName(" ");
        relleno.setItemMeta(rellenoMeta);

        for (int i = 0; i < inventario.getSize(); i++) {
            if (inventario.getItem(i) == null) {
                inventario.setItem(i, relleno);
            }
        }

        inventariosAbiertos.put(jugador.getUniqueId(), "menu_principal");
        jugador.openInventory(inventario);
    }

    /**
     * Abre el menú de lista de cinemáticas
     */
    public void abrirMenuListaCinematicas(Player jugador) {
        Map<String, Cinematica> cinematicas = plugin.getGestorCinematicas().getCinematicas();
        int tamaño = Math.max(18, ((cinematicas.size() / 9) + 1) * 9);
        if (tamaño > 54) tamaño = 54;

        Inventory inventario = Bukkit.createInventory(null, tamaño, "§6§lCinemáticas Disponibles");

        int slot = 0;
        for (Map.Entry<String, Cinematica> entrada : cinematicas.entrySet()) {
            if (slot >= 45) break; // Dejar espacio para botones de control

            Cinematica cinematica = entrada.getValue();
            ItemStack item = new ItemStack(Material.FILLED_MAP);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§a§l" + cinematica.getNombre());
            meta.setLore(Arrays.asList(
                    "§7Duración: §e" + String.format("%.1f", cinematica.getDuracionSegundos()) + " segundos",
                    "§7Frames: §e" + cinematica.getFrames().size(),
                    "§7Eventos: §e" + cinematica.getEventosProgramados().size(),
                    "",
                    "§e§lClic izquierdo: §7Reproducir solo para ti",
                    "§e§lClic derecho: §7Reproducir para todos",
                    "§e§lShift + Clic: §7Opciones avanzadas"
            ));
            item.setItemMeta(meta);
            inventario.setItem(slot++, item);
        }

        // Botón de volver
        ItemStack volverItem = new ItemStack(Material.ARROW);
        ItemMeta volverMeta = volverItem.getItemMeta();
        volverMeta.setDisplayName("§c§lVolver al Menú Principal");
        volverItem.setItemMeta(volverMeta);
        inventario.setItem(inventario.getSize() - 1, volverItem);

        inventariosAbiertos.put(jugador.getUniqueId(), "lista_cinematicas");
        jugador.openInventory(inventario);
    }

    /**
     * Abre el menú de configuración
     */
    public void abrirMenuConfiguracion(Player jugador) {
        Inventory inventario = Bukkit.createInventory(null, 27, "§6§lConfiguración de Cinemáticas");

        var gestor = plugin.getGestorCinematicas();

        // Silencio global
        ItemStack silencioItem = new ItemStack(gestor.isSilencioGlobal() ? Material.GREEN_WOOL : Material.RED_WOOL);
        ItemMeta silencioMeta = silencioItem.getItemMeta();
        silencioMeta.setDisplayName("§e§lSilencio Global");
        silencioMeta.setLore(Arrays.asList(
                "§7Estado: " + (gestor.isSilencioGlobal() ? "§aActivado" : "§cDesactivado"),
                "§7Silencia el chat durante las cinemáticas",
                "",
                "§e¡Clic para alternar!"
        ));
        silencioItem.setItemMeta(silencioMeta);
        inventario.setItem(10, silencioItem);

        // Efecto Fade
        ItemStack fadeItem = new ItemStack(gestor.isEfectoFade() ? Material.GREEN_WOOL : Material.RED_WOOL);
        ItemMeta fadeMeta = fadeItem.getItemMeta();
        fadeMeta.setDisplayName("§e§lEfecto Fade");
        fadeMeta.setLore(Arrays.asList(
                "§7Estado: " + (gestor.isEfectoFade() ? "§aActivado" : "§cDesactivado"),
                "§7Aplica transiciones suaves al inicio/fin",
                "",
                "§e¡Clic para alternar!"
        ));
        fadeItem.setItemMeta(fadeMeta);
        inventario.setItem(11, fadeItem);

        // Ocultar jugadores automáticamente
        ItemStack ocultarItem = new ItemStack(gestor.isOcultarJugadoresAuto() ? Material.GREEN_WOOL : Material.RED_WOOL);
        ItemMeta ocultarMeta = ocultarItem.getItemMeta();
        ocultarMeta.setDisplayName("§e§lOcultar Jugadores Auto");
        ocultarMeta.setLore(Arrays.asList(
                "§7Estado: " + (gestor.isOcultarJugadoresAuto() ? "§aActivado" : "§cDesactivado"),
                "§7Oculta automáticamente a otros jugadores",
                "",
                "§e¡Clic para alternar!"
        ));
        ocultarItem.setItemMeta(ocultarMeta);
        inventario.setItem(12, ocultarItem);

        // Restaurar ubicación
        ItemStack ubicacionItem = new ItemStack(gestor.isRestaurarUbicacion() ? Material.GREEN_WOOL : Material.RED_WOOL);
        ItemMeta ubicacionMeta = ubicacionItem.getItemMeta();
        ubicacionMeta.setDisplayName("§e§lRestaurar Ubicación");
        ubicacionMeta.setLore(Arrays.asList(
                "§7Estado: " + (gestor.isRestaurarUbicacion() ? "§aActivado" : "§cDesactivado"),
                "§7Restaura la posición original al finalizar",
                "",
                "§e¡Clic para alternar!"
        ));
        ubicacionItem.setItemMeta(ubicacionMeta);
        inventario.setItem(13, ubicacionItem);

        // Restaurar modo de juego
        ItemStack modoItem = new ItemStack(gestor.isRestaurarModoJuego() ? Material.GREEN_WOOL : Material.RED_WOOL);
        ItemMeta modoMeta = modoItem.getItemMeta();
        modoMeta.setDisplayName("§e§lRestaurar Modo de Juego");
        modoMeta.setLore(Arrays.asList(
                "§7Estado: " + (gestor.isRestaurarModoJuego() ? "§aActivado" : "§cDesactivado"),
                "§7Restaura el gamemode original al finalizar",
                "",
                "§e¡Clic para alternar!"
        ));
        modoItem.setItemMeta(modoMeta);
        inventario.setItem(14, modoItem);

        // Botón de volver
        ItemStack volverItem = new ItemStack(Material.ARROW);
        ItemMeta volverMeta = volverItem.getItemMeta();
        volverMeta.setDisplayName("§c§lVolver al Menú Principal");
        volverItem.setItemMeta(volverMeta);
        inventario.setItem(26, volverItem);

        inventariosAbiertos.put(jugador.getUniqueId(), "configuracion");
        jugador.openInventory(inventario);
    }

    @EventHandler
    public void alClickearInventario(InventoryClickEvent evento) {
        if (!(evento.getWhoClicked() instanceof Player jugador)) return;

        String tipoInventario = inventariosAbiertos.get(jugador.getUniqueId());
        if (tipoInventario == null) return;

        evento.setCancelled(true);

        ItemStack item = evento.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;

        switch (tipoInventario) {
            case "menu_principal" -> manejarClickMenuPrincipal(jugador, evento.getSlot());
            case "lista_cinematicas" -> manejarClickListaCinematicas(jugador, evento.getSlot(), evento.isRightClick(), evento.isShiftClick());
            case "configuracion" -> manejarClickConfiguracion(jugador, evento.getSlot());
        }
    }

    private void manejarClickMenuPrincipal(Player jugador, int slot) {
        switch (slot) {
            case 10 -> { // Crear nueva cinemática
                jugador.closeInventory();
                plugin.enviarMensaje(jugador, "§a§lEscribe el nombre de la nueva cinemática en el chat:");
                plugin.enviarMensaje(jugador, "§7Escribe 'cancelar' para cancelar la operación");
                esperandoInput.put(jugador.getUniqueId(), "crear_cinematica");
                contextosInput.put(jugador.getUniqueId(), "normal"); // Tipo por defecto
            }
            case 12 -> abrirMenuListaCinematicas(jugador); // Listar cinemáticas
            case 14 -> abrirMenuListaCinematicas(jugador); // Reproducir (redirige a lista)
            case 16 -> abrirMenuConfiguracion(jugador); // Configuración
            case 28 -> { // Cinemáticas en vivo
                jugador.closeInventory();
                // Crear cinemática en vivo directamente
                var gestorVivas = new net.viciont.cinematics.core.GestorCinematicasVivas(plugin);
                var cinematicaViva = gestorVivas.crearCinematicaViva(jugador);
                if (cinematicaViva != null) {
                    plugin.enviarMensaje(jugador, "§5¡Cinemática en vivo iniciada!");
                    plugin.enviarMensaje(jugador, "§7Usa /cinematica-viva add <jugador> para agregar espectadores");
                } else {
                    plugin.enviarMensaje(jugador, "§cNo se pudo iniciar la cinemática en vivo");
                }
            }
        }
    }

    private void manejarClickListaCinematicas(Player jugador, int slot, boolean clickDerecho, boolean shiftClick) {
        Inventory inventario = jugador.getOpenInventory().getTopInventory();
        if (slot == inventario.getSize() - 1) {
            // Botón volver
            abrirMenuPrincipal(jugador);
            return;
        }

        // Obtener la cinemática del slot
        List<String> nombres = new ArrayList<>(plugin.getGestorCinematicas().getCinematicas().keySet());
        if (slot >= nombres.size()) return;

        String nombreCinematica = nombres.get(slot);
        jugador.closeInventory();

        if (shiftClick) {
            plugin.enviarMensaje(jugador, "§eOpciones avanzadas para: §f" + nombreCinematica);
            // Aquí se podría abrir un menú de opciones avanzadas
        } else if (clickDerecho) {
            // Reproducir para todos
            List<UUID> todosJugadores = Bukkit.getOnlinePlayers().stream()
                    .map(Player::getUniqueId)
                    .toList();

            if (plugin.getGestorCinematicas().reproducir(todosJugadores, nombreCinematica)) {
                plugin.enviarMensaje(jugador, "§aReproduciendo cinemática '§f" + nombreCinematica + "§a' para todos los jugadores");
            } else {
                plugin.enviarMensaje(jugador, "§cError al reproducir la cinemática");
            }
        } else {
            // Reproducir solo para el jugador
            if (plugin.getGestorCinematicas().reproducir(List.of(jugador.getUniqueId()), nombreCinematica)) {
                plugin.enviarMensaje(jugador, "§aReproduciendo cinemática '§f" + nombreCinematica + "§a'");
            } else {
                plugin.enviarMensaje(jugador, "§cError al reproducir la cinemática");
            }
        }
    }

    private void manejarClickConfiguracion(Player jugador, int slot) {
        var gestor = plugin.getGestorCinematicas();

        switch (slot) {
            case 10 -> { // Silencio global
                gestor.setSilencioGlobal(!gestor.isSilencioGlobal());
                plugin.enviarMensaje(jugador, "§eSilencio global: " +
                        (gestor.isSilencioGlobal() ? "§aActivado" : "§cDesactivado"));
                abrirMenuConfiguracion(jugador); // Refrescar
            }
            case 11 -> { // Efecto Fade
                gestor.setEfectoFade(!gestor.isEfectoFade());
                plugin.enviarMensaje(jugador, "§eEfecto Fade: " +
                        (gestor.isEfectoFade() ? "§aActivado" : "§cDesactivado"));
                abrirMenuConfiguracion(jugador);
            }
            case 12 -> { // Ocultar jugadores auto
                gestor.setOcultarJugadoresAuto(!gestor.isOcultarJugadoresAuto());
                plugin.enviarMensaje(jugador, "§eOcultar jugadores automáticamente: " +
                        (gestor.isOcultarJugadoresAuto() ? "§aActivado" : "§cDesactivado"));
                abrirMenuConfiguracion(jugador);
            }
            case 13 -> { // Restaurar ubicación
                gestor.setRestaurarUbicacion(!gestor.isRestaurarUbicacion());
                plugin.enviarMensaje(jugador, "§eRestaurar ubicación: " +
                        (gestor.isRestaurarUbicacion() ? "§aActivado" : "§cDesactivado"));
                abrirMenuConfiguracion(jugador);
            }
            case 14 -> { // Restaurar modo de juego
                gestor.setRestaurarModoJuego(!gestor.isRestaurarModoJuego());
                plugin.enviarMensaje(jugador, "§eRestaurar modo de juego: " +
                        (gestor.isRestaurarModoJuego() ? "§aActivado" : "§cDesactivado"));
                abrirMenuConfiguracion(jugador);
            }
            case 26 -> abrirMenuPrincipal(jugador); // Volver
        }
    }

    @EventHandler
    public void alHablarEnChat(AsyncPlayerChatEvent evento) {
        Player jugador = evento.getPlayer();
        String tipoInput = esperandoInput.get(jugador.getUniqueId());

        if (tipoInput != null) {
            evento.setCancelled(true);
            String mensaje = evento.getMessage().trim();

            if (mensaje.equalsIgnoreCase("cancelar")) {
                esperandoInput.remove(jugador.getUniqueId());
                plugin.enviarMensaje(jugador, "§cOperación cancelada");
                return;
            }

            switch (tipoInput) {
                case "crear_cinematica" -> {
                    esperandoInput.remove(jugador.getUniqueId());
                    String contexto = contextosInput.remove(jugador.getUniqueId());

                    if (mensaje.isEmpty() || mensaje.length() > 32) {
                        plugin.enviarMensaje(jugador, "§cEl nombre debe tener entre 1 y 32 caracteres");
                        return;
                    }

                    if (plugin.getGestorCinematicas().getCinematicas().containsKey(mensaje)) {
                        plugin.enviarMensaje(jugador, "§cYa existe una cinemática con ese nombre");
                        return;
                    }

                    Cinematica.TipoCinematica tipo = contexto != null && contexto.equals("parts") ?
                            Cinematica.TipoCinematica.PARTES : Cinematica.TipoCinematica.NORMAL;

                    // Iniciar grabación
                    if (plugin.getGestorCinematicas().iniciarGrabacion(jugador, mensaje, tipo)) {
                        plugin.enviarMensaje(jugador, "§a¡Grabación iniciada para '§f" + mensaje + "§a'!");
                        plugin.enviarMensaje(jugador, "§eMuévete para grabar la cinemática. Usa §f/cinematica stop §epara finalizar.");
                    } else {
                        plugin.enviarMensaje(jugador, "§cError al iniciar la grabación");
                    }
                }
            }
        }
    }

    @EventHandler
    public void alCerrarInventario(InventoryCloseEvent evento) {
        if (evento.getPlayer() instanceof Player jugador) {
            inventariosAbiertos.remove(jugador.getUniqueId());
            // No limpiar esperandoInput aquí para permitir input en chat
        }
    }
}