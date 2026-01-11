package pt.ue.ambiente.server.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import pt.ue.ambiente.server.data.ServerAmbienteDataUE;
import pt.ue.ambiente.server.data.entity.Dispositivo;
import pt.ue.ambiente.server.rest.dao.ServerAmbienteRestDaoDispositivoUE;
import pt.ue.ambiente.server.rest.dao.ServerAmbienteRestDaoDispositivoCreateUE;
import pt.ue.ambiente.server.rest.dao.ServerAmbienteRestDaoDispositivoUpdateUE;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/devices")
public class ServerAmbienteRestDispositivosUE {

    private static final Logger logger = LoggerFactory.getLogger(ServerAmbienteRestDispositivosUE.class);

    private final ServerAmbienteDataUE repositories;

    public ServerAmbienteRestDispositivosUE(ServerAmbienteDataUE repositories) {
        this.repositories = repositories;
    }

    @GetMapping
    public List<ServerAmbienteRestDaoDispositivoUE> listAll() {
        return repositories.dispositivoRepository.findAll().stream()
                .map(ServerAmbienteRestDaoDispositivoUE::fromDatabase)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServerAmbienteRestDaoDispositivoUE> getById(@PathVariable Long id) {
        return repositories.dispositivoRepository.findById(id)
                .map(ServerAmbienteRestDaoDispositivoUE::fromDatabase)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ServerAmbienteRestDaoDispositivoUE> create(@Valid @RequestBody ServerAmbienteRestDaoDispositivoCreateUE dto) {
        boolean conflict = repositories.dispositivoRepository
                .findByNomeAndSala_SalaAndDepartamento_NomeAndPiso_NumeroAndEdificio_Nome(
                        dto.getNome(), dto.getSala(), dto.getDepartamento(), dto.getPiso(), dto.getEdificio()
                ).isPresent();
        if (conflict) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        Dispositivo d = dto.toDatabase(repositories);
        Dispositivo saved = repositories.dispositivoRepository.save(d);
        ServerAmbienteRestDaoDispositivoUE resp = ServerAmbienteRestDaoDispositivoUE.fromDatabase(saved);
        return ResponseEntity.created(URI.create("/api/devices/" + saved.getId())).body(resp);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServerAmbienteRestDaoDispositivoUE> update(@PathVariable Long id, @Valid @RequestBody ServerAmbienteRestDaoDispositivoUpdateUE dto) {
        Optional<Dispositivo> opt = repositories.dispositivoRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        Dispositivo existing = opt.get();
        dto.toDatabase(existing, repositories);
        Dispositivo saved = repositories.dispositivoRepository.save(existing);
        return ResponseEntity.ok(ServerAmbienteRestDaoDispositivoUE.fromDatabase(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repositories.dispositivoRepository.existsById(id)) return ResponseEntity.notFound().build();
        repositories.dispositivoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
