package net.viciont.cinematics.core;

import net.viciont.cinematics.objects.Frame;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Sistema avanzado de interpolación con rotaciones ultra suaves
 * Optimizado para 60 FPS y movimientos cinematográficos fluidos
 *
 * @author CrissyjuanxD
 */
public class InterpoladorAvanzado {

    private static final double EPSILON = 0.001;
    private static final double UMBRAL_ROTACION_RAPIDA = 90.0;

    /**
     * Interpola frames a 60 FPS con rotaciones ultra suaves
     */
    public List<Frame> interpolar60FPS(List<Frame> framesOriginales) {
        if (framesOriginales.size() < 2) {
            return new ArrayList<>(framesOriginales);
        }

        List<Frame> framesOptimizados = optimizarFrames(framesOriginales);

        List<Frame> resultado = new ArrayList<>();
        resultado.add(framesOptimizados.get(0));

        for (int i = 0; i < framesOptimizados.size() - 1; i++) {
            Frame actual = framesOptimizados.get(i);
            Frame siguiente = framesOptimizados.get(i + 1);

            List<Frame> intermedios = generarFramesIntermedios(actual, siguiente, 3);
            resultado.addAll(intermedios);
        }

        resultado.add(framesOptimizados.get(framesOptimizados.size() - 1));

        return resultado;
    }

    /**
     * Genera frames intermedios entre dos frames con interpolación cúbica
     */
    private List<Frame> generarFramesIntermedios(Frame inicio, Frame fin, int cantidad) {
        List<Frame> intermedios = new ArrayList<>();

        if (!inicio.getMundo().equals(fin.getMundo())) {
            return intermedios;
        }

        double diferenciaYaw = calcularDiferenciaAngulo(inicio.getYaw(), fin.getYaw());
        double diferenciaPitch = calcularDiferenciaAngulo(inicio.getPitch(), fin.getPitch());

        boolean rotacionRapida = Math.abs(diferenciaYaw) > UMBRAL_ROTACION_RAPIDA ||
                                 Math.abs(diferenciaPitch) > UMBRAL_ROTACION_RAPIDA;

        for (int i = 1; i <= cantidad; i++) {
            double t = (double) i / (cantidad + 1);

            double tSuave = rotacionRapida ? easeInOutQuint(t) : easeInOutCubic(t);

            Vector posInicio = new Vector(inicio.getX(), inicio.getY(), inicio.getZ());
            Vector posFin = new Vector(fin.getX(), fin.getY(), fin.getZ());

            Vector posInterpolada = interpolarCatmullRom(posInicio, posFin, tSuave);

            float yaw = interpolarAnguloSuave(inicio.getYaw(), fin.getYaw(), tSuave);
            float pitch = interpolarAnguloSuave(inicio.getPitch(), fin.getPitch(), tSuave);

            Frame frameIntermedio = new Frame(
                    inicio.getMundo(),
                    posInterpolada.getX(),
                    posInterpolada.getY(),
                    posInterpolada.getZ(),
                    yaw,
                    pitch
            );

            intermedios.add(frameIntermedio);
        }

        return intermedios;
    }

    /**
     * Interpolación Catmull-Rom para movimientos más naturales
     */
    private Vector interpolarCatmullRom(Vector p1, Vector p2, double t) {
        double t2 = t * t;
        double t3 = t2 * t;

        Vector direccion = p2.clone().subtract(p1);
        double magnitud = direccion.length();

        if (magnitud < EPSILON) {
            return p1.clone();
        }

        direccion.normalize();

        Vector p0 = p1.clone().subtract(direccion.clone().multiply(magnitud * 0.3));
        Vector p3 = p2.clone().add(direccion.clone().multiply(magnitud * 0.3));

        Vector resultado = new Vector();

        resultado.add(p0.clone().multiply(-0.5 * t3 + t2 - 0.5 * t));
        resultado.add(p1.clone().multiply(1.5 * t3 - 2.5 * t2 + 1.0));
        resultado.add(p2.clone().multiply(-1.5 * t3 + 2.0 * t2 + 0.5 * t));
        resultado.add(p3.clone().multiply(0.5 * t3 - 0.5 * t2));

        return resultado;
    }

    /**
     * Calcula la diferencia entre dos ángulos tomando el camino más corto
     */
    private double calcularDiferenciaAngulo(float a, float b) {
        double diferencia = b - a;
        while (diferencia > 180) diferencia -= 360;
        while (diferencia < -180) diferencia += 360;
        return diferencia;
    }

    /**
     * Interpola ángulos de forma ultra suave sin saltos
     */
    private float interpolarAnguloSuave(float inicio, float fin, double t) {
        double diferencia = calcularDiferenciaAngulo(inicio, fin);

        double anguloSuavizado = inicio + diferencia * t;

        while (anguloSuavizado > 180) anguloSuavizado -= 360;
        while (anguloSuavizado < -180) anguloSuavizado += 360;

        return (float) anguloSuavizado;
    }

    /**
     * Función de suavizado cúbica ease-in-out
     */
    private double easeInOutCubic(double t) {
        return t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2;
    }

    /**
     * Función de suavizado quíntica para rotaciones rápidas
     */
    private double easeInOutQuint(double t) {
        return t < 0.5 ? 16 * t * t * t * t * t : 1 - Math.pow(-2 * t + 2, 5) / 2;
    }

    /**
     * Optimiza frames removiendo redundantes pero manteniendo detalle
     */
    private List<Frame> optimizarFrames(List<Frame> frames) {
        if (frames.size() < 3) {
            return new ArrayList<>(frames);
        }

        List<Frame> optimizados = new ArrayList<>();
        optimizados.add(frames.get(0));

        Frame ultimoAgregado = frames.get(0);

        for (int i = 1; i < frames.size() - 1; i++) {
            Frame actual = frames.get(i);
            Frame siguiente = frames.get(i + 1);

            if (esFrameImportante(ultimoAgregado, actual, siguiente)) {
                optimizados.add(actual);
                ultimoAgregado = actual;
            }
        }

        optimizados.add(frames.get(frames.size() - 1));

        return optimizados;
    }

    /**
     * Determina si un frame es importante y debe mantenerse
     */
    private boolean esFrameImportante(Frame anterior, Frame actual, Frame siguiente) {
        double distancia1 = calcularDistancia(anterior, actual);
        double distancia2 = calcularDistancia(actual, siguiente);

        if (distancia1 > 0.15 || distancia2 > 0.15) {
            return true;
        }

        double cambioYaw1 = Math.abs(calcularDiferenciaAngulo(anterior.getYaw(), actual.getYaw()));
        double cambioYaw2 = Math.abs(calcularDiferenciaAngulo(actual.getYaw(), siguiente.getYaw()));

        double cambioPitch1 = Math.abs(calcularDiferenciaAngulo(anterior.getPitch(), actual.getPitch()));
        double cambioPitch2 = Math.abs(calcularDiferenciaAngulo(actual.getPitch(), siguiente.getPitch()));

        return (cambioYaw1 > 1.5 || cambioYaw2 > 1.5 || cambioPitch1 > 1.5 || cambioPitch2 > 1.5);
    }

    /**
     * Calcula distancia entre dos frames
     */
    private double calcularDistancia(Frame f1, Frame f2) {
        if (!f1.getMundo().equals(f2.getMundo())) {
            return Double.MAX_VALUE;
        }

        double dx = f1.getX() - f2.getX();
        double dy = f1.getY() - f2.getY();
        double dz = f1.getZ() - f2.getZ();

        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * Suaviza una lista completa de frames aplicando filtro de media móvil
     */
    public List<Frame> suavizarRotaciones(List<Frame> frames) {
        if (frames.size() < 3) {
            return new ArrayList<>(frames);
        }

        List<Frame> suavizados = new ArrayList<>();
        suavizados.add(frames.get(0));

        for (int i = 1; i < frames.size() - 1; i++) {
            Frame anterior = frames.get(i - 1);
            Frame actual = frames.get(i);
            Frame siguiente = frames.get(i + 1);

            float yawSuavizado = promediarAngulo(anterior.getYaw(), actual.getYaw(), siguiente.getYaw());
            float pitchSuavizado = promediarAngulo(anterior.getPitch(), actual.getPitch(), siguiente.getPitch());

            Frame suavizado = new Frame(
                    actual.getMundo(),
                    actual.getX(),
                    actual.getY(),
                    actual.getZ(),
                    yawSuavizado,
                    pitchSuavizado
            );

            suavizados.add(suavizado);
        }

        suavizados.add(frames.get(frames.size() - 1));

        return suavizados;
    }

    /**
     * Promedia tres ángulos considerando la circularidad
     */
    private float promediarAngulo(float a1, float a2, float a3) {
        double sin = Math.sin(Math.toRadians(a1)) +
                     Math.sin(Math.toRadians(a2)) +
                     Math.sin(Math.toRadians(a3));

        double cos = Math.cos(Math.toRadians(a1)) +
                     Math.cos(Math.toRadians(a2)) +
                     Math.cos(Math.toRadians(a3));

        return (float) Math.toDegrees(Math.atan2(sin / 3.0, cos / 3.0));
    }
}
