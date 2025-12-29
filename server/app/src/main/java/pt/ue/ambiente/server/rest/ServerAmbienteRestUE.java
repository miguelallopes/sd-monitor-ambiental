package pt.ue.ambiente.server.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import pt.ue.ambiente.server.data.ServerAmbienteDataUE;

@Controller
public class ServerAmbienteRestUE {
    @Autowired private final ServerAmbienteDataUE repositories;

    public ServerAmbienteRestUE(ServerAmbienteDataUE repositories) {
        this.repositories = repositories;
    }
}
