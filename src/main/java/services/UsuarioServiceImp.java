package services;

import models.Usuario;

import java.util.Date;

public class UsuarioServiceImp implements UsuarioService{
    @Override
    public boolean registrarse(String nombre, String apellidoPaterno, String apellidoMaterno, String correo, Date fechaNacimiento, String password) {
        return false;
    }

    @Override
    public Usuario login(String correo, String password) {
        return null;
    }

    @Override
    public void cerrarSesion(int id) {

    }
}
