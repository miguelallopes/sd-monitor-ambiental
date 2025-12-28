package pt.ue.ambiente.server.grpc;

import pt.ue.ambiente.server.grpc.AmbienteProto.AmbienteServiceReply;
import pt.ue.ambiente.server.grpc.AmbienteProto.AmbienteServiceRequest;
import pt.ue.ambiente.server.data.ServerAmbienteDataUE;
import pt.ue.ambiente.server.data.entity.Departamento;
import pt.ue.ambiente.server.data.entity.Dispositivo;
import pt.ue.ambiente.server.data.entity.Edificio;
import pt.ue.ambiente.server.data.entity.Metricas;
import pt.ue.ambiente.server.data.entity.Piso;
import pt.ue.ambiente.server.data.entity.Sala;
import pt.ue.ambiente.server.data.enumeration.Protocolo;
import pt.ue.ambiente.server.data.repository.DepartamentoRepository;
import pt.ue.ambiente.server.data.repository.DispositivoRepository;
import pt.ue.ambiente.server.data.repository.EdificioRepository;
import pt.ue.ambiente.server.data.repository.MetricasRepository;
import pt.ue.ambiente.server.data.repository.PisoRepository;
import pt.ue.ambiente.server.data.repository.SalaRepository;
import pt.ue.ambiente.server.grpc.AmbienteProto.AmbienteServiceClockStatus;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.zip.DataFormatException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.grpc.stub.StreamObserver;
import jakarta.persistence.EntityNotFoundException;

@Service
public class ServerAmbienteGrpcUE extends AmbienteServiceGrpc.AmbienteServiceImplBase {

    @Autowired
    private final ServerAmbienteDataUE repositories;

    public ServerAmbienteGrpcUE(ServerAmbienteDataUE repositories) {
        this.repositories = repositories;
    }

    @Override
    public void submeterDadosAmbiente(AmbienteServiceRequest request,
            StreamObserver<AmbienteServiceReply> responseObserver) {
        responseObserver.onNext(executarSubmeterDadosAmbiente(request));
        responseObserver.onCompleted();
    }

    private AmbienteServiceReply executarSubmeterDadosAmbiente(AmbienteServiceRequest request) {

        // Variaveis a usar na resposta
        OffsetDateTime tempoInicioProcessamento = OffsetDateTime.now();
        AmbienteServiceClockStatus status_clock = AmbienteServiceClockStatus.SUBMISSION_INVALID;
        boolean status_humidade = false;
        boolean status_temperatura = false;

        // Apanhar os dados
        int deviceId = request.getDeviceId();
        float temperatura = request.getTemperatura();
        int humidade = request.getHumidade();

        OffsetDateTime timestamp = null;

        try {
            timestamp = OffsetDateTime.parse(request.getTimestamp());

            long diferenca = java.time.Duration.between(timestamp, tempoInicioProcessamento).getSeconds();

            if (diferenca > 15) {
                // Relogio atrasado
                status_clock = AmbienteServiceClockStatus.SUBMISSION_CLOCK_EARLY;
            } else if (diferenca < -15) {
                // Relogio adientado
                status_clock = AmbienteServiceClockStatus.SUBMISSION_CLOCK_LATE;
            } else {
                status_clock = AmbienteServiceClockStatus.SUBMISSION_SUCCESS;
            }
        } catch (DateTimeParseException e) {
            timestamp = null;
        }

        boolean status = false;

        Optional<Dispositivo> device = null;
        try {
            device = repositories.dispositivoRepository.findById(Long.valueOf(deviceId));
        } catch (EntityNotFoundException e) {

        }

        if (device.isPresent()) {
            if (humidade >= 0 && humidade <= 100) {
                status_humidade = true;
            }

            if (temperatura >= -50 && temperatura <= 100) {
                status_temperatura = true;
            }

            status = status_temperatura && status_humidade
                    && (status_clock.equals(AmbienteServiceClockStatus.SUBMISSION_SUCCESS));

            repositories.metricasRepository.save(new Metricas(device.get(), Protocolo.gRPC, temperatura, humidade));

        }

        return AmbienteServiceReply.newBuilder()
                .setStatus(status)
                .setClockStatus(status_clock)
                .setHumidadeStatus(status_humidade)
                .setTemperaturaStatus(status_temperatura)
                .build();
    }
}
