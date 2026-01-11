package pt.ue.ambiente.client.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import java.util.concurrent.TimeUnit;
import pt.ue.ambiente.client.grpc.AmbienteProto.AmbienteServiceReply;
import pt.ue.ambiente.client.grpc.AmbienteProto.AmbienteServiceRequest;
import pt.ue.ambiente.client.grpc.message.AmbienteClockStatus;
import pt.ue.ambiente.client.grpc.message.AmbienteMessagePublish;
import pt.ue.ambiente.client.grpc.message.AmbienteMessageResponse;

public class ClientAmbienteGrpcServiceUE {

    private final ManagedChannel channel;
    private final AmbienteServiceGrpc.AmbienteServiceBlockingStub blockingStub;

    public ClientAmbienteGrpcServiceUE(String hostname, int port) {
        this.channel = ManagedChannelBuilder.forAddress(hostname, port).usePlaintext().build();
        this.blockingStub = AmbienteServiceGrpc.newBlockingStub(channel);
    }

    public AmbienteMessageResponse submeterLeituraAmbiente(AmbienteMessagePublish mensagem) {
        AmbienteServiceRequest request = AmbienteServiceRequest.newBuilder()
            .setDeviceId(mensagem.getDeviceId())
            .setTemperatura(mensagem.getTemperatura())
            .setHumidade(mensagem.getHumidade())
            .setTimestamp(mensagem.getTimestamp())
            .build();

        AmbienteServiceReply reply;
        try {
            reply = blockingStub.submeterDadosAmbiente(request);
        } catch (StatusRuntimeException e) {
            System.err.println(
                "[DISPOSITIVO-" +
                    mensagem.getDeviceId() +
                    "|MENSAGEM-" +
                    mensagem.getTimestamp() +
                    "] Ocorreu um erro ao submeter as métricas ao servidor. Erro: " +
                    e.getStatus()
            );
            return null;
        }

        AmbienteClockStatus clockStatus = mapClockStatus(reply.getClockStatus());
        AmbienteMessageResponse response = new AmbienteMessageResponse(
            reply.getStatus(),
            clockStatus,
            reply.getTemperaturaStatus(),
            reply.getHumidadeStatus()
        );

        if (!response.getStatus()) {
            System.err.println(
                "[DISPOSITIVO-" +
                    mensagem.getDeviceId() +
                    "|MENSAGEM-" +
                    mensagem.getTimestamp() +
                    "] Métricas submetidas mas marcadas como inválidas, verifique os parâmetros de temperatura e humidade, estado do relógio, estado da conexão ou o estado do dispositivo no servidor!"
            );
        } else {
            System.out.println(
                "[DISPOSITIVO-" +
                    mensagem.getDeviceId() +
                    "|MENSAGEM-" +
                    mensagem.getTimestamp() +
                    "] Métricas submetidas! Resposta: " +
                    response
            );
        }

        return response;
    }

    private AmbienteClockStatus mapClockStatus(AmbienteProto.AmbienteServiceClockStatus protoStatus) {
        switch (protoStatus) {
            case SUBMISSION_SUCCESS:
                return AmbienteClockStatus.SUBMISSION_SUCCESS;
            case SUBMISSION_CLOCK_EARLY:
                return AmbienteClockStatus.SUBMISSION_CLOCK_EARLY;
            case SUBMISSION_CLOCK_LATE:
                return AmbienteClockStatus.SUBMISSION_CLOCK_LATE;
            case SUBMISSION_INVALID:
                return AmbienteClockStatus.SUBMISSION_INVALID;
            default:
                return AmbienteClockStatus.SUBMISSION_INVALID;
        }
    }

    public void close() {
        if (channel != null && !channel.isShutdown()) {
            try {
                channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
            }
        }
    }
}
