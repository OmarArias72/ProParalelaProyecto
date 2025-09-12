package controllers;

import models.Usuario;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicReference;

public class SessionManager {

    private final AtomicReference<Usuario> activeUser = new AtomicReference<>();
    private final Object sessionLock = new Object();
    private LocalDateTime sessionStartTime;

    public void setActiveSession(Usuario usuario) {
        synchronized (sessionLock) {
            try {
                System.out.println("SessionManager: Iniciando sesión para " + usuario.getCorreo() +
                        " en hilo: " + Thread.currentThread().getName());

                // Establecer usuario activo de forma thread-safe
                activeUser.set(usuario);
                sessionStartTime = LocalDateTime.now();

                // Simular inicialización de sesión
                Thread.sleep(200);

                System.out.println("SessionManager: Sesión iniciada a las " +
                        sessionStartTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")));

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Sesión interrumpida: " + e.getMessage());
            }
        }
    }

    public Usuario getActiveUser() {
        return activeUser.get();
    }

    public void closeSession() {
        synchronized (sessionLock) {
            Usuario user = activeUser.getAndSet(null);
            if (user != null) {
                System.out.println("SessionManager: Cerrando sesión para " + user.getCorreo());
                logSessionActivity("LOGOUT", user.getId());
            }
            sessionStartTime = null;
        }
    }

    public void logSessionActivity(String activity, int userId) {
        // Ejecutar el log en un hilo separado para no bloquear
        Runnable logTask = new Runnable() {
            @Override
            public void run() {
                try {
                    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    String logEntry = String.format("[%s] Usuario ID: %d - Actividad: %s - Hilo: %s",
                            timestamp, userId, activity, Thread.currentThread().getName());

                    System.out.println("LOG: " + logEntry);

                    // Aquí podrías escribir a un archivo o base de datos
                    Thread.sleep(100); // Simular tiempo de escritura

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Log interrumpido: " + e.getMessage());
                }
            }
        };

        Thread logThread = new Thread(logTask, "SessionLogger");
        logThread.setDaemon(true);
        logThread.start();
    }

    public boolean hasActiveSession() {
        return activeUser.get() != null;
    }

    public LocalDateTime getSessionStartTime() {
        return sessionStartTime;
    }
}
