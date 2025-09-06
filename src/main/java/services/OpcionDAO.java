package services;

import models.Opcion;

import java.util.List;
import java.util.Optional;

public interface OpcionDAO {
    Optional<Opcion> findById(int idOpcion);
    List<Opcion> findAll();
    boolean save(Opcion opcion);
    boolean update(Opcion opcion);
    boolean delete(int idOpcion);
    List<Opcion> findByPreguntaId(int idPregunta);
    List<Opcion> findCorrectasByPreguntaId(int idPregunta);
}