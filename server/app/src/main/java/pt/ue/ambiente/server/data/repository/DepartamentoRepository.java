package pt.ue.ambiente.server.data.repository;


import org.springframework.data.repository.CrudRepository;

import pt.ue.ambiente.server.data.entity.Departamento;

public interface DepartamentoRepository extends CrudRepository<Departamento, String> {

    
}