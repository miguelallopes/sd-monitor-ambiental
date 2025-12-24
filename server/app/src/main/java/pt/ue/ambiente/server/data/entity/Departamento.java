package pt.ue.ambiente.server.data.entity;

import java.util.Collection;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Departamento {
    @Id @GeneratedValue private String nome;

    @OneToMany(mappedBy = "id")
    private Collection<Dispositivo> dispositivos;
    
    protected Departamento() {
    }

    public Departamento(String nome) {
        this.nome = nome;
    }
}
