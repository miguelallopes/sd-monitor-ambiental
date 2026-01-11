package pt.ue.ambiente.client.mqtt.message;

import java.io.Serializable;

/// Implementacao comum para o mqtt e rest
public class AmbienteMessagePublish implements Serializable {

    int deviceId;
    float temperatura;
    int humidade;
    String timestamp;

    @Override
    public String toString() {
        return (
            "AmbienteMessagePublish {" +
            "deviceId=" +
            deviceId +
            ", temperatura=" +
            temperatura +
            ", humidade=" +
            humidade +
            ", timestamp=" +
            timestamp +
            '}'
        );
    }

    public AmbienteMessagePublish(int deviceId, float temperatura, int humidade, String timestamp) {
        if (timestamp == null) {
            throw new IllegalArgumentException("Timestamp inválida");
        }

        if (deviceId <= 0) {
            throw new IllegalArgumentException("deviceId inválido (deve ser >= 1)");
        }

        this.deviceId = deviceId;
        this.temperatura = temperatura;
        this.humidade = humidade;
        this.timestamp = timestamp;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public float getTemperatura() {
        return temperatura;
    }

    public int getHumidade() {
        return humidade;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public byte[] toMqttMessage() {
        return new String(deviceId + ";" + temperatura + ";" + humidade + ";" + timestamp).getBytes();
    }

    public static AmbienteMessagePublish fromMqttPayload(byte[] payload) {
        if (payload == null) throw new IllegalArgumentException("Payload MQTT inválida");

        String message = new String(payload);
        String[] parts = message.split(";");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Payload MQTT inválida ou incompleta: " + message);
        }

        float temperatura = -50;
        int humidade = -1;
        int deviceId = -1;
        String timestamp = null;

        try {
            deviceId = Integer.parseInt(parts[0]);
        } catch (NumberFormatException _) {
            throw new IllegalArgumentException("Parâmetro deviceId da payload MQTT inválido: " + parts[0]);
        }

        try {
            temperatura = Float.parseFloat(parts[1]);
        } catch (NumberFormatException _) {
            throw new IllegalArgumentException("Parâmetro temperatura da payload MQTT inválido: " + parts[1]);
        }

        try {
            humidade = Integer.parseInt(parts[2]);
        } catch (NumberFormatException _) {
            throw new IllegalArgumentException("Parâmetro humidade da payload MQTT inválido: " + parts[2]);
        }

        timestamp = parts[3];

        return new AmbienteMessagePublish(deviceId, temperatura, humidade, timestamp);
    }
}
