package services;

import models.Pregunta;
import models.Respuesta;

import java.util.List;
import java.util.Optional;

public interface CuestionarioService {

    List<Pregunta> obtenerPreguntas(int idCuestionario);
    Optional<Pregunta> obtenerPreguntaConOpciones(int idPregunta);
    int evaluar(List<Respuesta> respuestas);
}
