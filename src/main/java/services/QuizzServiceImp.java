package services;

import models.ResultadoEvaluacion;

import java.util.Map;

public class QuizzServiceImp implements QuizzService {
    @Override
    public int iniciarIntento(int idUsuario, int idCuestionario) {
        return 0;
    }

    @Override
    public boolean registrarRespuesta(int idIntento, int idPregunta, int idOpcion) {
        return false;
    }

    @Override
    public ResultadoEvaluacion finalizarIntento(int idIntento) {
        return null;
    }

    @Override
    public Map<String, Double> obtenerClasificacion(int idCuestionario) {
        return null;
    }
}
