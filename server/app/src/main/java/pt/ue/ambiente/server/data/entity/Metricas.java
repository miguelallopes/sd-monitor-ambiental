package pt.ue.ambiente.server.data.entity;

import pt.ue.ambiente.server.data.enumeration.*;
import java.time.*;
import lombok.Data;
import jakarta.persistence.*;

@Entity
@Data
public class Metricas {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false)
    private Dispositivo dispositivo;

    private LocalDateTime tempoRegisto = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
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
        this.tempoRegisto = LocalDateTime.now();

        this.piso = dispositivo.getPiso();

        this.edificio = dispositivo.getEdificio();

        this.sala = dispositivo.getSala();

        this.departamento = dispositivo.getDepartamento();

        this.protocolo = protocolo;
        this.temperatura = temperatura;
        this.humidade = humidade;
    }

}
