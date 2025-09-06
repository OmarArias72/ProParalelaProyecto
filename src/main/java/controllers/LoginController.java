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

public class LoginController {

    private MainController mainController;

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

    @FXML
    private void handleLogin() {
        String user = usernameField.getText();
        String pass = passwordField.getText();

        // Mostrar indicador de carga
        loadingIndicator.setVisible(true);
        messageLabel.setText("Verificando...");

        // Ejecutar verificación en hilo secundario
        Task<Boolean> loginTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                // Simular tiempo de verificación
                Thread.sleep(1000);
                return user.equals("admin") && pass.equals("1234");
            }
        };

        loginTask.setOnSucceeded(event -> {
            loadingIndicator.setVisible(false);
            boolean success = loginTask.getValue();

            if (success) {
                messageLabel.setText("Login exitoso");
                messageLabel.setStyle("-fx-text-fill: green;");
            } else {
                messageLabel.setText("Usuario o contraseña incorrectos");
                messageLabel.setStyle("-fx-text-fill: red;");
            }
        });

        loginTask.setOnFailed(event -> {
            loadingIndicator.setVisible(false);
            messageLabel.setText("Error en el login");
            messageLabel.setStyle("-fx-text-fill: red;");
        });

        Thread thread = new Thread(loginTask);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void goToRegister() {
        mainController.changeScene("register.fxml");
    }
}