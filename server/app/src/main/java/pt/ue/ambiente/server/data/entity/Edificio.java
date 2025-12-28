package pt.ue.ambiente.server.data.entity;

import java.util.Collection;
import jakarta.persistence.*;

import lombok.Data;

@Entity
@Data
public class Edificio {
    @Id
    private String nome;

    @OneToMany(mappedBy = "id")
    private Collection<Dispositivo> dispositivos;

    protected Edificio() {
    }

    public Edificio(String nome) {
        this.nome = nome;
    }

}
