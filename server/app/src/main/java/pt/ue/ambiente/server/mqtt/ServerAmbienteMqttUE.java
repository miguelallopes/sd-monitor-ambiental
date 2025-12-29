package pt.ue.ambiente.server.mqtt;

import org.springframework.beans.factory.annotation.Autowired;
import pt.ue.ambiente.server.data.ServerAmbienteDataUE;

public class ServerAmbienteMqttUE {
    @Autowired private final ServerAmbienteDataUE repositories;

    public ServerAmbienteMqttUE(ServerAmbienteDataUE repositories) {
        this.repositories = repositories;
    }
}
