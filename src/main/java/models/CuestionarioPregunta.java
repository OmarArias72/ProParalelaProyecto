package models;

public class CuestionarioPregunta {
    private int idCuestionario;
    private int idPregunta;

    // Constructores
    public CuestionarioPregunta() {}

    public CuestionarioPregunta(int idCuestionario, int idPregunta) {
        this.idCuestionario = idCuestionario;
        this.idPregunta = idPregunta;
    }

    // Getters y Setters
    public int getIdCuestionario() { return idCuestionario; }
    public void setIdCuestionario(int idCuestionario) { this.idCuestionario = idCuestionario; }

    public int getIdPregunta() { return idPregunta; }
    public void setIdPregunta(int idPregunta) { this.idPregunta = idPregunta; }

    @Override
    public String toString() {
        return "CuestionarioPregunta{" +
                "idCuestionario=" + idCuestionario +
                ", idPregunta=" + idPregunta +
                '}';
    }
}