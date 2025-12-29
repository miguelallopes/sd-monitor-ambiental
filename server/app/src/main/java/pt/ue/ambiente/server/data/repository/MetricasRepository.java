package pt.ue.ambiente.server.data.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.ue.ambiente.server.data.entity.Departamento;
import pt.ue.ambiente.server.data.entity.Edificio;
import pt.ue.ambiente.server.data.entity.Metricas;
import pt.ue.ambiente.server.data.entity.Piso;
import pt.ue.ambiente.server.data.entity.Sala;

@Repository
public interface MetricasRepository extends JpaRepository<Metricas, Long> {

    List<Metricas> findBySala(Sala sala);

    List<Metricas> findByEdificio(Edificio edificio);

    List<Metricas> findByDepartamento(Departamento departamento);

    List<Metricas> findByPiso(Piso piso);
}
