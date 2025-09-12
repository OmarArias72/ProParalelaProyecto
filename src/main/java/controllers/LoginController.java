package controllers;


import dao.UsuarioDAOImp;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;


import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ProgressIndicator;
import models.Usuario;

import java.util.Optional;

public class LoginController {

    private MainController mainController;
    private UsuarioDAOImp usuarioDAO = new UsuarioDAOImp();
    private SessionManager sessionManager;

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label messageLabel;
    @FXML
    private ProgressIndicator loadingIndicator;

    public void setMainController(MainController controller) {
        this.mainController = controller;
    }

    public LoginController() {
        this.sessionManager = new SessionManager();
    }

    @FXML
    private void handleLogin() {
        String correo = usernameField.getText().trim();
        String pass = passwordField.getText();

        // Validar campos vacíos
        if (correo.isEmpty() || pass.isEmpty()) {
            messageLabel.setText("Por favor, complete todos los campos");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        // Mostrar indicador de carga en UI thread
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                loadingIndicator.setVisible(true);
                messageLabel.setText("Verificando...");
                messageLabel.setStyle("-fx-text-fill: black;");
            }
        });

        // Crear el Runnable para la autenticación
        Runnable loginTask = new Runnable() {
            @Override
            public void run() {
                try {
                    // Simular tiempo de verificación
                    Thread.sleep(1000);

                    // Verificar credenciales
                    Optional<Usuario> usuarioOpt = usuarioDAO.findByEmail(correo);

                    if (usuarioOpt.isPresent()) {
                        Usuario usuario = usuarioOpt.get();
                        boolean passwordCorrect = usuario.getPassword().equals(pass);

                        if (passwordCorrect) {
                            // Establecer sesión en un hilo separado
                            Thread sessionThread = new Thread(new SessionSetupRunnable(usuario), "SessionSetup");
                            sessionThread.setDaemon(true);
                            sessionThread.start();

                            // Esperar a que la sesión se establezca
                            try {
                                sessionThread.join(2000); // Esperar máximo 2 segundos
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }

                            // Actualizar UI - Login exitoso
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    loadingIndicator.setVisible(false);
                                    messageLabel.setText("Login exitoso");
                                    messageLabel.setStyle("-fx-text-fill: green;");
                                    mainController.changeScene("home.fxml");
                                }
                            });

                        } else {
                            // Credenciales incorrectas
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    loadingIndicator.setVisible(false);
                                    messageLabel.setText("Usuario o contraseña incorrectos");
                                    messageLabel.setStyle("-fx-text-fill: red;");
                                }
                            });
                        }
                    } else {
                        // Usuario no encontrado
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                loadingIndicator.setVisible(false);
                                messageLabel.setText("Usuario no encontrado");
                                messageLabel.setStyle("-fx-text-fill: red;");
                            }
                        });
                    }

                } catch (Exception e) {
                    // Error en el proceso
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            loadingIndicator.setVisible(false);
                            messageLabel.setText("Error en el login: " + e.getMessage());
                            messageLabel.setStyle("-fx-text-fill: red;");
                        }
                    });
                    e.printStackTrace();
                }
            }
        };

        // Ejecutar el login en un hilo separado
        Thread loginThread = new Thread(loginTask, "LoginAuthentication");
        loginThread.setDaemon(true);
        loginThread.start();
    }
    private class SessionSetupRunnable implements Runnable {
        private final Usuario usuario;

        public SessionSetupRunnable(Usuario usuario) {
            this.usuario = usuario;
        }

        @Override
        public void run() {
            try {
                System.out.println("Estableciendo sesión para usuario: " + usuario.getCorreo() +
                        " en hilo: " + Thread.currentThread().getName());

                // Establecer la sesión
                sessionManager.setActiveSession(usuario);
                mainController.setUsuarioActivo(usuario);

                // Simular configuraciones adicionales de sesión
                Thread.sleep(500);

                // Registrar actividad de inicio de sesión
                sessionManager.logSessionActivity("LOGIN_SUCCESS", usuario.getId());

                System.out.println("Sesión establecida exitosamente para: " + usuario.getCorreo());

            } catch (Exception e) {
                System.err.println("Error al establecer la sesión: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void goToRegister() {
        mainController.changeScene("register.fxml");
    }
}