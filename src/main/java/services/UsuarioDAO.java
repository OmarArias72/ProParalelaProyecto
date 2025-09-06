package services;

import models.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioDAO {
    Optional<Usuario> findById(int idUsuario);
    List<Usuario> findAll();
    boolean save(Usuario usuario);
    boolean update(Usuario usuario);
    boolean delete(int idUsuario);
    Optional<Usuario> findByEmail(String correo);
    boolean existsByEmail(String correo);
}
