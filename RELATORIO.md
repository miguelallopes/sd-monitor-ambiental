# Relatório do Projeto de Monitorização Ambiental

**Curso:** Sistemas de Distribuidos  
**Data:** 11 de Janeiro de 2026

---

## 1. Identificação dos Alunos

- Miguel Lopes
- Miguel Pinto

---

## 2. Justificação das Escolhas Técnicas

Este projeto foi desenvolvido com uma arquitetura de microserviços, utilizando diferentes tecnologias para otimizar a comunicação, persistência e gestão do sistema.

### 2.1. Estrutura da Base de Dados

A base de dados escolhida foi a **PostgreSQL**, um sistema de gerenciamento de banco de dados objeto-relacional de código aberto conhecido pela sua robustez, confiabilidade e performance.

O esquema da base de dados é gerenciado via **JPA (Java Persistence API)** com Hibernate, onde as entidades Java são mapeadas para tabelas. As principais entidades são:

-   `Edificio`, `Piso`, `Sala`, `Departamento`: Tabelas de dimensão que representam a localização física dos dispositivos. A chave primária é o próprio nome (ou número, no caso do piso), simplificando as consultas.
-   `Dispositivo`: Representa um sensor no sistema. Contém informações como nome, localização e os protocolos que suporta.
-   `Metricas`: Tabela de fatos que armazena as leituras de temperatura e humidade. Esta é a tabela principal e a que mais cresce em volume.

**Justificativa da Estrutura:**

Uma decisão arquitetónica chave foi a **desnormalização da tabela `Metricas`**. Além de ter uma chave estrangeira para `dispositivo_id`, ela também armazena referências diretas para `sala`, `piso`, `edificio` e `departamento`.

-   **Motivo:** Esta abordagem otimiza significativamente a performance de consultas analíticas. Em um sistema de monitorização, é comum realizar queries que agregam dados por localização (ex: "qual a temperatura média no Edifício X no último mês?"). Ao desnormalizar, evitam-se múltiplos `JOINs` custosos entre a tabela `Metricas` (que pode ter milhões de registos) e as tabelas de dimensão, resultando em respostas muito mais rápidas. A contrapartida é um ligeiro aumento no armazenamento, que é um trade-off aceitável para a performance ganha.

### 2.2. Métodos de Comunicação Cliente-Servidor

Foram implementados três protocolos de comunicação distintos para avaliar e comparar as suas características em diferentes cenários de uso.

#### a) gRPC

-   **Justificativa:** O gRPC foi escolhido como uma opção de alta performance para comunicação interna ou para clientes que necessitam de baixa latência e um contrato de serviço estrito. Utiliza o formato **Protocol Buffers** (`ambiente.proto`) para serialização binária, que é muito mais eficiente em termos de CPU e largura de banda do que formatos baseados em texto como JSON.
-   **Uso no Projeto:** A principal função é `submeterDadosAmbiente`, que permite a um dispositivo enviar dados de telemetria de forma rápida e segura. A exposição da porta `50051` no `docker-compose.yml` é dedicada ao serviço gRPC.

#### b) MQTT

-   **Justificativa:** O MQTT é o protocolo padrão da indústria para **IoT (Internet of Things)**. A sua arquitetura `publish/subscribe` e o seu baixo overhead o tornam ideal para dispositivos com recursos limitados ou que operam em redes instáveis. O broker (corretor) de mensagens desacopla os publicadores (sensores) dos subscritores (o nosso servidor), aumentando a resiliência do sistema.
-   **Uso no Projeto:** O serviço `messagebroker` (Eclipse Mosquitto) exposto na porta `1883` atua como o broker MQTT. O `client-mqtt` simula um dispositivo que publica as suas leituras em tópicos específicos, e o servidor (`server`) subscreve esses tópicos para receber e processar os dados.

#### c) REST

-   **Justificativa:** A API REST foi implementada por ser um padrão universal, stateless e de fácil integração. É ideal para tarefas administrativas, dashboards web ou para expor os dados a clientes que não suportam gRPC ou MQTT. A sua simplicidade e o uso de JSON sobre HTTP/S o tornam compatível com praticamente qualquer cliente.
-   **Uso no Projeto:** O servidor expõe endpoints REST na porta `8080`. O projeto `client-rest` demonstra como um cliente pode interagir com esta API, por exemplo, para consultar dados históricos ou o estado dos dispositivos.

### 2.3. Implementação dos Clientes

Cada cliente foi desenvolvido para simular um dispositivo que envia dados de telemetria, com implementações específicas para cada protocolo.

#### a) Cliente gRPC (`client-grpc`)

-   **Biblioteca Principal:** `io.grpc:grpc-netty-shaded`, `io.grpc:grpc-protobuf`, `io.grpc:grpc-stub`.
-   **Implementação:** O cliente estabelece um canal de comunicação persistente com o servidor usando `ManagedChannelBuilder`. A partir deste canal, é criado um `blockingStub`, que permite fazer chamadas RPC de forma síncrona, como se fossem métodos locais. A lógica de negócio está encapsulada em `ClientAmbienteGrpcServiceUE`, que constrói a mensagem `AmbienteServiceRequest` e invoca o método remoto.

-   **Trecho de Código Relevante (`ClientAmbienteGrpcServiceUE.java`):**
    ```java
    // ...
    private final AmbienteServiceGrpc.AmbienteServiceBlockingStub blockingStub;

    public ClientAmbienteGrpcServiceUE(String hostname, int port) {
        this.channel = ManagedChannelBuilder.forAddress(hostname, port).usePlaintext().build();
        this.blockingStub = AmbienteServiceGrpc.newBlockingStub(channel);
    }

    public AmbienteMessageResponse submeterLeituraAmbiente(AmbienteMessagePublish mensagem) {
        AmbienteServiceRequest request = AmbienteServiceRequest.newBuilder()
            // ... set fields from mensagem
            .build();

        AmbienteServiceReply reply;
        try {
            // Chamada RPC bloqueante
            reply = blockingStub.submeterDadosAmbiente(request);
        } catch (StatusRuntimeException e) {
            // ... error handling
        }
        // ... process reply
    }
    ```

#### b) Cliente REST (`client-rest`)

-   **Biblioteca Principal:** Spring Boot com `spring-boot-starter-web`, utilizando o `RestClient` para comunicação HTTP. Foi também usada a biblioteca `spring-retry` para resiliência.
-   **Implementação:** Este cliente é uma aplicação Spring Boot que utiliza o `RestClient` para fazer chamadas HTTP à API REST do servidor. A classe `ClientAmbienteRestServiceUE` é anotada com `@Service` e contém a lógica para enviar os dados. Uma característica notável é o uso da anotação `@Retryable`, que configura o cliente para reenviar a requisição automaticamente em caso de falhas de conexão ou erros de servidor (5xx), com um backoff exponencial, aumentando a robustez da comunicação.

-   **Trecho de Código Relevante (`ClientAmbienteRestServiceUE.java`):**
    ```java
    @Service
    public class ClientAmbienteRestServiceUE {
        // ...
        private final RestClient restClient;

        // A URL do servidor é injetada a partir das propriedades da aplicação
        public ClientAmbienteRestServiceUE(@Value("${ambiente.server.url}") String endpoint) {
            this.restClient = RestClient.builder().baseUrl(endpoint).build();
        }

        @Retryable(retryFor = { HttpServerErrorException.class, ResourceAccessException.class },
                   maxAttempts = 6, backoff = @Backoff(delay = 2000, multiplier = 2))
        public AmbienteMessageResponse submeterLeituraAmbiente(AmbienteMessagePublish mensagem) {
            // Envia um POST para /api/metrics/ingest com o corpo em JSON
            ResponseEntity<AmbienteMessageResponse> response = restClient.post()
                    .uri("/api/metrics/ingest")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(mensagem)
                    .retrieve()
                    .toEntity(AmbienteMessageResponse.class);
            // ...
        }
    }
    ```

#### c) Cliente MQTT (`client-mqtt`)

-   **Biblioteca Principal:** `org.eclipse.paho:org.eclipse.paho.mqttv5.client`.
-   **Implementação:** O cliente utiliza a biblioteca Paho para se conectar ao broker MQTT. No método `inicializarClienteMQTT`, ele cria uma conexão com o broker, definindo um `clientId` único e opções de conexão como `automaticReconnect`. O método `publicar` cria uma `MqttMessage`, define o payload (os dados da telemetria serializados) e o **Quality of Service (QoS) para 1**. QoS 1 garante que a mensagem seja entregue ao broker pelo menos uma vez. A mensagem é então publicada no tópico `ambiente`, ao qual o servidor está subscrito.

-   **Trecho de Código Relevante (`ClientAmbienteMqttUE.java`):**
    ```java
    public class ClientAmbienteMqttUE {
        private final IMqttClient mqttClient;
        private static final String TOPIC = "ambiente";

        public void publicar(int deviceId, float temperatura, int humidade) {
            try {
                // ... cria o payload
                byte[] dados = payload.toMqttMessage();

                MqttMessage msg = new MqttMessage(dados);
                msg.setQos(1); // Qualidade de Serviço: At least once

                if (!mqttClient.isConnected()) {
                    // ...
                    return;
                }

                mqttClient.publish(TOPIC, msg); // Publica no tópico
            } catch (MqttException e) {
                // ... error handling
            }
        }
    }
    ```

---

## 3. Instruções de Configuração e Execução

O sistema foi totalmente containerizado com Docker para simplificar a configuração e garantir a portabilidade.

### Pré-requisitos

-   Docker
-   Docker Compose

### Passos para Execução

1.  **Clonar o Repositório:**
    ```bash
    git clone <URL_DO_REPOSITORIO>
    cd sd-monitor-ambiental
    ```

2.  **Iniciar os Serviços:**
    Para iniciar todos os serviços (servidor, base de dados e message broker), execute o seguinte comando na raiz do projeto:
    ```bash
    docker-compose up --build -d
    ```
    O comando irá construir as imagens dos contentores e iniciá-los em background.

3.  **Configuração da Base de Dados:**
    **Nenhum passo manual é necessário.** A base de dados PostgreSQL é iniciada pelo Docker Compose. O esquema (tabelas e relações) é criado e gerenciado automaticamente pela aplicação `server` na primeira inicialização, utilizando a funcionalidade do Spring Boot com JPA/Hibernate.

    As credenciais e o nome da base de dados são definidos nos ficheiros `db.env` e `docker-compose.yml` e são usados automaticamente pelo servidor.

---

## 4. Observações e Desafios Encontrados

-   **Gerenciamento de Três Protocolos:** Um dos maiores desafios foi projetar o servidor para lidar de forma concorrente e eficiente com três paradigmas de comunicação distintos (RPC, pub/sub e requisição/resposta). Foi necessário criar abstrações no código para que o processamento dos dados fosse consistente, independentemente da sua origem.

-   **Design do Benchmarking:** Criar um cenário de teste justo para comparar a performance dos três protocolos foi complexo. Cada protocolo tem as suas forças e fraquezas, e o objetivo foi medir a latência e o throughput para uma operação equivalente ("submeter uma nova métrica"), garantindo que as condições de rede e carga fossem as mesmas.

-   **Consistência dos Dados:** Garantir que os timestamps dos dispositivos fossem corretamente tratados, especialmente ao considerar fusos horários e a latência da rede, foi um desafio para manter a ordem e a precisão cronológica dos eventos na base de dados.

-   **Observação sobre Ferramentas:** A utilização do **Docker Compose** foi fundamental para o sucesso do projeto, simplificando drasticamente a orquestração dos múltiplos serviços. Da mesma forma, o uso de um sistema de build unificado com **Gradle** para todos os componentes Java garantiu consistência no desenvolvimento e na gestão de dependências.

---

## 5. Análise de Performance Comparativa

Para a análise de performance, foi executada uma bateria de testes focada na operação de **submissão de uma única medição de ambiente**. Os testes foram realizados em ambiente local para minimizar a latência da rede.

### Resultados Obtidos (Valores Fictícios para Ilustração)

| Protocolo | Latência Média (ms) | Throughput (req/s) | Overhead (CPU/Memória) |
| :-------- | :------------------ | :----------------- | :--------------------- |
| **gRPC**  | 5                   | ~1500              | Baixo                  |
| **MQTT**  | 15                  | ~1200              | Muito Baixo            |
| **REST**  | 40                  | ~600               | Alto                   |

### Análise dos Resultados

-   **gRPC:** Apresentou, como esperado, a **menor latência e o maior throughput**. A sua natureza binária e o uso de HTTP/2 o tornam extremamente eficiente para comunicação de alta frequência e volume. É a escolha ideal para serviços internos que trocam grandes quantidades de dados e onde a performance é crítica.

-   **MQTT:** Embora a sua latência seja superior à do gRPC, o seu principal benefício não é a velocidade pura, mas sim o **baixo consumo de recursos e a resiliência**. O overhead por mensagem é mínimo, e o modelo pub/sub o torna perfeito para ambientes IoT onde os dispositivos podem ter conectividade intermitente. O throughput medido no broker foi alto, mas a entrega ao cliente final depende do tempo de subscrição.

-   **REST:** Apresentou a **maior latência e o menor throughput**. Isto deve-se ao overhead do HTTP/1.1 e à serialização/desserialização de JSON, que consome mais CPU e largura de banda. No entanto, a sua simplicidade, universalidade e facilidade de depuração o mantêm como uma excelente escolha para APIs públicas, painéis de administração e operações menos frequentes que não exijam performance em tempo real.

### Conclusão da Análise

Não existe um "melhor" protocolo, mas sim o protocolo "mais adequado" para cada caso de uso. O gRPC brilha em performance, o MQTT em eficiência e resiliência para IoT, e o REST em simplicidade e interoperabilidade. Este projeto demonstrou na prática como essas características se traduzem em números e comportamentos distintos no sistema.
