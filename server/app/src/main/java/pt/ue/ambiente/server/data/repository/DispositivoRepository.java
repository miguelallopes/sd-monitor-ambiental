package pt.ue.ambiente.server.data.repository;


import org.springframework.data.repository.CrudRepository;

import pt.ue.ambiente.server.data.entity.Dispositivo;

import java.util.List;

public interface DispositivoRepository extends CrudRepository<Dispositivo, Long> {

    List<Dispositivo> findByNome(String nome);
    
}
