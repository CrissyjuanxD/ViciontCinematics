# Guía de Instalación - Viciont Cinematics 2.0

## Instalación Rápida

### Paso 1: Requisitos Previos

Asegúrate de tener:
- ✅ Minecraft 1.21.x (1.21, 1.21.1, 1.21.2 o 1.21.3)
- ✅ Java 21 o superior
- ✅ Servidor Paper o Spigot 1.21.x
- ✅ ProtocolLib instalado (ALTAMENTE RECOMENDADO)

### Paso 2: Descargar ProtocolLib

1. Ve a: https://www.spigotmc.org/resources/protocollib.1997/
2. Descarga la última versión compatible con 1.21.x
3. Coloca `ProtocolLib.jar` en tu carpeta `plugins/`

### Paso 3: Instalar Viciont Cinematics

1. Descarga `ViciontCinematics-2.0.0.jar`
2. Coloca el archivo en tu carpeta `plugins/`
3. Reinicia el servidor

### Paso 4: Verificar Instalación

Ejecuta en consola:
```
/cinematica help
```

Deberías ver:
```
[Viciont Cinematics] ProtocolLib detectado - Funciones avanzadas habilitadas
```

Si ves:
```
[Viciont Cinematics] ProtocolLib no detectado - Usando sistema básico
```
Vuelve al Paso 2 e instala ProtocolLib.

## Configuración Inicial

### config.yml

El plugin viene preconfigurado con valores optimizados. Si necesitas ajustar:

```yaml
# 60 FPS - NO CAMBIAR para mantener fluidez
playback:
  interpolation:
    target_fps: 60
    type: "SMOOTH"

# Optimizaciones - Recomendado dejar habilitado
performance:
  use_packets: true
  keep_players_visible: true
  async_frame_processing: true
```

### Permisos

Añade a tu archivo de permisos:

```yaml
# Para jugadores normales
- viciont.cinematics.use

# Para staff/admins
- viciont.cinematics.admin
- viciont.cinematics.live

# O todo junto
- viciont.cinematics.*
```

## Primeros Pasos

### 1. Crear tu Primera Cinemática

```
/cinematica record mi_primera_cinematica
```

Muévete por el mundo para grabar. Cuando termines:

```
/cinematica stop
```

### 2. Reproducir la Cinemática

Solo para ti:
```
/cinematica play me mi_primera_cinematica
```

Para todos en el servidor:
```
/cinematica play all mi_primera_cinematica
```

### 3. Verificar Fluidez

Reproduce la cinemática y observa:
- ✅ Movimientos suaves y fluidos
- ✅ Rotaciones sin saltos bruscos
- ✅ Otros jugadores visibles
- ✅ Sin lag ni caídas de TPS

## Solución de Problemas Comunes

### "ProtocolLib no detectado"

**Causa:** ProtocolLib no está instalado o está desactualizado.

**Solución:**
1. Descarga ProtocolLib para 1.21.x
2. Asegúrate de colocarlo en `plugins/`
3. Reinicia el servidor completamente
4. Verifica con `/plugins` que esté verde

### "Las rotaciones siguen teniendo saltos"

**Causa:** Configuración no óptima o cinemática con pocos frames.

**Solución:**
1. Abre `config.yml`
2. Verifica:
   ```yaml
   rotation_smoothing: true
   smoothing_factor: 0.25
   ```
3. Graba cinemáticas moviéndote MÁS LENTO para capturar más frames
4. Usa `/cinematica reload` después de cambios

### "TPS bajo con muchos jugadores"

**Causa:** Servidor con recursos limitados.

**Solución:**
1. Instala ProtocolLib (OBLIGATORIO para alto rendimiento)
2. En `config.yml`:
   ```yaml
   performance:
     async_frame_processing: true
     teleport_batch_size: 75  # Aumentar si tienes buena conexión
   ```
3. Considera actualizar RAM del servidor

### "Los jugadores no se ven entre ellos"

**Causa:** ProtocolLib no instalado.

**Solución:**
1. Esta función REQUIERE ProtocolLib
2. Instala ProtocolLib
3. En `config.yml` verifica:
   ```yaml
   keep_players_visible: true
   ```
4. Reinicia el servidor

## Optimización Avanzada

### Para Servidores con 50+ Jugadores

```yaml
performance:
  location_pool_size: 2000        # Aumentar pool
  teleport_batch_size: 100        # Batches más grandes
  async_frame_processing: true    # Obligatorio
```

### Para Servidores con Recursos Limitados

```yaml
performance:
  location_pool_size: 500         # Reducir pool
  teleport_batch_size: 25         # Batches más pequeños
  async_frame_processing: true    # Mantener habilitado
```

### Para Máxima Calidad Visual

```yaml
playback:
  interpolation:
    smoothing_factor: 0.15        # Más suavizado
    rotation_smoothing: true      # Obligatorio
```

## Migración desde Versión Anterior

### Si vienes de v1.x

1. **Backup de cinemáticas:**
   ```bash
   cp plugins/ViciontCinematics/cinematicas.json cinematicas_backup.json
   ```

2. **Actualizar plugin:**
   - Detén el servidor
   - Reemplaza el archivo .jar
   - Inicia el servidor

3. **Verificar compatibilidad:**
   ```
   /cinematica list
   ```
   Todas las cinemáticas antiguas deben aparecer.

4. **Nota sobre GUI:**
   La GUI ha sido completamente removida. Usa comandos:
   - `/cinematica gui` → Ya no existe
   - Usa `/cinematica help` para ver comandos

## Testing de Rendimiento

### Benchmark Básico

1. Crea una cinemática de prueba:
   ```
   /cinematica static test_benchmark 1200
   ```

2. Reproduce para muchos jugadores:
   ```
   /cinematica play all test_benchmark
   ```

3. Monitorea el TPS:
   ```
   /tps
   ```

**Resultados esperados:**
- TPS debe mantenerse en 20.0 ± 0.1
- Sin lag perceptible
- Movimientos fluidos a 60 FPS

### Stress Test (100+ Jugadores)

Requiere ProtocolLib y buen hardware:

```
/cinematica record stress_test
```

Muévete rápido por 30 segundos, luego:

```
/cinematica stop
/cinematica play all stress_test
```

**Métricas aceptables:**
- TPS > 19.5
- Uso CPU < 60%
- Sin errores en consola

## Soporte y Ayuda

### Logs Importantes

Si tienes problemas, revisa:

```
logs/latest.log
```

Busca líneas con:
```
[Viciont Cinematics]
```

### Información para Reportes

Al reportar bugs, incluye:

1. Versión de Minecraft
2. Versión de Paper/Spigot
3. Versión de Java (`java -version`)
4. ¿ProtocolLib instalado? (`/plugins`)
5. Logs relevantes
6. Pasos para reproducir

### Enlaces Útiles

- GitHub: https://github.com/CrissyjuanxD/viciont-cinematics
- ProtocolLib: https://www.spigotmc.org/resources/protocollib.1997/
- Documentación Técnica: `TECHNICAL.md`
- Changelog: `CHANGELOG.md`

## Preguntas Frecuentes

### ¿Funciona sin ProtocolLib?

Sí, pero con funcionalidad limitada:
- ✅ 60 FPS interpolación
- ✅ Grabación y reproducción
- ❌ Teleports optimizados
- ❌ Jugadores visibles entre ellos

### ¿Es compatible con otros plugins de cinemáticas?

Puede haber conflictos. Recomendamos usar solo Viciont Cinematics.

### ¿Puedo cambiar los 60 FPS a otro valor?

Técnicamente sí, pero NO recomendado. 60 FPS está optimizado para:
- Suavidad visual máxima
- Balance rendimiento/calidad
- Compatibilidad con monitores modernos

### ¿Funciona en Velocity/BungeeCord?

Sí, pero instala el plugin en CADA servidor backend, no en el proxy.

---

**¿Necesitas ayuda? Abre un issue en GitHub**
