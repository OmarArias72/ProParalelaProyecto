package controllers;

import dao.UsuarioDAOImp;
import javafx.fxml.FXML;
import javafx.scene.control.*;


import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import models.Usuario;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class RegisterController {

    private MainController mainController;
    private UsuarioDAOImp usuarioDAO = new UsuarioDAOImp();
    private SessionManager sessionManager;

    @FXML
    private TextField nameField;
    @FXML
    private TextField firstLastNameField;
    @FXML
    private TextField secondLastNameField;
    @FXML
    private DatePicker birthDatePicker;
    @FXML
    private TextField correoField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Label messageLabel;
    @FXML
    private ProgressIndicator loadingIndicator;

    public RegisterController() {
        this.sessionManager = new SessionManager();
    }
    public void setMainController(MainController controller) {
        this.mainController = controller;
    }

    @FXML
    private void handleRegister() {
        String user = nameField.getText().trim();
        String firstLastName = firstLastNameField.getText().trim();
        String secondLastName = secondLastNameField.getText().trim();
        String correo = correoField.getText().trim();
        String pass = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        LocalDate localDate = birthDatePicker.getValue();
        Date date = null;
        if (localDate != null) {
            date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            System.out.println("Fecha convertida a Date: " + date);
        } else {
            System.out.println("No se ha seleccionado una fecha.");
        }

        // Validaciones
        if (user.isEmpty() || pass.isEmpty() || confirm.isEmpty() ||
                firstLastName.isEmpty() || secondLastName.isEmpty() ||
                correo.isEmpty() || localDate == null) {
            messageLabel.setText("Todos los campos son obligatorios");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        if (!pass.equals(confirm)) {
            messageLabel.setText("Las contraseñas no coinciden");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        // Validar formato de email básico
        if (!correo.contains("@") || !correo.contains(".")) {
            messageLabel.setText("Formato de correo inválido");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        // Construir objeto Usuario
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombre(user);
        nuevoUsuario.setApellidoPaterno(firstLastName);
        nuevoUsuario.setApellidoMaterno(secondLastName);
        nuevoUsuario.setCorreo(correo);
        nuevoUsuario.setPassword(pass); // 🔒 Idealmente deberías cifrarla
        nuevoUsuario.setFechaNacimiento(date);

        // Mostrar indicador de carga en UI thread
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                loadingIndicator.setVisible(true);
                messageLabel.setText("Registrando...");
                messageLabel.setStyle("-fx-text-fill: black;");
            }
        });

        // Crear el Runnable para el registro
        Runnable registerTask = new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("Iniciando registro de usuario: " + correo +
                            " en hilo: " + Thread.currentThread().getName());

                    // Simular tiempo de procesamiento
                    Thread.sleep(1000);

                    // Intentar guardar el usuario
                    boolean registroExitoso = usuarioDAO.save(nuevoUsuario);

                    if (registroExitoso) {
                        System.out.println("Usuario registrado exitosamente: " + correo);

                        // Establecer sesión en un hilo separado después del registro exitoso
                        Thread sessionThread = new Thread(new SessionSetupRunnable(nuevoUsuario, "REGISTER"),
                                "RegisterSessionSetup");
                        sessionThread.setDaemon(true);
                        sessionThread.start();

                        // Esperar a que la sesión se establezca
                        try {
                            sessionThread.join(3000); // Esperar máximo 3 segundos
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            System.err.println("Sesión de registro interrumpida: " + e.getMessage());
                        }

                        // Actualizar UI - Registro exitoso
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                loadingIndicator.setVisible(false);
                                messageLabel.setText("Registro exitoso. ¡Bienvenido!");
                                messageLabel.setStyle("-fx-text-fill: green;");

                                // Limpiar campos después del registro exitoso
                                clearFields();

                                // Cambiar a la vista principal después de un breve delay
                                Thread uiTransitionThread = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            Thread.sleep(1500); // Mostrar mensaje por 1.5 segundos
                                            Platform.runLater(new Runnable() {
                                                @Override
                                                public void run() {
                                                    mainController.changeScene("home.fxml");
                                                }
                                            });
                                        } catch (InterruptedException e) {
                                            Thread.currentThread().interrupt();
                                        }
                                    }
                                }, "UITransition");
                                uiTransitionThread.setDaemon(true);
                                uiTransitionThread.start();
                            }
                        });

                    } else {
                        // Error en el registro
                        System.err.println("Error al registrar usuario: " + correo);
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                loadingIndicator.setVisible(false);
                                messageLabel.setText("Error en el registro. El correo puede estar en uso.");
                                messageLabel.setStyle("-fx-text-fill: red;");
                            }
                        });
                    }

                } catch (Exception e) {
                    // Error durante el proceso
                    System.err.println("Excepción durante el registro: " + e.getMessage());
                    e.printStackTrace();

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            loadingIndicator.setVisible(false);
                            messageLabel.setText("Error inesperado en el registro");
                            messageLabel.setStyle("-fx-text-fill: red;");
                        }
                    });
                }
            }
        };

        // Ejecutar el registro en un hilo separado
        Thread registerThread = new Thread(registerTask, "UserRegistration");
        registerThread.setDaemon(true);
        registerThread.start();
    }


    @FXML
    private void goToLogin() {
        mainController.changeScene("login.fxml");
    }

    private void clearFields() {
        nameField.clear();
        firstLastNameField.clear();
        secondLastNameField.clear();
        birthDatePicker.setValue(null);
        correoField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
    }

    // Clase interna para manejar el establecimiento de sesión después del registro
    private class SessionSetupRunnable implements Runnable {
        private final Usuario usuario;
        private final String tipoOperacion;

        public SessionSetupRunnable(Usuario usuario, String tipoOperacion) {
            this.usuario = usuario;
            this.tipoOperacion = tipoOperacion;
        }

        @Override
        public void run() {
            try {
                System.out.println("Estableciendo sesión post-registro para usuario: " + usuario.getCorreo() +
                        " en hilo: " + Thread.currentThread().getName());

                // Establecer la sesión
                sessionManager.setActiveSession(usuario);
                mainController.setUsuarioActivo(usuario);

                // Simular configuraciones adicionales de sesión para nuevo usuario
                Thread.sleep(700);

                // Registrar actividad de registro y login automático
                sessionManager.logSessionActivity(tipoOperacion + "_SUCCESS", usuario.getId());
                sessionManager.logSessionActivity("AUTO_LOGIN", usuario.getId());

                // Simular inicialización de datos del usuario
                initializeUserData();

                System.out.println("Sesión post-registro establecida exitosamente para: " + usuario.getCorreo());

            } catch (Exception e) {
                System.err.println("Error al establecer la sesión post-registro: " + e.getMessage());
                e.printStackTrace();
            }
        }

        private void initializeUserData() throws InterruptedException {
            System.out.println("Inicializando datos del usuario en hilo: " + Thread.currentThread().getName());

            // Simular inicialización de preferencias del usuario
            Thread.sleep(200);
            System.out.println("Preferencias de usuario inicializadas");

            // Simular carga de configuración inicial
            Thread.sleep(300);
            System.out.println("Configuración inicial cargada");
        }
    }
}