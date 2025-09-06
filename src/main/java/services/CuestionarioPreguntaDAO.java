package services;

import java.util.List;

public interface CuestionarioPreguntaDAO {
    boolean save(int idCuestionario, int idPregunta);
    boolean delete(int idCuestionario, int idPregunta);
    boolean exists(int idCuestionario, int idPregunta);
    List<Integer> findPreguntasByCuestionario(int idCuestionario);
    List<Integer> findCuestionariosByPregunta(int idPregunta);
    boolean deleteAllByCuestionario(int idCuestionario);
    boolean deleteAllByPregunta(int idPregunta);
}
