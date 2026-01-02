package pt.ue.ambiente.server.message;

import java.io.Serializable;

public class AmbienteMessageResponse implements Serializable {
    boolean status;
    AmbienteClockStatus clockStatus;
    boolean temperaturaStatus;
    boolean humidadeStatus;

    public AmbienteMessageResponse(
            boolean status,
            AmbienteClockStatus clockStatus,
            boolean temperaturaStatus,
            boolean humidadeStatus) {
        this.status = status;
        this.clockStatus = clockStatus;
        this.temperaturaStatus = temperaturaStatus;
        this.humidadeStatus = humidadeStatus;
    }

    public boolean getStatus() {
        return status;
    }

    public AmbienteClockStatus getClockStatus() {
        return clockStatus;
    }

    public boolean getTemperaturaStatus() {
        return temperaturaStatus;
    }

    public boolean getHumidadeStatus() {
        return humidadeStatus;
    }

    public byte[] toMqttMessage() {
        return (status + ";" + clockStatus + ";" + temperaturaStatus + ";" + humidadeStatus)
                .getBytes();
    }

    public static AmbienteMessageResponse fromMqttPayload(byte[] payload) {
        String message = new String(payload);
        String[] parts = message.split(";");
        if (parts.length != 4) {
            throw new IllegalArgumentException(
                    "Invalid MQTT payload: expected 4 fields, got " + parts.length);
        }
        try {
            boolean status = Boolean.parseBoolean(parts[0]);
            AmbienteClockStatus clockStatus = AmbienteClockStatus.valueOf(parts[1]);
            boolean temperaturaStatus = Boolean.parseBoolean(parts[2]);
            boolean humidadeStatus = Boolean.parseBoolean(parts[3]);
            return new AmbienteMessageResponse(
                    status, clockStatus, temperaturaStatus, humidadeStatus);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Invalid number format in MQTT payload: " + message, e);
        }
    }
}
