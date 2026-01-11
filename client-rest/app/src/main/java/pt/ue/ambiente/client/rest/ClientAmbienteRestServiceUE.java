package pt.ue.ambiente.client.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import pt.ue.ambiente.client.rest.message.AmbienteMessagePublish;
import pt.ue.ambiente.client.rest.message.AmbienteMessageResponse;

@Service
public class ClientAmbienteRestServiceUE {

    private static final Logger logger = LoggerFactory.getLogger(ClientAmbienteRestServiceUE.class);

    private final RestClient restClient;

    public ClientAmbienteRestServiceUE(@Value("${ambiente.server.url}") String endpoint) {
        this.restClient = RestClient.builder().baseUrl(endpoint).build();
    }

    @Retryable(retryFor = { HttpServerErrorException.class, ResourceAccessException.class }, noRetryFor = {
            HttpClientErrorException.class }, maxAttempts = 6, backoff = @Backoff(delay = 2000, multiplier = 2))
    public AmbienteMessageResponse submeterLeituraAmbiente(AmbienteMessagePublish mensagem) {

        try {
            ResponseEntity<AmbienteMessageResponse> response = restClient.post()
                    .uri("/api/metrics/ingest")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(mensagem)
                    .retrieve()
                    .toEntity(AmbienteMessageResponse.class);

            if (response.getBody() != null) {
                AmbienteMessageResponse body = response.getBody();
                if (!body.getStatus()) {
                    logger.warn(
                            "[DISPOSITIVO-{}|MENSAGEM-{}] Métricas submetidas mas marcadas como inválidas, verifique os parâmetros de temperatura e humidade, estado do relógio ou estado da conexão com o servidor! Resposta: {}",
                            mensagem.getDeviceId(), mensagem.getTimestamp());
                } else {
                    logger.info("[DISPOSITIVO-{}|MENSAGEM-{}] Métricas submetidas! Resposta: {}",
                            mensagem.getDeviceId(), mensagem.getTimestamp(), body);
                }
                return body;
            }

        } catch (HttpClientErrorException e) {
            int code = e.getStatusCode().value();
            if (code == 401) {
                logger.error(
                        "[DISPOSITIVO-{}|MENSAGEM-{}] Não foi possível submeter as métricas pois o servidor não conseguiu identificar o dispositivo",
                        mensagem.getDeviceId(), mensagem.getTimestamp());
            } else if (code == 403) {
                logger.error(
                        "[DISPOSITIVO-{}|MENSAGEM-{}] Não foi possível submeter as métricas pois o dispositivo está desativo ou não está aprovado a receber métricas por este protocolo",
                        mensagem.getDeviceId(), mensagem.getTimestamp());
            } else if (code == 409) {
                logger.warn(
                        "[DISPOSITIVO-{}|MENSAGEM-{}] Descartando a submissão dos dados pois eles já foram corretamente submetidos anteriormente",
                        mensagem.getDeviceId(), mensagem.getTimestamp());
            } else {
                logger.error(
                        "[DISPOSITIVO-{}|MENSAGEM-{}] Ocorreu um erro ao submeter as métricas ao servidor. Erro: {}",
                        mensagem.getDeviceId(), mensagem.getTimestamp(), e.getMessage());
            }
        }

        return null;
    }

    @Recover
    public AmbienteMessageResponse recover(Exception e, AmbienteMessagePublish mensagem) {
        logger.error(
                "[DISPOSITIVO-{}|MENSAGEM-{}] As métricas serão descartadas pois não foi possível submeter as métricas passadas 6 tentativas  Erro: {}",
                mensagem.getDeviceId(), mensagem.getTimestamp(), e.getMessage());
        return null;
    }
}
