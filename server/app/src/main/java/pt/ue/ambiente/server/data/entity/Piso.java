package pt.ue.ambiente.server.data.entity;

import jakarta.persistence.*;
import java.util.Collection;
import lombok.Data;
import lombok.ToString;

@Entity
@Data
public class Piso {

    @Id private int numero;

    @OneToMany(mappedBy = "piso")
    @ToString.Exclude
    private Collection<Dispositivo> dispositivos;

    protected Piso() {}

    public Piso(int numero) {
        this.numero = numero;
    }
}
