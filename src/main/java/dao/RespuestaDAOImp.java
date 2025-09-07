package dao;

import models.Respuesta;
import services.RespuestaDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RespuestaDAOImp implements RespuestaDAO {

    private final Connection con;

    public RespuestaDAOImp() {
        this.con = DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public Optional<Respuesta> findById(int idIntento) {
        String sql = "SELECT * FROM Respuestas WHERE id_intento = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idIntento);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Respuesta respuesta = new Respuesta();
                respuesta.setIdRespuesta(rs.getInt("id_respuesta"));
                respuesta.setIdIntento(rs.getInt("id_intento"));
                respuesta.setIdPregunta(rs.getInt("id_pregunta"));
                respuesta.setIdOpcion(rs.getInt("id_opcion"));
                return Optional.of(respuesta);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    @Override
    public List<Respuesta> findAll() {
        return List.of();
    }

    @Override
    public boolean save(Respuesta respuesta) {
        String sql = "INSERT INTO Respuestas (id_intento, id_pregunta, id_opcion) VALUES (?, ?, ?)";
        try  {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, respuesta.getIdIntento());
            ps.setInt(2, respuesta.getIdPregunta());
            ps.setInt(3, respuesta.getIdOpcion());
            ps.executeUpdate();
            //System.out.println("Se ejecuta save en RESPUESTASDAOIMPL");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean update(Respuesta respuesta) {
        return false;
    }

    @Override
    public boolean delete(int idRespuesta) {
        return false;
    }

    @Override
    public List<Respuesta> findByIntentoId(int idIntento) {
        String sql = "SELECT * FROM Respuestas WHERE id_intento = ?";
        List<Respuesta> respuestas = new ArrayList<>();

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idIntento);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Respuesta respuesta = new Respuesta();
                respuesta.setIdRespuesta(rs.getInt("id_respuesta"));
                respuesta.setIdIntento(rs.getInt("id_intento"));
                respuesta.setIdPregunta(rs.getInt("id_pregunta"));
                respuesta.setIdOpcion(rs.getInt("id_opcion"));
                respuestas.add(respuesta);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return respuestas;
    }

    @Override
    public List<Respuesta> findByPreguntaId(int idPregunta) {
        return List.of();
    }

    @Override
    public boolean existsByIntentoAndPregunta(int idIntento, int idPregunta) {
        return false;
    }
}
