package cl.aduana.libertadores.config;

import cl.aduana.libertadores.model.DeclaracionJurada;
import cl.aduana.libertadores.model.Pasajero;
import cl.aduana.libertadores.model.PermisoMenor;
import cl.aduana.libertadores.model.VehiculoSat;
import cl.aduana.libertadores.repository.DeclaracionJuradaRepository;
import cl.aduana.libertadores.repository.PasajeroRepository;
import cl.aduana.libertadores.repository.PermisoMenorRepository;
import cl.aduana.libertadores.repository.VehiculoSatRepository;
import cl.aduana.libertadores.util.AesEncryptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Carga datos operativos de demostración cuando la BD no tiene pasajeros
 * (para reportes PDF/Excel con cifras realistas en presentaciones EFT).
 */
@Component
@Order(2)
public class DemoDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataInitializer.class);

    private final JdbcTemplate jdbcTemplate;
    private final PasajeroRepository pasajeroRepository;
    private final PermisoMenorRepository permisoMenorRepository;
    private final VehiculoSatRepository vehiculoSatRepository;
    private final DeclaracionJuradaRepository declaracionRepository;
    private final AesEncryptionUtil aes;

    public DemoDataInitializer(JdbcTemplate jdbcTemplate,
                               PasajeroRepository pasajeroRepository,
                               PermisoMenorRepository permisoMenorRepository,
                               VehiculoSatRepository vehiculoSatRepository,
                               DeclaracionJuradaRepository declaracionRepository,
                               AesEncryptionUtil aes) {
        this.jdbcTemplate = jdbcTemplate;
        this.pasajeroRepository = pasajeroRepository;
        this.permisoMenorRepository = permisoMenorRepository;
        this.vehiculoSatRepository = vehiculoSatRepository;
        this.declaracionRepository = declaracionRepository;
        this.aes = aes;
    }

    @Override
    public void run(String... args) {
        try {
            Integer pasajeros = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM pasajeros", Integer.class);
            if (pasajeros != null && pasajeros > 0) {
                return;
            }
            Integer empleados = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM empleados", Integer.class);
            if (empleados == null || empleados == 0) {
                return;
            }

            long pdiId = jdbcTemplate.queryForObject(
                    "SELECT id FROM empleados WHERE rol = 'PDI' ORDER BY id LIMIT 1", Long.class);
            long sagId = jdbcTemplate.queryForObject(
                    "SELECT id FROM empleados WHERE rol = 'SAG' ORDER BY id LIMIT 1", Long.class);

            seedPasajerosYPermisos(pdiId);
            seedVehiculos();
            seedDeclaraciones(sagId);

            log.info("Datos demo cargados: pasajeros, vehículos SAT, declaraciones SAG (reportes EFT)");
        } catch (Exception e) {
            log.warn("No se pudieron cargar datos demo: {}", e.getMessage());
        }
    }

    private void seedPasajerosYPermisos(long pdiId) throws Exception {
        List<Long> adultos = new ArrayList<>();
        adultos.add(insertPasajero("12.345.678-9", "RUT", "María", "González Pérez",
                LocalDate.of(1985, 3, 14), "Chilena", false, null));
        adultos.add(insertPasajero("18.765.432-1", "RUT", "Carlos", "Muñoz Rojas",
                LocalDate.of(1978, 7, 22), "Chilena", false, null));
        adultos.add(insertPasajero("AB123456", "PASAPORTE", "Juan", "Pérez García",
                LocalDate.of(1990, 11, 5), "Argentina", false, null));
        adultos.add(insertPasajero("16.234.567-8", "RUT", "Ana", "Silva Torres",
                LocalDate.of(1995, 1, 30), "Chilena", false, null));
        adultos.add(insertPasajero("CD789012", "PASAPORTE", "Lucía", "Fernández",
                LocalDate.of(1988, 9, 18), "Argentina", false, null));
        adultos.add(insertPasajero("15.678.901-2", "RUT", "Roberto", "Vargas López",
                LocalDate.of(1972, 4, 8), "Chilena", false, null));
        adultos.add(insertPasajero("EF345678", "PASAPORTE", "Diego", "Martínez",
                LocalDate.of(1982, 12, 1), "Uruguay", false, null));
        adultos.add(insertPasajero("14.890.123-4", "RUT", "Patricia", "Herrera",
                LocalDate.of(1993, 6, 25), "Chilena", false, null));

        Object[][] menores = {
                {"21.111.222-3", "RUT", "Tomás", "González", LocalDate.of(2016, 2, 10), "Chilena", 0},
                {"21.222.333-4", "RUT", "Sofía", "Muñoz", LocalDate.of(2014, 8, 3), "Chilena", 1},
                {"GH901234", "PASAPORTE", "Mateo", "Pérez", LocalDate.of(2015, 5, 20), "Argentina", 2},
                {"21.333.444-5", "RUT", "Valentina", "Silva", LocalDate.of(2017, 11, 15), "Chilena", 3},
                {"IJ567890", "PASAPORTE", "Emilia", "Fernández", LocalDate.of(2013, 7, 7), "Argentina", 4},
                {"21.444.555-6", "RUT", "Benjamín", "Vargas", LocalDate.of(2018, 4, 28), "Chilena", 5},
                {"21.555.666-7", "RUT", "Isidora", "Herrera", LocalDate.of(2016, 9, 12), "Chilena", 7},
                {"KL123789", "PASAPORTE", "Lucas", "Martínez", LocalDate.of(2014, 1, 22), "Uruguay", 6},
        };

        int permisosValidados = 0;
        for (int i = 0; i < menores.length; i++) {
            Object[] m = menores[i];
            int tutorIdx = (int) m[6];
            Long tutorId = adultos.get(tutorIdx);
            Long menorId = insertPasajero(
                    (String) m[0], (String) m[1], (String) m[2], (String) m[3],
                    (LocalDate) m[4], (String) m[5], true, tutorId);

            boolean validar = i < 6;
            PermisoMenor permiso = new PermisoMenor();
            permiso.setPasajeroMenorId(menorId);
            permiso.setTipoPermiso(i % 2 == 0 ? "NOTARIAL" : "JUDICIAL");
            permiso.setNumeroDocumento("PM-2026-" + String.format("%04d", i + 1));
            permiso.setFechaEmision(LocalDate.of(2026, 1, 15).plusDays(i * 3L));
            permiso.setValidado(validar);
            if (validar) {
                permiso.setEmpleadoValidadorId(pdiId);
                permisosValidados++;
            }
            permisoMenorRepository.insert(permiso);
        }

        Object[][] extrasAdultos = {
                {"13.456.789-0", "RUT", "Felipe", "Castro", LocalDate.of(1980, 3, 3), "Chilena"},
                {"MN456789", "PASAPORTE", "Gabriela", "Romero", LocalDate.of(1991, 10, 10), "Argentina"},
                {"12.567.890-1", "RUT", "Sebastián", "Morales", LocalDate.of(1987, 8, 8), "Chilena"},
                {"OP789012", "PASAPORTE", "Camila", "Díaz", LocalDate.of(1994, 2, 14), "Peruana"},
                {"11.678.901-2", "RUT", "Andrés", "Reyes", LocalDate.of(1975, 12, 30), "Chilena"},
                {"QR345901", "PASAPORTE", "Florencia", "Acosta", LocalDate.of(1989, 6, 6), "Argentina"},
                {"10.789.012-3", "RUT", "Ignacio", "Poblete", LocalDate.of(1998, 4, 4), "Chilena"},
                {"ST678234", "PASAPORTE", "Martín", "López", LocalDate.of(1983, 7, 19), "Argentina"},
                {"19.890.123-4", "RUT", "Daniela", "Contreras", LocalDate.of(1996, 5, 5), "Chilena"},
                {"UV901345", "PASAPORTE", "Pablo", "Soto", LocalDate.of(1979, 11, 11), "Boliviana"},
                {"18.901.234-5", "RUT", "Francisca", "Araya", LocalDate.of(1992, 9, 9), "Chilena"},
                {"WX234567", "PASAPORTE", "Hernán", "Gutiérrez", LocalDate.of(1986, 1, 1), "Argentina"},
                {"17.012.345-6", "RUT", "Javiera", "Espinoza", LocalDate.of(2000, 3, 21), "Chilena"},
                {"YZ567123", "PASAPORTE", "Ricardo", "Navarro", LocalDate.of(1984, 8, 16), "Uruguay"},
                {"16.123.456-7", "RUT", "Constanza", "Fuentes", LocalDate.of(1997, 7, 7), "Chilena"},
                {"AA890456", "PASAPORTE", "Oscar", "Medina", LocalDate.of(1977, 2, 2), "Argentina"},
                {"15.234.567-8", "RUT", "Paulina", "Carrasco", LocalDate.of(1999, 10, 10), "Chilena"},
                {"BB123567", "PASAPORTE", "Sergio", "Vega", LocalDate.of(1981, 6, 6), "Paraguaya"},
                {"14.345.678-9", "RUT", "Macarena", "Olivares", LocalDate.of(1993, 12, 12), "Chilena"},
                {"CC456789", "PASAPORTE", "Eduardo", "Ríos", LocalDate.of(1988, 4, 4), "Argentina"},
                {"13.456.789-1", "RUT", "Katherine", "Sepúlveda", LocalDate.of(2001, 5, 15), "Chilena"},
                {"DD567890", "PASAPORTE", "Alejandro", "Bravo", LocalDate.of(1976, 9, 9), "Argentina"},
                {"12.567.890-2", "RUT", "Carolina", "Maldonado", LocalDate.of(1990, 1, 1), "Chilena"},
                {"EE678901", "PASAPORTE", "Fernando", "Aguirre", LocalDate.of(1985, 3, 3), "Uruguay"},
                {"11.678.901-3", "RUT", "Beatriz", "Campos", LocalDate.of(1982, 11, 11), "Chilena"},
                {"FF789012", "PASAPORTE", "Gonzalo", "Ibarra", LocalDate.of(1995, 8, 8), "Argentina"},
        };

        for (Object[] a : extrasAdultos) {
            Long id = insertPasajero(
                    (String) a[0], (String) a[1], (String) a[2], (String) a[3],
                    (LocalDate) a[4], (String) a[5], false, null);
            adultos.add(id);
        }
        log.info("Demo: {} pasajeros ({} menores), {} permisos validados",
                adultos.size() + menores.length, menores.length, permisosValidados);
    }

    private Long insertPasajero(String doc, String tipoDoc, String nombres, String apellidos,
                                LocalDate nacimiento, String nacionalidad,
                                boolean menor, Long tutorId) throws Exception {
        Pasajero p = new Pasajero();
        p.setDocumentoEncriptado(aes.encrypt(doc));
        p.setTipoDocumento(tipoDoc);
        p.setNombresEncriptado(aes.encrypt(nombres));
        p.setApellidosEncriptado(aes.encrypt(apellidos));
        p.setFechaNacimiento(nacimiento);
        p.setNacionalidad(nacionalidad);
        p.setMenorEdad(menor);
        p.setTutorId(tutorId);
        return pasajeroRepository.insert(p);
    }

    private void seedVehiculos() throws Exception {
        Object[][] vehiculos = {
                {"ABCD12", "CHILE", "AUTOMOVIL", false, false, "VIGENTE"},
                {"AF123CD", "ARGENTINA", "AUTOMOVIL", false, false, "VIGENTE"},
                {"HJKL34", "CHILE", "CAMIONETA", false, true, "ALERTA_ROBO"},
                {"FG456GH", "ARGENTINA", "AUTOMOVIL", false, false, "VIGENTE"},
                {"MNOP56", "CHILE", "MOTOCICLETA", false, false, "VIGENTE"},
                {"IJ789JK", "ARGENTINA", "CAMIONETA", false, true, "ALERTA_ROBO"},
                {"QRST78", "CHILE", "AUTOMOVIL", false, false, "VIGENTE"},
                {"LM012MN", "ARGENTINA", "AUTOMOVIL", false, false, "VIGENTE"},
                {"UVWX90", "CHILE", "BUS", false, false, "VIGENTE"},
                {"OP345PQ", "ARGENTINA", "CAMIONETA", false, false, "VIGENTE"},
                {"YZAB12", "CHILE", "AUTOMOVIL", false, false, "VIGENTE"},
                {"RS678ST", "ARGENTINA", "AUTOMOVIL", false, true, "ALERTA_ROBO"},
                {"CDEF34", "CHILE", "CAMIONETA", false, false, "VIGENTE"},
                {"UV901VW", "ARGENTINA", "AUTOMOVIL", false, false, "VIGENTE"},
                {"GHIJ56", "CHILE", "AUTOMOVIL", false, false, "VIGENTE"},
                {"WX234XY", "ARGENTINA", "MOTOCICLETA", false, false, "VIGENTE"},
                {"KLMN78", "CHILE", "AUTOMOVIL", false, false, "VIGENTE"},
                {"YZ567ZA", "ARGENTINA", "AUTOMOVIL", false, false, "VIGENTE"},
                {"OPQR90", "CHILE", "CAMIONETA", false, false, "VIGENTE"},
                {"BC890CD", "ARGENTINA", "AUTOMOVIL", false, false, "VIGENTE"},
                {"STUV12", "CHILE", "AUTOMOVIL", false, false, "VIGENTE"},
                {"EF123FG", "ARGENTINA", "BUS", false, false, "VIGENTE"},
                {"WXYZ34", "CHILE", "AUTOMOVIL", true, false, "DIPLOMATICO"},
                {"HI456IJ", "ARGENTINA", "AUTOMOVIL", false, false, "VIGENTE"},
        };

        LocalDate hoy = LocalDate.now();
        for (Object[] v : vehiculos) {
            VehiculoSat sat = new VehiculoSat();
            sat.setPatenteEncriptada(aes.encrypt((String) v[0]));
            sat.setPaisOrigen((String) v[1]);
            sat.setTipoVehiculo((String) v[2]);
            sat.setDiplomatico((boolean) v[3]);
            sat.setEncargoRobo((boolean) v[4]);
            sat.setEstadoSat((String) v[5]);
            sat.setFechaIngreso(hoy.minusDays(15));
            sat.setFechaMaxSalida(hoy.plusDays(165));
            vehiculoSatRepository.insert(sat);
        }
    }

    private void seedDeclaraciones(long sagId) throws Exception {
        List<Long> pasajeroIds = jdbcTemplate.query(
                "SELECT id FROM pasajeros WHERE menor_edad = false ORDER BY id",
                (rs, rowNum) -> rs.getLong(1));

        String[] detalles = {
                "Sin productos agropecuarios declarados",
                "Mascota registrada: perro labrador con certificado veterinario",
                "Fruta fresca declarada y decomisada en control SAG",
                "Semillas de uso personal autorizadas",
                "Sin novedad en declaración jurada",
                "Productos lácteos declarados conforme normativa",
                "Mascota: gato persa con microchip",
                "Equipaje revisado — sin productos prohibidos",
                "Miel artesanal declarada (1 kg)",
                "Plantas ornamentales sin certificado fitosanitario — retención",
        };

        int idx = 0;
        for (Long pasajeroId : pasajeroIds) {
            if (idx >= 32) {
                break;
            }
            DeclaracionJurada d = new DeclaracionJurada();
            d.setPasajeroId(pasajeroId);
            d.setRepresentanteId(null);
            d.setProductosAgropecuarios(idx % 4 == 0);
            d.setAnimalesMascotas(idx % 5 == 1);
            d.setDetalle(detalles[idx % detalles.length]);
            d.setFechaDeclaracion(LocalDateTime.now().minusDays(30 - (idx % 20)));
            d.setEmpleadoSagId(sagId);
            declaracionRepository.insert(d);
            idx++;
        }

        List<Long> menores = jdbcTemplate.query(
                "SELECT id FROM pasajeros WHERE menor_edad = true ORDER BY id LIMIT 4",
                (rs, rowNum) -> rs.getLong(1));
        List<Long> tutores = jdbcTemplate.query(
                "SELECT tutor_id FROM pasajeros WHERE menor_edad = true AND tutor_id IS NOT NULL ORDER BY id LIMIT 4",
                (rs, rowNum) -> rs.getLong(1));

        for (int i = 0; i < menores.size() && i < tutores.size(); i++) {
            DeclaracionJurada d = new DeclaracionJurada();
            d.setPasajeroId(menores.get(i));
            d.setRepresentanteId(tutores.get(i));
            d.setProductosAgropecuarios(false);
            d.setAnimalesMascotas(i == 0);
            d.setDetalle("Declaración jurada SAG — menor representado por tutor legal");
            d.setFechaDeclaracion(LocalDateTime.now().minusDays(5 + i));
            d.setEmpleadoSagId(sagId);
            declaracionRepository.insert(d);
        }
    }
}
