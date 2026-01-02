package pt.ue.ambiente.client.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.time.OffsetDateTime;
import pt.ue.ambiente.client.grpc.AmbienteProto.AmbienteServiceReply;
import pt.ue.ambiente.client.grpc.AmbienteProto.AmbienteServiceRequest;

public class ClientAmbienteGrpcUE {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 50051;

    public static void main(String[] args) {
        System.out.println("ola");

        ManagedChannel channel =
                ManagedChannelBuilder.forAddress(SERVER_HOST, SERVER_PORT).usePlaintext().build();

        System.out.println("===========================================");
        System.out.println("Cliente conectado ao servidor em " + SERVER_HOST + ":" + SERVER_PORT);
        System.out.println("===========================================");

        try {

            AmbienteServiceGrpc.AmbienteServiceBlockingStub stub =
                    AmbienteServiceGrpc.newBlockingStub(channel);

            AmbienteServiceRequest request =
                    AmbienteServiceRequest.newBuilder()
                            .setTemperatura(10)
                            .setHumidade(99)
                            .setDeviceId(1)
                            .setTimestamp(OffsetDateTime.now().toString())
                            .build();

            AmbienteServiceReply response = stub.submeterDadosAmbiente(request);

            System.out.println("Resposta do servidor: " + response.getStatus());

        } catch (Exception e) {
            System.err.println("Erro ao comunicar com o servidor: " + e.getMessage());
            e.printStackTrace();

        } finally {
            System.out.println("A fechar conexão...");
            channel.shutdown();
        }
    }
}
