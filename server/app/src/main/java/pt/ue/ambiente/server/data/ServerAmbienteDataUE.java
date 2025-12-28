package pt.ue.ambiente.server.data;

import org.springframework.stereotype.Service;

import pt.ue.ambiente.server.data.repository.DepartamentoRepository;
import pt.ue.ambiente.server.data.repository.DispositivoRepository;
import pt.ue.ambiente.server.data.repository.EdificioRepository;
import pt.ue.ambiente.server.data.repository.MetricasRepository;
import pt.ue.ambiente.server.data.repository.PisoRepository;
import pt.ue.ambiente.server.data.repository.SalaRepository;

@Service
public class ServerAmbienteDataUE {
    public final DepartamentoRepository departamentoRepository;
    public final DispositivoRepository dispositivoRepository;
    public final EdificioRepository edificioRepository;
    public final MetricasRepository metricasRepository;
    public final PisoRepository pisoRepository;
    public final SalaRepository salaRepository;

    public ServerAmbienteDataUE(
            DepartamentoRepository departamentoRepository,
            DispositivoRepository dispositivoRepository,
            EdificioRepository edificioRepository,
            MetricasRepository metricasRepository,
            PisoRepository pisoRepository,
            SalaRepository salaRepository) {
        this.departamentoRepository = departamentoRepository;
        this.dispositivoRepository = dispositivoRepository;
        this.edificioRepository = edificioRepository;
        this.metricasRepository = metricasRepository;
        this.pisoRepository = pisoRepository;
        this.salaRepository = salaRepository;
    }
}
