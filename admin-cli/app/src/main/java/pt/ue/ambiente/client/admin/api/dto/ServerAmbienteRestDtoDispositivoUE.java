package pt.ue.ambiente.client.admin.api.dto;

import java.io.Serializable;
import java.util.Collection;

import pt.ue.ambiente.client.admin.api.enumeration.Protocolo;

import lombok.Data;


@Data
public class ServerAmbienteRestDtoDispositivoUE implements Serializable {
    private Long idDispositivo;
    private String nome;
    private Collection<Protocolo> protocolos;
    private boolean estado;
    private String sala;
    private String departamento;
    private int piso;
    private String edificio;
}
