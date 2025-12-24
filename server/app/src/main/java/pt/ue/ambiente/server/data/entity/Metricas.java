package pt.ue.ambiente.server.data.entity;

import jakarta.persistence.*;
import pt.ue.ambiente.server.data.enumeration.*;
import java.time.*;
import lombok.Data;

@Entity
@Data
@IdClass(MetricasId.class)
public class Metricas {

    @ManyToOne(optional = false)
    @Id
    private Dispositivo dispositivo;

    @Id
    private LocalDateTime tempoRegisto = LocalDateTime.now();

    private Protocolo protocolo;


    private float temperatura;
    private int humidade;

    @ManyToOne(optional = false)
    private Sala sala;

    @ManyToOne(optional = false)
    private Departamento departamento;

    @ManyToOne(optional = false)
    private Piso piso;

    @ManyToOne(optional = false)
    private Edificio edificio;


    protected Metricas() {
        
    }

    public Metricas(Dispositivo dispositivo, Protocolo protocolo, float temperatura, int humidade) {
        this.dispositivo = dispositivo;
        this.sala = this.dispositivo.getSala();
        this.piso = this.dispositivo.getPiso();
        this.departamento = this.dispositivo.getDepartamento();
        this.edificio = this.dispositivo.getEdificio();
        this.protocolo = protocolo;
        this.temperatura = temperatura;
        this.humidade = humidade;
    }

}
