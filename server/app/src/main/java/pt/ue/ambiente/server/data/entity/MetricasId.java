
package pt.ue.ambiente.server.data.entity;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Data;


@Entity
@Data
public class MetricasId {
    @ManyToOne(optional = false)
    private Dispositivo dispositivo;

    private LocalDateTime tempoRegisto = LocalDateTime.now();

    public MetricasId(Dispositivo dispositivo) {
        this.dispositivo = dispositivo;
    }
}
