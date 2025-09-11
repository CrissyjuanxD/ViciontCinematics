package net.viciont.cinematics.utils;

import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Utilidad para encadenar tareas con retrasos
 * Versión moderna y optimizada para Minecraft 1.21
 *
 * @author CrissyjuanxD
 */
public class CadenaTareas {

    private final ConcurrentLinkedQueue<Runnable> cola = new ConcurrentLinkedQueue<>();
    private int tareasTotal;
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    /**
     * Crea una nueva cadena de tareas vacía
     */
    public static CadenaTareas crear() {
        return new CadenaTareas();
    }

    /**
     * Verifica si la cola está vacía
     */
    public boolean estaVacia() {
        return cola.isEmpty();
    }

    /**
     * Obtiene la siguiente tarea de la cola
     */
    public Runnable obtenerSiguiente() {
        return cola.poll();
    }

    /**
     * Obtiene el número de tareas restantes
     */
    public int getTasksLeft() {
        return cola.size();
    }

    /**
     * Obtiene la tarea actual (basada en el progreso)
     */
    public int getCurrentTask() {
        return Math.abs(cola.size() - tareasTotal);
    }

    /**
     * Añade una tarea a la cadena
     */
    public CadenaTareas add(Runnable tarea) {
        cola.add(tarea);
        return this;
    }

    /**
     * Añade un retraso a la cadena
     */
    public CadenaTareas retraso(long milisegundos) {
        long retrasoReal = Math.abs(milisegundos);

        if (retrasoReal > 0) {
            cola.add(new TareaRetraso(retrasoReal));
        }

        return this;
    }

    /**
     * Repite la última tarea x cantidad de veces
     */
    public CadenaTareas repetir(int veces) {
        Runnable ultimaTarea = cola.stream()
                .reduce((primera, segunda) -> segunda)
                .orElse(null);

        if (ultimaTarea != null) {
            for (int i = 0; i < veces; i++) {
                cola.add(ultimaTarea);
            }
        }

        return this;
    }

    /**
     * Añade una tarea con retraso posterior
     */
    public CadenaTareas addWithDelay(Runnable tarea, long retrasoMilisegundos) {
        cola.add(() -> {
            tarea.run();

            long retrasoReal = Math.abs(retrasoMilisegundos);
            if (retrasoReal > 0) {
                new TareaRetraso(retrasoReal).run();
            }
        });

        return this;
    }

    /**
     * Ejecuta toda la cadena de tareas
     */
    public CompletableFuture<Boolean> execute() {
        this.tareasTotal = cola.size();
        CompletableFuture<Boolean> futuro = new CompletableFuture<>();

        // Usar el scheduler de Bukkit para mejor sincronización
        org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(
                net.viciont.cinematics.ViciontCinematics.getInstancia(), () -> {
                    while (!cola.isEmpty()) {
                        Runnable siguienteTarea = obtenerSiguiente();
                        if (siguienteTarea != null) {
                            try {
                                siguienteTarea.run();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    futuro.complete(true);
                });

        return futuro;
    }

    /**
     * Clona la cadena actual
     */
    @Override
    public CadenaTareas clone() {
        CadenaTareas clon = new CadenaTareas();

        for (Runnable tarea : cola) {
            clon.add(tarea);
        }

        return clon;
    }

    /**
     * Clase interna para manejar retrasos
     */
    private static class TareaRetraso implements Runnable {
        private final long tiempo;

        public TareaRetraso(long tiempo) {
            this.tiempo = tiempo;
        }

        public static TareaRetraso de(long milisegundos) {
            return new TareaRetraso(milisegundos);
        }

        @Override
        public void run() {
            try {
                Thread.sleep(tiempo);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
        }
    }
}