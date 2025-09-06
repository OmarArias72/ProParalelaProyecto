import controllers.LoginController;
import controllers.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

import controllers.LoginController;
import controllers.MainController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    private Stage primaryStage;
    private MainController mainController;

    @Override
    public void start(Stage stage) throws Exception {
        this.primaryStage = stage;
        this.mainController = new MainController(stage);

        primaryStage.setTitle("Aplicación JavaFX");

        // Carga la primera escena en un hilo secundario
        loadLoginSceneAsync();

        primaryStage.show();
    }

    private void loadLoginSceneAsync() {
        Task<Void> loadTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // Simular tiempo de carga (opcional)
                Thread.sleep(500);

                // Cargar FXML en hilo secundario
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/login.fxml"));
                Parent root = loader.load();

                // Obtener controlador
                LoginController loginController = loader.getController();

                // Actualizar UI en el hilo de JavaFX
                Platform.runLater(() -> {
                    loginController.setMainController(mainController);
                    Scene scene = new Scene(root);
                    scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
                    primaryStage.setScene(scene);
                });

                return null;
            }
        };

        // Manejar excepciones
        loadTask.setOnFailed(event -> {
            System.err.println("Error al cargar la escena: " + event.getSource().getException().getMessage());
            // Podrías mostrar una pantalla de error aquí
        });

        // Ejecutar la tarea en un hilo separado
        Thread thread = new Thread(loadTask);
        thread.setDaemon(true);
        thread.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}