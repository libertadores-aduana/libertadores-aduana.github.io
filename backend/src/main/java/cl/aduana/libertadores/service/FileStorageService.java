package cl.aduana.libertadores.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Set<String> MIME_PERMITIDOS = Set.of(
            "application/pdf",
            "image/jpeg",
            "image/png",
            "image/jpg"
    );

    private final Path uploadDir;
    private final long maxBytes;

    public FileStorageService(
            @Value("${app.upload.dir:uploads}") String uploadDir,
            @Value("${app.upload.max-size-mb:5}") int maxMb) throws IOException {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.maxBytes = maxMb * 1024L * 1024L;
        Files.createDirectories(this.uploadDir);
    }

    public String guardar(MultipartFile file) throws IOException {
        validar(file);
        String extension = obtenerExtension(file.getOriginalFilename());
        String nombreSeguro = UUID.randomUUID() + extension;
        Path destino = uploadDir.resolve(nombreSeguro);
        Files.copy(file.getInputStream(), destino);
        return destino.toString();
    }

    public Path resolverRuta(String rutaAlmacenada) {
        return Paths.get(rutaAlmacenada).normalize();
    }

    private void validar(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }
        if (file.getSize() > maxBytes) {
            throw new IllegalArgumentException("El archivo supera el tamaño máximo permitido (" + (maxBytes / 1024 / 1024) + " MB)");
        }
        String mime = file.getContentType();
        if (mime == null || !MIME_PERMITIDOS.contains(mime.toLowerCase())) {
            throw new IllegalArgumentException("Tipo de archivo no permitido. Use PDF, JPG o PNG.");
        }
    }

    private String obtenerExtension(String nombre) {
        if (nombre == null || !nombre.contains(".")) {
            return "";
        }
        return nombre.substring(nombre.lastIndexOf('.')).toLowerCase();
    }
}
