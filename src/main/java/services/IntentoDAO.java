package services;

import models.Intento;

import java.util.List;
import java.util.Optional;

public interface IntentoDAO {
    Optional<Intento> findById(int idIntento);
    List<Intento> findAll();
    boolean save(Intento intento);
    boolean update(Intento intento);
    boolean delete(int idIntento);
    List<Intento> findByUsuarioId(int idUsuario);
    List<Intento> findByCuestionarioId(int idCuestionario);
    Optional<Intento> findLastByUsuarioAndCuestionario(int idUsuario, int idCuestionario);
}
