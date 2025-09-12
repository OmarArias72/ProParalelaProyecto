package controllers;

import dao.CuestionarioDAOImp;
import dao.IntentoDAOImp;
import dao.RespuestaDAOImp;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import models.Intento;
import models.Opcion;
import models.Pregunta;
import models.Respuesta;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ResultsController implements Initializable {

    private MainController mainController;
    private IntentoDAOImp intentoDAO = new IntentoDAOImp();
    private CuestionarioDAOImp cuestionarioDAO = new CuestionarioDAOImp();
    private RespuestaDAOImp respuestaDAO = new RespuestaDAOImp();

    @FXML
    private ComboBox<String> intentoComboBox;
    @FXML
    private Label scoreLabel;
    @FXML
    private Label classificationLabel;
    @FXML
    private Label percentageLabel;
    @FXML
    private Label descriptionLabel;
    @FXML
    private ScrollPane answersScrollPane;
    @FXML
    private VBox answersContainer;
    @FXML
    private VBox resultsSummaryContainer;

    private int idUsuarioActual;
    private int idCuestionarioActual = -1;
    private Integer selectedIntentoId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Inicializar componentes si es necesario
        clearResults();
    }

    public void setMainController(MainController controller) {
        this.mainController = controller;
        this.idUsuarioActual = mainController.getUsuarioActivo().getId();
    }

    /**
     * Método principal para inicializar la vista de resultados
     * Puede recibir un ID de cuestionario específico o cargar todos los intentos del usuario
     */
    public void initializeResults(Integer idCuestionario) {
        if (idCuestionario != null && idCuestionario > 0) {
            this.idCuestionarioActual = idCuestionario;
            loadUserAttempts();
        } else {
            // Cargar todos los intentos del usuario de todos los cuestionarios
            loadAllUserAttempts();
        }
    }

    /**
     * Cargar intentos de un cuestionario específico
     */
    private void loadUserAttempts() {
        Task<List<Intento>> loadAttemptsTask = new Task<>() {
            @Override
            protected List<Intento> call() throws Exception {
                return intentoDAO.findByUsuarioAndCuestionario(idUsuarioActual, idCuestionarioActual);
            }
        };

        loadAttemptsTask.setOnSucceeded(event -> {
            List<Intento> intentos = loadAttemptsTask.getValue();
            if (intentos != null && !intentos.isEmpty()) {
                populateAttemptComboBox(intentos);
                // Auto-seleccionar el último intento si existe
                if (!intentos.isEmpty()) {
                    intentoComboBox.getSelectionModel().selectLast();
                    handleLoadSelectedAttempt(); // Cargar automáticamente el último intento
                }
            } else {
                intentoComboBox.setPromptText("No hay intentos registrados para este cuestionario");
                clearResults();
            }
        });

        loadAttemptsTask.setOnFailed(event -> {
            intentoComboBox.setPromptText("Error al cargar intentos");
            clearResults();
            loadAttemptsTask.getException().printStackTrace();
        });

        new Thread(loadAttemptsTask).start();
    }

    /**
     * Cargar todos los intentos del usuario (de todos los cuestionarios)
     */
    private void loadAllUserAttempts() {
        Task<List<Intento>> loadAllAttemptsTask = new Task<>() {
            @Override
            protected List<Intento> call() throws Exception {
                return intentoDAO.findByUsuarioId(idUsuarioActual);
            }
        };

        loadAllAttemptsTask.setOnSucceeded(event -> {
            List<Intento> intentos = loadAllAttemptsTask.getValue();
            if (intentos != null && !intentos.isEmpty()) {
                populateAttemptComboBoxWithQuizInfo(intentos);
            } else {
                intentoComboBox.setPromptText("No hay intentos registrados");
                clearResults();
            }
        });

        loadAllAttemptsTask.setOnFailed(event -> {
            intentoComboBox.setPromptText("Error al cargar intentos");
            clearResults();
            loadAllAttemptsTask.getException().printStackTrace();
        });

        new Thread(loadAllAttemptsTask).start();
    }

    /**
     * Popular el ComboBox con intentos de un cuestionario específico
     */
    private void populateAttemptComboBox(List<Intento> intentos) {
        ObservableList<String> items = FXCollections.observableArrayList();
        for (Intento intento : intentos) {
            String item = String.format("Intento %d - %.2f puntos (ID: %d)",
                    intento.getNoIntento(),
                    intento.getTotal(),
                    intento.getIdIntento());
            items.add(item);
        }
        intentoComboBox.setItems(items);
    }

    /**
     * Popular el ComboBox con intentos de múltiples cuestionarios
     */
    private void populateAttemptComboBoxWithQuizInfo(List<Intento> intentos) {
        ObservableList<String> items = FXCollections.observableArrayList();
        for (Intento intento : intentos) {
            String nombreCuestionario = obtenerNombreCuestionario(intento.getIdCuestionario());
            String item = String.format("%s - Intento %d - %.2f puntos (ID: %d)",
                    nombreCuestionario,
                    intento.getNoIntento(),
                    intento.getTotal(),
                    intento.getIdIntento());
            items.add(item);
        }
        intentoComboBox.setItems(items);
    }

    /**
     * Obtener el nombre de un cuestionario por su ID
     */
    private String obtenerNombreCuestionario(int idCuestionario) {
        // Asumiendo que tienes un método en el DAO para obtener el nombre
        // Si no existe, puedes implementarlo o usar "Cuestionario #" + idCuestionario
        try {
            // return cuestionarioDAO.findById(idCuestionario).map(c -> c.getNombre()).orElse("Cuestionario " + idCuestionario);
            return "Cuestionario " + idCuestionario; // Temporal
        } catch (Exception e) {
            return "Cuestionario " + idCuestionario;
        }
    }

    /**
     * Manejar la selección de un intento del ComboBox
     */
    @FXML
    private void handleLoadSelectedAttempt() {
        String selectedItem = intentoComboBox.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            try {
                int idIntento = extractIntentoId(selectedItem);
                showResults(idIntento);
            } catch (NumberFormatException e) {
                System.err.println("Formato de ID de intento inválido: " + selectedItem);
                displayErrorMessage("Error al procesar la selección del intento.");
            }
        } else {
            displayErrorMessage("Por favor, selecciona un intento para ver sus resultados.");
        }
    }

    /**
     * Extraer el ID del intento de la cadena del ComboBox
     */
    private int extractIntentoId(String selectedItem) {
        int startIndex = selectedItem.lastIndexOf("(ID: ") + 5;
        int endIndex = selectedItem.lastIndexOf(")");
        return Integer.parseInt(selectedItem.substring(startIndex, endIndex));
    }

    /**
     * Mostrar los resultados de un intento específico
     */
    public void showResults(int idIntento) {
        Task<CuestionarioDAOImp.ResultadoDetallado> getResultTask = new Task<>() {
            @Override
            protected CuestionarioDAOImp.ResultadoDetallado call() {
                Optional<Intento> intentoOpt = intentoDAO.findById(idIntento);
                if (intentoOpt.isPresent()) {
                    Intento intento = intentoOpt.get();

                    // Actualizar el cuestionario actual si es necesario
                    if (idCuestionarioActual == -1) {
                        idCuestionarioActual = intento.getIdCuestionario();
                    }

                    int totalPreguntas = cuestionarioDAO.getPreguntasByCuestionario(intento.getIdCuestionario()).size();
                    intento.setTotalPreguntas(totalPreguntas);

                    return cuestionarioDAO.obtenerResultadoDetallado(intento, totalPreguntas);
                }
                return null;
            }
        };

        getResultTask.setOnSucceeded(event -> {
            CuestionarioDAOImp.ResultadoDetallado resultado = getResultTask.getValue();
            if (resultado != null) {
                displayResultSummary(resultado);

                // Mostrar respuestas correctas solo en el tercer intento o según tus reglas de negocio
                if (resultado.getIntento().getNoIntento() == 3) {
                    answersScrollPane.setVisible(true);
                    loadAnswersComparison(resultado.getIntento().getIdCuestionario(),
                            resultado.getIntento().getIdIntento());
                } else {
                    answersScrollPane.setVisible(false);
                }
            } else {
                displayErrorMessage("No se pudieron cargar los resultados del intento seleccionado.");
            }
        });

        getResultTask.setOnFailed(event -> {
            displayErrorMessage("Error al cargar los resultados.");
            getResultTask.getException().printStackTrace();
        });

        new Thread(getResultTask).start();
    }

    /**
     * Limpiar los resultados mostrados
     */
    private void clearResults() {
        scoreLabel.setText("Puntaje: --");
        if (percentageLabel != null) percentageLabel.setText("Porcentaje de aciertos: --");
        if (classificationLabel != null) classificationLabel.setText("Clasificación: --");
        if (descriptionLabel != null) descriptionLabel.setText("--");
        if (answersContainer != null) answersContainer.getChildren().clear();
        if (answersScrollPane != null) answersScrollPane.setVisible(false);
        if (resultsSummaryContainer != null) resultsSummaryContainer.getChildren().clear();
    }

    private void displayResultSummary(CuestionarioDAOImp.ResultadoDetallado resultado) {
        Intento intento = resultado.getIntento();

        // Mostrar puntaje
        scoreLabel.setText(String.format("Puntaje: %.2f puntos", intento.getTotal()));

        // Mostrar porcentaje
        if (percentageLabel != null) {
            percentageLabel.setText(String.format("Porcentaje de aciertos: %.1f%%", resultado.getPorcentajeCorrectas()));
        }

        // Mostrar clasificación
        if (classificationLabel != null) {
            classificationLabel.setText("Clasificación: " + resultado.getClasificacion());

            // Aplicar estilo basado en la clasificación
            classificationLabel.getStyleClass().removeAll(
                    "excellent-classification", "very-good-classification",
                    "good-classification", "regular-classification",
                    "sufficient-classification", "insufficient-classification"
            );

            switch (resultado.getClasificacion()) {
                case "EXCELENTE":
                    classificationLabel.getStyleClass().add("excellent-classification");
                    break;
                case "MUY BUENO":
                    classificationLabel.getStyleClass().add("very-good-classification");
                    break;
                case "BUENO":
                    classificationLabel.getStyleClass().add("good-classification");
                    break;
                case "REGULAR":
                    classificationLabel.getStyleClass().add("regular-classification");
                    break;
                case "SUFICIENTE":
                    classificationLabel.getStyleClass().add("sufficient-classification");
                    break;
                case "INSUFICIENTE":
                    classificationLabel.getStyleClass().add("insufficient-classification");
                    break;
            }
        }

        // Mostrar descripción
        if (descriptionLabel != null) {
            descriptionLabel.setText(resultado.getDescripcionClasificacion());
        }

        // Crear resumen adicional
        createAdditionalSummary(intento);
    }

    private void createAdditionalSummary(Intento intento) {
        if (resultsSummaryContainer != null) {
            // Limpiar contenido previo pero mantener las etiquetas principales
            resultsSummaryContainer.getChildren().clear();

            // Información del intento
            Label intentoInfo = new Label(String.format("Intento número: %d", intento.getNoIntento()));
            intentoInfo.getStyleClass().add("summary-info-label");

            // Respuestas correctas vs incorrectas
            Label correctasInfo = new Label(String.format("Respuestas correctas: %d de %d",
                    intento.getRespuestasCorrectas(), intento.getTotalPreguntas()));
            correctasInfo.getStyleClass().add("summary-info-label");

            Label incorrectasInfo = new Label(String.format("Respuestas incorrectas: %d",
                    intento.getRespuestasIncorrectas()));
            incorrectasInfo.getStyleClass().add("summary-info-label");

            // Tiempo completado
            if (intento.getTiempoCompletado() != null) {
                Label tiempoInfo = new Label(String.format("Tiempo completado: %s",
                        intento.getTiempoCompletado().toString()));
                tiempoInfo.getStyleClass().add("summary-info-label");
                resultsSummaryContainer.getChildren().add(tiempoInfo);
            }

            // Estado de aprobación
            Label estadoInfo = new Label(intento.isAprobado() ? "✓ APROBADO" : "✗ NO APROBADO");
            estadoInfo.getStyleClass().add(intento.isAprobado() ? "approved-label" : "failed-label");

            resultsSummaryContainer.getChildren().addAll(
                    intentoInfo, correctasInfo, incorrectasInfo, estadoInfo
            );
        }
    }

    private void loadAnswersComparison(int idCuestionario, int idIntento) {
        Task<ComparisonData> loadComparisonTask = new Task<>() {
            @Override
            protected ComparisonData call() {
                // Obtener preguntas con respuestas correctas
                List<Pregunta> preguntasConCorrectas = cuestionarioDAO.getPreguntasConRespuestasCorrectas(idCuestionario);

                // Obtener respuestas del usuario
                List<Respuesta> respuestasUsuario = respuestaDAO.findByIntentoId(idIntento);

                return new ComparisonData(preguntasConCorrectas, respuestasUsuario);
            }
        };

        loadComparisonTask.setOnSucceeded(event -> {
            ComparisonData data = loadComparisonTask.getValue();
            if (data != null) {
                displayAnswersComparison(data);
            }
        });

        loadComparisonTask.setOnFailed(event -> {
            Label errorLabel = new Label("Error al cargar las respuestas correctas.");
            errorLabel.getStyleClass().add("error-label");
            answersContainer.getChildren().add(errorLabel);
        });

        new Thread(loadComparisonTask).start();
    }

    private void displayAnswersComparison(ComparisonData data) {
        answersContainer.getChildren().clear();

        // Título de la sección
        Label titleLabel = new Label("📋 RESPUESTAS CORRECTAS Y COMPARACIÓN");
        titleLabel.getStyleClass().add("answers-title-label");
        answersContainer.getChildren().add(titleLabel);

        for (Pregunta pregunta : data.preguntasConCorrectas) {
            // Contenedor para la pregunta
            VBox questionBox = new VBox(10);
            questionBox.setPadding(new Insets(15, 10, 15, 10));
            questionBox.getStyleClass().add("question-comparison-box");

            // Título de la pregunta
            Label questionLabel = new Label(pregunta.getPregunta());
            questionLabel.getStyleClass().add("question-comparison-label");
            questionBox.getChildren().add(questionLabel);

            // Mostrar respuestas correctas
            Label correctAnswersTitle = new Label("✅ Respuestas correctas:");
            correctAnswersTitle.getStyleClass().add("correct-answers-title");
            questionBox.getChildren().add(correctAnswersTitle);

            for (Opcion opcion : pregunta.getOpciones()) {
                if (opcion.isEsCorrecta()) {
                    Label correctOptionLabel = new Label("• " + opcion.getNombre());
                    correctOptionLabel.getStyleClass().add("correct-option-label");
                    questionBox.getChildren().add(correctOptionLabel);
                }
            }

            // Mostrar respuesta del usuario si existe
            Optional<Respuesta> respuestaUsuario = data.respuestasUsuario.stream()
                    .filter(r -> r.getIdPregunta() == pregunta.getId())
                    .findFirst();

            if (respuestaUsuario.isPresent()) {
                Label userAnswerTitle = new Label("👤 Tu respuesta:");
                userAnswerTitle.getStyleClass().add("user-answer-title");
                questionBox.getChildren().add(userAnswerTitle);

                // Obtener el texto de la opción seleccionada por el usuario
                String textoRespuestaUsuario = obtenerTextoOpcion(pregunta.getId(), respuestaUsuario.get().getIdOpcion());
                Label userAnswerLabel = new Label("• " + textoRespuestaUsuario);

                // Determinar si la respuesta fue correcta
                boolean esCorrectaUsuario = verificarRespuestaCorrecta(pregunta.getId(), respuestaUsuario.get().getIdOpcion());
                userAnswerLabel.getStyleClass().add(esCorrectaUsuario ? "user-correct-answer" : "user-incorrect-answer");

                questionBox.getChildren().add(userAnswerLabel);

                // Indicador visual
                String indicador = esCorrectaUsuario ? "✅ Correcto" : "❌ Incorrecto";
                Label statusLabel = new Label(indicador);
                statusLabel.getStyleClass().add(esCorrectaUsuario ? "status-correct" : "status-incorrect");
                questionBox.getChildren().add(statusLabel);
            } else {
                Label noAnswerLabel = new Label("⚠️ No se encontró respuesta para esta pregunta");
                noAnswerLabel.getStyleClass().add("no-answer-label");
                questionBox.getChildren().add(noAnswerLabel);
            }

            answersContainer.getChildren().add(questionBox);
        }
    }

    private String obtenerTextoOpcion(int idPregunta, int idOpcion) {
        List<Opcion> opciones = cuestionarioDAO.getOpcionesByPregunta(idPregunta);
        return opciones.stream()
                .filter(o -> o.getId() == idOpcion)
                .findFirst()
                .map(Opcion::getNombre)
                .orElse("Respuesta no encontrada");
    }

    private boolean verificarRespuestaCorrecta(int idPregunta, int idOpcion) {
        List<Opcion> opciones = cuestionarioDAO.getOpcionesByPregunta(idPregunta);
        return opciones.stream()
                .filter(o -> o.getId() == idOpcion)
                .findFirst()
                .map(Opcion::isEsCorrecta)
                .orElse(false);
    }

    private void displayErrorMessage(String mensaje) {
        scoreLabel.setText("Error: " + mensaje);
        if (classificationLabel != null) {
            classificationLabel.setText("");
        }
        if (percentageLabel != null) {
            percentageLabel.setText("");
        }
        if (descriptionLabel != null) {
            descriptionLabel.setText("");
        }
        if (answersContainer != null) {
            answersContainer.getChildren().clear();
        }
        if (answersScrollPane != null) {
            answersScrollPane.setVisible(false);
        }
    }

    private void displayErrorMessage() {
        displayErrorMessage("No se pudieron cargar los resultados.");
    }

    @FXML
    private void handleGoHome() {
        mainController.changeScene("home.fxml");
    }

    public MainController getMainController() {
        return mainController;
    }

    // Clase interna para encapsular los datos de comparación
    private static class ComparisonData {
        final List<Pregunta> preguntasConCorrectas;
        final List<Respuesta> respuestasUsuario;

        ComparisonData(List<Pregunta> preguntasConCorrectas, List<Respuesta> respuestasUsuario) {
            this.preguntasConCorrectas = preguntasConCorrectas;
            this.respuestasUsuario = respuestasUsuario;
        }
    }
}