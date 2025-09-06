package controllers;

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

public class RegisterController {

    private MainController mainController;

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Label messageLabel;
    @FXML
    private ProgressIndicator loadingIndicator;

    public void setMainController(MainController controller) {
        this.mainController = controller;
    }

    @FXML
    private void handleRegister() {
        String user = usernameField.getText();
        String pass = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        if (user.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
            messageLabel.setText("Todos los campos son obligatorios");
            return;
        }

        if (!pass.equals(confirm)) {
            messageLabel.setText("Las contraseñas no coinciden");
            return;
        }

        // Mostrar indicador de carga
        loadingIndicator.setVisible(true);
        messageLabel.setText("Registrando...");

        // Ejecutar registro en hilo secundario
        Task<Void> registerTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // Simular tiempo de registro
                Thread.sleep(1500);
                // Aquí iría la lógica real de registro
                return null;
            }
        };

        registerTask.setOnSucceeded(event -> {
            loadingIndicator.setVisible(false);
            messageLabel.setText("Registro exitoso");
            messageLabel.setStyle("-fx-text-fill: green;");
        });

        registerTask.setOnFailed(event -> {
            loadingIndicator.setVisible(false);
            messageLabel.setText("Error en el registro");
            messageLabel.setStyle("-fx-text-fill: red;");
        });

        Thread thread = new Thread(registerTask);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void goToLogin() {
        mainController.changeScene("login.fxml");
    }
}