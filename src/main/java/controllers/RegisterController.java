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

    public void setMainController(MainController controller) {
        this.mainController = controller;
    }

    @FXML
    private void handleRegister() {
        String user = nameField.getText();
        String firstLastName = firstLastNameField.getText();
        String secondLastName = secondLastNameField.getText();

        LocalDate localDate = birthDatePicker.getValue(); // obtiene la fecha seleccionada
        Date date=null;
        if (localDate != null) {
            date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            System.out.println("Fecha convertida a Date: " + date);
        } else {
            System.out.println("No se ha seleccionado una fecha.");
        }
        String correo = correoField.getText();
        String pass = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        if (user.isEmpty() || pass.isEmpty() || confirm.isEmpty() || firstLastName.isEmpty() || secondLastName.isEmpty()
                || correo.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
            messageLabel.setText("Todos los campos son obligatorios");
            return;
        }

        if (!pass.equals(confirm)) {
            messageLabel.setText("Las contraseñas no coinciden");
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

        // Mostrar indicador de carga
        loadingIndicator.setVisible(true);
        messageLabel.setText("Registrando...");

        // Ejecutar registro en hilo secundario
        Task<Boolean> registerTask = new Task<>() {
            @Override
            protected Boolean call() {
                return usuarioDAO.save(nuevoUsuario);
            }
        };

        registerTask.setOnSucceeded(event -> {
            loadingIndicator.setVisible(false);
            messageLabel.setText("Registro exitoso");
            messageLabel.setStyle("-fx-text-fill: green;");
            // 🔹 Crear sesión automáticamente
            mainController.setUsuarioActivo(nuevoUsuario);

            // 🔹 Cambiar a la vista principal
            mainController.changeScene("home.fxml");

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