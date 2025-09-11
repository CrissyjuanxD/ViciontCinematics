package net.viciont.cinematics.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Sound;

/**
 * Representa una cinemática con sus frames y eventos programados
 *
 * @author CrissyjuanxD
 */
public class Cinematica {

    private String nombre;
    private List<Frame> frames;
    private Map<Integer, List<String>> eventosProgramados;
    private Map<Integer, SonidoCinematica> sonidos;
    private TipoCinematica tipo;
    private List<ParteCinematica> partes;

    public Cinematica(String nombre) {
        this.nombre = nombre;
        this.frames = new ArrayList<>();
        this.eventosProgramados = new HashMap<>();
        this.sonidos = new HashMap<>();
        this.sonidos = new HashMap<>();
        this.tipo = TipoCinematica.NORMAL;
        this.partes = new ArrayList<>();
    }

    public Cinematica(String nombre, List<Frame> frames, Map<Integer, List<String>> eventosProgramados) {
        this.nombre = nombre;
        this.frames = frames;
        this.eventosProgramados = eventosProgramados;
        this.sonidos = new HashMap<>();
        this.tipo = TipoCinematica.NORMAL;
        this.partes = new ArrayList<>();
    }

    /**
     * Combina dos frames para crear un frame intermedio suavizado
     */
    private Frame combinarFrames(Frame frame1, Frame frame2) {
        return new Frame(
                frame1.getMundo(),
                (frame1.getX() + frame2.getX()) / 2,
                (frame1.getY() + frame2.getY()) / 2,
                (frame1.getZ() + frame2.getZ()) / 2,
                (frame1.getYaw() + frame2.getYaw()) / 2,
                (frame1.getPitch() + frame2.getPitch()) / 2
        );
    }

    /**
     * Obtiene una lista de frames suavizados (reduce la cantidad a la mitad)
     */
    public List<Frame> getFramesSuavizados() {
        List<Frame> framesSuavizados = new ArrayList<>();

        for (int i = 0; i < frames.size(); i += 2) {
            if (i + 1 < frames.size()) {
                framesSuavizados.add(combinarFrames(frames.get(i), frames.get(i + 1)));
            } else {
                framesSuavizados.add(frames.get(i));
            }
        }

        return framesSuavizados;
    }

    /**
     * Obtiene una lista de frames prolongados (añade frames intermedios)
     */
    public List<Frame> getFramesProlongados() {
        List<Frame> framesProlongados = new ArrayList<>();

        for (int i = 0; i < frames.size(); i++) {
            Frame frameActual = frames.get(i);
            framesProlongados.add(frameActual);

            if (i + 1 < frames.size()) {
                Frame siguienteFrame = frames.get(i + 1);
                Frame frameCombinado = combinarFrames(frameActual, siguienteFrame);
                framesProlongados.add(frameCombinado);
                framesProlongados.add(siguienteFrame);
            }
        }

        return framesProlongados;
    }

    /**
     * Añade un evento programado en un tick específico
     */
    public void agregarEvento(int tick, String comando) {
        eventosProgramados.computeIfAbsent(tick, k -> new ArrayList<>()).add(comando);
    }

    /**
     * Remueve un evento programado
     */
    public void removerEvento(int tick, String comando) {
        List<String> eventos = eventosProgramados.get(tick);
        if (eventos != null) {
            eventos.remove(comando);
            if (eventos.isEmpty()) {
                eventosProgramados.remove(tick);
            }
        }
    }

    /**
     * Remueve todos los eventos de un tick específico
     */
    public void removerEventosTick(int tick) {
        eventosProgramados.remove(tick);
    }

    /**
     * Obtiene la duración en ticks de la cinemática
     */
    public int getDuracionTicks() {
        return frames.size();
    }

    /**
     * Obtiene la duración en segundos de la cinemática
     */
    public double getDuracionSegundos() {
        return frames.size() / 20.0;
    }

    /**
     * Añade un sonido en un tick específico
     */
    public void agregarSonido(int tick, Sound sonido, float volumen, float pitch) {
        sonidos.put(tick, new SonidoCinematica(sonido, volumen, pitch));
    }

    /**
     * Añade un sonido personalizado en un tick específico
     */
    public void agregarSonidoCustom(int tick, String nombreSonido, float volumen, float pitch) {
        sonidos.put(tick, new SonidoCinematica(nombreSonido, volumen, pitch));
    }

    /**
     * Añade un sonido usando un preset del config
     */
    public void agregarSonidoPreset(int tick, String presetName, float volumen, float pitch) {
        sonidos.put(tick, new SonidoCinematica(presetName, volumen, pitch, true));
    }

    /**
     * Añade una parte a la cinemática (para tipo PARTES)
     */
    public void agregarParte(ParteCinematica parte) {
        partes.add(parte);
    }

    // Getters y Setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<Frame> getFrames() {
        return frames;
    }

    public void setFrames(List<Frame> frames) {
        this.frames = frames;
    }

    public Map<Integer, List<String>> getEventosProgramados() {
        return eventosProgramados;
    }

    public void setEventosProgramados(Map<Integer, List<String>> eventosProgramados) {
        this.eventosProgramados = eventosProgramados;
    }

    public Map<Integer, SonidoCinematica> getSonidos() {
        return sonidos != null ? sonidos : new HashMap<>();
    }

    public void setSonidos(Map<Integer, SonidoCinematica> sonidos) {
        this.sonidos = sonidos != null ? sonidos : new HashMap<>();
    }

    public TipoCinematica getTipo() {
        return tipo;
    }

    public void setTipo(TipoCinematica tipo) {
        this.tipo = tipo;
    }

    public List<ParteCinematica> getPartes() {
        return partes;
    }

    public void setPartes(List<ParteCinematica> partes) {
        this.partes = partes;
    }

    /**
     * Enumeración para tipos de cinemática
     */
    public enum TipoCinematica {
        NORMAL, PARTES
    }

    /**
     * Clase para representar sonidos en cinemáticas
     */
    public static class SonidoCinematica {
        private Sound sonido;
        private String sonidoCustom;
        private float volumen;
        private float pitch;
        private boolean esCustom;
        private boolean esPreset;

        public SonidoCinematica(Sound sonido, float volumen, float pitch) {
            this.sonido = sonido;
            this.volumen = volumen;
            this.pitch = pitch;
            this.esCustom = false;
            this.esPreset = false;
        }

        public SonidoCinematica(String sonidoCustom, float volumen, float pitch) {
            this.sonidoCustom = sonidoCustom;
            this.volumen = volumen;
            this.pitch = pitch;
            this.esCustom = true;
            this.esPreset = false;
        }

        public SonidoCinematica(String presetName, float volumen, float pitch, boolean esPreset) {
            this.sonidoCustom = presetName;
            this.volumen = volumen;
            this.pitch = pitch;
            this.esCustom = true;
            this.esPreset = esPreset;
        }

        // Getters
        public Sound getSonido() { return sonido; }
        public String getSonidoCustom() { return sonidoCustom; }
        public float getVolumen() { return volumen; }
        public float getPitch() { return pitch; }
        public boolean isEsCustom() { return esCustom; }
        public boolean isEsPreset() { return esPreset; }
    }

    /**
     * Clase para representar partes de cinemáticas
     */
    public static class ParteCinematica {
        private List<Frame> frames;
        private int numeroParte;

        public ParteCinematica(int numeroParte) {
            this.numeroParte = numeroParte;
            this.frames = new ArrayList<>();
        }

        public List<Frame> getFrames() { return frames; }
        public void setFrames(List<Frame> frames) { this.frames = frames; }
        public int getNumeroParte() { return numeroParte; }
        public void setNumeroParte(int numeroParte) { this.numeroParte = numeroParte; }
    }
}