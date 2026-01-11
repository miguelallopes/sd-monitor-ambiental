package pt.ue.ambiente.server.rest;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import pt.ue.ambiente.server.data.ServerAmbienteDataUE;
import pt.ue.ambiente.server.data.entity.Dispositivo;
import pt.ue.ambiente.server.rest.dto.ServerAmbienteRestDtoDispositivoCreateUE;
import pt.ue.ambiente.server.rest.dto.ServerAmbienteRestDtoDispositivoUE;
import pt.ue.ambiente.server.rest.dto.ServerAmbienteRestDtoDispositivoUpdateUE;

@RestController
@RequestMapping("/api/devices")
public class ServerAmbienteRestDispositivosUE {

    private static final Logger logger = LoggerFactory.getLogger(ServerAmbienteRestDispositivosUE.class);

    private final ServerAmbienteDataUE repositories;

    public ServerAmbienteRestDispositivosUE(ServerAmbienteDataUE repositories) {
        this.repositories = repositories;
    }

    @GetMapping
    public List<ServerAmbienteRestDtoDispositivoUE> listAll() {
        return repositories.dispositivoRepository.findAll().stream()
                .map(ServerAmbienteRestDtoDispositivoUE::fromDatabase)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServerAmbienteRestDtoDispositivoUE> getById(@PathVariable Long id) {
        return repositories.dispositivoRepository.findById(id)
                .map(ServerAmbienteRestDtoDispositivoUE::fromDatabase)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ServerAmbienteRestDtoDispositivoUE> create(
            @Valid @RequestBody ServerAmbienteRestDtoDispositivoCreateUE dto) {
        boolean conflict = repositories.dispositivoRepository
                .findByNomeAndSala_SalaAndDepartamento_NomeAndPiso_NumeroAndEdificio_Nome(
                        dto.getNome(), dto.getSala(), dto.getDepartamento(), dto.getPiso(), dto.getEdificio())
                .isPresent();
        if (conflict) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        Dispositivo d = dto.toDatabase(repositories);
        Dispositivo saved = repositories.dispositivoRepository.save(d);
        ServerAmbienteRestDtoDispositivoUE resp = ServerAmbienteRestDtoDispositivoUE.fromDatabase(saved);
        return ResponseEntity.created(URI.create("/api/devices/" + saved.getId())).body(resp);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServerAmbienteRestDtoDispositivoUE> update(@PathVariable Long id,
            @Valid @RequestBody ServerAmbienteRestDtoDispositivoUpdateUE dto) {
        Optional<Dispositivo> opt = repositories.dispositivoRepository.findById(id);
        if (opt.isEmpty())
            return ResponseEntity.notFound().build();
        Dispositivo existing = opt.get();
        dto.toDatabase(existing, repositories);
        Dispositivo saved = repositories.dispositivoRepository.save(existing);
        return ResponseEntity.ok(ServerAmbienteRestDtoDispositivoUE.fromDatabase(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repositories.dispositivoRepository.existsById(id))
            return ResponseEntity.notFound().build();
        repositories.dispositivoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
