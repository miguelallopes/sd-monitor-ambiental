package pt.ue.ambiente.server.rest.dao;

import java.io.Serializable;
import java.util.Collection;

import jakarta.validation.constraints.Min;
import lombok.Data;
import pt.ue.ambiente.server.data.ServerAmbienteDataUE;
import pt.ue.ambiente.server.data.entity.Dispositivo;
import pt.ue.ambiente.server.data.entity.Sala;
import pt.ue.ambiente.server.data.entity.Departamento;
import pt.ue.ambiente.server.data.entity.Piso;
import pt.ue.ambiente.server.data.entity.Edificio;
import pt.ue.ambiente.server.data.enumeration.Protocolo;

@Data
public class ServerAmbienteRestDaoDispositivoUpdateUE implements Serializable {
    private String nome;
    private Collection<Protocolo> protocolos;
    private Boolean estado;
    private String sala;
    private String departamento;
    private Integer piso;
    private String edificio;

    public void toDatabase(Dispositivo dbEntity, ServerAmbienteDataUE data) {
        if (nome != null) dbEntity.setNome(nome);
        if (protocolos != null) dbEntity.setProtocolos(protocolos);
        if (estado != null) dbEntity.setAtivo(estado);

        if (sala != null) {
            Sala salaEntity = data.salaRepository.findById(sala)
                    .orElseGet(() -> data.salaRepository.save(new Sala(sala)));
            dbEntity.setSala(salaEntity);
        }
        if (departamento != null) {
            Departamento departamentoEntity = data.departamentoRepository.findById(departamento)
                    .orElseGet(() -> data.departamentoRepository.save(new Departamento(departamento)));
            dbEntity.setDepartamento(departamentoEntity);
        }
        if (edificio != null) {
            Edificio edificioEntity = data.edificioRepository.findById(edificio)
                    .orElseGet(() -> data.edificioRepository.save(new Edificio(edificio)));
            dbEntity.setEdificio(edificioEntity);
        }
        if (piso != null) {
            Piso pisoEntity = data.pisoRepository.findById(piso)
                    .orElseGet(() -> data.pisoRepository.save(new Piso(piso)));
            dbEntity.setPiso(pisoEntity);
        }
    }
}
