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
cd client-grpc && gradle run
```

### Cliente gRPC

```sh
cd client-grpc && gradle run
```
