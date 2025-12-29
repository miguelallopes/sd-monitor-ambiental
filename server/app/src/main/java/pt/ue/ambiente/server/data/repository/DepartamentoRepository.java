package pt.ue.ambiente.server.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.ue.ambiente.server.data.entity.Departamento;

@Repository
public interface DepartamentoRepository extends JpaRepository<Departamento, String> {}
