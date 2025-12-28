package pt.ue.ambiente.server.data.entity;

import java.util.Collection;
import jakarta.persistence.*;

import lombok.Data;

@Entity
@Data
public class Piso {

    @Id
    private int numero;

    @OneToMany(mappedBy = "id")
    private Collection<Dispositivo> dispositivos;

    protected Piso() {

    }

    public Piso(int numero) {
        this.numero = numero;
    }
}
