package dao;

import models.Usuario;
import services.UsuarioDAO;

import java.util.List;
import java.util.Optional;

public class UsuarioDAOImp implements UsuarioDAO {
    @Override
    public Optional<Usuario> findById(int idUsuario) {
        return Optional.empty();
    }

    @Override
    public List<Usuario> findAll() {
        return List.of();
    }

    @Override
    public boolean save(Usuario usuario) {
        return false;
    }

    @Override
    public boolean update(Usuario usuario) {
        return false;
    }

    @Override
    public boolean delete(int idUsuario) {
        return false;
    }

    @Override
    public Optional<Usuario> findByEmail(String correo) {
        return Optional.empty();
    }

    @Override
    public boolean existsByEmail(String correo) {
        return false;
    }
}
