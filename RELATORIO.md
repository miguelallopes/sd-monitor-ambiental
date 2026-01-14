# Relatório do Sistema de Monitorização Ambiental

## 1. Identificação dos Alunos
*   **Miguel Pinto**, l58122
*   **Miguel Lopes**, l58540

## 2. Justificação das Escolhas

### Estrutura da Base de Dados
A persistência de dados é feita através do **PostgreSQL**, com o esquema gerado automaticamente pelo **Hibernate (JPA)** a partir das entidades definidas no pacote `pt.ue.ambiente.server.data.entity`.

#### Entidades e Tabelas

1.  **Metricas**
    *   Responsável por armazenar o histórico de leituras ambientais.
    *   **Atributos Principais:**
        *   `id` (Long): Identificador.
        *   `temperatura` (float) e `humidade` (int): Os valores medidos.
        *   `protocolo` (Enum): O protocolo de origem (MQTT, gRPC, REST).
        *   `tempoDispositivo` (Timestamp): O momento da leitura no sensor.
        *   `tempoRegisto` (Timestamp): O momento da ingestão no servidor.
    *   **Restrições:** Chave única composta por (`dispositivo_id`, `tempoDispositivo`, `protocolo`) para garantir a idempotência e evitar registos duplicados (caso que pode acontecer no MQTT pois a mesma mensagem pode ser entregada várias vezes pelo Broker).

2.  **Dispositivo**
    *   Representa os sensores instalados na universidade.
    *   **Atributos:**
        *   `id` (Long): Identificador.
        *   `nome` (String): Nome escolhido pelo administrador para o dispositivos
        *   `ativo` (boolean): *Flag* que informa o estado do dispositivo
        *   `protocolos` (): Lista de protocolos que o dispositivo suporta.
    *   **Relacionamentos:** Possui chaves estrangeiras obrigatórias para `Edificio`, `Piso`, `Sala` e `Departamento`, definindo a sua localização física atual.

3.  **Localização**
    *   Definem como os dispositivos estão localizados na universidade
        *   **Edificio:** `nome` (String).
        *   **Departamento:** `nome` (String).
        *   **Sala:** `sala` (String).
        *   **Piso:** `numero` (int).

A localização que está no dispositivo define onde são registadas as novas métricas e a localização das métricas informam onde o dispositivo estava quando essa métrica foi registada.

### Métodos de Comunicação
A escolha de múltiplos protocolos reflete a diversidade de dispositivos num ambiente IoT real:
*   **MQTT (Message Queuing Telemetry Transport):** Implementado para sensores simples e de baixa potência (simulados pelo `client-mqtt`). O modelo *Publish/Subscribe* permite que os dispositivos enviem dados assincronamente sem manter conexões HTTP pesadas, poupando bateria e largura de banda, além de não exigir um sistema com muitos recursos.
*   **gRPC (Google Remote Procedure Call):** Implementado para gateways ou dispositivos com maior capacidade de processamento (simulados pelo `client-grpc`). O uso de **Protobuf** (Protocol Buffers) garante uma serialização binária compacta e eficiente, ideal para tráfego intenso e baixa latência.
*   **REST (Representational State Transfer):** Implementado para integração com dispositivos simples com suporte HTTP/JSON enviam dados através de endpoints
REST (simulados pelo `client-rest`). 

### Arquitetura do Sistema

O sistema usa uma arquitetura distribuída baseada em **microserviços**, orquestrada através de **Docker Compose**.

#### Arquitetura Utilizada no Servidor

1.  **Servidor Backend:**
    *   Responsável pela validação de dispositivos, ingestão de dados (e persistência dos mesmos na base de dados), agregação de métricas e exposição de APIs (REST e gRPC).

2.  **Message Broker MQTT**
    *   **Eclipse Mosquitto**

3.  **Base de Dados:**
    *   **PostgreSQL**



#### Clientes (Simuladores e Administração)
    
-   Aplicações independentes (CLI) que atuam como produtores de dados (sensores/gateways) ou cliente de gestão do serviço (Admin CLI).
-   Comunicam com o servidor de ingestão de métricas (através dos stubs gRPC e endpoints REST), ou com o broker (caso usem MQTT)

Usamos o docker pois garante a homogenidade do sistema, permitindo que façamos deploy em qualquer ambiente que seja possível executar o Docker.

## 3. Instruções Detalhadas de Configuração

### Pré-requisitos
*   **Docker** e **Docker Compose V2** instalados (para o servidor).
*   **Java 25** (para os clientes).

### Configuração da Base de Dados
O sistema utiliza o mecanismo de ORM **Hibernate (JPA)** para a gestão do esquema da base de dados.
*   **Não é necessária a execução manual de scripts SQL.**
*   Ao iniciar o servidor pela primeira vez, o Hibernate deteta as entidades e cria automaticamente as tabelas e relações necessárias (`ddl-auto: update`).
*   As credenciais e o nome da base de dados estão definidos nos ficheiro `db.env`

### Execução do Sistema
Para colocar todo o ambiente em funcionamento (Servidor, Base de Dados e Broker MQTT), executar o seguinte comando no diretório raiz do projeto

```bash
docker-compose up --build -d
```

### Execução dos Clientes
Para interagir com o sistema, utilizam-se os clientes desenvolvidos. Abaixo estão os comandos para execução:

#### Cliente MQTT
*   **Modo Submissão Única:**
    ```bash
    gradle -p client-mqtt run --args="<host> <porta> <id>"
    ```
*   **Modo Submissão Contínua:**
    ```bash
    gradle -p client-mqtt run --args="<host> <porta> <id> <temperatura> <humidade>"
    ```

#### Cliente gRPC
*   **Modo Submissão Única:**
    ```bash
    gradle -p client-grpc run --args="<host> <porta> <id>"
    ```
*   **Modo Submissão Contínua:**
    ```bash
    gradle -p client-grpc run --args="<host> <porta> <id> <temperatura> <humidade>"
    ```

#### Cliente REST
*   **Modo Submissão Única:**
    ```bash
    gradle -p client-rest bootRun --args="<id> --ambiente.server.url=<endpoint>"
    ```
*   **Modo Submissão Contínua:**
    ```bash
    gradle -p client-rest bootRun --args="<id> <temperatura> <humidade> --ambiente.server.url=<endpoint>"
    ```

#### Cliente Admin
*   **Execução:**
    ```bash
    gradle -p admin-cli bootRun --args="--ambiente.server.url=<endpoint>"
    ```

## 4. Observações de Desenvolvimento

TODO: Tamos fudidos

## 5. Análise de Performance

Com base nos testes realizados (registados em `resultados.csv`), comparou-se a latência dos três protocolos suportados. Os testes consistiram em 10 pedidos sequenciais para cada protocolo.

### Dados Obtidos (Amostra)
| N Pedido | REST (ms) | MQTT (ms) | gRPC (ms) |
| :--- | :--- | :--- | :--- |
| 1 (Warm-up) | 176 | 17 | 179 |
| 2 | 5 | 42 | 4 |
| ... | ... | ... | ... |
| 10 | 5 | 43 | 3 |

### Análise Comparativa
1.  **gRPC:** Demonstrou ser o protocolo **mais rápido** após o estabelecimento inicial da conexão (warm-up), com latências consistentes na ordem dos **3-4ms**. A eficiência do formato binário Protobuf é evidente aqui. O primeiro pedido é mais lento devido à inicialização do canal HTTP/2.
2.  **REST:** Apresentou uma performance muito sólida, com latências em torno de **5-6ms**. Embora ligeiramente mais lento que o gRPC (devido ao *overhead* do JSON e HTTP/1.1), é extremamente competitivo para a maioria das aplicações não-críticas.
3.  **MQTT:** Apresentou a maior latência média e variabilidade (**10-44ms**). Isto é esperado, pois o MQTT é um protocolo assíncrono que envolve um intermediário (o Broker). A mensagem tem de viajar do Cliente -> Broker -> Servidor. No entanto, para IoT, a latência de ~40ms é perfeitamente aceitável, e a sua vantagem reside na robustez em redes instáveis e baixo consumo de energia, não na latência pura de pedido-resposta.

**Conclusão:** Para comunicação interna de alta velocidade ou gateways, **gRPC** é a escolha ideal. Para sensores IoT distribuídos, **MQTT** é o padrão correto apesar da latência superior. **REST** continua a ser o "canivete suíço" para integração geral.
