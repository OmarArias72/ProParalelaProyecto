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

    // Constructores
    public Intento() {}

    public Intento(int idIntento, LocalTime tiempoCompletado, int noIntento,
                   double total, int idUsuario, int idCuestionario) {
        this.idIntento = idIntento;
        this.tiempoCompletado = tiempoCompletado;
        this.noIntento = noIntento;
        this.total = total;
        this.idUsuario = idUsuario;
        this.idCuestionario = idCuestionario;
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

    @Override
    public String toString() {
        return "Intento{" +
                "idIntento=" + idIntento +
                ", tiempoCompletado=" + tiempoCompletado +
                ", noIntento=" + noIntento +
                ", total=" + total +
                ", idUsuario=" + idUsuario +
                ", idCuestionario=" + idCuestionario +
                '}';
    }
}