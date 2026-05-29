package cl.aduana.libertadores.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Envío de correos opcional. Si no hay SMTP configurado, el enlace se devuelve en la API (desarrollo/EFT).
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final boolean mailEnabled;
    private final String fromAddress;

    public EmailService(
            @Value("${app.mail.enabled:false}") boolean mailEnabled,
            @Value("${app.mail.from:noreply@libertadores.cl}") String fromAddress) {
        this.mailEnabled = mailEnabled;
        this.fromAddress = fromAddress;
    }

    public void enviarVerificacion(String destinatario, String enlace) {
        if (mailEnabled) {
            log.info("Correo de verificación a {} desde {} — enlace: {}", destinatario, fromAddress, enlace);
            // SMTP real: configurar spring.mail.* y JavaMailSender en producción
        } else {
            log.info("Verificación pendiente para {} — enlace: {}", destinatario, enlace);
        }
    }
}
