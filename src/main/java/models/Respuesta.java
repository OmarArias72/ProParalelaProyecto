package models;

public class Respuesta {
    private int idRespuesta;
    private int idIntento;
    private int idPregunta;
    private int idOpcion;

    // Constructores
    public Respuesta() {}

    public Respuesta(int idRespuesta, int idIntento, int idPregunta, int idOpcion) {
        this.idRespuesta = idRespuesta;
        this.idIntento = idIntento;
        this.idPregunta = idPregunta;
        this.idOpcion = idOpcion;
    }

    // Getters y Setters
    public int getIdRespuesta() { return idRespuesta; }
    public void setIdRespuesta(int idRespuesta) { this.idRespuesta = idRespuesta; }

    public int getIdIntento() { return idIntento; }
    public void setIdIntento(int idIntento) { this.idIntento = idIntento; }

    public int getIdPregunta() { return idPregunta; }
    public void setIdPregunta(int idPregunta) { this.idPregunta = idPregunta; }

    public int getIdOpcion() { return idOpcion; }
    public void setIdOpcion(int idOpcion) { this.idOpcion = idOpcion; }

    @Override
    public String toString() {
        return "Respuesta{" +
                "idRespuesta=" + idRespuesta +
                ", idIntento=" + idIntento +
                ", idPregunta=" + idPregunta +
                ", idOpcion=" + idOpcion +
                '}';
    }
}
