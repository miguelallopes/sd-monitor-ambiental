package pt.ue.ambiente.client.admin;

import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import pt.ue.ambiente.client.admin.api.ClientAmbienteAdminRestApiUE;

@SpringBootApplication
public class ClientAmbienteAdminUE implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(ClientAmbienteAdminUE.class);
    private final Scanner scanner = new Scanner(System.in);

    @Autowired
    private ClientAmbienteAdminRestApiUE restClient;

    public static void main(String[] args) {
        SpringApplication.run(ClientAmbienteAdminUE.class, args);
    }

    @Override
    public void run(String... args) {

        System.out.print(restClient.dispositivoGetById(1));
        logger.info("Hello");
        // Implementar aqui
        System.out.println("\n\n============================================");
        System.out.println("   ADMINISTRAÇÃO - MONITORIZAÇÃO AMBIENTAL  ");
        System.out.println("============================================");
        /*
        while (true) {
            System.out.println("\n=== MENU PRINCIPAL ===");
            System.out.println("1. Gestão de Dispositivos");
            System.out.println("2. Consulta de Métricas");
            System.out.println("3. Estatísticas do Sistema");
            System.out.println("0. Sair");
            System.out.print("Opção: ");


            int opcao = scanner.nextInt();

            switch (opcao) {
                case 1:
                    menuGestaoDispositivos();
                    break;
                case 2:
                    menuConsultaMetricas();
                    break;
                case 3:
                    verEstatisticas();
                    break;
                case 0: {
                    System.out.println("A sair...");
                    return;
                }
                default:
                    System.out.println("Opção inválida! Tente novamente.");
            }
            System.out.println("\n(Pressione ENTER para continuar...)");
            scanner.nextLine();


            */

        
    }

    private void enviarPedido() {

    }

    // opcao 1 Gestão de Dispositivos
    private void menuGestaoDispositivos() {
        System.out.println("\n--- GESTÃO DE DISPOSITIVOS ---");
        System.out.println("1. Listar todos");
        System.out.println("2. Adicionar novo");
        System.out.println("3. Atualizar existente");
        System.out.println("4. Remover dispositivo");
        System.out.println("5. Visualizar detalhes");
        System.out.println("0. Voltar");

        int opcao = scanner.nextInt();
        scanner.nextLine();

        switch (opcao) {
            case 1:
                enviarPedido();
                break;
            case 2:
                criarDispositivo();
                break;
            case 3:
                atualizarDispositivo();
                break;
            case 4:
                removerDispositivo();
                break;
            case 5:
                verDetalhesDispositivo();
                break;
            case 0:
                return;
            default:
                System.out.println("Opção inválida! Tente novamente.");
        }
    }

    private void criarDispositivo() {
        lerDadosDispositivo();
    }

    private void atualizarDispositivo() {
        System.out.println("\n[Atualizar Dispositivo]");
        System.out.print("ID do dispositivo a atualizar: ");
        int id = scanner.nextInt();
    }

    private void removerDispositivo() {
        System.out.print("\nID do dispositivo a remover: ");
        int id = scanner.nextInt();
    }

    private void verDetalhesDispositivo() {
        System.out.print("\nID do dispositivo: ");
        int id = scanner.nextInt();
        enviarPedido();
    }

    // opcao 2 Métricas
    private void menuConsultaMetricas() {
        System.out.println("\n--- CONSULTA DE MÉTRICAS ---");
        System.out.println("1. Consultar por Sala");
        System.out.println("2. Consultar por Departamento");
        System.out.println("3. Consultar por Piso");
        System.out.println("4. Consultar por Edifício");
        System.out.println("5. Consultar Dispositivo Específico");
        System.out.println("0. Voltar");

        int opcao = scanner.nextInt();
        scanner.nextLine();
        String paramName = "";

        switch (opcao) {
            case 1:
                paramName = "sala";
                System.out.print("Nome da Sala: ");
                break;
            case 2:
                paramName = "departamento";
                System.out.print("Nome do Departamento: ");
                break;
            case 3:
                paramName = "piso";
                System.out.print("Número do Piso: ");
                break;
            case 4:
                paramName = "edificio";
                System.out.print("Nome do Edifício: ");
                break;
            case 5:
                paramName = "deviceId";
                System.out.print("ID do Dispositivo: ");
                break;
            case 0:
                return;
            default:
                System.out.println("Inválido");
                return;
        }

    }

    // opcao 3 Estatísticas
    private void verEstatisticas() {
        System.out.println("\n--- ESTATÍSTICAS DO SISTEMA ---");
        enviarPedido();
    }

    // Leitura de Dados
    private void lerOpcaoSegura() {

    }

    private String lerDadosDispositivo() {
        try {

            System.out.print("ID (número): ");
            int id = Integer.parseInt(scanner.nextLine());

            System.out.print("Nome do Sensor: ");
            String nome = scanner.nextLine();

            System.out.print("Edifício: ");
            String edificio = scanner.nextLine();

            System.out.print("Sala: ");
            String sala = scanner.nextLine();

            System.out.print("Piso (número): ");
            int piso = Integer.parseInt(scanner.nextLine());

            System.out.print("Departamento: ");
            String dept = scanner.nextLine();

            System.out.println("Protocolo (1-GRPC, 2-MQTT, 3-REST): ");

        } catch (NumberFormatException e) {
            System.out.println("Erro: Valor numérico inválido.");
            return null;
        }
        return null;
    }

}
