package pt.ue.ambiente.server.grpc;

import pt.ue.ambiente.server.grpc.AmbienteProto.AmbienteServiceReply;
import pt.ue.ambiente.server.grpc.AmbienteProto.AmbienteServiceRequest;
import pt.ue.ambiente.server.grpc.AmbienteProto.AmbienteServiceClockStatus;


import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.zip.DataFormatException;

import io.grpc.stub.StreamObserver;

public class ServerAmbienteGrpcUE extends AmbienteServiceGrpc.AmbienteServiceImplBase {
    @Override
    public void submeterDadosAmbiente(AmbienteServiceRequest request, StreamObserver<AmbienteServiceReply> responseObserver) {
        responseObserver.onNext(executarSubmeterDadosAmbiente(request));
        responseObserver.onCompleted();
    }
    
    /* COMO VAI SER PROCESSADO O REQUEST*/
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
            status_clock = AmbienteServiceClockStatus.SUBMISSION_SUCCESS;
            // Verificar se atrasou ou adientou
        } catch (DateTimeParseException e) {
            timestamp = null;
        }


        // Validar se o deviceId existe na base de dados
        // Senao existir abortar

        if (humidade >= 0 && humidade <= 100) {
            status_humidade = true;
        }

        if (temperatura >= -50 && temperatura <= 100) {
            status_temperatura = true;
        }

        boolean status = status_temperatura && status_humidade && (status_clock.equals(AmbienteServiceClockStatus.SUBMISSION_SUCCESS));
        System.out.println(deviceId);
        System.out.println(temperatura);
        System.out.println(timestamp);
        System.out.println(humidade);
        // Colocar na base de dados

        return AmbienteServiceReply.newBuilder()
            .setStatus(status)
            .setClockStatus(status_clock)
            .setHumidadeStatus(status_humidade)
            .setTemperaturaStatus(status_temperatura)
            .build();
    } 
}
