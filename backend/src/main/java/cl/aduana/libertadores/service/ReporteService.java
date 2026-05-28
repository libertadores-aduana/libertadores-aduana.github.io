package cl.aduana.libertadores.service;

import cl.aduana.libertadores.repository.EstadisticasRepository;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class ReporteService {

    private final EstadisticasRepository estadisticasRepository;

    public ReporteService(EstadisticasRepository estadisticasRepository) {
        this.estadisticasRepository = estadisticasRepository;
    }

    public byte[] generarPdf() throws SQLException {
        Map<String, Long> stats = estadisticasRepository.resumenGeneral();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (PdfWriter writer = new PdfWriter(baos);
             PdfDocument pdf = new PdfDocument(writer);
             Document doc = new Document(pdf)) {

            doc.add(new Paragraph("Sistema Los Libertadores - Reporte Estadístico"));
            doc.add(new Paragraph("Generado: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));

            Table table = new Table(2);
            stats.forEach((k, v) -> {
                table.addCell(k);
                table.addCell(String.valueOf(v));
            });
            doc.add(table);
        }
        return baos.toByteArray();
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

            for (Map.Entry<String, Long> entry : stats.entrySet()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(entry.getKey());
                row.createCell(1).setCellValue(entry.getValue());
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
