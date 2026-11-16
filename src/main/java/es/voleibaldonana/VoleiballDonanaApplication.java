package es.voleibaldonana;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class VoleiballDonanaApplication {

    public static void main(String[] args) {
        SpringApplication.run(VoleiballDonanaApplication.class, args);
    }

}
