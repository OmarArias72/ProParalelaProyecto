package services;

import models.Cuestionario;

import java.util.List;
import java.util.Optional;

public interface CuestionarioDAO {
    Optional<Cuestionario> findById(int idCuestionario);
    List<Cuestionario> findAll();
    boolean save(Cuestionario cuestionario);
    boolean update(Cuestionario cuestionario);
    boolean delete(int idCuestionario);
    List<Cuestionario> findByUsuarioId(int idUsuario);
}