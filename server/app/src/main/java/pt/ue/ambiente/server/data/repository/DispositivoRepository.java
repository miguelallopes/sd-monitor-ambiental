package pt.ue.ambiente.server.data.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.ue.ambiente.server.data.entity.Dispositivo;

@Repository
public interface DispositivoRepository extends JpaRepository<Dispositivo, Long> {

    List<Dispositivo> findByNome(String nome);
}
