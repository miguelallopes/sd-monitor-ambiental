package pt.ue.ambiente.server.data.entity;

import java.util.Collection;

import jakarta.persistence.*;

import lombok.Data;

@Entity
@Data
public class Sala {
    @Id private String sala;


    @OneToMany(mappedBy = "id")
    private Collection<Dispositivo> dispositivos;

    protected Sala() {

    }

    public Sala(String sala) {
        this.sala = sala;
    }
}