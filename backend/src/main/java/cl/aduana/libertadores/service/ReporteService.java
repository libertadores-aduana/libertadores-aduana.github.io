package cl.aduana.libertadores.service;

import cl.aduana.libertadores.repository.EstadisticasRepository;
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

    public ReporteService(EstadisticasRepository estadisticasRepository) {
        this.estadisticasRepository = estadisticasRepository;
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
        return new Cell()
                .add(new Paragraph(texto).setFont(font).setFontSize(9).setFontColor(ADUANA_AZUL).setMargin(0))
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
}
