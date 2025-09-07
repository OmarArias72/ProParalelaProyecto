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
        String correo = usernameField.getText();
        String pass = passwordField.getText();

        // Mostrar indicador de carga
        loadingIndicator.setVisible(true);
        messageLabel.setText("Verificando...");

        // Ejecutar verificación en hilo secundario
        Task<Boolean> loginTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                Optional<Usuario> usuarioOpt = usuarioDAO.findByEmail(correo);
                if (usuarioOpt.isPresent()) {
                    Usuario usuario = usuarioOpt.get();
                    mainController.setUsuarioActivo(usuario);
                    return usuario.getPassword().equals(pass); // 🔒 Aquí podrías usar hash (ej. BCrypt)
                }
                return false;
            }
        };

        loginTask.setOnSucceeded(event -> {
            loadingIndicator.setVisible(false);
            boolean success = loginTask.getValue();

            if (success) {
                messageLabel.setText("Login exitoso");
                messageLabel.setStyle("-fx-text-fill: green;");
                mainController.changeScene("home.fxml");
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