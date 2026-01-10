package pt.ue.ambiente.client.admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ClientAmbienteAdminUE implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(ClientAmbienteAdminUE.class);

    public static void main(String[] args) {
        SpringApplication.run(ClientAmbienteAdminUE.class, args);
    }

    @Override
    public void run(String... args) {
        logger.info("Hello");
        // Implementar aqui
    }
}
