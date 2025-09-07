package dao;

import models.Intento;
import services.IntentoDAO;

import java.math.BigDecimal;
import java.sql.*;
import java.util.List;
import java.util.Optional;

public class IntentoDAOImp implements IntentoDAO {

    private final Connection con;

    public IntentoDAOImp() {
        this.con = DatabaseConnection.getInstance().getConnection();
    }
    @Override
    public Optional<Intento> findById(int idIntento) {
        String sql = "SELECT * FROM Intentos WHERE id_intento = ?";

        try (
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, idIntento);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToIntento(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding intento by ID: " + e.getMessage());
        }
        return Optional.empty();
    }


    @Override
    public List<Intento> findAll() {
        return List.of();
    }

    @Override
    public boolean save(Intento intento) {
        String sql = "INSERT INTO Intentos (id_usuario, id_cuestionario, noIntento) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, intento.getIdUsuario());
            stmt.setInt(2, intento.getIdCuestionario());
            stmt.setInt(3, intento.getNoIntento());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 1) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    intento.setIdIntento(rs.getInt(1)); // Recupera el ID generado automáticamente
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(Intento intento) {
        String sql = "UPDATE Intentos SET tiempoCompletado = ?, total = ?, noRespuestasCorrectas = ? WHERE id_intento = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            // Convertir LocalTime a java.sql.Time
            java.sql.Time sqlTime = java.sql.Time.valueOf(intento.getTiempoCompletado());

            // Convertir double a BigDecimal con precisión
            BigDecimal total = BigDecimal.valueOf(intento.getTotal());

            stmt.setTime(1, sqlTime);
            stmt.setBigDecimal(2, total);
            stmt.setInt(3, intento.getRespuestasCorrectas()); // 🔹 Nuevo campo
            stmt.setInt(4, intento.getIdIntento());
            System.out.println("Tiempo completado: " + sqlTime+" total : " + total+
                    " respuestasCorrectas : " + intento.getRespuestasCorrectas()+
                    " idIntento : " + intento.getIdIntento());
            return stmt.executeUpdate() == 1;
        } catch (SQLException e) {
            System.err.println("Error updating intento: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(int idIntento) {
        return false;
    }

    @Override
    public List<Intento> findByUsuarioId(int idUsuario) {
        return List.of();
    }

    @Override
    public List<Intento> findByCuestionarioId(int idCuestionario) {
        return List.of();
    }


    @Override
    public Optional<Intento> findLastByCuestionarioId(int idUsuario, int idCuestionario) {
        String sql = "SELECT * FROM Intentos WHERE id_usuario = ? AND id_cuestionario = ? ORDER BY noIntento DESC LIMIT 1";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);
            stmt.setInt(2, idCuestionario);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Intento intento = new Intento();
                intento.setIdIntento(rs.getInt("id_intento"));
                intento.setIdUsuario(rs.getInt("id_usuario"));
                intento.setIdCuestionario(rs.getInt("id_cuestionario"));
                intento.setNoIntento(rs.getInt("noIntento"));
                return Optional.of(intento);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<Intento> findLastByUsuarioId(int idUsuario) {
        String sql = "SELECT id_intento, id_usuario, id_cuestionario, noIntento FROM Intentos WHERE id_usuario = ? ORDER BY id_intento DESC LIMIT 1";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Intento intento = new Intento();
                intento.setIdIntento(rs.getInt("id_intento"));
                intento.setIdUsuario(rs.getInt("id_usuario"));
                intento.setIdCuestionario(rs.getInt("id_cuestionario"));
                intento.setNoIntento(rs.getInt("noIntento"));
                return Optional.of(intento);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<Intento> findIncompleteByUserAndQuiz(int idUsuario, int idCuestionario) {
        String sql = "SELECT * FROM Intentos WHERE id_usuario = ? AND id_cuestionario = ? AND (total IS NULL OR total = 0) AND (tiempoCompletado IS NULL) ORDER BY id_intento DESC LIMIT 1";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);
            stmt.setInt(2, idCuestionario);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToIntento(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding incomplete intento: " + e.getMessage());
            e.printStackTrace();
        }
        return Optional.empty();
    }

    private Intento mapResultSetToIntento(ResultSet rs) throws SQLException {
        Intento intento = new Intento();
        intento.setIdIntento(rs.getInt("id_intento"));

        Time tiempo = rs.getTime("tiempoCompletado");
        if (tiempo != null) {
            intento.setTiempoCompletado(tiempo.toLocalTime());
        }

        intento.setNoIntento(rs.getInt("noIntento"));
        intento.setTotal(rs.getDouble("total"));
        intento.setRespuestasCorrectas(rs.getInt("noRespuestasCorrectas"));
        intento.setIdUsuario(rs.getInt("id_usuario"));
        intento.setIdCuestionario(rs.getInt("id_cuestionario"));
        return intento;
    }
}
