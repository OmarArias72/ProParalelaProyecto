package controllers;


import java.time.Duration;
import java.time.LocalTime;
import java.util.List;


import dao.CuestionarioDAOImp;
import dao.IntentoDAOImp; // 🔹 Importar el nuevo DAO
import dao.RespuestaDAOImp;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import models.Cuestionario;
import models.Intento; // 🔹 Importar la clase Intento
import models.Opcion;
import models.Pregunta;
import models.Respuesta;
import services.CuestionarioDAO;
import services.RespuestaDAO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

public class QuizController {

    private MainController mainController;
    private CuestionarioDAO cuestionarioDAO = new CuestionarioDAOImp();
    private RespuestaDAO respuestaDAO = new RespuestaDAOImp();
    private IntentoDAOImp intentoDAO = new IntentoDAOImp();

    private Cuestionario cuestionario;
    private List<Pregunta> preguntas;
    private int currentQuestionIndex = 0;
    private ToggleGroup optionsGroup = new ToggleGroup();
    private int idIntento;
    private int respuestasCorrectas = 0;

    // 🔹 Nuevas variables para el temporizador
    private LocalTime startTime;
    private LocalTime elapsedTime;
    private boolean timerRunning = false;
    private Thread timerThread;

    @FXML private Label questionNumberLabel;
    @FXML private Label questionTextLabel;
    @FXML private VBox optionsContainer;
    @FXML private Button nextButton;
    @FXML private Label messageLabel;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Label timerLabel;;

    public void setMainController(MainController controller) {
        this.mainController = controller;
    }

    public void loadCuestionario(int idCuestionario) {
        loadingIndicator.setVisible(true);
        nextButton.setDisable(true);
        messageLabel.setText("Preparando cuestionario...");

        // 🔹 Iniciar un nuevo intento antes de cargar las preguntas
        Task<Integer> startIntentoTask = new Task<>() {
            @Override
            protected Integer call() throws Exception {
                int userId = mainController.getUsuarioActivo().getId();

                // 1. Primero verificar si hay un intento incompleto existente
                Optional<Intento> intentoIncompleto = intentoDAO.findIncompleteByUserAndQuiz(userId, idCuestionario);

                if (intentoIncompleto.isPresent()) {
                    // Si existe un intento incompleto, usar ese
                    Intento intentoExistente = intentoIncompleto.get();
                    System.out.println("Reanudando intento existente con ID: " + intentoExistente.getIdIntento());
                    return intentoExistente.getIdIntento();
                } else {
                    // No hay intento incompleto, crear uno nuevo
                    // 2. Obtener el último intento del usuario para calcular el número de intento
                    Optional<Intento> lastIntentoOpt = intentoDAO.findLastByCuestionarioId(userId, idCuestionario);
                    int noIntento = 1;
                    if (lastIntentoOpt.isPresent()) {
                        noIntento = lastIntentoOpt.get().getNoIntento() + 1;
                    }

                    // 3. Crear un nuevo objeto Intento
                    Intento nuevoIntento = new Intento();
                    nuevoIntento.setIdUsuario(userId);
                    nuevoIntento.setIdCuestionario(idCuestionario);
                    nuevoIntento.setNoIntento(noIntento);

                    // 4. Guardar el nuevo intento en la BD
                    if (intentoDAO.save(nuevoIntento)) {
                        System.out.println("Nuevo intento creado con ID: " + nuevoIntento.getIdIntento());
                        return nuevoIntento.getIdIntento(); // Devolver el ID generado
                    } else {
                        return -1; // Indicar fallo
                    }
                }
            }
        };

        startIntentoTask.setOnSucceeded(event -> {
            int newIntentoId = startIntentoTask.getValue();
            if (newIntentoId != -1) {
                this.idIntento = newIntentoId;
                System.out.println("Numero intento: " + idIntento);
                messageLabel.setText("Cargando preguntas...");
                // Continuar con la carga de las preguntas
                loadQuestions(idCuestionario);
            } else {
                messageLabel.setText("Error al iniciar el intento.");
                loadingIndicator.setVisible(false);
                nextButton.setDisable(true);
            }
        });

        startIntentoTask.setOnFailed(event -> {
            messageLabel.setText("Error inesperado al iniciar el intento.");
            loadingIndicator.setVisible(false);
            nextButton.setDisable(true);
            event.getSource().getException().printStackTrace();
        });

        new Thread(startIntentoTask).start();
    }

    // 🔹 Nuevo método para cargar solo las preguntas
    private void loadQuestions(int idCuestionario) {
        startTimer();
        Task<Cuestionario> loadTask = new Task<>() {
            @Override
            protected Cuestionario call() {
                return cuestionarioDAO.getCuestionario(idCuestionario);
            }
        };

        loadTask.setOnSucceeded(event -> {
            cuestionario = loadTask.getValue();
            if (cuestionario != null && !cuestionario.getPreguntas().isEmpty()) {
                this.preguntas = cuestionario.getPreguntas();
                showQuestion(currentQuestionIndex);
                nextButton.setDisable(false);
            } else {
                messageLabel.setText("No se pudo cargar el cuestionario.");
                nextButton.setDisable(true);
            }
            loadingIndicator.setVisible(false);
        });

        loadTask.setOnFailed(event -> {
            messageLabel.setText("Error al cargar el cuestionario.");
            loadingIndicator.setVisible(false);
            nextButton.setDisable(true);
            event.getSource().getException().printStackTrace();
        });

        new Thread(loadTask).start();
    }

    private void showQuestion(int index) {
        if (index >= 0 && index < preguntas.size()) {
            Pregunta pregunta = preguntas.get(index);
            questionNumberLabel.setText("Pregunta " + (index + 1) + " de " + preguntas.size());
            questionTextLabel.setText(pregunta.getPregunta());
            optionsContainer.getChildren().clear();
            optionsGroup = new ToggleGroup();

            for (Opcion opcion : pregunta.getOpciones()) {
                RadioButton rb = new RadioButton(opcion.getNombre());
                rb.setToggleGroup(optionsGroup);
                rb.setUserData(opcion.getId()); // Almacena el ID de la opción
                optionsContainer.getChildren().add(rb);
            }

            if (index == preguntas.size() - 1) {
                nextButton.setText("Finalizar");
            } else {
                nextButton.setText("Siguiente");
            }
        }
    }



    @FXML
    private void handleNextQuestion() {
        RadioButton selectedRadioButton = (RadioButton) optionsGroup.getSelectedToggle();
        if (selectedRadioButton == null) {
            messageLabel.setText("Por favor, selecciona una opción.");
            return;
        }

        int selectedOptionId = (int) selectedRadioButton.getUserData();
        int currentQuestionId = preguntas.get(currentQuestionIndex).getId();

        Respuesta respuesta = new Respuesta();
        respuesta.setIdIntento(idIntento);
        respuesta.setIdPregunta(currentQuestionId);
        respuesta.setIdOpcion(selectedOptionId);

        Task<Boolean> saveTask = new Task<>() {
            @Override
            protected Boolean call() {
                return respuestaDAO.save(respuesta);
            }
        };

        saveTask.setOnSucceeded(event -> {
            if (saveTask.getValue()) {
                messageLabel.setText("");
                currentQuestionIndex++;
                if (currentQuestionIndex < preguntas.size()) {
                    showQuestion(currentQuestionIndex);
                } else {
                    // 🔹 Finalizar cuestionario y calcular resultados
                    Task<Void> finishTask = new Task<>() {
                        @Override
                        protected Void call() throws Exception {
                            calcularYActualizarResultados();
                            return null;
                        }
                    };

                    finishTask.setOnSucceeded(e -> {
                        mainController.changeScene("resultados.fxml", idIntento);
                    });

                    finishTask.setOnFailed(e -> {
                        messageLabel.setText("Error al calcular resultados");
                        e.getSource().getException().printStackTrace();
                    });

                    new Thread(finishTask).start();
                }
            } else {
                messageLabel.setText("Error al guardar la respuesta.");
            }
        });

        saveTask.setOnFailed(event -> {
            messageLabel.setText("Error inesperado al guardar la respuesta.");
            event.getSource().getException().printStackTrace();
        });

        new Thread(saveTask).start();
    }


    // 🔹 Método para calcular resultados y actualizar el intento
    private void calcularYActualizarResultados() {
        try {
            // Detener el temporizador primero
            stopTimer();

            // 1. Obtener todas las respuestas del intento
            List<Respuesta> respuestasUsuario = respuestaDAO.findByIntentoId(idIntento);

            // 2. Obtener el cuestionario completo con respuestas correctas
            Cuestionario cuestionarioCompleto = cuestionarioDAO.getCuestionario(cuestionario.getId());

            int respuestasCorrectas = 0;
            int totalPreguntas = cuestionarioCompleto.getPreguntas().size();

            // 3. Comparar cada respuesta del usuario con la correcta
            for (Respuesta respuesta : respuestasUsuario) {
                // Encontrar la pregunta correspondiente
                Pregunta pregunta = cuestionarioCompleto.getPreguntas().stream()
                        .filter(p -> p.getId() == respuesta.getIdPregunta())
                        .findFirst()
                        .orElse(null);

                if (pregunta != null) {
                    // Encontrar la opción seleccionada por el usuario
                    Opcion opcionSeleccionada = pregunta.getOpciones().stream()
                            .filter(o -> o.getId() == respuesta.getIdOpcion())
                            .findFirst()
                            .orElse(null);

                    // Si la opción seleccionada es correcta, sumar punto
                    if (opcionSeleccionada != null && opcionSeleccionada.isEsCorrecta()) {
                        respuestasCorrectas++;
                    }
                }
            }

            // 4. Calcular puntuación (por ejemplo, porcentaje)
            double puntuacion = (double) respuestasCorrectas / totalPreguntas * 100;

            // 5. Obtener el objeto Intento y actualizarlo
            Optional<Intento> intentoOpt = intentoDAO.findById(idIntento);
            if (intentoOpt.isPresent()) {
                Intento intento = intentoOpt.get();
                intento.setTotal(puntuacion);
                intento.setRespuestasCorrectas(respuestasCorrectas);
                // 🔹 Establecer el tiempo completado (ya calculado en stopTimer())
                intento.setTiempoCompletado(elapsedTime);

                // 6. Actualizar el intento en la base de datos
                boolean actualizado = intentoDAO.update(intento);
                if (!actualizado) {
                    throw new RuntimeException("No se pudo actualizar el intento en la base de datos");
                }
            } else {
                throw new RuntimeException("No se encontró el intento con ID: " + idIntento);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error al calcular resultados", e);
        }
    }

    @FXML
    private void finishQuizz() {

    }
    // 🔹 Método para iniciar el temporizador
    private void startTimer() {
        startTime = LocalTime.now();
        timerRunning = true;

        timerThread = new Thread(() -> {
            while (timerRunning) {
                try {
                    Thread.sleep(1000); // Actualizar cada segundo
                    Platform.runLater(() -> updateTimerDisplay());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        timerThread.setDaemon(true);
        timerThread.start();
    }

    // 🔹 Método para actualizar la visualización del temporizador
    private void updateTimerDisplay() {
        if (timerRunning) {
            elapsedTime = LocalTime.now().minusNanos(startTime.toNanoOfDay());
            String timeFormatted = String.format("%02d:%02d:%02d",
                    elapsedTime.getHour(),
                    elapsedTime.getMinute(),
                    elapsedTime.getSecond());
            timerLabel.setText("Tiempo: " + timeFormatted);
        }
    }

    // 🔹 Método para detener el temporizador
    private void stopTimer() {
        timerRunning = false;
        if (timerThread != null && timerThread.isAlive()) {
            timerThread.interrupt();
        }
        // Calcular el tiempo transcurrido con un Duration
        Duration duration = Duration.between(startTime, LocalTime.now());
        elapsedTime = LocalTime.ofSecondOfDay(duration.getSeconds());
    }

    @FXML
    private void initialize() {
        // Detener el temporizador cuando se cierra la ventana
        Platform.runLater(() -> {
            Scene scene = timerLabel.getScene();
            if (scene != null) {
                scene.windowProperty().addListener((observable, oldWindow, newWindow) -> {
                    if (newWindow != null) {
                        newWindow.setOnHidden(e -> stopTimer());
                    }
                });
            }
        });
    }
}