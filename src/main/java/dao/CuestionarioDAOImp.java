package dao;

import models.Cuestionario;
import models.Opcion;
import models.Pregunta;
import services.CuestionarioDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CuestionarioDAOImp implements CuestionarioDAO {
    private final Connection con;

    public CuestionarioDAOImp() {
        this.con = DatabaseConnection.getInstance().getConnection();
    }
    @Override
    public Cuestionario getCuestionario(int id) {
            Cuestionario cuestionario = null;
            try {
                String sql = "SELECT * FROM Cuestionario WHERE id_cuestionario = ?";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setInt(1, id);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    cuestionario = new Cuestionario();
                    cuestionario.setId(rs.getInt("id_cuestionario"));
                    cuestionario.setTitulo(rs.getString("titulo"));
                    cuestionario.setDescripcion(rs.getString("descripcion"));
                    cuestionario.setPreguntas(getPreguntasByCuestionario(id));
                }
            }catch (SQLException e){
                e.printStackTrace();
            }
        return cuestionario;
    }

    @Override
    public List<Pregunta> getPreguntasByCuestionario(int id) {
        List<Pregunta> preguntas = new ArrayList<>();
        try  {
            String sql = "SELECT P.* FROM Preguntas P JOIN cuestionario_Pregunta CP ON P.id_pregunta = CP.id_pregunta WHERE CP.id_cuestionario = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Pregunta pregunta = new Pregunta();
                pregunta.setId(rs.getInt("id_pregunta"));
                pregunta.setPregunta(rs.getString("pregunta"));
                pregunta.setTipoPregunta(rs.getString("tipo"));
                pregunta.setOpciones(getOpcionesByPregunta(pregunta.getId()));
                preguntas.add(pregunta);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return preguntas;
    }

    @Override
    public List<Opcion> getOpcionesByPregunta(int id) {
        List<Opcion> opciones = new ArrayList<>();
        try {
            String sql = "SELECT * FROM Opciones WHERE id_pregunta = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Opcion opcion = new Opcion();
                opcion.setId(rs.getInt("id_opcion"));
                opcion.setNombre(rs.getString("texto"));
                opcion.setEsCorrecta(rs.getBoolean("esCorrecta"));
                opcion.setIdPregunta(rs.getInt("id_pregunta"));
                opciones.add(opcion);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return opciones;
    }
}
