package cl.aduana.libertadores.config;

import cl.aduana.libertadores.util.AesEncryptionUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityBeansConfig {

    @Bean
    public AesEncryptionUtil aesEncryptionUtil(@Value("${app.aes.secret-key}") String secretKey) {
        return new AesEncryptionUtil(secretKey);
    }
}
