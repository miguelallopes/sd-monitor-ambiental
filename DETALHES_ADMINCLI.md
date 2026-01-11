# 📄 Detalhes da Integração e Utilização da Admin CLI

Bem-vindo ao guia completo da **Admin CLI**! Este documento explica como a aplicação de linha de comandos interage com o servidor e como pode configurá-la para as suas necessidades.

---

### 📚 Tabela de Conteúdos
1. [▶️ **Como Executar a Aplicação**](#1-como-executar-a-aplicação)
2. [🏗️ **Arquitetura e Integração**](#2-arquitetura-e-integração)
3. [🚀 **Utilização da API para Fazer Pedidos**](#3-utilização-da-api-para-fazer-pedidos)
4. [🐞 **Configuração do Logging para Depuração**](#4-configuração-do-logging)

---

## 1. ▶️ Como Executar a Aplicação

Para executar a aplicação `admin-cli` a partir da raiz do projeto, utilize o wrapper do Gradle.

### Comando Básico

O comando seguinte compila e executa a aplicação:

```bash
./gradlew -p admin-cli bootRun
```
- `./gradlew`: Invoca o wrapper do Gradle, garantindo que a versão correta do Gradle é usada.
- `-p admin-cli`: Especifica que o comando deve ser executado no subprojeto `admin-cli`.
- `bootRun`: Uma tarefa do Spring Boot que executa a aplicação.

### Especificar o Endpoint do Servidor

Por defeito, a aplicação liga-se ao servidor em `http://localhost:8080`, conforme definido em `admin-cli/app/src/main/resources/application.properties`.

Pode especificar um endpoint diferente no momento da execução, passando um argumento de programa. Isto é útil para testar contra um servidor de produção ou um servidor em outra máquina.

O comando para tal é:
```bash
./gradlew -p admin-cli bootRun --args='--ambiente.server.url=http://<outro-host>:<porta>'
```

**Exemplo prático:** Para ligar a um servidor no endereço `192.168.1.100` na porta `80`, o comando seria:
```bash
./gradlew -p admin-cli bootRun --args='--ambiente.server.url=http://192.168.1.100:80'
```

> **⚙️ Como Funciona?**
> O Spring Boot permite sobrepor qualquer propriedade do ficheiro `application.properties` através de argumentos de linha de comandos. O argumento `--args='...'` passa a string diretamente para a aplicação Spring Boot, que a interpreta e ajusta a configuração dinamicamente.

---

## 2. 🏗️ Arquitetura e Integração

A `admin-cli` foi construída com **Spring Boot**, o que nos oferece um sistema robusto de configuração e injeção de dependências. A magia acontece com a combinação de três componentes principais:

-   **`ClientAmbienteAdminUE`**: O coração da aplicação. É a classe principal (`@SpringBootApplication`) que gere a interface de linha de comandos, incluindo menus e a leitura de dados do utilizador.

-   **`RestClientConfig`**: A classe de configuração (`@Configuration`) que atua como uma "fábrica" para o nosso cliente REST. Ela cria uma instância de `ClientAmbienteAdminRestApiUE` e regista-a como um *bean* no contexto do Spring. O URL do servidor é lido a partir do ficheiro `application.properties`.

-   **`ClientAmbienteAdminRestApiUE`**: O nosso comunicador. Esta classe encapsula toda a lógica para enviar pedidos HTTP para o servidor. Graças ao Spring, uma instância desta classe é automaticamente injetada no `ClientAmbienteAdminUE` através da anotação `@Autowired`, pronta a ser usada.

> **💡 Nota:** Esta arquitetura desacoplada significa que a lógica da linha de comandos (`ClientAmbienteAdminUE`) não precisa de saber os detalhes de como os pedidos HTTP são feitos. Ela apenas utiliza o `restClient` injetado.

---

## 3. 🚀 Utilização da API para Fazer Pedidos

Com o `restClient` injetado, podemos implementar facilmente todas as funcionalidades do menu.

### Gestão de Dispositivos

<details>
<summary><strong>📝 Listar Todos os Dispositivos</strong></summary>

Chama `dispositivoList()` para obter uma lista de todos os dispositivos registados.

```java
private void listarDispositivos() {
    System.out.println("\n[Listar Dispositivos]");
    List<ServerAmbienteRestDtoDispositivoUE> dispositivos = restClient.dispositivoList();
    if (dispositivos.isEmpty()) {
        System.out.println("Nenhum dispositivo encontrado.");
        return;
    }
    // ... formatação da tabela ...
}
```
</details>

<details>
<summary><strong>🆔 Obter Dispositivo por ID</strong></summary>

Pede um ID ao utilizador e chama `dispositivoGetById()` para obter os detalhes de um dispositivo específico. O método retorna um `Optional`, que estará vazio se o dispositivo não for encontrado.

```java
private void verDetalhesDispositivo() {
    System.out.print("\nID do dispositivo: ");
    long id = scanner.nextLong();
    scanner.nextLine(); // Limpar buffer

    Optional<ServerAmbienteRestDtoDispositivoUE> dispositivoOpt = restClient.dispositivoGetById(id);

    if (dispositivoOpt.isPresent()) {
        ServerAmbienteRestDtoDispositivoUE dto = dispositivoOpt.get();
        System.out.println("Detalhes do Dispositivo ID: " + dto.getIdDispositivo());
        System.out.println("  - Nome: " + dto.getNome());
        System.out.println("  - Edifício: " + dto.getEdificio());
        System.out.println("  - Estado: " + (dto.isEstado() ? "Ativo" : "Inativo"));
    } else {
        System.out.println("❌ Dispositivo com ID " + id + " não encontrado.");
    }
}
```
</details>

<details>
<summary><strong>➕ Criar um Novo Dispositivo</strong></summary>

Recolhe os dados, cria um DTO `ServerAmbienteRestDtoDispositivoCreateUE`, e envia-o com `dispositivoCreate()`.

```java
private void criarDispositivo() {
    // ... lógica para ler dados do utilizador ...
    ServerAmbienteRestDtoDispositivoCreateUE novoDispositivo = new ServerAmbienteRestDtoDispositivoCreateUE();
    novoDispositivo.setNome("Sensor de Teste");
    // ... popular outros campos ...

    Optional<ServerAmbienteRestDtoDispositivoUE> dispositivoCriado = restClient.dispositivoCreate(novoDispositivo);
    if (dispositivoCriado.isPresent()) {
        System.out.println("✅ Dispositivo criado com sucesso! ID: " + dispositivoCriado.get().getIdDispositivo());
    } else {
        System.out.println("❌ Erro ao criar o dispositivo.");
    }
}
```
</details>

<details>
<summary><strong>🔄 Atualizar um Dispositivo</strong></summary>

Pede um ID e os novos dados, cria um DTO `ServerAmbienteRestDtoDispositivoUpdateUE` e chama `dispositivoUpdate()`. Note que apenas os campos preenchidos no DTO serão atualizados no servidor.

```java
private void atualizarDispositivo() {
    System.out.print("\nID do dispositivo a atualizar: ");
    long id = scanner.nextLong();
    scanner.nextLine();

    // Primeiro, verificar se o dispositivo existe
    if (restClient.dispositivoGetById(id).isEmpty()) {
        System.out.println("❌ Dispositivo com ID " + id + " não encontrado.");
        return;
    }

    System.out.print("Novo nome (deixe em branco para não alterar): ");
    String novoNome = scanner.nextLine();

    ServerAmbienteRestDtoDispositivoUpdateUE dadosUpdate = new ServerAmbienteRestDtoDispositivoUpdateUE();
    if (novoNome != null && !novoNome.trim().isEmpty()) {
        dadosUpdate.setNome(novoNome);
    }
    // ... ler e definir outros campos a atualizar ...

    Optional<ServerAmbienteRestDtoDispositivoUE> dispositivoAtualizado = restClient.dispositivoUpdate(id, dadosUpdate);
    if (dispositivoAtualizado.isPresent()) {
        System.out.println("✅ Dispositivo atualizado com sucesso!");
    } else {
        System.out.println("❌ Erro ao atualizar o dispositivo.");
    }
}
```
</details>

<details>
<summary><strong>🗑️ Remover um Dispositivo</strong></summary>

Pede um ID e chama `delete()`. O método retorna `true` se a remoção for bem-sucedida.

```java
private void removerDispositivo() {
    System.out.print("\nID do dispositivo a remover: ");
    long id = scanner.nextLong();
    scanner.nextLine();

    if (restClient.delete(id)) {
        System.out.println("✅ Dispositivo com ID " + id + " removido com sucesso.");
    } else {
        System.out.println("❌ Erro ao remover o dispositivo. Verifique se o ID existe.");
    }
}
```
</details>

### Consulta de Métricas

<details>
<summary><strong>📊 Obter Média de Métricas</strong></summary>

Usa `metricsAverage()` para calcular a temperatura e humidade médias para um determinado `Level` (edifício, piso, etc.) e um identificador (nome do edifício, número do piso, etc.). Pode também filtrar por data.

```java
private void consultarMediaMetricas() {
    // ... pedir ao user o Level (edificio, sala, etc.) e o ID (nome da sala, etc.) ...
    Level level = Level.sala;
    String id = "B2.10";
    String from = "2026-01-01T00:00:00"; // Opcional
    String to = "2026-01-12T00:00:00";   // Opcional

    Optional<ServerAmbienteRestDtoDispositivoMediaMetricasUE> media = restClient.metricsAverage(level, id, from, to);
    if (media.isPresent()) {
        System.out.println("Resultados para " + level + " '" + id + "':");
        System.out.println("  🌡️Temperatura Média: " + media.get().getTemperaturaMedia() + "°C");
        System.out.println("  💧 Humidade Média: " + media.get().getHumidadeMedia() + "%");
    } else {
        System.out.println("Nenhum resultado encontrado.");
    }
}
```
</details>

<details>
<summary><strong>📈 Obter Métricas Brutas (Raw)</strong></summary>

Usa `metricsRaw()` para obter uma lista de todas as leituras de um dispositivo específico. É possível filtrar por data e incluir ou não as leituras consideradas inválidas pelo servidor.

```java
private void consultarMetricasRaw() {
    System.out.print("\nID do dispositivo: ");
    int deviceId = scanner.nextInt();
    scanner.nextLine();

    System.out.print("Incluir leituras inválidas? (true/false): ");
    boolean invalid = scanner.nextBoolean();
    scanner.nextLine();

    List<ServerAmbienteRestDtoDispositivoMetricasUE> metricas = restClient.metricsRaw(deviceId, null, null, invalid); 
    
    if (metricas.isEmpty()) {
        System.out.println("Nenhuma métrica encontrada para este dispositivo.");
        return;
    }

    System.out.println("--- Métricas para o Dispositivo " + deviceId + " ---");
    for(ServerAmbienteRestDtoDispositivoMetricasUE metrica : metricas) {
        System.out.printf("Registo: %s | Temp: %.1f°C | Hum: %d%% | Status: %s%n",
            metrica.getTempoRegisto().toString(),
            metrica.getTemperatura(),
            metrica.getHumidade(),
            metrica.getStatus() ? "Válido" : "Inválido"
        );
    }
}
```
</details>

---

## 4. 🐞 Configuração do Logging

O logging é a sua melhor ferramenta para depurar a aplicação. Pode controlar facilmente o nível de detalhe dos logs no ficheiro `admin-cli/app/src/main/resources/application.properties`.

**Níveis de Log:**
- `TRACE`: O mais detalhado.
- `DEBUG`: Ótimo para depuração. Mostra os logs da nossa API.
- `INFO`: Standard. Mostra o fluxo geral da aplicação.
- `WARN`: Apenas avisos e erros.
- `ERROR`: Apenas erros críticos.
- `OFF`: Desliga todos os logs.

### Para Ativar Logging Detalhado (Modo de Depuração)

Para ver todos os detalhes das chamadas REST, defina o nível de log para `DEBUG` para o pacote da API.

```properties
# admin-cli/app/src/main/resources/application.properties

# URL do servidor
ambiente.server.url=http://localhost:8080

# Define o nível de log para o pacote da API como DEBUG
# Isto irá mostrar os logs de "logger.info(...)" na ClientAmbienteAdminRestApiUE
logging.level.pt.ue.ambiente.client.admin.api=DEBUG

# Opcional: Para ver logs da própria aplicação Spring Boot
logging.level.org.springframework=INFO
```

### Para Desativar o Logging (Modo de Produção)

Para uma saída mais limpa, pode silenciar os logs da API, mostrando apenas erros.

```properties
# admin-cli/app/src/main/resources/application.properties

ambiente.server.url=http://localhost:8080

# Mostra apenas avisos e erros da API
logging.level.pt.ue.ambiente.client.admin.api=WARN
```

> **🐛 Dica de Profissional:** Se algo não funcionar como esperado, a primeira coisa a fazer é ativar o logging `DEBUG` e verificar a consola. Os logs geralmente contêm a chave para resolver o problema!
