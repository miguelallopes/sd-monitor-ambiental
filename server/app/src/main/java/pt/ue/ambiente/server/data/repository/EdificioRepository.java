package pt.ue.ambiente.server.data.repository;

import pt.ue.ambiente.server.data.entity.Edificio;

import org.springframework.data.repository.CrudRepository;



public interface EdificioRepository extends CrudRepository<Edificio, String> {
    
}
