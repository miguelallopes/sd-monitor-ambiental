# Sistema de Monitorização Ambiental para a Universidade de Évora

## Como compilar/Executar

### Servidor

```sh
docker compose down
docker compose build
docker compose up
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
gradle -p client-rest run --args="<id> --ambiente.server.url=<endpoint>"
```

#### Modo Submissão Continua

```sh
gradle -p client-rest run --args="<id> <temperatura> <humidade> --ambiente.server.url=<endpoint>"
```
