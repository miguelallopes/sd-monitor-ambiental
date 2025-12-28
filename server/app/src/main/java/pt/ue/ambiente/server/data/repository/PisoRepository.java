package pt.ue.ambiente.server.data.repository;

import pt.ue.ambiente.server.data.entity.Piso;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PisoRepository extends JpaRepository<Piso, Integer> {

    
}

