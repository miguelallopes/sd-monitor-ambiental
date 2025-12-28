package pt.ue.ambiente.server.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pt.ue.ambiente.server.data.repository.DepartamentoRepository;
import pt.ue.ambiente.server.data.repository.DispositivoRepository;
import pt.ue.ambiente.server.data.repository.EdificioRepository;
import pt.ue.ambiente.server.data.repository.MetricasRepository;
import pt.ue.ambiente.server.data.repository.PisoRepository;
import pt.ue.ambiente.server.data.repository.SalaRepository;


@Service
public class ServerAmbienteDataUE {
    @Autowired
    public DepartamentoRepository departamentoRepository;
    
    @Autowired
    public DispositivoRepository dispositivoRepository;
    
    @Autowired
    public EdificioRepository edificioRepository;

    @Autowired
    public MetricasRepository metricasRepository;
    
    @Autowired
    public PisoRepository pisoRepository;

    @Autowired
    public SalaRepository salaRepository;
}
