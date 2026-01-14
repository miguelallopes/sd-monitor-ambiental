
package pt.ue.ambiente.server.rest;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import pt.ue.ambiente.server.data.ServerAmbienteDataUE;
import pt.ue.ambiente.server.data.entity.Departamento;
import pt.ue.ambiente.server.data.entity.Dispositivo;
import pt.ue.ambiente.server.data.entity.Edificio;
import pt.ue.ambiente.server.data.entity.Metricas;
import pt.ue.ambiente.server.data.entity.Piso;
import pt.ue.ambiente.server.data.entity.Sala;
import pt.ue.ambiente.server.data.enumeration.Protocolo;
import pt.ue.ambiente.server.message.AmbienteClockStatus;
import pt.ue.ambiente.server.message.AmbienteMessagePublish;
import pt.ue.ambiente.server.message.AmbienteMessageResponse;
import pt.ue.ambiente.server.rest.dto.ServerAmbienteRestDtoDispositivoMediaMetricasUE;
import pt.ue.ambiente.server.rest.dto.ServerAmbienteRestDtoDispositivoMetricasUE;

@RestController
@RequestMapping("/api/metrics")
public class ServerAmbienteRestMetricasUE {

    private static final Logger logger = LoggerFactory.getLogger(ServerAmbienteRestMetricasUE.class);

    private final ServerAmbienteDataUE repositories;
    private final Set<String> levels = Set.copyOf(Arrays.asList("sala", "departamento", "piso", "edificio"));

    public ServerAmbienteRestMetricasUE(ServerAmbienteDataUE repositories) {
        this.repositories = repositories;
    }

    @PostMapping("/ingest")
    public ResponseEntity<AmbienteMessageResponse> ingestMetrics(
            @RequestBody AmbienteMessagePublish request) {
        Optional<Dispositivo> device = Optional.empty();
        OffsetDateTime tempoInicioProcessamento = OffsetDateTime.now();

        int deviceId = -1;
        float temperatura = 0;
        int humidade = 0;
        OffsetDateTime timestamp = null;
        String timestampStr = null;

        boolean status_humidade = false;
        boolean status_temperatura = false;
        AmbienteClockStatus status_clock = AmbienteClockStatus.SUBMISSION_INVALID;
        boolean status = false;

        // Ler dados recebidos do dispositivo
        try {
            deviceId = request.getDeviceId();
            device = repositories.dispositivoRepository.findById(Long.valueOf(deviceId));
            if (deviceId < 0 || device.isEmpty()) {
                logger.error(
                        "[REST] Métricas não registadas pois o dispositivo "
                                + deviceId
                                + " não existe!");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(
                                new AmbienteMessageResponse(
                                        false,
                                        AmbienteClockStatus.SUBMISSION_INVALID,
                                        false,
                                        false));
            } else if (!device.get().isAtivo()) {
                logger.error(
                        "[REST] Métricas não registadas pois o dispositivo "
                                + deviceId
                                + " está desativo!");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(
                                new AmbienteMessageResponse(
                                        false,
                                        AmbienteClockStatus.SUBMISSION_INVALID,
                                        false,
                                        false));
            } else if (!device.get().getProtocolos().contains(Protocolo.REST)) {

                logger.error(
                        "[REST] Métricas não registadas pois o dispositivo "
                                + deviceId
                                + " não envia dados através deste protocolo!");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(
                                new AmbienteMessageResponse(
                                        false,
                                        AmbienteClockStatus.SUBMISSION_INVALID,
                                        false,
                                        false));
            }

            temperatura = request.getTemperatura();
            humidade = request.getHumidade();
            timestampStr = request.getTimestamp();
        } catch (Exception e) {
            logger.error(
                    "[REST] Métricas não registadas pois ocorreu um erro a descodificar a mensagem recebida: "
                            + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(
                            new AmbienteMessageResponse(
                                    false, AmbienteClockStatus.SUBMISSION_INVALID, false, false));
        }

        // Validar timestamp emitida pelo dispositivo
        try {
            timestamp = OffsetDateTime.parse(timestampStr);
            long diferenca = Duration.between(timestamp, tempoInicioProcessamento).getSeconds();
            if (diferenca > 25) {
                status_clock = AmbienteClockStatus.SUBMISSION_CLOCK_EARLY;
            } else if (diferenca < -25) {
                status_clock = AmbienteClockStatus.SUBMISSION_CLOCK_LATE;
            } else {
                status_clock = AmbienteClockStatus.SUBMISSION_SUCCESS;
            }
        } catch (DateTimeParseException e) {
            status_clock = AmbienteClockStatus.SUBMISSION_INVALID;
        }

        // Registar métricas na base de dados
        try {
            repositories.metricasRepository.save(
                    new Metricas(
                            device.get(),
                            Protocolo.REST,
                            temperatura,
                            humidade,
                            timestamp != null ? timestamp.toLocalDateTime() : null));
        } catch (DataIntegrityViolationException _) {
            logger.debug("[REST] Descartando metricas duplicadas do dispositivo " + deviceId);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new AmbienteMessageResponse(false, status_clock, false, false));
        } catch (Exception e) {
            logger.error("[REST] Métricas não registadas pois ocorreu um erro: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AmbienteMessageResponse(false, status_clock, false, false));
        }

        // Verificar estado dos campos enviados
        if (humidade >= 0 && humidade <= 100) {
            status_humidade = true;
        }
        if (temperatura >= -50 && temperatura <= 100) {
            status_temperatura = true;
        }
        status = status_temperatura
                && status_humidade
                && (status_clock.equals(AmbienteClockStatus.SUBMISSION_SUCCESS));

        logger.info("[REST] Métricas registadas com sucesso:");
        logger.info("-> Dispositivo: " + deviceId);
        logger.info("-> Temperatura: " + temperatura);
        logger.info("-> Humidade: " + humidade);
        logger.info("-> Timestamp: " + timestamp);
        logger.info("-> Estado Global (sucesso): " + status);
        logger.info("-> Estado Temperatura (sucesso): " + status_temperatura);
        logger.info("-> Estado Humidade (sucesso): " + status_humidade);
        logger.info("-> Estado Clock: " + status_clock);

        AmbienteMessageResponse reply = new AmbienteMessageResponse(
                status, status_clock, status_temperatura, status_humidade);
        return ResponseEntity.ok(reply);
    }

    @GetMapping("/raw")
    public ResponseEntity<?> getRawMetrics(
            @RequestParam int deviceId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false, defaultValue = "false") boolean invalid) {
        LocalDateTime toDate;
        LocalDateTime fromDate;

        Optional<Dispositivo> dispositivo = repositories.dispositivoRepository.findById((long) deviceId);
        if (dispositivo.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("{\"error\": \"Dispositivo não encontrado\"}");
        }
        try {
            toDate = (to != null) ? LocalDateTime.parse(to) : LocalDateTime.now();
            fromDate = (from != null) ? LocalDateTime.parse(from) : toDate.minusDays(1);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\": \"Formato de data inválido\"}");
        }
        if (fromDate.isAfter(toDate)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\": \"Data inicial deve ser anterior à data final\"}");
        }

        List<Metricas> metricas = repositories.metricasRepository.findByDispositivo(dispositivo.get());

        List<ServerAmbienteRestDtoDispositivoMetricasUE> dtos = metricas.stream()
                .filter(m -> m.getTempoRegisto() != null
                        && !m.getTempoDispositivo().isBefore(fromDate)
                        && !m.getTempoDispositivo().isAfter(toDate))
                .map(ServerAmbienteRestDtoDispositivoMetricasUE::fromDatabase)
                .filter(dto -> invalid || dto.getStatus())
                .toList();

        logger.info("[REST] Métricas consultadas:");
        logger.info("-> Dispositivo: " + deviceId);
        logger.info("-> De: " + fromDate);
        logger.info("-> Até: " + toDate);
        logger.info("-> Total: " + dtos.size());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/average")
    public ResponseEntity<?> getAverageMetrics(
            @RequestParam String level,
            @RequestParam String id,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        LocalDateTime toDate;
        LocalDateTime fromDate;

        if (!levels.contains(level.trim().toLowerCase())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\": \"Formato do level inválido\"}");
        }
        try {
            toDate = (to != null) ? LocalDateTime.parse(to) : LocalDateTime.now();
            fromDate = (from != null) ? LocalDateTime.parse(from) : toDate.minusDays(1);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\": \"Formato de data inválido\"}");
        }
        if (fromDate.isAfter(toDate)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\": \"Data inicial deve ser anterior à data final\"}");
        }
        try {
            String levelLower = level.trim().toLowerCase();
            List<Metricas> metricas = new ArrayList<>();

            // Obter métricas baseado no nível
            if ("sala".equals(levelLower)) {
                Optional<Sala> sala = repositories.salaRepository.findById(id);
                if (sala.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body("{\"error\": \"Sala não encontrada\"}");
                }
                metricas = repositories.metricasRepository.findBySala(sala.get());
            } else if ("departamento".equals(levelLower)) {
                Optional<Departamento> departamento = repositories.departamentoRepository.findById(id);
                if (departamento.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body("{\"error\": \"Departamento não encontrado\"}");
                }
                metricas = repositories.metricasRepository.findByDepartamento(departamento.get());
            } else if ("piso".equals(levelLower)) {
                Optional<Piso> piso = repositories.pisoRepository.findById(Integer.parseInt(id));
                if (piso.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body("{\"error\": \"Piso não encontrado\"}");
                }
                metricas = repositories.metricasRepository.findByPiso(piso.get());
            } else if ("edificio".equals(levelLower)) {
                Optional<Edificio> edificio = repositories.edificioRepository.findById(id);
                if (edificio.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body("{\"error\": \"Edifício não encontrado\"}");
                }
                metricas = repositories.metricasRepository.findByEdificio(edificio.get());
            }

            // Filtrar métricas por intervalo de datas e status válido
            double temperaturaMedia = metricas.stream()
                    .filter(m -> m.getTempoRegisto() != null
                            && !m.getTempoDispositivo().isBefore(fromDate)
                            && !m.getTempoDispositivo().isAfter(toDate)
                            && !m.getTempoDispositivo().isAfter(m.getTempoRegisto())
                            && m.getTemperatura() >= -50
                            && m.getTemperatura() <= 100)
                    .mapToDouble(Metricas::getTemperatura)
                    .average()
                    .orElse(0);

            double humidadeMedia = metricas.stream()
                    .filter(m -> m.getTempoRegisto() != null
                            && !m.getTempoDispositivo().isBefore(fromDate)
                            && !m.getTempoDispositivo().isAfter(toDate)
                            && !m.getTempoDispositivo().isAfter(m.getTempoRegisto())
                            && m.getHumidade() >= 0
                            && m.getHumidade() <= 100)
                    .mapToDouble(Metricas::getHumidade)
                    .average()
                    .orElse(0);

            ServerAmbienteRestDtoDispositivoMediaMetricasUE response = new ServerAmbienteRestDtoDispositivoMediaMetricasUE(
                    (int) Math.round(temperaturaMedia),
                    (int) Math.round(humidadeMedia));

            logger.info("[REST] Médias de Métricas consultadas:");
            logger.info("-> Level: " + levelLower);
            logger.info("-> ID: " + id);
            logger.info("-> De: " + fromDate);
            logger.info("-> Até: " + toDate);
            logger.info("-> Temperatura média: " + temperaturaMedia);
            logger.info("-> Humidade média: " + humidadeMedia);

            return ResponseEntity.ok(response);
        } catch (NumberFormatException e) {
            logger.error("[REST] Erro ao consultar médias - ID inválido: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\": \"ID do recurso inválido\"}");
        } catch (Exception e) {
            logger.error("[REST] Erro ao consultar médias: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Erro ao consultar médias\"}");
        }
    }
}
