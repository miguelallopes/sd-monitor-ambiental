package pt.ue.ambiente.client.admin.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import pt.ue.ambiente.client.admin.api.ClientAmbienteAdminRestApiUE;

@Configuration
public class RestClientConfig {
    @Bean
    public ClientAmbienteAdminRestApiUE ambienteAdminRestApiUE(@Value("${ambiente.server.url}") String url) {
        return new ClientAmbienteAdminRestApiUE(url);
    }
}
