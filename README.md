# Viciont Cinematics

Plugin de cinemáticas ultra optimizado para Minecraft 1.21.x - **60 FPS garantizados**

## 🎬 Características

- **60 FPS Ultra Fluidos**: Sistema avanzado de interpolación Catmull-Rom
- **Rotaciones Suaves**: Algoritmo especial que elimina giros bruscos completamente
- **Optimizado para 100+ Jugadores**: Usa packets de ProtocolLib para máximo rendimiento
- **Visibilidad entre Jugadores**: Los jugadores se ven entre ellos durante cinemáticas
- **Cinemáticas en Vivo**: Permite que otros jugadores te sigan en tiempo real
- **Efectos Visuales**: Transiciones suaves con efectos de fade
- **Eventos Programados**: Ejecuta comandos en momentos específicos
- **Sin Lag**: Procesamiento asíncrono y pooling de objetos

## Requisitos

- **Minecraft**: 1.21, 1.21.1, 1.21.2, 1.21.3
- **Java**: 21+
- **Servidor**: Paper/Spigot 1.21.x
- **Dependencias Recomendadas**:
  - **ProtocolLib** (ALTAMENTE RECOMENDADO): Para optimización extrema y visibilidad entre jugadores
  - Sin ProtocolLib el plugin funciona pero con funciones limitadas

## Instalación

1. Descarga el archivo `.jar` del plugin
2. **Instala ProtocolLib** (recomendado): https://www.spigotmc.org/resources/protocollib.1997/
3. Coloca ambos archivos en la carpeta `plugins` de tu servidor
4. Reinicia el servidor
5. ¡Listo para usar!

## 🎮 Uso Básico

### Comandos Principales

- `/cinematica record <nombre> [normal/parts]` - Inicia la grabación
- `/cinematica stop` - Detiene la grabación actual
- `/cinematica play <all/me/jugador> <nombres...>` - Reproduce cinemáticas
- `/cinematica list` - Lista todas las cinemáticas disponibles
- `/cinematica cut` - Corta grabación por partes
- `/cinematica playcut` - Continúa grabación por partes
- `/cinematica static <nombre> <ticks>` - Graba cinemática estática
- `/cinematica delete <nombre>` - Elimina una cinemática
- `/cinematica clone <original> <nuevo>` - Clona una cinemática
- `/cinematica forcestop <all/jugador>` - Fuerza fin de cinemática
- `/cinematica reload` - Recarga configuración

### Sistema de 60 FPS

El plugin automáticamente:
- Interpola frames a 60 FPS para máxima suavidad
- Suaviza rotaciones para eliminar giros bruscos
- Optimiza el procesamiento para 100+ jugadores simultáneos
- Usa packets cuando ProtocolLib está disponible

## ⚙️ Configuración

### Opciones en config.yml

```yaml
playback:
  interpolation:
    enabled: true           # Siempre habilitado
    target_fps: 60          # 60 FPS fijo
    type: "SMOOTH"          # Optimizado para rotaciones
    rotation_smoothing: true
    smoothing_factor: 0.25  # Ajustado para cinemáticas

performance:
  use_packets: true                  # Usa ProtocolLib
  keep_players_visible: true         # Jugadores se ven entre ellos
  location_pool_size: 1000           # Pool de objetos
  async_frame_processing: true       # Procesamiento asíncrono
  teleport_batch_size: 50            # Optimiza red
```

## 🎭 Cinemáticas en Vivo

Las cinemáticas en vivo permiten que otros jugadores te sigan en tiempo real:

```
/cinematica-viva start - Inicia una cinemática en vivo
/cinematica-viva add <jugador> - Añade un jugador a tu cinemática
/cinematica-viva remove <jugador> - Remueve un jugador
/cinematica-viva range <radio> - Añade jugadores en un radio
/cinematica-viva stop - Detiene la cinemática en vivo
```

## 🚀 Optimizaciones Implementadas

### Rendimiento
- **Packets de ProtocolLib**: Teleports optimizados y visibilidad mejorada
- **Pooling de Objetos**: Reduce garbage collection
- **Procesamiento Asíncrono**: No afecta el TPS del servidor
- **Batch Processing**: Agrupa operaciones para máxima eficiencia

### Interpolación Avanzada
- **Catmull-Rom Splines**: Movimientos naturales y suaves
- **Suavizado Angular**: Promedio circular de rotaciones
- **Ease-In-Out Quint**: Para rotaciones rápidas sin saltos
- **Optimización de Frames**: Elimina redundantes manteniendo detalle

### Visibilidad
- **Packets Personalizados**: Mantiene jugadores visibles en modo espectador
- **Sincronización Automática**: Se ejecuta cada segundo durante cinemáticas
- **Compatible sin ProtocolLib**: Fallback a sistema básico

## 🔧 Para Desarrolladores

### API Básica

```java
// Obtener el gestor de cinemáticas
GestorCinematicas gestor = ViciontCinematics.getInstancia().getGestorCinematicas();

// Reproducir una cinemática
gestor.reproducir(List.of(jugador.getUniqueId()), "mi_cinematica");

// Verificar si un jugador está en una cinemática
ProgresoCinematica progreso = gestor.obtenerProgresoCinematica(jugador);

// Usar packets optimizados
GestorPackets packets = ViciontCinematics.getInstancia().getGestorPackets();
if (packets.isDisponible()) {
    packets.teleportOptimizado(jugador, x, y, z, yaw, pitch);
}
```

## 📝 Permisos

- `viciont.cinematics.*` - Acceso completo al plugin
- `viciont.cinematics.use` - Usar comandos básicos
- `viciont.cinematics.admin` - Administrar cinemáticas
- `viciont.cinematics.live` - Crear cinemáticas en vivo

## 🐛 Reportar Problemas

1. Asegúrate de tener **ProtocolLib** instalado
2. Verifica que usas Minecraft 1.21.x y Java 21+
3. Revisa los logs del servidor para errores
4. Crea un issue con información detallada

## Licencia

Este proyecto está bajo la Licencia MIT. Ver el archivo `LICENSE` para más detalles.

## Autor

**CrissyjuanxD** - Desarrollador principal

---

**Optimizado para 60 FPS | Soporta 100+ jugadores | Sin lag | Rotaciones ultra suaves**
