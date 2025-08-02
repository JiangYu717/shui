package com.aquaguardian.controller;

import com.aquaguardian.entity.WaterSituation;
import com.aquaguardian.entity.ImportResult;
import com.aquaguardian.service.WaterSituationService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.core.io.FileSystemResource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileOutputStream;

@RestController
@RequestMapping("/api/waterSituation")
public class WaterSituationController {
    @Autowired
    private WaterSituationService waterSituationService;

    @GetMapping("/list")
    public PageInfo<WaterSituation> list(@RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "10") int pageSize,
                                         @RequestParam(required = false) String reservoirName,
                                         @RequestParam(required = false) String date,
                                         @RequestParam(required = false) Double waterLevelMin,
                                         @RequestParam(required = false) Double waterLevelMax,
                                         @RequestParam(required = false) Double storageMin,
                                         @RequestParam(required = false) Double storageMax,
                                         @RequestParam(required = false) Double avgInflowMin,
                                         @RequestParam(required = false) Double avgInflowMax,
                                         @RequestParam(required = false) Double avgOutflowMin,
                                         @RequestParam(required = false) Double avgOutflowMax,
                                         @RequestParam(required = false) Double yoyIncreaseMin,
                                         @RequestParam(required = false) Double yoyIncreaseMax,
                                         @RequestParam(required = false) Double totalCapacityMin,
                                         @RequestParam(required = false) Double totalCapacityMax,
                                         @RequestParam(required = false) Double floodLevelMin,
                                         @RequestParam(required = false) Double floodLevelMax) {
        return waterSituationService.getWaterSituations(page, pageSize, reservoirName, date, 
                waterLevelMin, waterLevelMax, storageMin, storageMax, avgInflowMin, avgInflowMax,
                avgOutflowMin, avgOutflowMax, yoyIncreaseMin, yoyIncreaseMax, 
                totalCapacityMin, totalCapacityMax, floodLevelMin, floodLevelMax);
    }

    @PostMapping("/create")
    public WaterSituation create(@RequestBody WaterSituation waterSituation) {
        return waterSituationService.createWaterSituation(waterSituation);
    }

    @PostMapping("/update")
    public WaterSituation update(@RequestBody WaterSituation waterSituation) {
        return waterSituationService.updateWaterSituation(waterSituation);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        waterSituationService.deleteWaterSituation(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/detail/{id}")
    public WaterSituation detail(@PathVariable Integer id) {
        return waterSituationService.getWaterSituationById(id);
    }

    @PostMapping("/import")
    public List<WaterSituation> importExcel(@RequestParam("file") MultipartFile file) throws IOException {
        return waterSituationService.importFromExcel(file);
    }

    @PostMapping("/batchImport")
    public ImportResult<WaterSituation> batchImportExcel(@RequestParam("file") MultipartFile file) throws IOException {
        return waterSituationService.batchImportFromExcel(file);
    }

    @GetMapping("/checkReservoirName")
    public boolean checkReservoirName(@RequestParam String reservoirName) {
        return waterSituationService.isReservoirNameExists(reservoirName);
    }

    @GetMapping("/export")
    public ResponseEntity<FileSystemResource> export(@RequestParam(defaultValue = "xlsx") String format) throws IOException {
        // 临时文件路径
        String fileName = "water_situation_export." + ("csv".equalsIgnoreCase(format) ? "csv" : "xlsx");
        Path tempPath = Paths.get(System.getProperty("java.io.tmpdir"), fileName);
        waterSituationService.exportToFile(tempPath.toString(), null, null, null, null, format);
        FileSystemResource resource = new FileSystemResource(tempPath);
        String contentType = "csv".equalsIgnoreCase(format) ? "text/csv" : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

    @GetMapping("/template/download")
    public ResponseEntity<FileSystemResource> downloadTemplate() throws IOException {
        // 生成临时模板文件
        String fileName = "水情数据导入模板.xlsx";
        Path tempPath = Paths.get(System.getProperty("java.io.tmpdir"), fileName);
        generateWaterSituationTemplate(tempPath.toString());
        FileSystemResource resource = new FileSystemResource(tempPath);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resource);
    }

    private void generateWaterSituationTemplate(String outputPath) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("水情数据");
            String[] headers = {"库名*", "日期*（yyyy-MM-dd HH:mm:ss 或 yyyy年MM月dd日8时）", "库水位(米)", "蓄水量(万立方米)", "日平均入库流量(立方米/秒)", "日平均出库流量(立方米/秒)", "比去年同期增减(万立方米)", "总库容(万立方米)", "汛限水位(米)"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                sheet.setColumnWidth(i, 20 * 256);
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }
            Row exampleRow = sheet.createRow(1);
            String[] exampleData = {"示例水库", "2025-06-01 08:00:00", "591.95", "4210.00", "4.690", "9.570", "132.0", "9060.0", "592.6"};
            for (int i = 0; i < exampleData.length; i++) {
                exampleRow.createCell(i).setCellValue(exampleData[i]);
            }
            try (FileOutputStream fileOut = new FileOutputStream(outputPath)) {
                workbook.write(fileOut);
            }
        }
    }
} 