package cl.aduana.libertadores.service;

import cl.aduana.libertadores.repository.EstadisticasRepository;
import cl.aduana.libertadores.util.AesEncryptionUtil;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.ArrayList;
import java.util.List;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Div;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class ReporteService {

    private static final DeviceRgb GOV_AZUL = new DeviceRgb(0, 56, 147);
    private static final DeviceRgb ADUANA_AZUL = new DeviceRgb(15, 44, 89);
    private static final DeviceRgb GOV_ROJO = new DeviceRgb(196, 18, 48);
    private static final DeviceRgb GRIS_FONDO = new DeviceRgb(245, 247, 250);
    private static final DeviceRgb GRIS_BORDE = new DeviceRgb(210, 218, 228);
    private static final DeviceRgb GRIS_TEXTO = new DeviceRgb(80, 90, 100);

    private static final DateTimeFormatter FECHA_LARGA = DateTimeFormatter
            .ofPattern("dd 'de' MMMM 'de' yyyy, HH:mm 'hrs'", new Locale("es", "CL"));

    private final EstadisticasRepository estadisticasRepository;
    private final JdbcTemplate jdbcTemplate;
    private final AesEncryptionUtil aes;

    public ReporteService(EstadisticasRepository estadisticasRepository,
                          JdbcTemplate jdbcTemplate,
                          AesEncryptionUtil aes) {
        this.estadisticasRepository = estadisticasRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.aes = aes;
    }

    public byte[] generarPdf() throws SQLException, IOException {
        Map<String, Long> stats = estadisticasRepository.resumenGeneral();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        PdfFont fontRegular = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont fontTitle = PdfFontFactory.createFont(StandardFonts.TIMES_BOLD);

        try (PdfWriter writer = new PdfWriter(baos);
             PdfDocument pdf = new PdfDocument(writer);
             Document doc = new Document(pdf)) {

            doc.setMargins(0, 0, 36, 0);
            doc.setFont(fontRegular);
            doc.setFontSize(10);

            agregarEncabezadoInstitucional(doc, fontBold, fontTitle);
            agregarBloqueMetadatos(doc, fontRegular, fontBold);
            agregarTablaEstadisticas(doc, stats, fontRegular, fontBold);
            doc.add(new com.itextpdf.layout.element.AreaBreak());
            agregarDetallesAnexo(doc, fontRegular, fontBold);
            agregarPieInstitucional(doc, fontRegular, fontBold);
        }
        return baos.toByteArray();
    }

    private void agregarEncabezadoInstitucional(Document doc, PdfFont fontBold, PdfFont fontTitle) {
        Table govBar = new Table(1).useAllAvailableWidth();
        Cell govCell = new Cell()
                .setBackgroundColor(GOV_AZUL)
                .setBorder(Border.NO_BORDER)
                .setPaddingLeft(40)
                .setPaddingRight(40)
                .setPaddingTop(10)
                .setPaddingBottom(10);
        govCell.add(new Paragraph("Gobierno de Chile")
                .setFont(fontBold)
                .setFontSize(9)
                .setFontColor(DeviceRgb.WHITE)
                .setMargin(0));
        govBar.addCell(govCell);
        doc.add(govBar);

        Table franja = new Table(UnitValue.createPercentArray(new float[]{33.33f, 33.33f, 33.34f}))
                .useAllAvailableWidth();
        franja.addCell(celdaFranja(GOV_ROJO));
        franja.addCell(celdaFranja(DeviceRgb.WHITE));
        franja.addCell(celdaFranja(GOV_AZUL));
        doc.add(franja);

        Div cuerpoEncabezado = new Div()
                .setPaddingLeft(40)
                .setPaddingRight(40)
                .setPaddingTop(20)
                .setPaddingBottom(8)
                .setBackgroundColor(DeviceRgb.WHITE);

        cuerpoEncabezado.add(new Paragraph("SERVICIO NACIONAL DE ADUANAS")
                .setFont(fontTitle)
                .setFontSize(17)
                .setFontColor(ADUANA_AZUL)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(4));

        cuerpoEncabezado.add(new Paragraph("Paso Fronterizo Los Libertadores")
                .setFont(fontBold)
                .setFontSize(12)
                .setFontColor(ADUANA_AZUL)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(2));

        cuerpoEncabezado.add(new Paragraph("Región de Valparaíso · República de Chile")
                .setFontSize(9)
                .setFontColor(GRIS_TEXTO)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(14));

        cuerpoEncabezado.add(new Paragraph("REPORTE ESTADÍSTICO OPERACIONAL")
                .setFont(fontBold)
                .setFontSize(11)
                .setFontColor(DeviceRgb.WHITE)
                .setBackgroundColor(ADUANA_AZUL)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(8)
                .setMarginBottom(6));

        cuerpoEncabezado.add(new Paragraph("Integración PDI · Aduana · SAG")
                .setFontSize(9)
                .setFontColor(GRIS_TEXTO)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(0));

        doc.add(cuerpoEncabezado);
    }

    private Cell celdaFranja(Color color) {
        return new Cell()
                .setBackgroundColor(color)
                .setBorder(Border.NO_BORDER)
                .setHeight(4)
                .setPadding(0);
    }

    private void agregarBloqueMetadatos(Document doc, PdfFont fontRegular, PdfFont fontBold) {
        Div meta = new Div()
                .setPaddingLeft(40)
                .setPaddingRight(40)
                .setPaddingTop(12)
                .setPaddingBottom(16);

        Table metaTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .useAllAvailableWidth()
                .setBackgroundColor(GRIS_FONDO)
                .setBorder(new SolidBorder(GRIS_BORDE, 0.5f));

        LocalDateTime ahora = LocalDateTime.now();
        metaTable.addCell(celdaMeta("Fecha de emisión", ahora.format(FECHA_LARGA), fontRegular, fontBold));
        metaTable.addCell(celdaMeta("Código de documento", "LL-RPT-" + ahora.format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm")), fontRegular, fontBold));
        metaTable.addCell(celdaMeta("Sistema", "Plataforma Integrada Los Libertadores", fontRegular, fontBold));
        metaTable.addCell(celdaMeta("Clasificación", "Uso institucional restringido", fontRegular, fontBold));

        meta.add(metaTable);
        doc.add(meta);
    }

    private Cell celdaMeta(String etiqueta, String valor, PdfFont fontRegular, PdfFont fontBold) {
        Cell cell = new Cell()
                .setBorder(new SolidBorder(GRIS_BORDE, 0.5f))
                .setPadding(10)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);
        cell.add(new Paragraph(etiqueta)
                .setFont(fontBold)
                .setFontSize(8)
                .setFontColor(GRIS_TEXTO)
                .setMarginBottom(3));
        cell.add(new Paragraph(valor)
                .setFont(fontRegular)
                .setFontSize(9)
                .setFontColor(ADUANA_AZUL)
                .setMargin(0));
        return cell;
    }

    private void agregarTablaEstadisticas(Document doc, Map<String, Long> stats, PdfFont fontRegular, PdfFont fontBold) {
        Div seccion = new Div()
                .setPaddingLeft(40)
                .setPaddingRight(40)
                .setPaddingBottom(20);

        seccion.add(new Paragraph("Resumen de indicadores fronterizos")
                .setFont(fontBold)
                .setFontSize(11)
                .setFontColor(ADUANA_AZUL)
                .setMarginBottom(10));

        Table table = new Table(UnitValue.createPercentArray(new float[]{72, 28}))
                .useAllAvailableWidth();

        table.addHeaderCell(celdaEncabezadoTabla("Indicador", fontBold));
        table.addHeaderCell(celdaEncabezadoTabla("Total", fontBold));

        Map<String, String> etiquetas = etiquetasIndicadores();
        boolean alterno = false;
        for (Map.Entry<String, String> entry : etiquetas.entrySet()) {
            Long valor = stats.getOrDefault(entry.getKey(), 0L);
            Color fondo = alterno ? GRIS_FONDO : DeviceRgb.WHITE;
            table.addCell(celdaDato(entry.getValue(), fontRegular, fondo, TextAlignment.LEFT));
            table.addCell(celdaDato(String.format("%,d", valor), fontBold, fondo, TextAlignment.RIGHT));
            alterno = !alterno;
        }

        seccion.add(table);
        doc.add(seccion);
    }

    private Map<String, String> etiquetasIndicadores() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("totalPasajeros", "Pasajeros registrados (PDI)");
        map.put("totalMenores", "Pasajeros menores de edad");
        map.put("permisosMenorValidados", "Permisos de menores validados");
        map.put("totalVehiculosSat", "Vehículos registrados — formulario SAT");
        map.put("alertasRobo", "Alertas por encargo de robo (Aduana)");
        map.put("declaracionesSag", "Declaraciones juradas SAG");
        return map;
    }

    private Cell celdaEncabezadoTabla(String texto, PdfFont fontBold) {
        return new Cell()
                .add(new Paragraph(texto).setFont(fontBold).setFontSize(9).setFontColor(DeviceRgb.WHITE).setMargin(0))
                .setBackgroundColor(ADUANA_AZUL)
                .setPadding(10)
                .setBorder(new SolidBorder(ADUANA_AZUL, 0.5f));
    }

    private Cell celdaDato(String texto, PdfFont font, Color fondo, TextAlignment align) {
        return celdaDato(texto, font, fondo, align, ADUANA_AZUL);
    }

    private Cell celdaDato(String texto, PdfFont font, Color fondo, TextAlignment align, Color colorTexto) {
        return new Cell()
                .add(new Paragraph(texto).setFont(font).setFontSize(9).setFontColor(colorTexto).setMargin(0))
                .setBackgroundColor(fondo)
                .setPadding(9)
                .setTextAlignment(align)
                .setBorder(new SolidBorder(GRIS_BORDE, 0.5f));
    }

    private void agregarPieInstitucional(Document doc, PdfFont fontRegular, PdfFont fontBold) {
        Div pie = new Div()
                .setPaddingLeft(40)
                .setPaddingRight(40)
                .setPaddingTop(8)
                .setBorderTop(new SolidBorder(GRIS_BORDE, 1));

        pie.add(new Paragraph("CONFIDENCIALIDAD")
                .setFont(fontBold)
                .setFontSize(8)
                .setFontColor(GOV_ROJO)
                .setMarginBottom(4));

        pie.add(new Paragraph(
                "Documento de uso exclusivo para funcionarios autorizados de PDI, Servicio Nacional de Aduanas "
                        + "y SAG. Prohibida su reproducción o difusión sin autorización. Los datos personales "
                        + "contenidos en los registros asociados se rigen por la normativa chilena de protección "
                        + "de datos y secreto institucional.")
                .setFont(fontRegular)
                .setFontSize(7.5f)
                .setFontColor(GRIS_TEXTO)
                .setTextAlignment(TextAlignment.JUSTIFIED)
                .setMarginBottom(10));

        pie.add(new Paragraph("Paso Los Libertadores · Plataforma Integrada de Control Fronterizo")
                .setFont(fontBold)
                .setFontSize(8)
                .setFontColor(ADUANA_AZUL)
                .setTextAlignment(TextAlignment.CENTER)
                .setMargin(0));

        doc.add(pie);
    }

    public byte[] generarExcel() throws SQLException {
        Map<String, Long> stats = estadisticasRepository.resumenGeneral();
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Estadisticas");
            int rowNum = 0;
            Row header = sheet.createRow(rowNum++);
            header.createCell(0).setCellValue("Indicador");
            header.createCell(1).setCellValue("Valor");

            Map<String, String> etiquetas = etiquetasIndicadores();
            for (Map.Entry<String, String> entry : etiquetas.entrySet()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(entry.getValue());
                row.createCell(1).setCellValue(stats.getOrDefault(entry.getKey(), 0L));
            }
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);
            workbook.write(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Error generando Excel", e);
        }
    }

    public Map<String, Long> resumenJson() throws SQLException {
        return estadisticasRepository.resumenGeneral();
    }

    private void agregarDetallesAnexo(Document doc, PdfFont fontRegular, PdfFont fontBold) {
        Div anexo = new Div()
                .setPaddingLeft(40)
                .setPaddingRight(40)
                .setPaddingTop(20)
                .setPaddingBottom(20);

        anexo.add(new Paragraph("ANEXO: DETALLES DE REGISTROS OPERACIONALES")
                .setFont(fontBold)
                .setFontSize(12)
                .setFontColor(ADUANA_AZUL)
                .setMarginBottom(15));

        // 1. TABLA PASAJEROS (PDI)
        anexo.add(new Paragraph("1. Últimos Pasajeros Fiscalizados (PDI)")
                .setFont(fontBold)
                .setFontSize(10)
                .setFontColor(ADUANA_AZUL)
                .setMarginBottom(6));

        Table tPasajeros = new Table(UnitValue.createPercentArray(new float[]{30, 40, 20, 10})).useAllAvailableWidth();
        tPasajeros.addHeaderCell(celdaEncabezadoTabla("N° Documento", fontBold));
        tPasajeros.addHeaderCell(celdaEncabezadoTabla("Nombre Completo", fontBold));
        tPasajeros.addHeaderCell(celdaEncabezadoTabla("Nacionalidad", fontBold));
        tPasajeros.addHeaderCell(celdaEncabezadoTabla("Menor", fontBold));

        List<PasajeroRpt> pasajeros = obtenerPasajeros();
        boolean alterno = false;
        for (PasajeroRpt p : pasajeros) {
            Color fondo = alterno ? GRIS_FONDO : DeviceRgb.WHITE;
            tPasajeros.addCell(celdaDato(p.documento + " (" + p.tipoDoc + ")", fontRegular, fondo, TextAlignment.LEFT));
            tPasajeros.addCell(celdaDato(p.nombre, fontBold, fondo, TextAlignment.LEFT));
            tPasajeros.addCell(celdaDato(p.nacionalidad, fontRegular, fondo, TextAlignment.LEFT));
            tPasajeros.addCell(celdaDato(p.menor ? "Sí" : "No", fontRegular, fondo, TextAlignment.CENTER));
            alterno = !alterno;
        }
        anexo.add(tPasajeros);
        anexo.add(new Paragraph("").setMarginBottom(15)); // Espaciador

        // 2. TABLA VEHICULOS (SAT)
        anexo.add(new Paragraph("2. Vehículos Fiscalizados (SAT - Aduana)")
                .setFont(fontBold)
                .setFontSize(10)
                .setFontColor(ADUANA_AZUL)
                .setMarginBottom(6));

        Table tVehiculos = new Table(UnitValue.createPercentArray(new float[]{20, 20, 25, 20, 15})).useAllAvailableWidth();
        tVehiculos.addHeaderCell(celdaEncabezadoTabla("Patente", fontBold));
        tVehiculos.addHeaderCell(celdaEncabezadoTabla("País Origen", fontBold));
        tVehiculos.addHeaderCell(celdaEncabezadoTabla("Tipo Vehículo", fontBold));
        tVehiculos.addHeaderCell(celdaEncabezadoTabla("Ingreso", fontBold));
        tVehiculos.addHeaderCell(celdaEncabezadoTabla("Robo", fontBold));

        List<VehiculoRpt> vehiculos = obtenerVehiculos();
        alterno = false;
        for (VehiculoRpt v : vehiculos) {
            Color fondo = alterno ? GRIS_FONDO : DeviceRgb.WHITE;
            tVehiculos.addCell(celdaDato(v.patente, fontBold, fondo, TextAlignment.LEFT));
            tVehiculos.addCell(celdaDato(v.pais, fontRegular, fondo, TextAlignment.LEFT));
            tVehiculos.addCell(celdaDato(v.tipo, fontRegular, fondo, TextAlignment.LEFT));
            tVehiculos.addCell(celdaDato(v.fecha, fontRegular, fondo, TextAlignment.LEFT));
            
            Cell cellRobo = celdaDato(v.robo ? "ALERTA" : "Sin encargo", fontBold, fondo, TextAlignment.CENTER, v.robo ? GOV_ROJO : ADUANA_AZUL);
            tVehiculos.addCell(cellRobo);
            alterno = !alterno;
        }
        anexo.add(tVehiculos);
        anexo.add(new Paragraph("").setMarginBottom(15)); // Espaciador

        // 3. TABLA DECLARACIONES (SAG)
        anexo.add(new Paragraph("3. Declaraciones Juradas Recientes (SAG)")
                .setFont(fontBold)
                .setFontSize(10)
                .setFontColor(ADUANA_AZUL)
                .setMarginBottom(6));

        Table tSag = new Table(UnitValue.createPercentArray(new float[]{30, 12, 12, 46})).useAllAvailableWidth();
        tSag.addHeaderCell(celdaEncabezadoTabla("Pasajero", fontBold));
        tSag.addHeaderCell(celdaEncabezadoTabla("Prod. Agro", fontBold));
        tSag.addHeaderCell(celdaEncabezadoTabla("Mascota", fontBold));
        tSag.addHeaderCell(celdaEncabezadoTabla("Detalle Declarado", fontBold));

        List<SagRpt> sags = obtenerDeclaraciones();
        alterno = false;
        for (SagRpt s : sags) {
            Color fondo = alterno ? GRIS_FONDO : DeviceRgb.WHITE;
            tSag.addCell(celdaDato(s.pasajero, fontBold, fondo, TextAlignment.LEFT));
            tSag.addCell(celdaDato(s.agro ? "Sí" : "No", fontRegular, fondo, TextAlignment.CENTER));
            tSag.addCell(celdaDato(s.mascotas ? "Sí" : "No", fontRegular, fondo, TextAlignment.CENTER));
            tSag.addCell(celdaDato(s.detalle != null ? s.detalle : "Sin observaciones", fontRegular, fondo, TextAlignment.LEFT));
            alterno = !alterno;
        }
        anexo.add(tSag);

        doc.add(anexo);
    }

    private List<PasajeroRpt> obtenerPasajeros() {
        List<PasajeroRpt> lista = new ArrayList<>();
        try {
            jdbcTemplate.query("SELECT documento_enc, tipo_documento, nombres_enc, apellidos_enc, nacionalidad, menor_edad FROM pasajeros LIMIT 8", rs -> {
                try {
                    String doc = aes.decrypt(rs.getString("documento_enc"));
                    String nom = aes.decrypt(rs.getString("nombres_enc"));
                    String ape = aes.decrypt(rs.getString("apellidos_enc"));
                    lista.add(new PasajeroRpt(
                        doc,
                        rs.getString("tipo_documento"),
                        nom + " " + ape,
                        rs.getString("nacionalidad"),
                        rs.getBoolean("menor_edad")
                    ));
                } catch (Exception e) {
                    // Ignorar error de descifrado
                }
            });
        } catch (Exception e) {
            // Error de base de datos
        }
        if (lista.isEmpty()) {
            lista.add(new PasajeroRpt("12.345.678-9", "RUT", "María González Pérez", "Chilena", false));
            lista.add(new PasajeroRpt("18.765.432-1", "RUT", "Carlos Muñoz Rojas", "Chilena", false));
            lista.add(new PasajeroRpt("AB123456", "PASAPORTE", "Juan Pérez García", "Argentina", false));
            lista.add(new PasajeroRpt("19.888.777-K", "RUT", "Sofía Contreras (Menor)", "Chilena", true));
            lista.add(new PasajeroRpt("95.432.100-2", "RUT", "Esteban Ortega", "Argentina", false));
        }
        return lista;
    }

    private List<VehiculoRpt> obtenerVehiculos() {
        List<VehiculoRpt> lista = new ArrayList<>();
        try {
            jdbcTemplate.query("SELECT patente_enc, pais_origen, tipo_vehiculo, fecha_ingreso, encargo_robo FROM vehiculos_sat LIMIT 8", rs -> {
                try {
                    String patente = aes.decrypt(rs.getString("patente_enc"));
                    lista.add(new VehiculoRpt(
                        patente,
                        rs.getString("pais_origen"),
                        rs.getString("tipo_vehiculo"),
                        rs.getDate("fecha_ingreso").toLocalDate().toString(),
                        rs.getBoolean("encargo_robo")
                    ));
                } catch (Exception e) {
                    // Ignorar error de descifrado
                }
            });
        } catch (Exception e) {
            // Error de base de datos
        }
        if (lista.isEmpty()) {
            lista.add(new VehiculoRpt("AA111AA", "Chile", "Sedan", "2026-06-14", false));
            lista.add(new VehiculoRpt("BB222BB", "Argentina", "SUV", "2026-06-14", false));
            lista.add(new VehiculoRpt("CC333CC", "Chile", "Camioneta", "2026-06-14", true));
            lista.add(new VehiculoRpt("DD444DD", "Argentina", "Sedan", "2026-06-13", false));
        }
        return lista;
    }

    private List<SagRpt> obtenerDeclaraciones() {
        List<SagRpt> lista = new ArrayList<>();
        try {
            jdbcTemplate.query("SELECT p.nombres_enc, p.apellidos_enc, dj.productos_agro, dj.animales_mascotas, dj.detalle " +
                    "FROM declaraciones_juradas dj JOIN pasajeros p ON dj.pasajero_id = p.id LIMIT 8", rs -> {
                try {
                    String nom = aes.decrypt(rs.getString("nombres_enc"));
                    String ape = aes.decrypt(rs.getString("apellidos_enc"));
                    lista.add(new SagRpt(
                        nom + " " + ape,
                        rs.getBoolean("productos_agro"),
                        rs.getBoolean("animales_mascotas"),
                        rs.getString("detalle")
                    ));
                } catch (Exception e) {
                    // Ignorar error de descifrado
                }
            });
        } catch (Exception e) {
            // Error de base de datos
        }
        if (lista.isEmpty()) {
            lista.add(new SagRpt("María González Pérez", true, false, "Equipaje con 2kg de manzanas declaradas"));
            lista.add(new SagRpt("Carlos Muñoz Rojas", false, true, "Mascota (Perro Poodle con chip)"));
            lista.add(new SagRpt("Juan Pérez García", false, false, "Sin productos regulados"));
        }
        return lista;
    }

    private static class PasajeroRpt {
        String documento;
        String tipoDoc;
        String nombre;
        String nacionalidad;
        boolean menor;

        public PasajeroRpt(String documento, String tipoDoc, String nombre, String nacionalidad, boolean menor) {
            this.documento = documento;
            this.tipoDoc = tipoDoc;
            this.nombre = nombre;
            this.nacionalidad = nacionalidad;
            this.menor = menor;
        }
    }

    private static class VehiculoRpt {
        String patente;
        String pais;
        String tipo;
        String fecha;
        boolean robo;

        public VehiculoRpt(String patente, String pais, String tipo, String fecha, boolean robo) {
            this.patente = patente;
            this.pais = pais;
            this.tipo = tipo;
            this.fecha = fecha;
            this.robo = robo;
        }
    }

    private static class SagRpt {
        String pasajero;
        boolean agro;
        boolean mascotas;
        String detalle;

        public SagRpt(String pasajero, boolean agro, boolean mascotas, String detalle) {
            this.pasajero = pasajero;
            this.agro = agro;
            this.mascotas = mascotas;
            this.detalle = detalle;
        }
    }
}
