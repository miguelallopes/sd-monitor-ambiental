package pt.ue.ambiente.server.data.entity;

import java.util.Collection;
import jakarta.persistence.*;

import lombok.Data;
import lombok.ToString;

@Entity
@Data
public class Sala {
    @Id
    private String sala;

    @OneToMany(mappedBy = "sala")
    @ToString.Exclude
    private Collection<Dispositivo> dispositivos;

    protected Sala() {

    }

    public Sala(String sala) {
        this.sala = sala;
    }
}