package pt.ue.ambiente.server.data.repository;


import pt.ue.ambiente.server.data.entity.Sala;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface SalaRepository extends JpaRepository<Sala, String> {

    
}
