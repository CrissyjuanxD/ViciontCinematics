package net.viciont.cinematics.core;

import net.viciont.cinematics.objects.Frame;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Interpolador MEJORADO para cinemáticas:
 * ✔ Rotaciones suaves cuando estás quieto
 * ✔ Giros ultra fluidos en movimiento
 * ✔ Sin aceleraciones artificiales
 */
public class InterpoladorFrames {

    public enum TipoInterpolacion {
        LINEAR,
        SMOOTH,
        BEZIER
    }

    private final TipoInterpolacion tipo;
    private final boolean suavizadoRotacion;
    private final double factorSuavizado;

    // Factores de suavizado cinematográfico
    private static final double FACTOR_SUAVIZADO_BASE = 0.12; // Suavizado base para todas las rotaciones

    public InterpoladorFrames(TipoInterpolacion tipo, boolean suavizadoRotacion, double factorSuavizado) {
        this.tipo = tipo;
        this.suavizadoRotacion = suavizadoRotacion;
        this.factorSuavizado = Math.max(0.1, Math.min(1.0, factorSuavizado));
    }

    public List<Frame> interpolar(List<Frame> framesOriginales, int fpsObjetivo) {
        if (framesOriginales.size() < 2) return framesOriginales;

        // Ajustado para mejor slow motion
        double factor = fpsObjetivo / 18.0;
        int intermedios = Math.max(1, (int) Math.floor(factor) - 1);

        List<Frame> salida = new ArrayList<>();

        for (int i = 0; i < framesOriginales.size() - 1; i++) {
            Frame a = framesOriginales.get(i);
            Frame b = framesOriginales.get(i + 1);

            salida.add(a);

            for (int j = 1; j <= intermedios; j++) {
                double t = (double) j / (intermedios + 1);
                salida.add(interpolarFrame(a, b, t));
            }
        }

        salida.add(framesOriginales.get(framesOriginales.size() - 1));
        return salida;
    }

    private Frame interpolarFrame(Frame a, Frame b, double t) {
        return switch (tipo) {
            case SMOOTH -> interpolarSuave(a, b, t);
            case BEZIER -> interpolarBezier(a, b, t);
            default -> interpolarLineal(a, b, t);
        };
    }

    private Frame interpolarLineal(Frame a, Frame b, double t) {
        if (!a.getMundo().equals(b.getMundo())) return a.clonar();

        // Posición con interpolación suave
        double x = lerp(a.getX(), b.getX(), t);
        double y = lerp(a.getY(), b.getY(), t);
        double z = lerp(a.getZ(), b.getZ(), t);

        // Rotación con suavizado uniforme
        float yaw = suavizadoRotacion
                ? suavizarYaw(a, b, t)
                : (float) lerp(a.getYaw(), b.getYaw(), t);

        float pitch = suavizadoRotacion
                ? suavizarPitch(a, b, t)
                : (float) lerp(a.getPitch(), b.getPitch(), t);

        return new Frame(a.getMundo(), x, y, z, yaw, pitch);
    }

    private Frame interpolarSuave(Frame a, Frame b, double t) {
        if (!a.getMundo().equals(b.getMundo())) return a.clonar();

        // Aplicar easing para posición
        double tEased = easeInOutCubic(t);

        double x = lerp(a.getX(), b.getX(), tEased);
        double y = lerp(a.getY(), b.getY(), tEased);
        double z = lerp(a.getZ(), b.getZ(), tEased);

        // Rotación con easing suave uniforme
        double tRotacion = easeInOutQuart(t);
        float yaw = suavizarYaw(a, b, tRotacion);
        float pitch = suavizarPitch(a, b, tRotacion);

        return new Frame(a.getMundo(), x, y, z, yaw, pitch);
    }

    private Frame interpolarBezier(Frame a, Frame b, double t) {
        if (!a.getMundo().equals(b.getMundo())) return a.clonar();

        Vector p0 = new Vector(a.getX(), a.getY(), a.getZ());
        Vector p3 = new Vector(b.getX(), b.getY(), b.getZ());

        Vector dir = p3.clone().subtract(p0).normalize();
        double dist = p0.distance(p3);

        Vector p1 = p0.clone().add(dir.clone().multiply(dist * 0.33));
        Vector p2 = p3.clone().subtract(dir.clone().multiply(dist * 0.33));

        Vector r = bezier(p0, p1, p2, p3, t);

        double tRotacion = easeInOutQuart(t);
        float yaw = suavizarYaw(a, b, tRotacion);
        float pitch = suavizarPitch(a, b, tRotacion);

        return new Frame(a.getMundo(), r.getX(), r.getY(), r.getZ(), yaw, pitch);
    }

    // -----------------------------------------------------------
    // ROTACIÓN SUAVIZADA: Cinematografía consistente
    // -----------------------------------------------------------

    private float suavizarYaw(Frame a, Frame b, double t) {
        float yawA = a.getYaw();
        float yawB = b.getYaw();

        float diff = normalizarAngulo(yawB - yawA);

        float yawInterpolado = yawA + diff * (float) t;

        if (suavizadoRotacion) {
            yawInterpolado = yawA + (yawInterpolado - yawA) * (float) FACTOR_SUAVIZADO_BASE;
        }

        return normalizarAngulo(yawInterpolado);
    }

    private float suavizarPitch(Frame a, Frame b, double t) {
        float pitchA = a.getPitch();
        float pitchB = b.getPitch();

        float diff = normalizarAngulo(pitchB - pitchA);

        float pitchInterpolado = pitchA + diff * (float) t;

        if (suavizadoRotacion) {
            pitchInterpolado = pitchA + (pitchInterpolado - pitchA) * (float) FACTOR_SUAVIZADO_BASE;
        }

        return normalizarAngulo(pitchInterpolado);
    }

    private float normalizarAngulo(double ang) {
        ang %= 360;
        if (ang < -180) ang += 360;
        if (ang > 180) ang -= 360;
        return (float) ang;
    }

    private double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    /**
     * Easing cúbico para transiciones suaves
     */
    private double easeInOutCubic(double t) {
        return t < 0.5
                ? 4 * t * t * t
                : 1 - Math.pow(-2 * t + 2, 3) / 2;
    }

    /**
     * Easing cuártico para rotaciones ultra suaves
     */
    private double easeInOutQuart(double t) {
        return t < 0.5
                ? 8 * t * t * t * t
                : 1 - Math.pow(-2 * t + 2, 4) / 2;
    }

    private Vector bezier(Vector p0, Vector p1, Vector p2, Vector p3, double t) {
        double u = 1 - t;
        return p0.clone().multiply(u * u * u)
                .add(p1.clone().multiply(3 * u * u * t))
                .add(p2.clone().multiply(3 * u * t * t))
                .add(p3.clone().multiply(t * t * t));
    }
}
