package pt.ue.ambiente.client.admin.api.dto;

import java.util.Collection;

import lombok.Data;
import pt.ue.ambiente.client.admin.api.enumeration.Protocolo;

@Data
public class ServerAmbienteRestDtoDispositivoCreateUE {
    private String nome;

    private Collection<Protocolo> protocolos;

    private boolean estado;

    private String sala;

    private String departamento;

    private int piso;

    private String edificio;


}
