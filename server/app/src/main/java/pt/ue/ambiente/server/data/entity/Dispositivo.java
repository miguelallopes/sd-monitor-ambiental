package pt.ue.ambiente.server.data.entity;

import java.util.Collection;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Data;
import pt.ue.ambiente.server.data.enumeration.Protocolo;

@Entity
@Data
public class Dispositivo {
    @Id
    @GeneratedValue
    private Long id;

    private String nome;

    @ElementCollection(fetch = FetchType.EAGER)
    private Collection<Protocolo> protocolos;


    @ManyToOne(optional = false)
    private Sala sala;

    @ManyToOne(optional = false)
    private Departamento departamento;

    private boolean ativo = false;

    @ManyToOne(optional = false)
    private Piso piso;

    @ManyToOne(optional = false)
    private Edificio edificio;

    @OneToMany(mappedBy = "dispositivo")
    private Collection<Metricas> metricas;

    protected Dispositivo() {
    }

    public Dispositivo(
            String nome, Sala sala, Departamento departamento, Piso piso, Edificio edificio) {
        this.nome = nome;
        this.sala = sala;
        this.departamento = departamento;
        this.piso = piso;
        this.edificio = edificio;
    }
}
