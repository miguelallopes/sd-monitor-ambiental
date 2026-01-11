package pt.ue.ambiente.server.rest.dao;

import java.util.Collection;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import pt.ue.ambiente.server.data.ServerAmbienteDataUE;
import pt.ue.ambiente.server.data.entity.Dispositivo;
import pt.ue.ambiente.server.data.entity.Sala;
import pt.ue.ambiente.server.data.entity.Departamento;
import pt.ue.ambiente.server.data.entity.Piso;
import pt.ue.ambiente.server.data.entity.Edificio;
import pt.ue.ambiente.server.data.enumeration.Protocolo;

@Data
public class ServerAmbienteRestDaoDispositivoCreateUE {
    @NotBlank
    private String nome;

    private Collection<Protocolo> protocolos;

    private boolean estado;

    @NotBlank
    private String sala;

    @NotBlank
    private String departamento;

    private int piso;

    @NotBlank
    private String edificio;

    public Dispositivo toDatabase(ServerAmbienteDataUE data) {
        Sala salaEntity = data.salaRepository.findById(sala)
                .orElseGet(() -> data.salaRepository.save(new Sala(sala)));
        Departamento departamentoEntity = data.departamentoRepository.findById(departamento)
                .orElseGet(() -> data.departamentoRepository.save(new Departamento(departamento)));
        Piso pisoEntity = data.pisoRepository.findById(piso)
                .orElseGet(() -> data.pisoRepository.save(new Piso(piso)));
        Edificio edificioEntity = data.edificioRepository.findById(edificio)
                .orElseGet(() -> data.edificioRepository.save(new Edificio(edificio)));
        Dispositivo d = new Dispositivo(nome, salaEntity, departamentoEntity, pisoEntity, edificioEntity);
        d.setProtocolos(protocolos);
        d.setAtivo(estado);
        return d;
    }
}
