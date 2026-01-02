package pt.ue.ambiente.client.mqtt;

import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.client.MqttCallback;
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse;
import java.time.OffsetDateTime;
import pt.ue.ambiente.client.mqtt.message.AmbienteMessagePublish;

public class ClientAmbienteMqttUE {
    private static final int deviceId = 1;
    private static final String BROKER = "tcp://localhost:1883";
    private static final String PUBLISH_TOPIC = "ambiente";

    public static void main(String[] args) {
        try {
            MqttClient client = new MqttClient(BROKER, "sd-monitor-ambiental-client-" + java.util.UUID.randomUUID());
            MqttConnectionOptions options = new MqttConnectionOptions();
            options.setAutomaticReconnect(true);
            options.setCleanStart(true);
            client.connect(options);

            client.setCallback(new MqttCallback() {
                @Override
                public void disconnected(MqttDisconnectResponse disconnectResponse) {
                    System.out.println("MQTT disconnected: " + disconnectResponse.getReasonString());
                }

                @Override
                public void mqttErrorOccurred(org.eclipse.paho.mqttv5.common.MqttException exception) {
                    System.err.println("MQTT error: " + exception.getMessage());
                }

                @Override
                public void messageArrived(String topic, MqttMessage msg) throws Exception {
                }

                @Override
                public void deliveryComplete(org.eclipse.paho.mqttv5.client.IMqttToken token) {

                }

                @Override
                public void connectComplete(boolean reconnect, String serverURI) {
                    System.out.println("MQTT connect complete: " + serverURI);
                }

                @Override
                public void authPacketArrived(int reasonCode,
                        org.eclipse.paho.mqttv5.common.packet.MqttProperties properties) {

                }
            });

            // Publish message
            AmbienteMessagePublish publishMsg = new AmbienteMessagePublish(
                    deviceId, // deviceId
                    10.0f, // temperatura
                    99, // humidade
                    OffsetDateTime.now().toString());
            client.publish(PUBLISH_TOPIC, new MqttMessage(publishMsg.toMqttMessage()));
            System.out.println("Mensagem publicada no tópico: " + PUBLISH_TOPIC);

            client.disconnect();
            System.out.println("Cliente MQTT desconectado.");
        } catch (Exception e) {
            System.err.println("Erro no cliente MQTT: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
