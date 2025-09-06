package services;

import models.Pregunta;
import models.TipoPregunta;

import java.util.List;
import java.util.Optional;

public interface PreguntaDAO {
    Optional<Pregunta> findById(int idPregunta);
    List<Pregunta> findAll();
    boolean save(Pregunta pregunta);
    boolean update(Pregunta pregunta);
    boolean delete(int idPregunta);
    List<Pregunta> findByCuestionarioId(int idCuestionario);
    List<Pregunta> findByTipo(TipoPregunta tipo);
}
