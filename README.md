# Sistema de Monitorização Ambiental para a Universidade de Évora

## Como compilar/Executar

### Servidor

```sh
docker compose down
docker compose build
docker compose up
```

### Cliente MQTT

```sh
gradle -p client-mqtt run
```

### Cliente gRPC

```sh
gradle -p client-grpc run
```
