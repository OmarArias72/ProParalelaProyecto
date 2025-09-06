package services;

import models.ResultadoEvaluacion;

import java.util.Map;

public interface QuizzService {

    int iniciarIntento(int idUsuario, int idCuestionario);
    boolean registrarRespuesta(int idIntento, int idPregunta, int idOpcion);
    ResultadoEvaluacion finalizarIntento(int idIntento);
    Map<String, Double> obtenerClasificacion(int idCuestionario);
}
