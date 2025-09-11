# Viciont Cinematics

plugin de cinematicas usado en Viciont Hardcore 3.

## 🎬 Características

- **Grabación de Cinemáticas**: Graba tus movimientos para crear cinemáticas fluidas
- **Interfaz Gráfica Intuitiva**: GUI completa para manejar todas las funciones sin comandos
- **Cinemáticas en Vivo**: Permite que otros jugadores te sigan en tiempo real
- **Sistema de NPCs**: Crea clones de jugadores durante las cinemáticas
- **Efectos Visuales**: Transiciones suaves con efectos de fade
- **Eventos Programados**: Ejecuta comandos en momentos específicos de las cinemáticas
- **Configuración Avanzada**: Múltiples opciones para personalizar la experiencia

## Requisitos

- **Minecraft**: 1.21, 1.21.1, 1.21.2, 1.21.3
- **Java**: 21+
- **Servidor**: Paper/Spigot 1.21.x
- **Dependencias Opcionales**: ProtocolLib (para funciones avanzadas)

## Instalación

1. Descarga el archivo `.jar` del plugin
2. Colócalo en la carpeta `plugins` de tu servidor
3. Reinicia el servidor
4. ¡Listo para usar!

## 🎮 Uso Básico

### Comandos Principales

- `/cinematica gui` - Abre la interfaz gráfica principal
- `/cinematica grabar <nombre>` - Inicia la grabación de una cinemática
- `/cinematica parar` - Detiene la grabación actual
- `/cinematica reproducir <todos/solo> <nombres...>` - Reproduce cinemáticas
- `/cinematica lista` - Lista todas las cinemáticas disponibles

### Interfaz Gráfica

El plugin incluye una GUI completa que permite:
- Crear nuevas cinemáticas
- Reproducir cinemáticas existentes
- Configurar todas las opciones
- Gestionar cinemáticas en vivo
- Ver información detallada

## ⚙️ Configuración

### Opciones Disponibles

- **Silencio Global**: Silencia el chat durante las cinemáticas
- **Mostrar NPCs**: Crea NPCs de los jugadores durante las cinemáticas
- **Efecto Fade**: Aplica transiciones suaves al inicio y final
- **Ocultar Jugadores**: Oculta automáticamente a otros jugadores
- **Restaurar Ubicación**: Devuelve a los jugadores a su posición original
- **Restaurar Modo de Juego**: Restaura el gamemode original

## 🎭 Cinemáticas en Vivo

Las cinemáticas en vivo permiten que otros jugadores te sigan en tiempo real:

```
/cinematica-viva start - Inicia una cinemática en vivo
/cinematica-viva add <jugador> - Añade un jugador a tu cinemática
/cinematica-viva remove <jugador> - Remueve un jugador
/cinematica-viva stop - Detiene la cinemática en vivo
```

## 🔧 Para Desarrolladores

### Eventos Personalizados

El plugin dispara varios eventos que puedes escuchar:

- `CinematicaInicioEvent` - Cuando inicia una cinemática
- `CinematicaFinEvent` - Cuando termina una cinemática
- `CinematicaTickEvent` - En cada tick de la cinemática

### API Básica

```java
// Obtener el gestor de cinemáticas
GestorCinematicas gestor = ViciontCinematics.getInstancia().getGestorCinematicas();

// Reproducir una cinemática
gestor.reproducir(List.of(jugador.getUniqueId()), "mi_cinematica");

// Verificar si un jugador está en una cinemática
ProgresoCinematica progreso = gestor.obtenerProgresoCinematica(jugador);
```

## 📝 Permisos

- `viciont.cinematics.*` - Acceso completo al plugin
- `viciont.cinematics.use` - Usar comandos básicos
- `viciont.cinematics.admin` - Administrar cinemáticas
- `viciont.cinematics.live` - Crear cinemáticas en vivo
- `viciont.cinematics.gui` - Usar la interfaz gráfica

## 🐛 Reportar Problemas

Si encuentras algún problema o tienes sugerencias:

1. Verifica que estés usando la versión correcta de Minecraft
2. Revisa los logs del servidor para errores
3. Crea un issue en el repositorio con información detallada

## Licencia

Este proyecto está bajo la Licencia MIT. Ver el archivo `LICENSE` para más detalles.

## Autor

**CrissyjuanxD** - Desarrollador principal

---
