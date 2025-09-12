package controllers;

import dao.IntentoDAOImp;
import dao.UsuarioDAOImp;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import models.Intento;
import services.IntentoDAO;
import services.UsuarioService;
import services.UsuarioServiceImp;

import java.util.List;

public class HomeController {

    private MainController mainController;
    private UsuarioDAOImp usuarioDAO = new UsuarioDAOImp();
    private UsuarioService usuarioService = new UsuarioServiceImp();
    private IntentoDAO intentoDAO = new IntentoDAOImp();
    private SessionManager sessionManager;

    public void setMainController(MainController controller) {
        this.mainController = controller;
    }

    @FXML
    private void handleTakeQuiz(){
        mainController.changeScene("cuestionario.fxml");
    }

    @FXML
    private void handleViewResults(){
        try {
            // Verificar si el usuario tiene intentos registrados
            int idUsuarioActual = mainController.getUsuarioActivo().getId();
            List<Intento> intentosUsuario = ((IntentoDAOImp) intentoDAO).findByUsuarioId(idUsuarioActual);

            if (intentosUsuario.isEmpty()) {
                // Mostrar mensaje o alerta de que no hay intentos
                System.out.println("El usuario no tiene intentos registrados.");
                // Aquí podrías mostrar un diálogo de alerta si tienes configurado JavaFX Alert
                return;
            }

            // Cargar la vista de resultados
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/resultados.fxml"));
            Parent resultsView = loader.load();

            // Obtener el controlador de resultados
            ResultsController resultsController = loader.getController();
            resultsController.setMainController(mainController);

            // Inicializar la vista de resultados sin un cuestionario específico
            // Esto cargará todos los intentos del usuario
            resultsController.initializeResults(null);

            // Cambiar a la escena de resultados
            mainController.changeSceneWithLoader(resultsView);

        } catch (Exception e) {
            System.err.println("Error al cargar la vista de resultados: " + e.getMessage());
            e.printStackTrace();

        }
    }


    @FXML
    private void handleQuitSession(){
        try {
            // Cerrar la sesión usando SessionManager
            if (sessionManager != null && sessionManager.hasActiveSession()) {
                sessionManager.closeSession();
                System.out.println("Sesión cerrada exitosamente");
            }

            // Redirigir a la vista de login
            mainController.changeScene("login.fxml");

        } catch (Exception e) {
            System.err.println("Error al cerrar sesión: " + e.getMessage());
            e.printStackTrace();

            // Aún así intentar redirigir al login en caso de error
            mainController.changeScene("login.fxml");
        }
    }
}

