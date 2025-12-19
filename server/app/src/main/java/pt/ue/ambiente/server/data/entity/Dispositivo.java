package pt.ue.ambiente.server.data.entity;

import jakarta.persistence.*;

@Entity
public class Dispositivo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String nome;

    public Dispositivo() {
    }

    public Dispositivo(String nome) {
        this.nome = nome;
    }
}
