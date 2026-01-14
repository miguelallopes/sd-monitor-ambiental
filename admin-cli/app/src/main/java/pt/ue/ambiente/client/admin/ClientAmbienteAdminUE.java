package pt.ue.ambiente.client.admin;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import pt.ue.ambiente.client.admin.api.ClientAmbienteAdminRestApiUE;
import pt.ue.ambiente.client.admin.api.dto.ServerAmbienteRestDtoDispositivoCreateUE;
import pt.ue.ambiente.client.admin.api.dto.ServerAmbienteRestDtoDispositivoMediaMetricasUE;
import pt.ue.ambiente.client.admin.api.dto.ServerAmbienteRestDtoDispositivoMetricasUE;
import pt.ue.ambiente.client.admin.api.dto.ServerAmbienteRestDtoDispositivoUE;
import pt.ue.ambiente.client.admin.api.dto.ServerAmbienteRestDtoDispositivoUpdateUE;
import pt.ue.ambiente.client.admin.api.enumeration.Level;
import pt.ue.ambiente.client.admin.api.enumeration.Protocolo;

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
        System.out.println("\n\n============================================");
        System.out.println("   ADMINISTRAÇÃO - MONITORIZAÇÃO AMBIENTAL  ");
        System.out.println("============================================");

        while (true) {
            System.out.println("\n=== MENU PRINCIPAL ===");
            System.out.println("1. Gestão de Dispositivos");
            System.out.println("2. Consulta de Métricas");
            System.out.println("3. Estatísticas do Sistema");
            System.out.println("0. Sair");
            System.out.print("Opção: ");

            String opcao = scanner.nextLine();

            switch (opcao) {
                case "1":
                    menuGestaoDispositivos();
                    break;
                case "2":
                    menuConsultaMetricas();
                    break;
                case "3":
                    verEstatisticas();
                    break;
                case "0":
                    System.out.println("A sair...");
                    return;
                default:
                    System.out.println("Opção inválida! Tente novamente.");
            }
        }
    }

    // --- GESTÃO DE DISPOSITIVOS ---

    private void menuGestaoDispositivos() {
        while (true) {
            System.out.println("\n--- GESTÃO DE DISPOSITIVOS ---");
            System.out.println("1. Listar todos");
            System.out.println("2. Adicionar novo");
            System.out.println("3. Atualizar existente");
            System.out.println("4. Remover dispositivo");
            System.out.println("5. Visualizar detalhes");
            System.out.println("0. Voltar");
            System.out.print("Opção: ");

            String opcao = scanner.nextLine();

            switch (opcao) {
                case "1":
                    listarDispositivos();
                    break;
                case "2":
                    criarDispositivo();
                    break;
                case "3":
                    atualizarDispositivo();
                    break;
                case "4":
                    removerDispositivo();
                    break;
                case "5":
                    verDetalhesDispositivo();
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Opção inválida!");
            }
        }
    }

    private void listarDispositivos() {
        System.out.println("\n[Listar Dispositivos]");
        List<ServerAmbienteRestDtoDispositivoUE> dispositivos = restClient.dispositivoList();

        if (dispositivos.isEmpty()) {
            System.out.println("Nenhum dispositivo encontrado.");
            return;
        }

        System.out.printf("%-4s | %-15s | %-10s | %-4s | %-10s | %-15s | %-20s | %-7s%n",
                "ID", "Nome", "Edifício", "Piso", "Sala", "Depto", "Protocolos", "Estado");
        System.out.println("-".repeat(105));
        for (ServerAmbienteRestDtoDispositivoUE d : dispositivos) {
            String protos = d.getProtocolos() != null ? d.getProtocolos().toString() : "[]";
            System.out.printf("%-4d | %-15.15s | %-10.10s | %-4d | %-10.10s | %-15.15s | %-20.20s | %-7s%n",
                    d.getIdDispositivo(),
                    d.getNome(),
                    d.getEdificio(),
                    d.getPiso(),
                    d.getSala(),
                    d.getDepartamento(),
                    protos,
                    d.isEstado() ? "Ativo" : "Inativo");
        }
    }

    private void verDetalhesDispositivo() {
        System.out.print("\nID do dispositivo: ");
        try {
            long id = Long.parseLong(scanner.nextLine());
            Optional<ServerAmbienteRestDtoDispositivoUE> opt = restClient.dispositivoGetById(id);

            if (opt.isPresent()) {
                ServerAmbienteRestDtoDispositivoUE d = opt.get();
                System.out.println("\n--- Detalhes do Dispositivo " + id + " ---");
                System.out.println("Nome: " + d.getNome());
                System.out.println("Estado: " + (d.isEstado() ? "Ativo" : "Inativo"));
                System.out.println("Protocolos: " + d.getProtocolos());
                System.out.println("Edifício: " + d.getEdificio());
                System.out.println("Piso: " + d.getPiso());
                System.out.println("Departamento: " + d.getDepartamento());
                System.out.println("Sala: " + d.getSala());
            } else {
                System.out.println(" Dispositivo não encontrado.");
            }
        } catch (NumberFormatException e) {
            System.out.println("ID inválido.");
        }
    }

    private void criarDispositivo() {
        System.out.println("\n[Novo Dispositivo]");
        try {
            ServerAmbienteRestDtoDispositivoCreateUE dto = new ServerAmbienteRestDtoDispositivoCreateUE();

            System.out.print("Nome: ");
            dto.setNome(scanner.nextLine());

            System.out.print("Edifício: ");
            dto.setEdificio(scanner.nextLine());

            System.out.print("Piso (número): ");
            dto.setPiso(Integer.parseInt(scanner.nextLine()));

            System.out.print("Departamento: ");
            dto.setDepartamento(scanner.nextLine());

            System.out.print("Sala: ");
            dto.setSala(scanner.nextLine());

            dto.setProtocolos(lerProtocolos());
            dto.setEstado(true); // Default ativo

            Optional<ServerAmbienteRestDtoDispositivoUE> created = restClient.dispositivoCreate(dto);
            if (created.isPresent()) {
                System.out.println("Dispositivo criado com ID: " + created.get().getIdDispositivo());
            } else {
                System.out.println("Erro ao criar dispositivo.");
            }

        } catch (NumberFormatException e) {
            System.out.println("Erro: Entrada numérica inválida.");
        }
    }

    private void atualizarDispositivo() {
        System.out.println("\n[Atualizar Dispositivo]");
        try {
            System.out.print("ID do dispositivo a atualizar: ");
            long id = Long.parseLong(scanner.nextLine());

            Optional<ServerAmbienteRestDtoDispositivoUE> existingOpt = restClient.dispositivoGetById(id);
            if (existingOpt.isEmpty()) {
                System.out.println("Dispositivo não encontrado.");
                return;
            }
            ServerAmbienteRestDtoDispositivoUE existing = existingOpt.get();

            ServerAmbienteRestDtoDispositivoUpdateUE dto = new ServerAmbienteRestDtoDispositivoUpdateUE();

            System.out.println("Deixe em branco para manter o valor atual (" + existing.getNome() + ")");
            System.out.print("Novo Nome: ");
            String nome = scanner.nextLine();
            if (!nome.isBlank()) dto.setNome(nome);

            System.out.println("Deixe em branco para manter (" + existing.getEdificio() + ")");
            System.out.print("Novo Edifício: ");
            String edificio = scanner.nextLine();
            if (!edificio.isBlank()) dto.setEdificio(edificio);

            System.out.println("Deixe em branco para manter (" + existing.getPiso() + ")");
            System.out.print("Novo Piso: ");
            String pisoStr = scanner.nextLine();
            if (!pisoStr.isBlank()) dto.setPiso(Integer.parseInt(pisoStr));

            System.out.println("Deixe em branco para manter (" + existing.getDepartamento() + ")");
            System.out.print("Novo Departamento: ");
            String dept = scanner.nextLine();
            if (!dept.isBlank()) dto.setDepartamento(dept);

            System.out.println("Deixe em branco para manter (" + existing.getSala() + ")");
            System.out.print("Nova Sala: ");
            String sala = scanner.nextLine();
            if (!sala.isBlank()) dto.setSala(sala);

            System.out.println("Deseja alterar protocolos? (s/n)");
            if (scanner.nextLine().equalsIgnoreCase("s")) {
                dto.setProtocolos(lerProtocolos());
            }

            System.out.println("Novo Estado (1-Ativo, 0-Inativo, ENTER-Manter): ");
            String estadoStr = scanner.nextLine();
            if (estadoStr.equals("1")) dto.setEstado(true);
            else if (estadoStr.equals("0")) dto.setEstado(false);

            Optional<ServerAmbienteRestDtoDispositivoUE> updated = restClient.dispositivoUpdate(id, dto);
            if (updated.isPresent()) {
                System.out.println("Dispositivo atualizado com sucesso.");
            } else {
                System.out.println("Erro ao atualizar dispositivo.");
            }

        } catch (NumberFormatException e) {
            System.out.println("Erro: Entrada numérica inválida.");
        }
    }

    private void removerDispositivo() {
        System.out.print("\nID do dispositivo a remover: ");
        try {
            long id = Long.parseLong(scanner.nextLine());
            if (restClient.delete(id)) {
                System.out.println("Dispositivo removido.");
            } else {
                System.out.println("Erro ao remover (verifique se ID existe).");
            }
        } catch (NumberFormatException e) {
            System.out.println("ID inválido.");
        }
    }

    // --- CONSULTA DE MÉTRICAS ---

    private void menuConsultaMetricas() {
        while (true) {
            System.out.println("\n--- CONSULTA DE MÉTRICAS ---");
            System.out.println("1. Consultar por Sala");
            System.out.println("2. Consultar por Departamento");
            System.out.println("3. Consultar por Piso");
            System.out.println("4. Consultar por Edifício");
            System.out.println("5. Métricas Brutas de um Dispositivo");
            System.out.println("0. Voltar");
            System.out.print("Opção: ");

            String opcao = scanner.nextLine();
            Level level = null;
            String entityId = "";

            switch (opcao) {
                case "1":
                    level = Level.sala;
                    System.out.print("Nome da Sala: ");
                    entityId = scanner.nextLine();
                    break;
                case "2":
                    level = Level.departamento;
                    System.out.print("Nome do Departamento: ");
                    entityId = scanner.nextLine();
                    break;
                case "3":
                    level = Level.piso;
                    while (true) {
                        System.out.print("Número do Piso: ");
                        String input = scanner.nextLine();
                        try {
                            Integer.parseInt(input);
                            entityId = input;
                            break;
                        } catch (NumberFormatException e) {
                            System.out.println("Erro: O piso deve ser um número inteiro. Tente novamente.");
                        }
                    }
                    break;
                case "4":
                    level = Level.edificio;
                    System.out.print("Nome do Edifício: ");
                    entityId = scanner.nextLine();
                    break;
                case "5":
                    consultarMetricasRaw();
                    return;
                case "0":
                    return;
                default:
                    System.out.println("Opção inválida.");
                    continue;
            }

            if (level != null) {
                System.out.print("Data Início (yyyy-MM-ddTHH:mm:ss) [ENTER para pular]: ");
                String from = scanner.nextLine();
                if (from.isBlank()) from = null;

                System.out.print("Data Fim (yyyy-MM-ddTHH:mm:ss) [ENTER para pular]: ");
                String to = scanner.nextLine();
                if (to.isBlank()) to = null;

                Optional<ServerAmbienteRestDtoDispositivoMediaMetricasUE> media = restClient.metricsAverage(level, entityId, from, to);
                if (media.isPresent()) {
                    System.out.println("\n--- Resultados ---");
                    System.out.println("Temp Média: " + media.get().getTemperaturaMedia() + "°C");
                    System.out.println("Humidade Média: " + media.get().getHumidadeMedia() + "%");
                } else {
                    System.out.println("Nenhum dado encontrado para os critérios.");
                }
            }
        }
    }

    private void consultarMetricasRaw() {
        try {
            System.out.print("ID do Dispositivo: ");
            int deviceId = Integer.parseInt(scanner.nextLine());

            System.out.print("Data Início (yyyy-MM-ddTHH:mm:ss) [ENTER para pular]: ");
            String from = scanner.nextLine();
            if (from.isBlank()) from = null;

            System.out.print("Data Fim (yyyy-MM-ddTHH:mm:ss) [ENTER para pular]: ");
            String to = scanner.nextLine();
            if (to.isBlank()) to = null;

            System.out.print("Incluir leituras inválidas? (s/n): ");
            boolean invalid = scanner.nextLine().equalsIgnoreCase("s");

            List<ServerAmbienteRestDtoDispositivoMetricasUE> metrics = restClient.metricsRaw(deviceId, from, to, invalid);

            if (metrics.isEmpty()) {
                System.out.println("Nenhuma métrica encontrada.");
                return;
            }

            System.out.printf("%-25s | %-10s | %-10s | %-10s | %-10s | %-40s%n", "Data/Hora", "Temp", "Hum", "Status", "Protocolo", "Relógio");
            System.out.println("-".repeat(120));
            for (ServerAmbienteRestDtoDispositivoMetricasUE m : metrics) {
                long diferenca = Duration.between(m.getTempoDispositivo(), m.getTempoRegisto()).toMillis();
                String clockInfo = String.format("%s (%+dms)", m.getAmbienteClockStatus(), diferenca);
                System.out.printf("%-25s | %-10.1f | %-10d | %-10s | %-10s | %-40s%n",
                        m.getTempoRegisto(),
                        m.getTemperatura(),
                        m.getHumidade(),
                        m.getStatus() ? "OK" : "Inválido",
                        m.getProtocolo(),
                        clockInfo);
            }

        } catch (NumberFormatException e) {
            System.out.println("ID inválido.");
        }
    }

    // --- UTILITÁRIOS ---

    private void verEstatisticas() {
        System.out.println("\n--- ESTATÍSTICAS DO SISTEMA ---");
        List<ServerAmbienteRestDtoDispositivoUE> all = restClient.dispositivoList();
        System.out.println("Total de Dispositivos Registados: " + all.size());
        long ativos = all.stream().filter(ServerAmbienteRestDtoDispositivoUE::isEstado).count();
        System.out.println("Dispositivos Ativos: " + ativos);
        System.out.println("Dispositivos Inativos: " + (all.size() - ativos));
    }

    private List<Protocolo> lerProtocolos() {
        List<Protocolo> protos = new ArrayList<>();
        System.out.println("Selecione os protocolos (usando os números, separados por vírgula):");
        System.out.println("1. gRPC");
        System.out.println("2. MQTT");
        System.out.println("3. REST");
        System.out.print("Escolha: ");
        String input = scanner.nextLine();

        for (String s : input.split(",")) {
            switch (s.trim()) {
                case "1": protos.add(Protocolo.gRPC); break;
                case "2": protos.add(Protocolo.MQTT); break;
                case "3": protos.add(Protocolo.REST); break;
            }
        }
        return protos;
    }
}
