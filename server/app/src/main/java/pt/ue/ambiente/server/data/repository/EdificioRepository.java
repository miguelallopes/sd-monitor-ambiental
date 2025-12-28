package pt.ue.ambiente.server.data.repository;

import pt.ue.ambiente.server.data.entity.Edificio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface EdificioRepository extends JpaRepository<Edificio, String> {
    
}
