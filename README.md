# Sistema de Monitorização Ambiental para a Universidade de Évora

**Trabalho Realizado Por:**

- [Miguel Pinto, 58122](https://github.com/MiguelPinto20)
- [Miguel Lopes, 58540](https://github.com/miguelallopes)

## Como compilar/Executar

### Servidor

```sh
docker compose up --build
```

### Cliente MQTT

#### Modo Submissão Única

```sh
gradle -p client-mqtt run --args="<host> <porta> <id>"
```

#### Modo Submissão Continua

```sh
gradle -p client-mqtt run --args="<host> <porta> <id>  <temperatura> <humidade>"
```

### Cliente gRPC

#### Modo Submissão Única

```sh
gradle -p client-grpc run --args="<host> <porta> <id>"
```

#### Modo Submissão Continua

```sh
gradle -p client-grpc run --args="<host> <porta> <id>  <temperatura> <humidade>"
```

### Cliente REST

#### Modo Submissão Única

```sh
gradle -p client-rest bootRun --args="<id> --ambiente.server.url=<endpoint>"
```

#### Modo Submissão Continua

```sh
gradle -p client-rest bootRun --args="<id> <temperatura> <humidade> --ambiente.server.url=<endpoint>"
```

### Cliente Admin

```sh
gradle -p admin-cli bootRun --args="--ambiente.server.url=<endpoint>"
```
