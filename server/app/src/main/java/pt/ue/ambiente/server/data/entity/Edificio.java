package pt.ue.ambiente.server.data.entity;

import jakarta.persistence.*;

@Entity
public class Edificio {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String nome;

    public Edificio() {
    }

    public Edificio(String nome) {
        this.nome = nome;
    }
}
