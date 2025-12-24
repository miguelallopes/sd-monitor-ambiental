package pt.ue.ambiente.server.data.repository;


import org.springframework.data.repository.CrudRepository;

import pt.ue.ambiente.server.data.entity.Sala;

import pt.ue.ambiente.server.data.entity.Departamento;
import pt.ue.ambiente.server.data.entity.Edificio;
import pt.ue.ambiente.server.data.entity.Piso;
import pt.ue.ambiente.server.data.entity.Metricas;
import pt.ue.ambiente.server.data.entity.MetricasId;

import java.util.List;

public interface MetricasRepository extends CrudRepository<Metricas, MetricasId> {

    List<Metricas> findBySala(Sala sala);
    List<Metricas> findByEdificio(Edificio edificio);
    List<Metricas> findByDepartamento(Departamento departamento);
    List<Metricas> findByPiso(Piso piso);
    
}
