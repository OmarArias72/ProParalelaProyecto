package controllers;


import dao.CuestionarioDAOImp;
import dao.IntentoDAOImp;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import models.Intento;
import models.Opcion;
import models.Pregunta;
import models.Respuesta;
import services.CuestionarioDAO;

import java.util.List;

public class ResultsController {

    private MainController mainController;
    private IntentoDAOImp intentoDAO = new IntentoDAOImp();
    private CuestionarioDAO cuestionarioDAO = new CuestionarioDAOImp();

    @FXML
    private Label scoreLabel;
    @FXML private ScrollPane answersScrollPane;
    @FXML private VBox answersContainer;

    public void setMainController(MainController controller) {
        this.mainController = controller;
    }

    // 🔹 Nuevo método para mostrar los resultados
    public void showResults(int idIntento) {
        // Hilo para obtener el puntaje y el número de intento
        Task<Intento> getIntentoTask = new Task<>() {
            @Override
            protected Intento call() {
                // Lógica para obtener el intento de la BD por su ID
                return intentoDAO.findById(idIntento).orElse(null);
            }
        };

        getIntentoTask.setOnSucceeded(event -> {
            Intento intento = getIntentoTask.getValue();
            if (intento != null) {
                // Muestra el puntaje
                scoreLabel.setText(String.format("Puntaje Obtenido: %.2f", intento.getTotal()));

                // 🔹 Condición para el tercer intento
                if (intento.getNoIntento() == 3) {
                    answersScrollPane.setVisible(true);
                    loadCorrectAnswers(intento.getIdCuestionario(), idIntento);
                } else {
                    answersScrollPane.setVisible(false);
                }
            } else {
                scoreLabel.setText("Error: No se encontró el intento.");
            }
        });

        new Thread(getIntentoTask).start();
    }

    private void loadCorrectAnswers(int idCuestionario, int idIntento) {
        // Hilo para cargar preguntas y respuestas del cuestionario y el intento
        Task<List<Pregunta>> loadAnswersTask = new Task<>() {
            @Override
            protected List<Pregunta> call() {
                return cuestionarioDAO.getPreguntasByCuestionario(idCuestionario);
            }
        };

        loadAnswersTask.setOnSucceeded(event -> {
            List<Pregunta> preguntas = loadAnswersTask.getValue();
            if (preguntas != null) {
                for (Pregunta pregunta : preguntas) {
                    Label questionLabel = new Label(pregunta.getPregunta());
                    // 🔹 Estilo para la pregunta
                    questionLabel.getStyleClass().add("question-result-label");
                    answersContainer.getChildren().add(questionLabel);

                    for (Opcion opcion : pregunta.getOpciones()) {
                        Label optionLabel = new Label("• " + opcion.getNombre());
                        if (opcion.isEsCorrecta()) {
                            // 🔹 Estilo para la respuesta correcta
                            optionLabel.getStyleClass().add("correct-answer-label");
                        }
                        // 🔹 Lógica para obtener y mostrar la respuesta del usuario (requiere otro DAO)
                        answersContainer.getChildren().add(optionLabel);
                    }
                }
            }
        });

        new Thread(loadAnswersTask).start();
    }

    @FXML
    private void handleGoHome() {
        mainController.changeScene("home.fxml");
    }

    public MainController getMainController() {
        return mainController;
    }
}