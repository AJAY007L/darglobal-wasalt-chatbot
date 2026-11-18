package com.realestate.assistant;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.util.Map;
@SpringBootApplication
public class PropertyAssistantApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(PropertyAssistantApplication.class);
        String port = System.getenv("PORT");
        if (port != null && !port.isBlank()) app.setDefaultProperties(Map.of("server.port", port));
        app.run(args);
    }
}
