package dao;

import models.Usuario;
import services.UsuarioDAO;

import java.sql.*;
import java.util.List;
import java.util.Optional;

public class UsuarioDAOImp implements UsuarioDAO {

    private final Connection con;

    public UsuarioDAOImp() {
        this.con = DatabaseConnection.getInstance().getConnection();
    }

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
        try {
            String sql = "INSERT INTO usuarios(nombre,apellidoP,apellidoM,fechaNacimiento, correo, password) VALUES(?,?,?,?,?,?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, usuario.getNombre());
            ps.setString(2,usuario.getApellidoPaterno());
            ps.setString(3,usuario.getApellidoMaterno());
            if (usuario.getFechaNacimiento() != null) {
                java.sql.Date sqlDate = new java.sql.Date(usuario.getFechaNacimiento().getTime());
                ps.setDate(4, sqlDate);
            } else {
                ps.setNull(4, java.sql.Types.DATE);
            }
            ps.setString(5, usuario.getCorreo());
            ps.setString(6, usuario.getPassword());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
        try {
            String sql = "SELECT * FROM usuarios WHERE correo = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, correo);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Usuario usuario = mapResultSet(rs);
                return Optional.of(usuario);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public boolean existsByEmail(String correo) {
        return false;
    }
    private Usuario mapResultSet(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(rs.getInt("id_usuario"));
        usuario.setNombre(rs.getString("nombre"));
        usuario.setApellidoPaterno(rs.getString("apellidoP"));
        usuario.setApellidoMaterno(rs.getString("apellidoM"));
        usuario.setFechaNacimiento(rs.getDate("fechaNacimiento"));
        usuario.setCorreo(rs.getString("correo"));
        usuario.setPassword(rs.getString("password"));
        return usuario;
    }
}
