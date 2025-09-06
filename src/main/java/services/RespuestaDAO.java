package services;

import models.Respuesta;

import java.util.List;
import java.util.Optional;

public interface RespuestaDAO {
    Optional<Respuesta> findById(int idRespuesta);
    List<Respuesta> findAll();
    boolean save(Respuesta respuesta);
    boolean update(Respuesta respuesta);
    boolean delete(int idRespuesta);
    List<Respuesta> findByIntentoId(int idIntento);
    List<Respuesta> findByPreguntaId(int idPregunta);
    boolean existsByIntentoAndPregunta(int idIntento, int idPregunta);
}
