package pt.ue.ambiente.server.rest.dto;
import java.time.Duration;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import pt.ue.ambiente.server.data.entity.Metricas;
import pt.ue.ambiente.server.message.AmbienteClockStatus;

@Data
public class ServerAmbienteRestDtoDispositivoMetricasUE {
    private Long id;
    private String dispositivo;
    private String sala;
    private float temperatura;
    private int humidade;
    private String protocolo;
    private LocalDateTime tempoRegisto;
    private LocalDateTime tempoDispositivo;

    public ServerAmbienteRestDtoDispositivoMetricasUE(Long id, String dispositivo, String sala, float temperatura,
            int humidade, String protocolo, LocalDateTime tempoRegisto, LocalDateTime tempoDispositivo) {
        this.id = id;
        this.dispositivo = dispositivo;
        this.sala = sala;
        this.temperatura = temperatura;
        this.humidade = humidade;
        this.protocolo = protocolo;
        this.tempoRegisto = tempoRegisto;
        this.tempoDispositivo = tempoDispositivo;
    }

    public static ServerAmbienteRestDtoDispositivoMetricasUE fromDatabase(Metricas m) {
        return new ServerAmbienteRestDtoDispositivoMetricasUE(
                m.getId(),
                m.getDispositivo() != null ? m.getDispositivo().getNome() : null,
                m.getSala() != null ? m.getSala().getSala() : null,
                m.getTemperatura(),
                m.getHumidade(),
                m.getProtocolo() != null ? m.getProtocolo().name() : null,
                m.getTempoRegisto(),
                m.getTempoDispositivo());
    }

    public Long getId() {
        return id;
    }

    public String getDispositivo() {
        return dispositivo;
    }

    public String getSala() {
        return sala;
    }

    public float getTemperatura() {
        return temperatura;
    }

    public int getHumidade() {
        return humidade;
    }

    public String getProtocolo() {
        return protocolo;
    }

    public LocalDateTime getTempoRegisto() {
        return tempoRegisto;
    }

    public LocalDateTime getTempoDispositivo() {
        return tempoDispositivo;
    }

    @JsonProperty("clockStatus")
    public AmbienteClockStatus getAmbienteClockStatus() {
        if (tempoRegisto == null) {
            return AmbienteClockStatus.SUBMISSION_INVALID;
        }
        long diferenca = Duration.between(tempoDispositivo, tempoRegisto).getSeconds();
        if (diferenca > 25) {
            return AmbienteClockStatus.SUBMISSION_CLOCK_EARLY;
        } else if (diferenca < -25) {
            return AmbienteClockStatus.SUBMISSION_CLOCK_LATE;
        } else {
            return AmbienteClockStatus.SUBMISSION_SUCCESS;
        }
    }

    @JsonProperty("humidadeStatus")
    public boolean getHumidadeStatus() {
        return humidade >= 0 && humidade <= 100;
    }

    @JsonProperty("temperaturaStatus")
    public boolean getTemperaturaStatus() {
        return temperatura >= -50f && temperatura <= 100f;
    }

    @JsonProperty("status")
    public boolean getStatus() {
        return getTemperaturaStatus() && getHumidadeStatus()
                && (getAmbienteClockStatus() == AmbienteClockStatus.SUBMISSION_SUCCESS);
    }
}
