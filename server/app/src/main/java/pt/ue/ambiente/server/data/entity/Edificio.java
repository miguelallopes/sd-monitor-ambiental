package pt.ue.ambiente.server.data.entity;

import jakarta.persistence.*;
import java.util.Collection;
import lombok.Data;
import lombok.ToString;

@Entity
@Data
public class Edificio {
    @Id private String nome;

    @OneToMany(mappedBy = "edificio")
    @ToString.Exclude
    private Collection<Dispositivo> dispositivos;

    protected Edificio() {}

    public Edificio(String nome) {
        this.nome = nome;
    }
}
