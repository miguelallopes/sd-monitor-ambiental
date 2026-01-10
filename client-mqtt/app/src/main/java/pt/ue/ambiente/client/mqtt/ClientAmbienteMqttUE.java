package pt.ue.ambiente.client.mqtt;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.paho.mqttv5.client.IMqttClient;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;

import pt.ue.ambiente.client.mqtt.message.AmbienteMessagePublish;

public class ClientAmbienteMqttUE {
    private static final Logger logger = Logger.getLogger(ClientAmbienteMqttUE.class.getName());
    private static final String TOPIC = "ambiente";
    public static final float temperatura_maxima = 30.0f;
    public static final float temperatura_minima = 15.0f;
    public static final int humidade_maxima = 80;
    public static final int humidade_minima = 30;
    private float ultimaTemperatura;
    private int ultimaHumidade;


    private final IMqttClient mqttClient;
    private final Random random;

    public ClientAmbienteMqttUE(String hostname, int port) throws MqttException {
        this.mqttClient = inicializarClienteMQTT(hostname, port);
        this.random = new Random();
        ultimaTemperatura=temperatura_minima +(temperatura_maxima - temperatura_minima)* random.nextFloat();
        ultimaHumidade = humidade_minima + random.nextInt(humidade_maxima-humidade_minima + 1);
        ultimaTemperatura= new BigDecimal(ultimaTemperatura).setScale(2, RoundingMode.HALF_UP).floatValue();
    }

    private void gerarTempHumd() {
        float variacaoTemp = (random.nextFloat() * 2.0f) - 1.0f; // Variação entre -1.0 e +1.0
        ultimaTemperatura += variacaoTemp;
        if (ultimaTemperatura < temperatura_minima) {
            ultimaTemperatura = temperatura_minima;
        } else if (ultimaTemperatura > temperatura_maxima) {
            ultimaTemperatura = temperatura_maxima;
        }
        ultimaTemperatura= new BigDecimal(ultimaTemperatura).setScale(2, RoundingMode.HALF_UP).floatValue();

        int variacaoHum = random.nextInt(3) - 1; // Variação entre -1, 0 e +1
        ultimaHumidade += variacaoHum;
        if (ultimaHumidade < humidade_minima) {
            ultimaHumidade = humidade_minima;
        } else if (ultimaHumidade > humidade_maxima) {
            ultimaHumidade = humidade_maxima;
        }

    }

    public void close() {
        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                mqttClient.disconnect();
            } catch (MqttException e) {
                logger.log(Level.SEVERE, "Erro ao desconectar cliente MQTT", e);
            }
        }
    }

    public static IMqttClient inicializarClienteMQTT(String hostname, int port) throws MqttException {
        String clientId = "cliente-iot-" + UUID.randomUUID().toString();
        hostname = "tcp://" + hostname + ":" + port;
        MqttClient mqttClient = new MqttClient(hostname, clientId);

        MqttConnectionOptions options = new MqttConnectionOptions();
        options.setCleanStart(true);
        options.setAutomaticReconnect(true);
        mqttClient.connect(options);
        return mqttClient;
    }

    public void publicar(int deviceId, float temperatura, int humidade) {
        try {
            String timestamp = OffsetDateTime.now().toString();
            AmbienteMessagePublish payload = new AmbienteMessagePublish(
                    deviceId, temperatura, humidade, timestamp);

            byte[] dados = payload.toMqttMessage();

            MqttMessage msg = new MqttMessage(dados);
            msg.setQos(1);

            if (!mqttClient.isConnected()) {
                logger.warning("Cliente desconectado.");
                return;
            }

            mqttClient.publish(TOPIC, msg);
            logger.info("Publicado via MQTT: " + new String(dados));

        } catch (MqttException e) {
            logger.log(Level.SEVERE, "Erro ao publicar MQTT", e);
        }
    }

    public void publicar(int deviceId) {
        gerarTempHumd();

        publicar(deviceId, ultimaTemperatura, ultimaHumidade);
    }


    public static void main(String[] args) throws Exception {
        // Processamento de Argumentos
        Integer deviceId = null;
        Float manualTemp = null;
        Integer manualHum = null;
        String hostname = null;
        Integer port = null;
        // client-mqtt host porta devid <temperatura> <humidade>

        if (args.length >= 3) {
            hostname = args[0];

            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.exit(-1);
            }

            try {
                deviceId = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                System.exit(-1);
            }

            if (args.length == 5) {
                try {
                    manualTemp = Float.valueOf(args[3]);
                } catch (NumberFormatException e) {
                    System.exit(-1);
                }

                try {
                    manualHum = Integer.parseInt(args[4]);
                } catch (NumberFormatException e) {
                    System.exit(-1);
                }
            } else if (args.length != 3) {

                System.exit(-1);
            }
        } else {
            System.exit(-1);
        }

        ClientAmbienteMqttUE cliente = new ClientAmbienteMqttUE(hostname, port);
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
        cliente.close();
    }
}
