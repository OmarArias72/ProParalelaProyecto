package controllers;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import models.Usuario;

import java.io.IOException;

public class MainController {

    private Stage stage;
    private Usuario usuarioActivo;

    public MainController(Stage stage) {
        this.stage = stage;
    }

    public Usuario getUsuarioActivo() {
        return usuarioActivo;
    }

    public void setUsuarioActivo(Usuario usuario) {
        this.usuarioActivo = usuario;
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
                        } else if (fxml.equals("home.fxml")) {
                            HomeController controller = loader.getController();
                            controller.setMainController(MainController.this);
                        }else if (fxml.equals("cuestionario.fxml")) {
                            // 🔹 Agrega este bloque para el QuizController
                            QuizController controller = loader.getController();
                            controller.setMainController(MainController.this);

                            // 🔹 Pasa la información necesaria, como el ID del cuestionario
                            // Para este ejemplo, usaremos un ID fijo (1), pero en una app real,
                            // lo obtendrías dinámicamente.
                            int idCuestionario = 1;
                            controller.loadCuestionario(idCuestionario);
                        } else if (fxml.equals("resultados.fxml")) {
                            ResultsController controller = loader.getController();
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


    public void changeScene(String fxml, int idIntento) {

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
                        } else if (fxml.equals("home.fxml")) {
                            HomeController controller = loader.getController();
                            controller.setMainController(MainController.this);
                        }else if (fxml.equals("cuestionario.fxml")) {
                            // 🔹 Agrega este bloque para el QuizController
                            QuizController controller = loader.getController();
                            controller.setMainController(MainController.this);

                            // 🔹 Pasa la información necesaria, como el ID del cuestionario
                            // Para este ejemplo, usaremos un ID fijo (1), pero en una app real,
                            // lo obtendrías dinámicamente.
                            int idCuestionario = 1;
                            controller.loadCuestionario(idCuestionario);
                        } else if (fxml.equals("resultados.fxml")) {
                            ResultsController controller = loader.getController();
                            controller.setMainController(MainController.this);
                            //System.out.println("CHANGE ESCENA ID INTENTO "+idIntento);
                            controller.showResults(idIntento);

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

    /**
     * Nuevo método para cambiar escenas con un Parent ya cargado
     * Útil cuando el controlador ya ha sido configurado externamente
     */
    public void changeSceneWithLoader(Parent root) {
        Platform.runLater(() -> {
            try {
                applySceneAndShow(root);
            } catch (Exception e) {
                System.err.println("Error al cambiar escena con loader: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    /**
     * Método utilitario para aplicar la escena y mostrarla
     */
    private void applySceneAndShow(Parent root) {
        try {
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            stage.setScene(scene);
            stage.show(); // Asegurar que la ventana se muestre
        } catch (Exception e) {
            System.err.println("Error aplicando escena: " + e.getMessage());
            e.printStackTrace();
        }
    }


}