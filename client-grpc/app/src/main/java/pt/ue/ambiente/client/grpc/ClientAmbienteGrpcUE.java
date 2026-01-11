package pt.ue.ambiente.client.grpc;

import java.time.OffsetDateTime;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import pt.ue.ambiente.client.grpc.message.AmbienteMessagePublish;
import pt.ue.ambiente.client.grpc.message.AmbienteMessageResponse;
import pt.ue.ambiente.client.grpc.sensor.ClientAmbienteSensorUE;

public class ClientAmbienteGrpcUE {

    private final ClientAmbienteGrpcServiceUE ambienteGrpcService;

    public ClientAmbienteGrpcUE(String hostname, int port) {
        this.ambienteGrpcService = new ClientAmbienteGrpcServiceUE(hostname, port);
    }

    public static void erro(String mensagem) {
        erro(mensagem, false);
    }

    public static void erro(String mensagem, boolean imprimirUtilizacao) {
        System.err.println("ERRO: " + mensagem);
        if (imprimirUtilizacao) {
            System.err.println();
            System.err.println("Utilização do cliente no modo de submissão continua:");
            System.err.println("    cliente-grpc <hostname> <port> <id> ");
            System.err.println("Utilização do cliente no modo de submissão única: ");
            System.err.println("    cliente-grpc <hostname> <port> <id> <temperatura> <humidade> ");
        }
        System.err.flush();
        System.exit(-1);
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            erro("Parâmetros incorretamente especificados!", true);
        }

        String hostname = args[0];
        int port = -1;
        try {
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            erro("O parâmetro da porta deve ser um número!", true);
        }

        int deviceId;
        try {
            deviceId = Integer.parseInt(args[2]);
            if (deviceId <= 0) {
                erro("O parâmetro do id do dispositivo deve ser igual ou maior que 1!");
            }
        } catch (NumberFormatException e) {
            erro("O parâmetro do id do dispositivo deve ser um número!", true);
        }

        ClientAmbienteGrpcUE client = new ClientAmbienteGrpcUE(hostname, port);
        client.run(args);
    }

    public void run(String... args) {
        int deviceId = Integer.parseInt(args[2]);
        float temperatura = -51;
        int humidade = -1;

        if (args.length >= 5) {
            try {
                temperatura = Float.parseFloat(args[3]);
                if (temperatura < -50 || temperatura > 100) {
                    System.err.println(
                        "A temperatura especificada (" +
                            temperatura +
                            "ºC) está fora dos limites considerados normais (-50º C a 100º C). O registo será efetuado mas a leitura será considerada inválida!"
                    );
                }
            } catch (NumberFormatException e) {
                erro("O parâmetro da temperatura deve ser um número (ex: 18.75)!", true);
            }

            try {
                humidade = Integer.parseInt(args[4]);
                if (humidade < 0 || humidade > 100) {
                    System.err.println(
                        "A humidade especificada (" +
                            humidade +
                            "%) está fora dos limites considerados normais (0% a 100%). O registo será efetuado mas a leitura será considerada inválida!"
                    );
                }
            } catch (NumberFormatException e) {
                erro("O parâmetro da humidade deve ser um número!", true);
            }

            System.exit(modoSubmissaoUnica(deviceId, temperatura, humidade) ? 0 : 1);
        } else {
            modoSubmissaoContinua(deviceId);
        }
    }

    private void modoSubmissaoContinua(int deviceId) {
        BlockingQueue<AmbienteMessagePublish> queue = new LinkedBlockingQueue<>();
        ExecutorService executor = Executors.newCachedThreadPool();

        Runtime.getRuntime().addShutdownHook(
            new Thread(() -> {
                ambienteGrpcService.close();
                executor.shutdown();
            })
        );

        ClientAmbienteSensorUE sensor = new ClientAmbienteSensorUE(deviceId, queue);
        executor.submit(sensor);
        System.out.println("[DISPOSITIVO-" + deviceId + "] A geração de leituras ambientais foi iniciada!");

        executor.submit(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    AmbienteMessagePublish message = queue.take();
                    ambienteGrpcService.submeterLeituraAmbiente(message);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    private boolean modoSubmissaoUnica(int deviceId, float temperatura, int humidade) {
        String timestamp = OffsetDateTime.now().toString();
        AmbienteMessagePublish message = new AmbienteMessagePublish(deviceId, temperatura, humidade, timestamp);

        AmbienteMessageResponse response = ambienteGrpcService.submeterLeituraAmbiente(message);

        return (response != null && response.getStatus());
    }
}
