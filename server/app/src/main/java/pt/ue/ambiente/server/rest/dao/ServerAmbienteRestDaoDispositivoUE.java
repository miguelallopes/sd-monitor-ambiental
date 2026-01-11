package pt.ue.ambiente.server.rest.dao;

import java.io.Serializable;
import java.util.Collection;

import lombok.Data;
import pt.ue.ambiente.server.data.ServerAmbienteDataUE;
import pt.ue.ambiente.server.data.entity.Dispositivo;
import pt.ue.ambiente.server.data.entity.Sala;
import pt.ue.ambiente.server.data.entity.Departamento;
import pt.ue.ambiente.server.data.entity.Piso;
import pt.ue.ambiente.server.data.entity.Edificio;
import pt.ue.ambiente.server.data.enumeration.Protocolo;


@Data
public class ServerAmbienteRestDaoDispositivoUE implements Serializable {
    private Long idDispositivo;
    private String nome;
    private Collection<Protocolo> protocolos;
    private boolean estado;
    private String sala;
    private String departamento;
    private int piso;
    private String edificio;

    public static ServerAmbienteRestDaoDispositivoUE fromDatabase(Dispositivo dbEntity) {
        ServerAmbienteRestDaoDispositivoUE restEntity = new ServerAmbienteRestDaoDispositivoUE();
        restEntity.setIdDispositivo(dbEntity.getId());
        restEntity.setNome(dbEntity.getNome());
        restEntity.setProtocolos(dbEntity.getProtocolos());
        restEntity.setEstado(dbEntity.isAtivo());
        restEntity.setSala(dbEntity.getSala().getSala());
        restEntity.setDepartamento(dbEntity.getDepartamento().getNome());
        restEntity.setPiso(dbEntity.getPiso().getNumero());
        restEntity.setEdificio(dbEntity.getEdificio().getNome());
        return restEntity;
    }

    public Dispositivo toDatabase(ServerAmbienteDataUE data) {
        Sala salaEntity = data.salaRepository.findById(sala)
                .orElseGet(() -> data.salaRepository.save(new Sala(sala)));
        Departamento departamentoEntity = data.departamentoRepository.findById(departamento)
                .orElseGet(() -> data.departamentoRepository.save(new Departamento(departamento)));
        Piso pisoEntity = data.pisoRepository.findById(piso)
                .orElseGet(() -> data.pisoRepository.save(new Piso(piso)));
        Edificio edificioEntity = data.edificioRepository.findById(edificio)
                .orElseGet(() -> data.edificioRepository.save(new Edificio(edificio)));
        Dispositivo d = new Dispositivo(nome == null ? "Device" : nome, salaEntity, departamentoEntity, pisoEntity, edificioEntity);
        d.setProtocolos(protocolos);
        d.setAtivo(estado);
        return d;
    }

    public void updateDatabase(Dispositivo dbEntity, ServerAmbienteDataUE data) {
        if (nome != null) dbEntity.setNome(nome);
        if (protocolos != null) dbEntity.setProtocolos(protocolos);
        dbEntity.setAtivo(estado);
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
        if (piso != 0) {
            Piso pisoEntity = data.pisoRepository.findById(piso)
                    .orElseGet(() -> data.pisoRepository.save(new Piso(piso)));
            dbEntity.setPiso(pisoEntity);
        }
    }
}
