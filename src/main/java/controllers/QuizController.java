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
import java.util.concurrent.atomic.AtomicBoolean;

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

    private final Object quizLock = new Object();
    private final AtomicBoolean isProcessing = new AtomicBoolean(false);

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
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                loadingIndicator.setVisible(true);
                nextButton.setDisable(true);
                messageLabel.setText("Preparando cuestionario...");
            }
        });

        // Implementación tradicional con Runnable para iniciar intento
        Runnable startIntentoTask = new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("Iniciando proceso de intento en hilo: " + Thread.currentThread().getName());

                    int userId = mainController.getUsuarioActivo().getId();
                    int newIntentoId = -1;

                    // 1. Obtener el número de intentos del usuario para este cuestionario
                    int intentosRealizados = intentoDAO.countCompletedAttempts(userId, idCuestionario);
                    System.out.println("Intentos completados: " + intentosRealizados);

                    // 2. Si el número de intentos completados es 3 o más, abortar la ejecución
                    if (intentosRealizados >= 3) {
                        Platform.runLater(() -> {
                            messageLabel.setText("Límite de intentos (3) alcanzado para este cuestionario.");
                            loadingIndicator.setVisible(false);
                            nextButton.setDisable(true);
                        });
                        return; //Abortar la ejecución
                    }

                    //Verificar si hay un intento incompleto existente
                    Optional<Intento> intentoIncompleto = intentoDAO.findIncompleteByUserAndQuiz(userId, idCuestionario);

                    if (intentoIncompleto.isPresent()) {
                        // Si existe un intento incompleto, usar ese
                        Intento intentoExistente = intentoIncompleto.get();
                        newIntentoId = intentoExistente.getIdIntento();
                        System.out.println("Reanudando intento existente con ID: " + newIntentoId);
                    } else {
                        // No hay intento incompleto, crear uno nuevo
                        Optional<Intento> lastIntentoOpt = intentoDAO.findLastByCuestionarioId(userId, idCuestionario);
                        int noIntento = 1;
                        if (lastIntentoOpt.isPresent()) {
                            noIntento = lastIntentoOpt.get().getNoIntento() + 1;
                        }

                        // Crear un nuevo objeto Intento
                        Intento nuevoIntento = new Intento();
                        nuevoIntento.setIdUsuario(userId);
                        nuevoIntento.setIdCuestionario(idCuestionario);
                        nuevoIntento.setNoIntento(noIntento);

                        // Guardar el nuevo intento en la BD
                        if (intentoDAO.save(nuevoIntento)) {
                            newIntentoId = nuevoIntento.getIdIntento();
                            System.out.println("Nuevo intento creado con ID: " + newIntentoId);
                        }
                    }

                    final int finalIntentoId = newIntentoId;

                    // Actualizar UI en el hilo principal
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if (finalIntentoId != -1) {
                                idIntento = finalIntentoId;
                                System.out.println("Numero intento: " + idIntento);
                                messageLabel.setText("Cargando preguntas...");
                                // Continuar con la carga de las preguntas
                                loadQuestions(idCuestionario);
                            } else {
                                messageLabel.setText("Error al iniciar el intento.");
                                loadingIndicator.setVisible(false);
                                nextButton.setDisable(true);
                            }
                        }
                    });

                } catch (Exception e) {
                    System.err.println("Error inesperado al iniciar el intento: " + e.getMessage());
                    e.printStackTrace();

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            messageLabel.setText("Error inesperado al iniciar el intento.");
                            loadingIndicator.setVisible(false);
                            nextButton.setDisable(true);
                        }
                    });
                }
            }
        };

        // Ejecutar en un hilo separado
        Thread startIntentoThread = new Thread(startIntentoTask, "StartIntentoTask");
        startIntentoThread.setDaemon(true);
        startIntentoThread.start();
    }

    // 🔹 Nuevo método para cargar solo las preguntas
    private void loadQuestions(int idCuestionario) {
        startTimer();

        Runnable loadQuestionsTask = new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("Cargando preguntas en hilo: " + Thread.currentThread().getName());

                    // Simular tiempo de carga
                    Thread.sleep(500);

                    // Cargar cuestionario
                    Cuestionario loadedCuestionario = cuestionarioDAO.getCuestionario(idCuestionario);

                    // Actualizar UI en el hilo principal
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            cuestionario = loadedCuestionario;
                            if (cuestionario != null && !cuestionario.getPreguntas().isEmpty()) {
                                preguntas = cuestionario.getPreguntas();
                                showQuestion(currentQuestionIndex);
                                nextButton.setDisable(false);
                                messageLabel.setText("Cuestionario cargado correctamente");
                            } else {
                                messageLabel.setText("No se pudo cargar el cuestionario.");
                                nextButton.setDisable(true);
                            }
                            loadingIndicator.setVisible(false);
                        }
                    });

                } catch (Exception e) {
                    System.err.println("Error al cargar el cuestionario: " + e.getMessage());
                    e.printStackTrace();

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            messageLabel.setText("Error al cargar el cuestionario.");
                            loadingIndicator.setVisible(false);
                            nextButton.setDisable(true);
                        }
                    });
                }
            }
        };

        // Ejecutar en un hilo separado
        Thread loadQuestionsThread = new Thread(loadQuestionsTask, "LoadQuestionsTask");
        loadQuestionsThread.setDaemon(true);
        loadQuestionsThread.start();
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
        // Evitar procesamiento múltiple
        if (isProcessing.get()) {
            return;
        }

        RadioButton selectedRadioButton = (RadioButton) optionsGroup.getSelectedToggle();
        if (selectedRadioButton == null) {
            messageLabel.setText("Por favor, selecciona una opción.");
            return;
        }

        isProcessing.set(true);
        nextButton.setDisable(true);

        int selectedOptionId = (int) selectedRadioButton.getUserData();
        int currentQuestionId = preguntas.get(currentQuestionIndex).getId();

        Respuesta respuesta = new Respuesta();
        respuesta.setIdIntento(idIntento);
        respuesta.setIdPregunta(currentQuestionId);
        respuesta.setIdOpcion(selectedOptionId);

        // Implementación tradicional con Runnable para guardar respuesta
        Runnable saveResponseTask = new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("Guardando respuesta en hilo: " + Thread.currentThread().getName());

                    // Simular tiempo de guardado
                    Thread.sleep(200);

                    boolean saveResult = respuestaDAO.save(respuesta);

                    // Actualizar UI en el hilo principal
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            isProcessing.set(false);
                            nextButton.setDisable(false);

                            if (saveResult) {
                                messageLabel.setText("");
                                currentQuestionIndex++;
                                if (currentQuestionIndex < preguntas.size()) {
                                    showQuestion(currentQuestionIndex);
                                } else {
                                    // Finalizar cuestionario y calcular resultados
                                    nextButton.setDisable(true);
                                    messageLabel.setText("Calculando resultados...");
                                    loadingIndicator.setVisible(true);

                                    // Ejecutar cálculo en hilo separado
                                    Thread calculateResultsThread = new Thread(
                                            new CalculateResultsRunnable(),
                                            "CalculateResultsTask"
                                    );
                                    calculateResultsThread.setDaemon(true);
                                    calculateResultsThread.start();
                                }
                            } else {
                                messageLabel.setText("Error al guardar la respuesta.");
                            }
                        }
                    });

                } catch (Exception e) {
                    System.err.println("Error inesperado al guardar la respuesta: " + e.getMessage());
                    e.printStackTrace();

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            isProcessing.set(false);
                            nextButton.setDisable(false);
                            messageLabel.setText("Error inesperado al guardar la respuesta.");
                        }
                    });
                }
            }
        };

        // Ejecutar en un hilo separado
        Thread saveResponseThread = new Thread(saveResponseTask, "SaveResponseTask");
        saveResponseThread.setDaemon(true);
        saveResponseThread.start();
    }


    // 🔹 Método para calcular resultados y actualizar el intento
    private class CalculateResultsRunnable implements Runnable {
        @Override
        public void run() {
            try {
                System.out.println("Calculando resultados en hilo: " + Thread.currentThread().getName());

                // Detener el temporizador primero
                stopTimer();

                // Simular tiempo de cálculo
                Thread.sleep(1000);

                synchronized (quizLock) {
                    // 1. Obtener todas las respuestas del intento
                    List<Respuesta> respuestasUsuario = respuestaDAO.findByIntentoId(idIntento);
                    System.out.println("Respuestas encontradas: " + respuestasUsuario.size());

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
                    System.out.println("Puntuación calculada: " + puntuacion + "%");

                    // 5. Obtener el objeto Intento y actualizarlo
                    Optional<Intento> intentoOpt = intentoDAO.findById(idIntento);
                    if (intentoOpt.isPresent()) {
                        Intento intento = intentoOpt.get();
                        intento.setTotal(puntuacion);
                        intento.setRespuestasCorrectas(respuestasCorrectas);
                        intento.setTiempoCompletado(elapsedTime);

                        // 6. Actualizar el intento en la base de datos
                        boolean actualizado = intentoDAO.update(intento);

                        if (actualizado) {
                            System.out.println("Resultados actualizados exitosamente");

                            // Actualizar UI y navegar a resultados
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    loadingIndicator.setVisible(false);
                                    messageLabel.setText("¡Cuestionario completado!");
                                    mainController.changeScene("resultados.fxml", idIntento);
                                }
                            });
                        } else {
                            throw new RuntimeException("No se pudo actualizar el intento en la base de datos");
                        }
                    } else {
                        throw new RuntimeException("No se encontró el intento con ID: " + idIntento);
                    }
                }

            } catch (Exception e) {
                System.err.println("Error al calcular resultados: " + e.getMessage());
                e.printStackTrace();

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        loadingIndicator.setVisible(false);
                        messageLabel.setText("Error al calcular resultados");
                        nextButton.setDisable(false);
                    }
                });
            }
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