package pt.ue.ambiente.server.data.entity;

import jakarta.persistence.*;
import pt.ue.ambiente.server.data.enumeration.*;
import java.time.*;

@Entity
public class Metricas {

    private Dispositivo dispositivo;

    private Protocolo protocolo;

    private LocalDateTime tempoRegisto;

    private int sala;
    private float temperatura;
    private int humidade;

    public Metricas() {
    }

}
