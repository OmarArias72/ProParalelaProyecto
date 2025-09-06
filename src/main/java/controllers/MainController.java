package controllers;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {

    private Stage stage;

    public MainController(Stage stage) {
        this.stage = stage;
    }

    public void changeScene(String fxml) {
        // Crear tarea para cargar la escena en segundo plano
        Task<Void> sceneChangeTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // Simular tiempo de carga si es necesario
                Thread.sleep(300);

                // Cargar FXML en hilo secundario
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/" + fxml));
                Parent root = loader.load();

                // Configurar controlador en el hilo de JavaFX
                Platform.runLater(() -> {
                    try {
                        if (fxml.equals("login.fxml")) {
                            LoginController controller = loader.getController();
                            controller.setMainController(MainController.this);
                        } else if (fxml.equals("register.fxml")) {
                            RegisterController controller = loader.getController();
                            controller.setMainController(MainController.this);
                        }

                        Scene scene = new Scene(root);
                        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
                        stage.setScene(scene);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

                return null;
            }
        };

        // Manejar errores
        sceneChangeTask.setOnFailed(event -> {
            System.err.println("Error al cambiar de escena: " +
                    event.getSource().getException().getMessage());
        });

        // Ejecutar en hilo separado
        Thread thread = new Thread(sceneChangeTask);
        thread.setDaemon(true);
        thread.start();
    }
}