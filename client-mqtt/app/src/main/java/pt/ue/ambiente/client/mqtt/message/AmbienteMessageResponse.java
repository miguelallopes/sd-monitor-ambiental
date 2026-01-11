package pt.ue.ambiente.client.mqtt.message;

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
        boolean humidadeStatus
    ) {
        if (clockStatus == null) this.clockStatus = AmbienteClockStatus.SUBMISSION_INVALID;
        else this.clockStatus = clockStatus;

        this.status = status;
        this.temperaturaStatus = temperaturaStatus;
        this.humidadeStatus = humidadeStatus;
    }

    @Override
    public String toString() {
        return (
            "AmbienteMessageResponse {" +
            "status=" +
            status +
            ", clockStatus=" +
            clockStatus +
            ", temperaturaStatus=" +
            temperaturaStatus +
            ", humidadeStatus=" +
            humidadeStatus +
            '}'
        );
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
        return (status + ";" + clockStatus + ";" + temperaturaStatus + ";" + humidadeStatus).getBytes();
    }

    public static AmbienteMessageResponse fromMqttPayload(byte[] payload) {
        if (payload == null) throw new IllegalArgumentException("Payload MQTT inválida");

        String message = new String(payload);
        String[] parts = message.split(";");

        if (parts.length != 4) {
            throw new IllegalArgumentException("Payload MQTT inválida ou incompleta: " + message);
        }

        boolean status = Boolean.parseBoolean(parts[0]);
        AmbienteClockStatus clockStatus = AmbienteClockStatus.valueOf(parts[1]);
        boolean temperaturaStatus = Boolean.parseBoolean(parts[2]);
        boolean humidadeStatus = Boolean.parseBoolean(parts[3]);
        return new AmbienteMessageResponse(status, clockStatus, temperaturaStatus, humidadeStatus);
    }
}
