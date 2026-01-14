package pt.ue.ambiente.server.mqtt;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import pt.ue.ambiente.server.data.ServerAmbienteDataUE;
import pt.ue.ambiente.server.data.entity.Dispositivo;
import pt.ue.ambiente.server.data.entity.Metricas;
import pt.ue.ambiente.server.data.enumeration.Protocolo;
import pt.ue.ambiente.server.message.AmbienteClockStatus;
import pt.ue.ambiente.server.message.AmbienteMessagePublish;

@Service
public class ServerAmbienteMqttUE {

    private static final Logger logger = LoggerFactory.getLogger(ServerAmbienteMqttUE.class);

    @Value("${server.ambiente.mqttbroker}")
    private String BROKER;

    private static final String TOPIC = "ambiente";

    private final ServerAmbienteDataUE repositories;

    private MqttClient client;
    private MqttConnectionOptions options;
    private ExecutorService executor;

    public ServerAmbienteMqttUE(ServerAmbienteDataUE repositories) {
        this.repositories = repositories;
    }

    @PostConstruct
    public void init() {
        try {
            client = new MqttClient(
                    BROKER,
                    "sd-monitor-ambiental-server-"
                            + java.util.UUID.randomUUID().toString());

            executor = Executors.newCachedThreadPool();

            client.setCallback(
                    new org.eclipse.paho.mqttv5.client.MqttCallback() {
                        @Override
                        public void disconnected(
                                org.eclipse.paho.mqttv5.client.MqttDisconnectResponse disconnectResponse) {
                            System.out.println(
                                    "MQTT disconnected: " + disconnectResponse.getReasonString());
                        }

                        @Override
                        public void mqttErrorOccurred(
                                org.eclipse.paho.mqttv5.common.MqttException exception) {
                            System.err.println("MQTT error: " + exception.getMessage());
                        }

                        @Override
                        public void messageArrived(String topic, MqttMessage msg) throws Exception {
                            if (!msg.isDuplicate()) {
                                executor.submit(
                                        () -> {
                                            try {
                                                processMessage(msg);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        });
                            }
                        }

                        @Override
                        public void deliveryComplete(
                                org.eclipse.paho.mqttv5.client.IMqttToken token) {
                        }

                        @Override
                        public void connectComplete(boolean reconnect, String serverURI) {
                            if (reconnect) {
                                System.out.println("MQTT reconnected to " + serverURI);
                            } else {
                                System.out.println("MQTT connected to " + serverURI);
                            }
                        }

                        @Override
                        public void authPacketArrived(
                                int reasonCode,
                                org.eclipse.paho.mqttv5.common.packet.MqttProperties properties) {
                        }
                    });

            options = new MqttConnectionOptions();

            options.setAutomaticReconnect(true);

            client.connect(options);
            client.subscribe(TOPIC, 0);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PreDestroy
    public void shutdown() {
        try {
            if (client != null && client.isConnected()) {
                client.disconnect();
            }
            if (executor != null) {
                executor.shutdownNow();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processMessage(MqttMessage msg) throws Exception {
        // Variabeis para processamento
        AmbienteMessagePublish request = null;
        Optional<Dispositivo> device = Optional.empty();
        OffsetDateTime tempoInicioProcessamento = OffsetDateTime.now();

        // Campos com a leitura de metricas
        int deviceId = -1;
        float temperatura = 0;
        int humidade = 0;
        OffsetDateTime timestamp = null;

        String timestampStr = null;

        // Campos com o estado das leituras
        boolean status_humidade = false;
        boolean status_temperatura = false;
        AmbienteClockStatus status_clock = AmbienteClockStatus.SUBMISSION_INVALID;
        boolean status = false;

        // Ler dados recebidos do dispositivo
        try {
            request = AmbienteMessagePublish.fromMqttPayload(msg.getPayload());
            deviceId = request.getDeviceId();
            device = repositories.dispositivoRepository.findById(Long.valueOf(deviceId));
            if (deviceId < 0 || device.isEmpty()) {
                logger.error(
                        "[MQTT] Métricas não registadas pois o dispositivo "
                                + deviceId
                                + " não existe!");
                return;
            } else if (!device.get().isAtivo()) {
                logger.error(
                        "[MQTT] Métricas não registadas pois o dispositivo "
                                + deviceId
                                + " está desativo!");
                return;
            } else if (!device.get().getProtocolos().contains(Protocolo.MQTT)) {

                logger.error(
                        "[MQTT] Métricas não registadas pois o dispositivo "
                                + deviceId
                                + " não envia dados através deste protocolo!");

                return;
            }

            temperatura = request.getTemperatura();
            humidade = request.getHumidade();
            timestampStr = request.getTimestamp();
        } catch (Exception e) {
            logger.error(
                    "[MQTT] Métricas não registadas pois ocorreu um erro a descodificar a mensagem recebida: "
                            + e.getMessage());
            return;
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
                            Protocolo.MQTT,
                            temperatura,
                            humidade,
                            timestamp != null ? timestamp.toLocalDateTime() : null));
        } catch (DataIntegrityViolationException _) {
            logger.debug("[MQTT] Descartando metricas duplicadas do dispositivo " + deviceId);
            return;
        } catch (Exception e) {

            logger.error("[MQTT] Métricas não registadas pois ocorreu um erro: " + e.getMessage());
            return;
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

        logger.info("[MQTT] Métricas registadas com sucesso:");
        logger.info("-> Dispositivo: " + deviceId);
        logger.info("-> Temperatura: " + temperatura);
        logger.info("-> Humidade: " + humidade);
        logger.info("-> Timestamp: " + timestamp);

        logger.info("-> Estado Global (sucesso): " + status);
        logger.info("-> Estado Temperatura (sucesso): " + status_temperatura);
        logger.info("-> Estado Humidade (sucesso): " + status_humidade);
        logger.info("-> Estado Clock: " + status_clock);
    }
}
