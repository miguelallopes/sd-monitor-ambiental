
package pt.ue.ambiente.client.admin.api;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import pt.ue.ambiente.client.admin.api.dto.ServerAmbienteRestDtoDispositivoCreateUE;
import pt.ue.ambiente.client.admin.api.dto.ServerAmbienteRestDtoDispositivoMediaMetricasUE;
import pt.ue.ambiente.client.admin.api.dto.ServerAmbienteRestDtoDispositivoMetricasUE;
import pt.ue.ambiente.client.admin.api.dto.ServerAmbienteRestDtoDispositivoUE;
import pt.ue.ambiente.client.admin.api.dto.ServerAmbienteRestDtoDispositivoUpdateUE;
import pt.ue.ambiente.client.admin.api.enumeration.Level;


public class ClientAmbienteAdminRestApiUE {

    private static final Logger logger = LoggerFactory.getLogger(ClientAmbienteAdminRestApiUE.class);

    private final RestClient restClient;

    public ClientAmbienteAdminRestApiUE(String endpoint) {
        this.restClient = RestClient.builder().baseUrl(endpoint).build();
    }

    public List<ServerAmbienteRestDtoDispositivoUE> dispositivoList() {
        logger.info("[REST CLIENT] GET /api/devices");
        try {
            ServerAmbienteRestDtoDispositivoUE[] arr = restClient.get()
                    .uri("/api/devices")
                    .retrieve()
                    .body(ServerAmbienteRestDtoDispositivoUE[].class);
            logger.info("[REST CLIENT] Dispositivos recebidos: {}", arr != null ? arr.length : 0);
            return arr != null ? Arrays.asList(arr) : Collections.emptyList();
        } catch (RestClientException e) {
            logger.error("[REST CLIENT] Erro ao listar dispositivos: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public Optional<ServerAmbienteRestDtoDispositivoUE> dispositivoGetById(int id) {
        return dispositivoGetById(Long.valueOf(id));
    }

    public Optional<ServerAmbienteRestDtoDispositivoUE> dispositivoGetById(Long id) {
        logger.info("[REST CLIENT] GET /api/devices/{}", id);
        try {
            ServerAmbienteRestDtoDispositivoUE dto = restClient.get()
                    .uri("/api/devices/{id}", id)
                    .retrieve()
                    .body(ServerAmbienteRestDtoDispositivoUE.class);
            logger.info("[REST CLIENT] Dispositivo {} encontrado: {}", id, dto != null);
            return Optional.ofNullable(dto);
        } catch (HttpClientErrorException.NotFound e) {
            logger.warn("[REST CLIENT] Dispositivo {} não encontrado.", id);
            return Optional.empty();
        } catch (RestClientException e) {
            logger.error("[REST CLIENT] Erro ao buscar dispositivo {}: {}", id, e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<ServerAmbienteRestDtoDispositivoUE> dispositivoCreate(
            ServerAmbienteRestDtoDispositivoCreateUE dto) {
        logger.info("[REST CLIENT] POST /api/devices - Payload: {}", dto);
        try {
            ServerAmbienteRestDtoDispositivoUE created = restClient.post()
                    .uri("/api/devices")
                    .body(dto)
                    .retrieve()
                    .body(ServerAmbienteRestDtoDispositivoUE.class);
            logger.info("[REST CLIENT] Dispositivo criado: {}", created != null ? created.getIdDispositivo() : null);
            return Optional.ofNullable(created);
        } catch (HttpClientErrorException.Conflict e) {
            logger.warn("[REST CLIENT] Conflito ao criar dispositivo: {}", dto.getNome());
            return Optional.empty();
        } catch (RestClientException e) {
            logger.error("[REST CLIENT] Erro ao criar dispositivo: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<ServerAmbienteRestDtoDispositivoUE> dispositivoUpdate(Long id,
            ServerAmbienteRestDtoDispositivoUpdateUE dto) {
        logger.info("[REST CLIENT] PUT /api/devices/{} - Payload: {}", id, dto);
        try {
            ServerAmbienteRestDtoDispositivoUE updated = restClient.put()
                    .uri("/api/devices/{id}", id)
                    .body(dto)
                    .retrieve()
                    .body(ServerAmbienteRestDtoDispositivoUE.class);
            logger.info("[REST CLIENT] Dispositivo atualizado: {}",
                    updated != null ? updated.getIdDispositivo() : null);
            return Optional.ofNullable(updated);
        } catch (HttpClientErrorException.NotFound e) {
            logger.warn("[REST CLIENT] Dispositivo {} não encontrado para update.", id);
            return Optional.empty();
        } catch (RestClientException e) {
            logger.error("[REST CLIENT] Erro ao atualizar dispositivo {}: {}", id, e.getMessage());
            return Optional.empty();
        }
    }

    public boolean delete(Long id) {
        logger.info("[REST CLIENT] DELETE /api/devices/{}", id);
        try {
            restClient.delete()
                    .uri("/api/devices/{id}", id)
                    .retrieve()
                    .toBodilessEntity();
            logger.info("[REST CLIENT] Dispositivo {} removido com sucesso.", id);
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            logger.warn("[REST CLIENT] Dispositivo {} não encontrado para remoção.", id);
            return false;
        } catch (RestClientException e) {
            logger.error("[REST CLIENT] Erro ao remover dispositivo {}: {}", id, e.getMessage());
            return false;
        }
    }

    public Optional<ServerAmbienteRestDtoDispositivoMediaMetricasUE> metricsAverage(Level level, String id,
            String from, String to) {
        logger.info("[REST CLIENT] GET /api/metrics/average?level={}&id={}&from={}&to={}", level, id, from, to);
        try {
            StringBuilder uri = new StringBuilder("/api/metrics/average?level={level}&id={id}");
            if (from != null)
                uri.append("&from={from}");
            if (to != null)
                uri.append("&to={to}");
            ServerAmbienteRestDtoDispositivoMediaMetricasUE dto = restClient.get()
                    .uri(uri.toString(), level, id, from, to)
                    .retrieve()
                    .body(ServerAmbienteRestDtoDispositivoMediaMetricasUE.class);
            logger.info("[REST CLIENT] Média recebida: {}", dto);
            return Optional.ofNullable(dto);
        } catch (HttpClientErrorException.NotFound | HttpClientErrorException.BadRequest e) {
            logger.warn("[REST CLIENT] Nenhum resultado para média (level={}, id={})", level, id);
            return Optional.empty();
        } catch (RestClientException e) {
            logger.error("[REST CLIENT] Erro ao buscar média: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public List<ServerAmbienteRestDtoDispositivoMetricasUE> metricsRaw(int deviceId, String from, String to,
            boolean invalid) {
        logger.info("[REST CLIENT] GET /api/metrics/raw?deviceId={}&from={}&to={}&invalid={}", deviceId, from, to,
                invalid);
        try {
            StringBuilder uri = new StringBuilder("/api/metrics/raw?deviceId={deviceId}");
            if (from != null)
                uri.append("&from={from}");
            if (to != null)
                uri.append("&to={to}");
            if (invalid)
                uri.append("&invalid=true");
            ServerAmbienteRestDtoDispositivoMetricasUE[] arr = restClient.get()
                    .uri(uri.toString(), deviceId, from, to)
                    .retrieve()
                    .body(ServerAmbienteRestDtoDispositivoMetricasUE[].class);
            logger.info("[REST CLIENT] Métricas recebidas: {}", arr != null ? arr.length : 0);
            return arr != null ? Arrays.asList(arr) : Collections.emptyList();
        } catch (HttpClientErrorException.NotFound | HttpClientErrorException.BadRequest e) {
            logger.warn("[REST CLIENT] Nenhum resultado para métricas raw (deviceId={})", deviceId);
            return Collections.emptyList();
        } catch (RestClientException e) {
            logger.error("[REST CLIENT] Erro ao buscar métricas raw: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
