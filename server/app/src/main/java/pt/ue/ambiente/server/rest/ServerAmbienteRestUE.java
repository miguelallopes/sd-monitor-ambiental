package pt.ue.ambiente.server.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import pt.ue.ambiente.server.data.ServerAmbienteDataUE;

@Controller
public class ServerAmbienteRestUE {

    private static final Logger logger = LoggerFactory.getLogger(ServerAmbienteRestUE.class);

    @Autowired private final ServerAmbienteDataUE repositories;

    public ServerAmbienteRestUE(ServerAmbienteDataUE repositories) {
        this.repositories = repositories;
    }
}
