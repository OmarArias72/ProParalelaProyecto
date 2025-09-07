package controllers;

import dao.IntentoDAOImp;
import dao.UsuarioDAOImp;
import javafx.fxml.FXML;
import services.IntentoDAO;
import services.UsuarioService;
import services.UsuarioServiceImp;

public class HomeController {

    private MainController mainController;
    private UsuarioDAOImp usuarioDAO = new UsuarioDAOImp();
    private UsuarioService usuarioService = new UsuarioServiceImp();
    private IntentoDAO intentoDAO = new IntentoDAOImp();

    public void setMainController(MainController controller) {
        this.mainController = controller;
    }

    @FXML
    private void handleTakeQuiz(){
        mainController.changeScene("cuestionario.fxml");
    }

    @FXML
    private void handleViewResults(){
        intentoDAO.findLastByUsuarioId(mainController.getUsuarioActivo().getId());
        mainController.changeScene("resultados.fxml");
    }

    @FXML
    private void handleQuitSession(){

    }
}

