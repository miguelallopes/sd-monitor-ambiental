package pt.ue.ambiente.client.rest;

import java.time.OffsetDateTime;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

import pt.ue.ambiente.client.rest.message.AmbienteMessagePublish;
import pt.ue.ambiente.client.rest.sensor.ClientAmbienteSensorUE;

@SpringBootApplication
@EnableRetry
public class ClientAmbienteRestUE implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(ClientAmbienteRestUE.class);

    private final ClientAmbienteRestServiceUE ambienteRestService;

    public ClientAmbienteRestUE(ClientAmbienteRestServiceUE ambienteRestService) {
        this.ambienteRestService = ambienteRestService;
    }

    public static void erro(String mensagem) {
        erro(mensagem, false);
    }

    public static void erro(String mensagem, boolean imprimirUtilizacao) {
        System.err.println("ERRO: " + mensagem);
        if (imprimirUtilizacao) {
            System.err.println();
            System.err.println("Utilização do cliente no modo de submissão continua:");
            System.err.println("    cliente-rest <id> --ambiente.server.url=<endpoint> ");
            System.err.println("Utilização do cliente no modo de submissão única: ");
            System.err.println("    cliente-rest <id> <temperatura> <humidade> --ambiente.server.url=<endpoint> ");
        }
        System.err.flush();
        System.exit(-1);
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            erro("Nenhum parâmetro especificado!", true);
        }

        SpringApplication.run(ClientAmbienteRestUE.class, args);
    }

    @Override
    public void run(String... args) {
        int deviceId = -1;
        float temperatura = -51;
        int humidade = -1;
        try {
            deviceId = Integer.parseInt(args[0]);
            if (deviceId <= 0)
                erro("O parâmetro do id do dispositivo deve de ser igual ou maior que 1!");
        } catch (NumberFormatException _) {
            erro("O parâmetro do id do dispositivo espera um número!", true);
        }

        if (args.length >= 3) {
            try {
                temperatura = Float.parseFloat(args[1]);
                if (temperatura < -50 || temperatura > 100) logger.warn("A temperatura especificada (" + temperatura  +"ºC) está fora dos limites considerados normais (-50º C a 100º C). O registo será efetuado mas a leitura será considerada inválida!");
            } catch (NumberFormatException _) {
                erro("O parâmetro da temperatura espera um número (ex: 18.75)!", true);
            }

            try {
                humidade = Integer.parseInt(args[2]);
                if (humidade < 0 || humidade > 100) logger.warn("A humidade especificada (" + humidade  +"%) está fora dos limites considerados normais (0% a 100%). O registo será efetuado mas a leitura será considerada inválida!");
            } catch (NumberFormatException _) {
                erro("O parâmetro da temperatura espera um número (ex: 18.75)!", true);
            }

            System.exit(modoSubmissaoUnica(deviceId, temperatura, humidade) ? 0 : 1);
        } else {
            modoSubmissaoContinua(deviceId);
        }
    }

    private void modoSubmissaoContinua(int deviceId) {
        BlockingQueue<AmbienteMessagePublish> queue = new LinkedBlockingQueue<>();
        ExecutorService executor = Executors.newCachedThreadPool();

        ClientAmbienteSensorUE sensor = new ClientAmbienteSensorUE(deviceId, queue);
        executor.submit(sensor);
        logger.info("[DISPOSITIVO-"+ deviceId + "] A geração de leituras ambientais foi iniciada!");

        executor.submit(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    AmbienteMessagePublish message = queue.take();
                    ambienteRestService.submeterLeituraAmbiente(message);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    private boolean modoSubmissaoUnica(int deviceId, float temperatura, int humidade) {
        String timestamp = OffsetDateTime.now().toString();
        AmbienteMessagePublish message = new AmbienteMessagePublish(deviceId, temperatura, humidade, timestamp);

        var response = ambienteRestService.submeterLeituraAmbiente(message);

        return (response != null && response.getStatus());
    }
}
