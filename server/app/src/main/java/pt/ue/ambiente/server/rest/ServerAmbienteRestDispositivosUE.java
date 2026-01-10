package pt.ue.ambiente.server.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pt.ue.ambiente.server.data.ServerAmbienteDataUE;

@RestController
@RequestMapping("/api/devices")
public class ServerAmbienteRestDispositivosUE {

    private static final Logger logger = LoggerFactory.getLogger(ServerAmbienteRestDispositivosUE.class);

    private final ServerAmbienteDataUE repositories;

    public ServerAmbienteRestDispositivosUE(ServerAmbienteDataUE repositories) {
        this.repositories = repositories;
    }
}
