package dao;

import models.Cuestionario;
import models.Intento;
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


    /**
     * Método modificado para obtener solo las opciones correctas de una pregunta
     * Útil para mostrar respuestas correctas en resultados
     */
    public List<Opcion> getOpcionesCorrectasByPregunta(int id) {
        List<Opcion> opcionesCorrectas = new ArrayList<>();
        try {
            String sql = "SELECT * FROM Opciones WHERE id_pregunta = ? AND esCorrecta = TRUE";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Opcion opcion = new Opcion();
                opcion.setId(rs.getInt("id_opcion"));
                opcion.setNombre(rs.getString("texto"));
                opcion.setEsCorrecta(rs.getBoolean("esCorrecta"));
                opcion.setIdPregunta(rs.getInt("id_pregunta"));
                opcionesCorrectas.add(opcion);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return opcionesCorrectas;
    }

    /**
     * Método para cargar preguntas con solo las respuestas correctas
     * Útil para mostrar resultados o respuestas correctas
     */
    public List<Pregunta> getPreguntasConRespuestasCorrectas(int idCuestionario) {
        List<Pregunta> preguntas = new ArrayList<>();
        try {
            String sql = "SELECT P.* FROM Preguntas P JOIN cuestionario_Pregunta CP ON P.id_pregunta = CP.id_pregunta WHERE CP.id_cuestionario = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, idCuestionario);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Pregunta pregunta = new Pregunta();
                pregunta.setId(rs.getInt("id_pregunta"));
                pregunta.setPregunta(rs.getString("pregunta"));
                pregunta.setTipoPregunta(rs.getString("tipo"));
                // Cargar solo las opciones correctas
                pregunta.setOpciones(getOpcionesCorrectasByPregunta(pregunta.getId()));
                preguntas.add(pregunta);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return preguntas;
    }

    /**
     * Establece la clasificación del resultado basado en el porcentaje de respuestas correctas
     */
    public String obtenerClasificacionResultado(double porcentajeCorrectas) {
        if (porcentajeCorrectas >= 90.0) {
            return "EXCELENTE";
        } else if (porcentajeCorrectas >= 80.0) {
            return "MUY BUENO";
        } else if (porcentajeCorrectas >= 70.0) {
            return "BUENO";
        } else if (porcentajeCorrectas >= 60.0) {
            return "REGULAR";
        } else if (porcentajeCorrectas >= 50.0) {
            return "SUFICIENTE";
        } else {
            return "INSUFICIENTE";
        }
    }

    /**
     * Calcula el porcentaje de respuestas correctas
     */
    public double calcularPorcentajeCorrectas(int respuestasCorrectas, int totalPreguntas) {
        if (totalPreguntas == 0) {
            return 0.0;
        }
        return ((double) respuestasCorrectas / totalPreguntas) * 100.0;
    }

    /**
     * Obtiene el detalle completo del resultado con clasificación
     */
    public ResultadoDetallado obtenerResultadoDetallado(Intento intento, int totalPreguntas) {
        double porcentaje = calcularPorcentajeCorrectas(intento.getRespuestasCorrectas(), totalPreguntas);
        String clasificacion = obtenerClasificacionResultado(porcentaje);

        return new ResultadoDetallado(
                intento,
                porcentaje,
                clasificacion,
                getPreguntasConRespuestasCorrectas(intento.getIdCuestionario())
        );
    }

    /**
     * Clase interna para encapsular el resultado detallado
     */
    public static class ResultadoDetallado {
        private Intento intento;
        private double porcentajeCorrectas;
        private String clasificacion;
        private List<Pregunta> preguntasConRespuestasCorrectas;

        public ResultadoDetallado(Intento intento, double porcentajeCorrectas,
                                  String clasificacion, List<Pregunta> preguntasConRespuestasCorrectas) {
            this.intento = intento;
            this.porcentajeCorrectas = porcentajeCorrectas;
            this.clasificacion = clasificacion;
            this.preguntasConRespuestasCorrectas = preguntasConRespuestasCorrectas;
        }

        // Getters
        public Intento getIntento() { return intento; }
        public double getPorcentajeCorrectas() { return porcentajeCorrectas; }
        public String getClasificacion() { return clasificacion; }
        public List<Pregunta> getPreguntasConRespuestasCorrectas() { return preguntasConRespuestasCorrectas; }

        public String getDescripcionClasificacion() {
            switch (clasificacion) {
                case "EXCELENTE":
                    return "¡Felicitaciones! Has demostrado un dominio excepcional del tema.";
                case "MUY BUENO":
                    return "Muy buen trabajo. Tienes un sólido conocimiento del tema.";
                case "BUENO":
                    return "Buen resultado. Conoces bien la mayoría de los conceptos.";
                case "REGULAR":
                    return "Resultado aceptable, pero hay área de mejora.";
                case "SUFICIENTE":
                    return "Has alcanzado el mínimo requerido, considera repasar algunos temas.";
                case "INSUFICIENTE":
                    return "Te recomendamos estudiar más y volver a intentarlo.";
                default:
                    return "Resultado evaluado.";
            }
        }
    }


}
