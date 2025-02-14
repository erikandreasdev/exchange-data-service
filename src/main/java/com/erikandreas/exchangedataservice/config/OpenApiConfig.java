package com.erikandreas.exchangedataservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Crypto Price API")
                .version("1.0")
                .description("API for retrieving cryptocurrency prices from various exchanges")
                .contact(new Contact()
                    .name("Erik Andreas")
                    .email("contact@example.com")));
    }
}
