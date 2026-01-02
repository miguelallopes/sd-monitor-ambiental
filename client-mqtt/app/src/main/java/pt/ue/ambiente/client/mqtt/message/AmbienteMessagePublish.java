package pt.ue.ambiente.client.mqtt.message;

import java.io.Serializable;

/// Implementacao comum para o mqtt e rest
public class AmbienteMessagePublish implements Serializable {
    int deviceId;
    float temperatura;
    int humidade;
    String timestamp;

    public AmbienteMessagePublish(int deviceId, float temperatura, int humidade, String timestamp) {
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
        String message = new String(payload);
        String[] parts = message.split(";");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid MQTT payload: expected 4 fields, got " + parts.length);
        }
        try {
            int deviceId = Integer.parseInt(parts[0]);
            float temperatura = Float.parseFloat(parts[1]);
            int humidade = Integer.parseInt(parts[2]);
            String timestamp = parts[3];
            return new AmbienteMessagePublish(deviceId, temperatura, humidade, timestamp);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format in MQTT payload: " + message, e);
        }
    }

}
