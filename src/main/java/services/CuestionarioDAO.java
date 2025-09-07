package services;

import models.Cuestionario;
import models.Opcion;
import models.Pregunta;

import java.util.List;
import java.util.Optional;

public interface CuestionarioDAO {
    Cuestionario getCuestionario(int id);
    List<Pregunta> getPreguntasByCuestionario(int id);
    List<Opcion> getOpcionesByPregunta(int id);
}