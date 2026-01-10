package pt.ue.ambiente.client.rest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import pt.ue.ambiente.client.rest.message.AmbienteMessagePublish;
import pt.ue.ambiente.client.rest.message.AmbienteMessageResponse;

public class ClientAmbienteRestUE {
    private static final Logger logger = Logger.getLogger(ClientAmbienteRestUE.class.getName());
    public static final float temperatura_maxima = 30.0f;
    public static final float temperatura_minima = 15.0f;
    public static final int humidade_maxima = 80;
    public static final int humidade_minima = 30;
    private float ultimaTemperatura;
    private int ultimaHumidade;

    private final RestClient restClient;
    private final Random random;

    public ClientAmbienteRestUE(String endpoint) {
        this.restClient = inicializarClienteRest(endpoint);
        this.random = new Random();
        ultimaTemperatura = temperatura_minima + (temperatura_maxima - temperatura_minima) * random.nextFloat();
        ultimaHumidade = humidade_minima + random.nextInt(humidade_maxima - humidade_minima + 1);
        ultimaTemperatura = new BigDecimal(ultimaTemperatura).setScale(2, RoundingMode.HALF_UP).floatValue();
    }

    private void gerarTempHumd() {
        float variacaoTemp = (random.nextFloat() * 2.0f) - 1.0f; // Variação entre -1.0 e +1.0
        ultimaTemperatura += variacaoTemp;
        if (ultimaTemperatura < temperatura_minima) {
            ultimaTemperatura = temperatura_minima;
        } else if (ultimaTemperatura > temperatura_maxima) {
            ultimaTemperatura = temperatura_maxima;
        }
        ultimaTemperatura = new BigDecimal(ultimaTemperatura).setScale(2, RoundingMode.HALF_UP).floatValue();

        int variacaoHum = random.nextInt(3) - 1; // Variação entre -1, 0 e +1
        ultimaHumidade += variacaoHum;
        if (ultimaHumidade < humidade_minima) {
            ultimaHumidade = humidade_minima;
        } else if (ultimaHumidade > humidade_maxima) {
            ultimaHumidade = humidade_maxima;
        }

    }

    public static RestClient inicializarClienteRest(String endpoint) {
        RestClient client = RestClient.builder().baseUrl(endpoint).build();
        return client;
    }

    public void publicar(int deviceId, float temperatura, int humidade) {
        String timestamp = OffsetDateTime.now().toString();
        AmbienteMessagePublish payload = new AmbienteMessagePublish(
                deviceId, temperatura, humidade, timestamp);

        logger.info("Iniciando envio REST [DevID: " + deviceId + "]");

        for (int tentativa = 1; tentativa <= 6; tentativa++) {
            try {
                ResponseEntity<AmbienteMessageResponse> response = restClient.post()
                        .uri("/api/metrics/ingest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(payload)
                        .retrieve()
                        .toEntity(AmbienteMessageResponse.class);

                if (response.getBody() != null) {
                    AmbienteMessageResponse body = response.getBody();
                    logger.info("Sucesso (Tentativa " + tentativa + ")! Resposta: " + body.toString());

                    if (!body.getStatus()) {
                        logger.warning("Nota: Servidor aceitou, mas dados marcados como inválidos (Clock/Range).");
                    }
                }

                return;

            } catch (HttpClientErrorException e) {
                if (e.getStatusCode().value() == 401) {
                    logger.log(Level.SEVERE, "Erro Fatal 401: Dispositivo não autorizado.");
                } else if (e.getStatusCode().value() == 403) {
                    logger.log(Level.SEVERE, "Erro Fatal 403: Dispositivo inativo ou proibido.");
                } else if (e.getStatusCode().value() == 409) {
                    logger.log(Level.WARNING, "Erro 409: Dados duplicados.");
                } else {
                    logger.log(Level.SEVERE, "Erro Cliente HTTP: " + e.getStatusCode());
                }
                return;

            } catch (HttpServerErrorException | ResourceAccessException e) {

                logger.warning("Falha na tentativa " + tentativa + " de " + 6 + ". Erro: " + e.getMessage());

                if (tentativa == 6) {
                    logger.log(Level.SEVERE, "Desistindo após " + 6 + " tentativas falhadas.");
                    break;
                } else {
                    try {
                        long waitSeconds = (long) Math.pow(2, tentativa);
                        long waitMs = waitSeconds * 1000;

                        logger.info("Aguardando " + waitSeconds + " segundos antes de tentar novamente...");
                        Thread.sleep(waitMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Erro inesperado na serialização ou sistema.", e);
                return;
            }
        }
    }

    public void publicar(int deviceId) {
        gerarTempHumd();

        publicar(deviceId, ultimaTemperatura, ultimaHumidade);
    }

    public static void main(String[] args) throws Exception {
        Integer deviceId = null;
        Float manualTemp = null;
        Integer manualHum = null;
        String endpoint = null;

        if (args.length >= 1) {
            endpoint = args[0];

            try {
                deviceId = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.exit(-1);
            }

            if (args.length == 4) {
                try {
                    manualTemp = Float.valueOf(args[2]);
                } catch (NumberFormatException e) {
                    System.exit(-1);
                }

                try {
                    manualHum = Integer.parseInt(args[3]);
                } catch (NumberFormatException e) {
                    System.exit(-1);
                }
            } else if (args.length != 2) {

                System.exit(-1);
            }
        } else {
            System.exit(-1);
        }

        ClientAmbienteRestUE cliente = new ClientAmbienteRestUE(endpoint);
        if (manualTemp == null) {
            while (true) {
                cliente.publicar(deviceId);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        } else {
            cliente.publicar(deviceId, manualTemp, manualHum);
        }
    }
}
