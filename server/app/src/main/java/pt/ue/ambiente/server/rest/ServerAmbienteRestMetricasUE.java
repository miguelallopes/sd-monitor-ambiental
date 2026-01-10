package pt.ue.ambiente.server.rest;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pt.ue.ambiente.server.data.ServerAmbienteDataUE;
import pt.ue.ambiente.server.data.entity.Dispositivo;
import pt.ue.ambiente.server.data.entity.Metricas;
import pt.ue.ambiente.server.data.enumeration.Protocolo;
import pt.ue.ambiente.server.message.AmbienteClockStatus;
import pt.ue.ambiente.server.message.AmbienteMessagePublish;
import pt.ue.ambiente.server.message.AmbienteMessageResponse;

@RestController
@RequestMapping("/api/metrics")
public class ServerAmbienteRestMetricasUE {

    private static final Logger logger = LoggerFactory.getLogger(ServerAmbienteRestMetricasUE.class);

    private final ServerAmbienteDataUE repositories;

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
            long diferenca = java.time.Duration.between(timestamp, tempoInicioProcessamento).getSeconds();
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
        } catch (org.springframework.dao.DataIntegrityViolationException _) {
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
}
