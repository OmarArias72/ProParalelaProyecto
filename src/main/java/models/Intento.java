package models;

import java.time.LocalTime;
import java.util.List;


public class Intento {
    private int idIntento;
    private LocalTime tiempoCompletado;
    private int noIntento;
    private double total;
    private int idUsuario;
    private int idCuestionario;
    private List<Respuesta> respuestas;
    private int respuestasCorrectas;
    private int totalPreguntas;

    // Constructores
    public Intento() {}

    public Intento(int idIntento, LocalTime tiempoCompletado, int noIntento,
                   double total, int idUsuario, int idCuestionario,
                   List<Respuesta> respuestas, int respuestasCorrectas) {
        this.idIntento = idIntento;
        this.tiempoCompletado = tiempoCompletado;
        this.noIntento = noIntento;
        this.total = total;
        this.idUsuario = idUsuario;
        this.idCuestionario = idCuestionario;
        this.respuestas = respuestas;
        this.respuestasCorrectas = respuestasCorrectas;
    }

    // Getters y Setters
    public int getIdIntento() { return idIntento; }
    public void setIdIntento(int idIntento) { this.idIntento = idIntento; }

    public LocalTime getTiempoCompletado() { return tiempoCompletado; }
    public void setTiempoCompletado(LocalTime tiempoCompletado) { this.tiempoCompletado = tiempoCompletado; }

    public int getNoIntento() { return noIntento; }
    public void setNoIntento(int noIntento) { this.noIntento = noIntento; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public int getIdCuestionario() { return idCuestionario; }
    public void setIdCuestionario(int idCuestionario) { this.idCuestionario = idCuestionario; }

    public List<Respuesta> getRespuestas() { return respuestas; }
    public void setRespuestas(List<Respuesta> respuestas) { this.respuestas = respuestas; }

    public int getRespuestasCorrectas() { return respuestasCorrectas; }
    public void setRespuestasCorrectas(int respuestasCorrectas) { this.respuestasCorrectas = respuestasCorrectas; }

    public int getTotalPreguntas() { return totalPreguntas; }
    public void setTotalPreguntas(int totalPreguntas) { this.totalPreguntas = totalPreguntas; }

    // Métodos de utilidad para clasificación
    public double calcularPorcentaje() {
        if (totalPreguntas == 0) return 0.0;
        return ((double) respuestasCorrectas / totalPreguntas) * 100.0;
    }

    public String obtenerClasificacion() {
        double porcentaje = calcularPorcentaje();
        if (porcentaje >= 90.0) return "EXCELENTE";
        if (porcentaje >= 80.0) return "MUY BUENO";
        if (porcentaje >= 70.0) return "BUENO";
        if (porcentaje >= 60.0) return "REGULAR";
        if (porcentaje >= 50.0) return "SUFICIENTE";
        return "INSUFICIENTE";
    }

    public String obtenerDescripcionClasificacion() {
        String clasificacion = obtenerClasificacion();
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

    public int getRespuestasIncorrectas() {
        return totalPreguntas - respuestasCorrectas;
    }

    public boolean isAprobado() {
        return calcularPorcentaje() >= 60.0; // Considerando 60% como nota mínima aprobatoria
    }

    @Override
    public String toString() {
        return String.format("Intento %d: %d/%d correctas (%.1f%%) - %s",
                noIntento, respuestasCorrectas, totalPreguntas,
                calcularPorcentaje(), obtenerClasificacion());
    }
}