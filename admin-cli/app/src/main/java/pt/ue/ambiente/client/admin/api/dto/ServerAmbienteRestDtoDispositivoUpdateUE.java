package pt.ue.ambiente.client.admin.api.dto;

import java.io.Serializable;
import java.util.Collection;

import lombok.Data;
import pt.ue.ambiente.client.admin.api.enumeration.Protocolo;

@Data
public class ServerAmbienteRestDtoDispositivoUpdateUE implements Serializable {
    private String nome;
    private Collection<Protocolo> protocolos;
    private Boolean estado;
    private String sala;
    private String departamento;
    private Integer piso;
    private String edificio;

 
}
