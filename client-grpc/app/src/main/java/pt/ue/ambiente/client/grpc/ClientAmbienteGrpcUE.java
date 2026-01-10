package pt.ue.ambiente.client.grpc;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.ue.ambiente.client.grpc.AmbienteProto.AmbienteServiceReply;
import pt.ue.ambiente.client.grpc.AmbienteProto.AmbienteServiceRequest;

public class ClientAmbienteGrpcUE {

    public static final float temperatura_maxima = 30.0f;
    public static final float temperatura_minima = 15.0f;
    public static final int humidade_maxima = 80;
    public static final int humidade_minima = 30;
    private float ultimaTemperatura;
    private int ultimaHumidade;
    private final Random random;
    private final ManagedChannel channel;

    private static final Logger logger = Logger.getLogger(ClientAmbienteGrpcUE.class.getName());

    private final AmbienteServiceGrpc.AmbienteServiceBlockingStub blockingStub;

    public ClientAmbienteGrpcUE(String hostname, int port) {
        channel = ManagedChannelBuilder.forAddress(hostname, port).usePlaintext().build();

        blockingStub = AmbienteServiceGrpc.newBlockingStub(channel);
        this.random = new Random();
        ultimaTemperatura = temperatura_minima + (temperatura_maxima - temperatura_minima) * random.nextFloat();
        ultimaHumidade = humidade_minima + random.nextInt(humidade_maxima - humidade_minima + 1);
        ultimaTemperatura = new BigDecimal(ultimaTemperatura).setScale(2, RoundingMode.HALF_UP).floatValue();
    }

    private void gerarTempHumd() {
        float variacaoTemp = (random.nextFloat() * 2.0f) - 1.0f; // Variação entre -1.0 e +1.0
        ultimaTemperatura += variacaoTemp;
        if (ultimaTemperatura < temperatura_minima) {
            ultimaTemperatura = temperatura_minima;
        } else if (ultimaTemperatura > temperatura_maxima) {
            ultimaTemperatura = temperatura_maxima;
        }
        ultimaTemperatura = new BigDecimal(ultimaTemperatura).setScale(2, RoundingMode.HALF_UP).floatValue();

        int variacaoHum = random.nextInt(3) - 1; // Variação entre -1, 0 e +1
        ultimaHumidade += variacaoHum;
        if (ultimaHumidade < humidade_minima) {
            ultimaHumidade = humidade_minima;
        } else if (ultimaHumidade > humidade_maxima) {
            ultimaHumidade = humidade_maxima;
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

    public void submeterDados(int deviceId, float temperatura, int humidade) {
        logger.info("A preparar envio -> ID: " + deviceId + " Temp: " + temperatura + " Hum: " + humidade);

        AmbienteServiceRequest request = AmbienteServiceRequest.newBuilder()
                .setDeviceId(deviceId)
                .setTemperatura(temperatura)
                .setHumidade(humidade)
                .setTimestamp(OffsetDateTime.now().toString())
                .build();

        AmbienteServiceReply response;
        try {
            response = blockingStub.submeterDadosAmbiente(request);
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC falhou com estado: {0}", e.getStatus());
            return;
        }

        logger.info("Resposta do Servidor: " + (response.getStatus() ? "ACEITE" : "REJEITADO"));
    }

    public void submeterDados(int deviceId) {
        gerarTempHumd();
        submeterDados(deviceId, ultimaTemperatura, ultimaHumidade);
    }

    public static void main(String[] args) throws Exception {
        Integer deviceId = null;
        Float manualTemp = null;
        Integer manualHum = null;
        String hostname = null;
        Integer port = null;

        if (args.length >= 3) {
            hostname = args[0];

            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.exit(-1);
            }

            try {
                deviceId = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                System.exit(-1);
            }

            if (args.length == 5) {
                try {
                    manualTemp = Float.valueOf(args[3]);
                } catch (NumberFormatException e) {
                    System.exit(-1);
                }

                try {
                    manualHum = Integer.parseInt(args[4]);
                } catch (NumberFormatException e) {
                    System.exit(-1);
                }
            } else if (args.length != 3) {

                System.exit(-1);
            }
        } else {
            System.exit(-1);
        }

        ClientAmbienteGrpcUE cliente = new ClientAmbienteGrpcUE(hostname, port);
        if (manualTemp == null) {
            while (true) {
                cliente.submeterDados(deviceId);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        } else {
            cliente.submeterDados(deviceId, manualTemp, manualHum);
        }
        cliente.close();
    }
}
