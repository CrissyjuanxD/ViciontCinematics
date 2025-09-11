package net.viciont.cinematics.objects;

/**
 * Representa un frame individual de una cinemática
 * Contiene la posición y rotación en un momento específico
 *
 * @author CrissyjuanxD
 */
public class Frame {

    private String mundo;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;

    public Frame(String mundo, double x, double y, double z, float yaw, float pitch) {
        this.mundo = mundo;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    /**
     * Crea una copia de este frame
     */
    public Frame clonar() {
        return new Frame(mundo, x, y, z, yaw, pitch);
    }

    /**
     * Calcula la distancia a otro frame
     */
    public double distanciaA(Frame otroFrame) {
        if (!this.mundo.equals(otroFrame.mundo)) {
            return Double.MAX_VALUE;
        }

        double dx = this.x - otroFrame.x;
        double dy = this.y - otroFrame.y;
        double dz = this.z - otroFrame.z;

        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * Interpola entre este frame y otro
     */
    public Frame interpolar(Frame otroFrame, double factor) {
        if (!this.mundo.equals(otroFrame.mundo)) {
            return this.clonar();
        }

        double nuevoX = this.x + (otroFrame.x - this.x) * factor;
        double nuevoY = this.y + (otroFrame.y - this.y) * factor;
        double nuevoZ = this.z + (otroFrame.z - this.z) * factor;
        float nuevoYaw = (float) (this.yaw + (otroFrame.yaw - this.yaw) * factor);
        float nuevoPitch = (float) (this.pitch + (otroFrame.pitch - this.pitch) * factor);

        return new Frame(mundo, nuevoX, nuevoY, nuevoZ, nuevoYaw, nuevoPitch);
    }

    @Override
    public String toString() {
        return String.format("Frame{mundo='%s', x=%.2f, y=%.2f, z=%.2f, yaw=%.2f, pitch=%.2f}",
            mundo, x, y, z, yaw, pitch);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Frame frame = (Frame) obj;
        return Double.compare(frame.x, x) == 0 &&
               Double.compare(frame.y, y) == 0 &&
               Double.compare(frame.z, z) == 0 &&
               Float.compare(frame.yaw, yaw) == 0 &&
               Float.compare(frame.pitch, pitch) == 0 &&
               mundo.equals(frame.mundo);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(mundo, x, y, z, yaw, pitch);
    }

    // Getters y Setters
    public String getMundo() {
        return mundo;
    }

    public void setMundo(String mundo) {
        this.mundo = mundo;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }
}