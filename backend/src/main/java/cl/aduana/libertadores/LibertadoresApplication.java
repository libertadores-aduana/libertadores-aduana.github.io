package cl.aduana.libertadores;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class LibertadoresApplication {

    public static void main(String[] args) {
        SpringApplication.run(LibertadoresApplication.class, args);
    }
}
