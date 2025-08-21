package com.aquaguardian.controller;

import com.aquaguardian.entity.SectionMonitor;
import com.aquaguardian.entity.ImportResult;
import com.aquaguardian.service.SectionMonitorService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.core.io.FileSystemResource;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileOutputStream;
import javax.servlet.http.HttpServletResponse;
import com.aquaguardian.util.ExcelTemplateGenerator;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/sectionMonitor")
public class SectionMonitorController {
    @Autowired
    private SectionMonitorService sectionMonitorService;

    @Autowired
    private ExcelTemplateGenerator excelTemplateGenerator;

    @GetMapping("/list")
    public PageInfo<SectionMonitor> list(@RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "10") int pageSize,
                                         @RequestParam(required = false) String monitorPointName,
                                         @RequestParam(required = false) String reservoirName,
                                         @RequestParam(required = false) Integer yearMin,
                                         @RequestParam(required = false) Integer yearMax,
                                         @RequestParam(required = false) Integer monthMin,
                                         @RequestParam(required = false) Integer monthMax,
                                         @RequestParam(required = false) Double oxygenMin,
                                         @RequestParam(required = false) Double oxygenMax,
                                         @RequestParam(required = false) Double potassiumPermanganateMin,
                                         @RequestParam(required = false) Double potassiumPermanganateMax,
                                         @RequestParam(required = false) Double codMin,
                                         @RequestParam(required = false) Double codMax,
                                         @RequestParam(required = false) Double flowMin,
                                         @RequestParam(required = false) Double flowMax,
                                         @RequestParam(required = false) Double waterDepthMin,
                                         @RequestParam(required = false) Double waterDepthMax,
                                         @RequestParam(required = false) Double totalNitrogenMin,
                                         @RequestParam(required = false) Double totalNitrogenMax,
                                         @RequestParam(required = false) Double totalPhosphorusMin,
                                         @RequestParam(required = false) Double totalPhosphorusMax) {
        return sectionMonitorService.getSectionMonitors(page, pageSize, monitorPointName, reservoirName, 
                yearMin, yearMax, monthMin, monthMax, oxygenMin, oxygenMax, potassiumPermanganateMin, potassiumPermanganateMax,
                codMin, codMax, flowMin, flowMax, waterDepthMin, waterDepthMax, 
                totalNitrogenMin, totalNitrogenMax, totalPhosphorusMin, totalPhosphorusMax);
    }

    @PostMapping("/create")
    public SectionMonitor create(@RequestBody SectionMonitor sectionMonitor) {
        return sectionMonitorService.createSectionMonitor(sectionMonitor);
    }

    @PostMapping("/update")
    public SectionMonitor update(@RequestBody SectionMonitor sectionMonitor) {
        return sectionMonitorService.updateSectionMonitor(sectionMonitor);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteSectionMonitor(@PathVariable Long id) {
        sectionMonitorService.deleteById(id);
        return ResponseEntity.ok().body("删除成功");
    }

    @GetMapping("/detail/{id}")
    public SectionMonitor detail(@PathVariable Long id) {
        return sectionMonitorService.getSectionMonitorById(id);
    }

    @PostMapping("/import")
    public List<SectionMonitor> importExcel(@RequestParam("file") MultipartFile file) throws IOException {
        return sectionMonitorService.importFromExcel(file);
    }

    @PostMapping("/batchImport")
    public ImportResult<SectionMonitor> batchImportExcel(@RequestParam("file") MultipartFile file) throws IOException {
        return sectionMonitorService.batchImportFromExcel(file);
    }

    @GetMapping("/checkMonitorPointName")
    public boolean checkMonitorPointName(@RequestParam String monitorPointName, @RequestParam String reservoirName) {
        return sectionMonitorService.isMonitorPointNameExists(monitorPointName, reservoirName);
    }

    @PostMapping("/export")
    public ResponseEntity<?> exportSectionMonitor(@RequestParam(defaultValue = "xlsx") String format) throws IOException {
        String fileName = "section_monitor_export." + ("csv".equalsIgnoreCase(format) ? "csv" : "xlsx");
        String filePath = System.getProperty("java.io.tmpdir") + File.separator + fileName;
        sectionMonitorService.exportToFile(filePath, format);
        FileSystemResource resource = new FileSystemResource(filePath);
        String encodedFileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName);
        if ("csv".equalsIgnoreCase(format)) {
            headers.setContentType(MediaType.TEXT_PLAIN);
        } else {
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        }
        return ResponseEntity.ok().headers(headers).body(resource);
    }

    @GetMapping("/template")
    public ResponseEntity<FileSystemResource> downloadTemplate() throws IOException {
        String fileName = "section_monitor_template.xlsx";
        Path tempPath = java.nio.file.Paths.get(System.getProperty("java.io.tmpdir"), fileName);
        generateSectionMonitorTemplate(tempPath.toString());
        org.springframework.core.io.FileSystemResource resource = new org.springframework.core.io.FileSystemResource(tempPath);
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .contentType(org.springframework.http.MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resource);
    }

    private void generateSectionMonitorTemplate(String outputPath) throws IOException {
        try (org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("监测断面数据");
            String[] headers = {"监测点名称", "水库名称", "年份", "月份", "氨氮(mg/l)", "高锰酸盐(mg/l)", "化学需氧量(mg/l)", "流量(m³/s)", "水深(m)", "总氮(mg/l)", "总磷(mg/l)"};
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                sheet.setColumnWidth(i, 20 * 256);
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }
            org.apache.poi.ss.usermodel.Row exampleRow = sheet.createRow(1);
            String[] exampleData = {"示例名称", "崇青", "2019", "3", "0.887", "5.5", "18.2", "1.37", "2", "3.15", "0.193"};
            for (int i = 0; i < exampleData.length; i++) {
                exampleRow.createCell(i).setCellValue(exampleData[i]);
            }
            try (java.io.FileOutputStream fileOut = new java.io.FileOutputStream(outputPath)) {
                workbook.write(fileOut);
            }
        }
    }
} 
