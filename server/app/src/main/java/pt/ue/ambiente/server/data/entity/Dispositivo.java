package pt.ue.ambiente.server.data.entity;

import lombok.Data;

import java.util.Collection;

import jakarta.persistence.*;

@Entity
@Data
public class Dispositivo {
    @Id
    @GeneratedValue
    private Long id;

    private String nome;

    @ManyToOne(optional = false)
    private Sala sala;

    @ManyToOne(optional = false)
    private Departamento departamento;

    @ManyToOne(optional = false)
    private Piso piso;

    @ManyToOne(optional = false)
    private Edificio edificio;

    @OneToMany(mappedBy = "dispositivo")
    private Collection<Metricas> metricas;

    protected Dispositivo() {
    }

    public Dispositivo(String nome) {
        this.nome = nome;
    }
}
