package services;

import models.Pregunta;
import models.Respuesta;

import java.util.List;
import java.util.Optional;

public class CuestionarioServiceImpl implements CuestionarioService{
    @Override
    public List<Pregunta> obtenerPreguntas(int idCuestionario) {
        return List.of();
    }

    @Override
    public Optional<Pregunta> obtenerPreguntaConOpciones(int idPregunta) {
        return Optional.empty();
    }

    @Override
    public int evaluar(List<Respuesta> respuestas) {
        return 0;
    }
}
