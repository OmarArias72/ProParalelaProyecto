package models;

public class ResultadoEvaluacion {
    private double puntuacion;
    private int totalPreguntas;
    private int respuestasCorrectas;
    private int respuestasIncorrectas;
    private String mensaje;

    // Constructores, getters y setters
    public ResultadoEvaluacion(double puntuacion, int totalPreguntas,
                               int respuestasCorrectas, int respuestasIncorrectas, String mensaje) {
        this.puntuacion = puntuacion;
        this.totalPreguntas = totalPreguntas;
        this.respuestasCorrectas = respuestasCorrectas;
        this.respuestasIncorrectas = respuestasIncorrectas;
        this.mensaje = mensaje;
    }

    // Getters
    public double getPuntuacion() { return puntuacion; }
    public int getTotalPreguntas() { return totalPreguntas; }
    public int getRespuestasCorrectas() { return respuestasCorrectas; }
    public int getRespuestasIncorrectas() { return respuestasIncorrectas; }
    public String getMensaje() { return mensaje; }

}
