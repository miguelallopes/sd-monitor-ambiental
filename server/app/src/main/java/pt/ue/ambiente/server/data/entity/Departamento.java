package pt.ue.ambiente.server.data.entity;

import java.util.Collection;
import jakarta.persistence.*;

import lombok.Data;
import lombok.ToString;

@Entity
@Data
public class Departamento {
    @Id
    private String nome;

    @OneToMany(mappedBy = "departamento")
    @ToString.Exclude
    private Collection<Dispositivo> dispositivos;

    protected Departamento() {
    }

    public Departamento(String nome) {
        this.nome = nome;
    }
}
