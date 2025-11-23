package net.viciont.cinematics.core;

import net.viciont.cinematics.objects.Frame;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Sistema de interpolación de frames para cinemáticas fluidas
 * Genera frames intermedios entre los frames grabados para mayor suavidad
 *
 * @author CrissyjuanxD
 */
public class InterpoladorFrames {

    /**
     * Tipos de interpolación disponibles
     */
    public enum TipoInterpolacion {
        LINEAR,    // Interpolación lineal simple
        SMOOTH,    // Interpolación suave con curvas
        BEZIER     // Interpolación con curvas de Bézier
    }

    private final TipoInterpolacion tipo;
    private final boolean suavizadoRotacion;
    private final double factorSuavizado;

    public InterpoladorFrames(TipoInterpolacion tipo, boolean suavizadoRotacion, double factorSuavizado) {
        this.tipo = tipo;
        this.suavizadoRotacion = suavizadoRotacion;
        this.factorSuavizado = Math.max(0.1, Math.min(1.0, factorSuavizado));
    }

    /**
     * Interpola una lista de frames para alcanzar el FPS objetivo
     */
    public List<Frame> interpolar(List<Frame> framesOriginales, int fpsObjetivo) {
        if (framesOriginales.size() < 2) {
            return new ArrayList<>(framesOriginales);
        }

        // Calcular cuántos frames intermedios necesitamos
        // Asumiendo que los frames originales están a ~20 FPS
        double factorInterpolacion = fpsObjetivo / 20.0;

        List<Frame> framesInterpolados = new ArrayList<>();

        for (int i = 0; i < framesOriginales.size() - 1; i++) {
            Frame frameActual = framesOriginales.get(i);
            Frame siguienteFrame = framesOriginales.get(i + 1);

            // Añadir el frame actual
            framesInterpolados.add(frameActual);

            // Calcular frames intermedios
            int framesIntermedios = (int) Math.ceil(factorInterpolacion) - 1;

            for (int j = 1; j <= framesIntermedios; j++) {
                double t = (double) j / (framesIntermedios + 1);
                Frame frameInterpolado = interpolarFrame(frameActual, siguienteFrame, t);
                framesInterpolados.add(frameInterpolado);
            }
        }

        // Añadir el último frame
        framesInterpolados.add(framesOriginales.get(framesOriginales.size() - 1));

        return framesInterpolados;
    }

    /**
     * Interpola entre dos frames usando el tipo de interpolación configurado
     */
    private Frame interpolarFrame(Frame frame1, Frame frame2, double t) {
        switch (tipo) {
            case LINEAR -> {
                return interpolarLineal(frame1, frame2, t);
            }
            case SMOOTH -> {
                return interpolarSuave(frame1, frame2, t);
            }
            case BEZIER -> {
                return interpolarBezier(frame1, frame2, t);
            }
            default -> {
                return interpolarLineal(frame1, frame2, t);
            }
        }
    }

    /**
     * Interpolación lineal simple
     */
    private Frame interpolarLineal(Frame frame1, Frame frame2, double t) {
        if (!frame1.getMundo().equals(frame2.getMundo())) {
            return frame1.clonar();
        }

        double x = lerp(frame1.getX(), frame2.getX(), t);
        double y = lerp(frame1.getY(), frame2.getY(), t);
        double z = lerp(frame1.getZ(), frame2.getZ(), t);

        float yaw = suavizadoRotacion ?
                lerpAngulo(frame1.getYaw(), frame2.getYaw(), t) :
                (float) lerp(frame1.getYaw(), frame2.getYaw(), t);

        float pitch = suavizadoRotacion ?
                lerpAngulo(frame1.getPitch(), frame2.getPitch(), t) :
                (float) lerp(frame1.getPitch(), frame2.getPitch(), t);

        return new Frame(frame1.getMundo(), x, y, z, yaw, pitch);
    }

    /**
     * Interpolación suave usando función ease-in-out
     */
    private Frame interpolarSuave(Frame frame1, Frame frame2, double t) {
        if (!frame1.getMundo().equals(frame2.getMundo())) {
            return frame1.clonar();
        }

        // Aplicar función de suavizado
        double tSuave = easeInOutCubic(t);

        double x = lerp(frame1.getX(), frame2.getX(), tSuave);
        double y = lerp(frame1.getY(), frame2.getY(), tSuave);
        double z = lerp(frame1.getZ(), frame2.getZ(), tSuave);

        float yaw = suavizadoRotacion ?
                lerpAnguloSuave(frame1.getYaw(), frame2.getYaw(), tSuave) :
                (float) lerp(frame1.getYaw(), frame2.getYaw(), tSuave);

        float pitch = suavizadoRotacion ?
                lerpAnguloSuave(frame1.getPitch(), frame2.getPitch(), tSuave) :
                (float) lerp(frame1.getPitch(), frame2.getPitch(), tSuave);

        return new Frame(frame1.getMundo(), x, y, z, yaw, pitch);
    }

    /**
     * Interpolación con curvas de Bézier (más compleja pero muy suave)
     */
    private Frame interpolarBezier(Frame frame1, Frame frame2, double t) {
        if (!frame1.getMundo().equals(frame2.getMundo())) {
            return frame1.clonar();
        }

        // Para Bézier necesitamos puntos de control
        // Usamos una aproximación simple con puntos de control calculados
        Vector p1 = new Vector(frame1.getX(), frame1.getY(), frame1.getZ());
        Vector p2 = new Vector(frame2.getX(), frame2.getY(), frame2.getZ());

        // Calcular puntos de control basados en la dirección y distancia
        Vector direccion = p2.clone().subtract(p1).normalize();
        double distancia = p1.distance(p2);

        Vector control1 = p1.clone().add(direccion.clone().multiply(distancia * 0.3));
        Vector control2 = p2.clone().subtract(direccion.clone().multiply(distancia * 0.3));

        // Interpolación cúbica de Bézier
        Vector resultado = bezierCubico(p1, control1, control2, p2, t);

        float yaw = suavizadoRotacion ?
                lerpAnguloSuave(frame1.getYaw(), frame2.getYaw(), easeInOutCubic(t)) :
                (float) lerp(frame1.getYaw(), frame2.getYaw(), t);

        float pitch = suavizadoRotacion ?
                lerpAnguloSuave(frame1.getPitch(), frame2.getPitch(), easeInOutCubic(t)) :
                (float) lerp(frame1.getPitch(), frame2.getPitch(), t);

        return new Frame(frame1.getMundo(), resultado.getX(), resultado.getY(), resultado.getZ(), yaw, pitch);
    }

    /**
     * Interpolación lineal básica
     */
    private double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    /**
     * Interpolación de ángulos considerando la rotación más corta
     */
    private float lerpAngulo(float a, float b, double t) {
        float diferencia = b - a;

        // Normalizar la diferencia para tomar el camino más corto
        while (diferencia > 180) diferencia -= 360;
        while (diferencia < -180) diferencia += 360;

        return (float) (a + diferencia * t);
    }

    /**
     * Interpolación suave de ángulos con factor de suavizado
     */
    private float lerpAnguloSuave(float a, float b, double t) {
        float resultado = lerpAngulo(a, b, t);

        // Aplicar factor de suavizado
        if (factorSuavizado < 1.0) {
            float diferencia = resultado - a;
            resultado = a + (float) (diferencia * factorSuavizado);
        }

        return resultado;
    }

    /**
     * Función de suavizado ease-in-out cúbica
     */
    private double easeInOutCubic(double t) {
        return t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2;
    }

    /**
     * Interpolación cúbica de Bézier
     */
    private Vector bezierCubico(Vector p0, Vector p1, Vector p2, Vector p3, double t) {
        double u = 1 - t;
        double tt = t * t;
        double uu = u * u;
        double uuu = uu * u;
        double ttt = tt * t;

        Vector punto = p0.clone().multiply(uuu);
        punto.add(p1.clone().multiply(3 * uu * t));
        punto.add(p2.clone().multiply(3 * u * tt));
        punto.add(p3.clone().multiply(ttt));

        return punto;
    }

    /**
     * Optimiza una lista de frames removiendo frames redundantes
     */
    public List<Frame> optimizar(List<Frame> frames) {
        if (frames.size() < 3) {
            return new ArrayList<>(frames);
        }

        List<Frame> framesOptimizados = new ArrayList<>();
        framesOptimizados.add(frames.get(0)); // Siempre incluir el primer frame

        for (int i = 1; i < frames.size() - 1; i++) {
            Frame anterior = frames.get(i - 1);
            Frame actual = frames.get(i);
            Frame siguiente = frames.get(i + 1);

            // Verificar si el frame actual es significativamente diferente
            if (esFrameSignificativo(anterior, actual, siguiente)) {
                framesOptimizados.add(actual);
            }
        }

        framesOptimizados.add(frames.get(frames.size() - 1)); // Siempre incluir el último frame
        return framesOptimizados;
    }

    /**
     * Determina si un frame es significativo para la interpolación
     */
    private boolean esFrameSignificativo(Frame anterior, Frame actual, Frame siguiente) {
        double umbralPosicion = 0.1; // 0.1 bloques
        double umbralRotacion = 2.0; // 2 grados

        // Verificar cambios significativos en posición
        double distanciaAnterior = calcularDistancia(anterior, actual);
        double distanciaSiguiente = calcularDistancia(actual, siguiente);

        if (distanciaAnterior > umbralPosicion || distanciaSiguiente > umbralPosicion) {
            return true;
        }

        // Verificar cambios significativos en rotación
        double cambioYaw = Math.abs(actual.getYaw() - anterior.getYaw());
        double cambioPitch = Math.abs(actual.getPitch() - anterior.getPitch());

        return cambioYaw > umbralRotacion || cambioPitch > umbralRotacion;
    }

    /**
     * Calcula la distancia entre dos frames
     */
    private double calcularDistancia(Frame frame1, Frame frame2) {
        if (!frame1.getMundo().equals(frame2.getMundo())) {
            return Double.MAX_VALUE;
        }

        double dx = frame1.getX() - frame2.getX();
        double dy = frame1.getY() - frame2.getY();
        double dz = frame1.getZ() - frame2.getZ();

        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    // Getters
    public TipoInterpolacion getTipo() {
        return tipo;
    }

    public boolean isSuavizadoRotacion() {
        return suavizadoRotacion;
    }

    public double getFactorSuavizado() {
        return factorSuavizado;
    }
}