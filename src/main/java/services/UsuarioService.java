package services;

import models.Usuario;

import java.util.Date;

public interface UsuarioService {

    boolean registrarse(String nombre,
                        String apellidoPaterno, String apellidoMaterno,
                        String correo,
                        Date fechaNacimiento,
                        String password);
    Usuario login(String correo, String password);
    void cerrarSesion(int id);

}
