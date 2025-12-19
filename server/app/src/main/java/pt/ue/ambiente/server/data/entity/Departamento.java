package pt.ue.ambiente.server.data.entity;

import jakarta.persistence.*;

@Entity
public class Departamento {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String nome;

    public Departamento() {
    }

    public Departamento(String nome) {
        this.nome = nome;
    }
}
