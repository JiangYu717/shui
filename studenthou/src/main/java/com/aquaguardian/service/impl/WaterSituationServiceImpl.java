package com.aquaguardian.service.impl;

import com.aquaguardian.entity.ImportResult;
import com.aquaguardian.entity.WaterSituation;
import com.aquaguardian.mapper.WaterSituationMapper;
import com.aquaguardian.service.WaterSituationService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WaterSituationServiceImpl implements WaterSituationService {
    @Autowired
    private WaterSituationMapper waterSituationMapper;

    @Override
    public PageInfo<WaterSituation> getWaterSituations(int page, int pageSize, String reservoirName, String date, 
                                                       Double storageMin, Double storageMax, Double totalCapacityMin, Double totalCapacityMax) {
        PageHelper.startPage(page, pageSize);
        List<WaterSituation> list = waterSituationMapper.findByConditions(reservoirName, date, storageMin, storageMax, totalCapacityMin, totalCapacityMax);
        return new PageInfo<>(list);
    }

    @Override
    public WaterSituation createWaterSituation(WaterSituation waterSituation) {
        // 检查库名是否重复
        if (isReservoirNameExists(waterSituation.getReservoirName())) {
            throw new RuntimeException("库名 '" + waterSituation.getReservoirName() + "' 已存在，请使用其他库名！");
        }
        waterSituationMapper.insert(waterSituation);
        return waterSituation;
    }

    @Override
    public WaterSituation updateWaterSituation(WaterSituation waterSituation) {
        // 更新时检查库名是否重复（排除自己）
        WaterSituation existing = waterSituationMapper.findById(waterSituation.getId());
        if (existing != null && !existing.getReservoirName().equals(waterSituation.getReservoirName())) {
            // 检查新库名是否与其他记录重复
            if (waterSituationMapper.countByReservoirName(waterSituation.getReservoirName()) > 0) {
                throw new RuntimeException("库名 '" + waterSituation.getReservoirName() + "' 已存在，请使用其他库名！");
            }
        }
        waterSituationMapper.update(waterSituation);
        return waterSituation;
    }

    @Override
    public void deleteWaterSituation(Integer id) {
        waterSituationMapper.deleteById(id);
    }

    @Override
    public WaterSituation getWaterSituationById(Integer id) {
        return waterSituationMapper.findById(id);
    }

    @Override
    public boolean isReservoirNameExists(String reservoirName) {
        return waterSituationMapper.countByReservoirName(reservoirName) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<WaterSituation> importFromExcel(MultipartFile file) throws IOException {
        List<WaterSituation> list = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            // 从第1行开始遍历（含表头），但只处理真实数据
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                String reservoirName = getStringCellValue(row.getCell(0));
                // 跳过库名为空或为"示例水库"的行
                if (reservoirName == null || reservoirName.trim().isEmpty() || "示例水库".equals(reservoirName.trim())) {
                    continue;
                }
                WaterSituation ws = new WaterSituation();
                ws.setReservoirName(reservoirName);
                String dateStr = getStringCellValue(row.getCell(1));
                if (dateStr != null) ws.setDate(parseDateTime(dateStr));
                ws.setWaterLevel(getBigDecimalCellValue(row.getCell(2)));
                ws.setStorage(getBigDecimalCellValue(row.getCell(3)));
                ws.setAvgInflow(getBigDecimalCellValue(row.getCell(4)));
                ws.setAvgOutflow(getBigDecimalCellValue(row.getCell(5)));
                ws.setYoyIncrease(getBigDecimalCellValue(row.getCell(6)));
                ws.setTotalCapacity(getBigDecimalCellValue(row.getCell(7)));
                ws.setFloodLevel(getBigDecimalCellValue(row.getCell(8)));
                list.add(ws);
            }
        }
        // 校验
        for (int i = 0; i < list.size(); i++) {
            WaterSituation ws = list.get(i);
            if (ws.getReservoirName() == null || ws.getReservoirName().trim().isEmpty()) {
                throw new RuntimeException("第" + (i + 2) + "行库名不能为空，导入失败！");
            }
            // 可继续加其它字段校验
        }
        // 批量插入
        for (WaterSituation ws : list) {
            waterSituationMapper.insert(ws);
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportResult<WaterSituation> batchImportFromExcel(MultipartFile file) throws IOException {
        List<WaterSituation> list = new ArrayList<>();
        List<String> duplicateReservoirs = new ArrayList<>();
        
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            // 从第1行开始遍历（含表头），但只处理真实数据
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                String reservoirName = getStringCellValue(row.getCell(0));
                // 跳过库名为空或为"示例水库"的行
                if (reservoirName == null || reservoirName.trim().isEmpty() || 
                    "示例水库".equals(reservoirName.trim()) || "实例水库".equals(reservoirName.trim())) {
                    continue;
                }
                
                // 检查库名是否已存在
                if (isReservoirNameExists(reservoirName)) {
                    duplicateReservoirs.add(reservoirName);
                    continue; // 跳过重复的库名
                }
                
                WaterSituation ws = new WaterSituation();
                ws.setReservoirName(reservoirName);
                String dateStr = getStringCellValue(row.getCell(1));
                if (dateStr != null) ws.setDate(parseDateTime(dateStr));
                ws.setWaterLevel(getBigDecimalCellValue(row.getCell(2)));
                ws.setStorage(getBigDecimalCellValue(row.getCell(3)));
                ws.setAvgInflow(getBigDecimalCellValue(row.getCell(4)));
                ws.setAvgOutflow(getBigDecimalCellValue(row.getCell(5)));
                ws.setYoyIncrease(getBigDecimalCellValue(row.getCell(6)));
                ws.setTotalCapacity(getBigDecimalCellValue(row.getCell(7)));
                ws.setFloodLevel(getBigDecimalCellValue(row.getCell(8)));
                list.add(ws);
            }
        }
        
        // 校验
        for (int i = 0; i < list.size(); i++) {
            WaterSituation ws = list.get(i);
            if (ws.getReservoirName() == null || ws.getReservoirName().trim().isEmpty()) {
                throw new RuntimeException("第" + (i + 2) + "行库名不能为空，导入失败！");
            }
        }
        
        // 批量插入
        if (!list.isEmpty()) {
            waterSituationMapper.batchInsert(list);
        }
        
        // 构建导入结果
        String message = "导入成功";
        if (!duplicateReservoirs.isEmpty()) {
            String duplicateNames = String.join(", ", duplicateReservoirs);
            message = "导入成功，跳过重复库名：" + duplicateNames;
            System.out.println("批量导入时跳过重复库名：" + duplicateNames);
        }
        
        return new ImportResult<>(list, duplicateReservoirs, message, true);
    }

    private String getStringCellValue(Cell cell) {
        if (cell == null) return null;
        DataFormatter formatter = new DataFormatter();
        String value = formatter.formatCellValue(cell);
        return value.trim().isEmpty() ? null : value.trim();
    }
    private BigDecimal getBigDecimalCellValue(Cell cell) {
        if (cell == null) return null;
        try {
            DataFormatter formatter = new DataFormatter();
            String value = formatter.formatCellValue(cell).trim();
            return value.isEmpty() ? null : new BigDecimal(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private LocalDateTime parseDateTime(String dateStr) {
        if (dateStr == null) return null;
        DateTimeFormatter[] formatters = new DateTimeFormatter[] {
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy年MM月dd日H时"),
            DateTimeFormatter.ofPattern("yyyy年MM月dd日HH时")
        };
        for (DateTimeFormatter fmt : formatters) {
            try {
                return LocalDateTime.parse(dateStr, fmt);
            } catch (Exception ignored) {}
        }
        throw new RuntimeException("日期格式不支持: " + dateStr);
    }

    @Override
    public void exportToFile(String filePath, List<String> content, String startDate, String endDate, List<String> reservoirs, String format) throws IOException {
        // 简单实现：导出全部水情数据
        List<WaterSituation> dataList = waterSituationMapper.findAll();
        if ("xlsx".equalsIgnoreCase(format) || "excel".equalsIgnoreCase(format)) {
            exportToExcel(filePath, dataList);
        } else {
            exportToCsv(filePath, dataList);
        }
    }

    private void exportToExcel(String filePath, List<WaterSituation> list) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("水情数据");
            String[] headers = {"库名", "日期", "库水位(米)", "蓄水量(万立方米)", "日平均入库流量(立方米/秒)", "日平均出库流量(立方米/秒)", "比去年同期增减(万立方米)", "总库容(万立方米)", "汛限水位(米)"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                sheet.setColumnWidth(i, 20 * 256);
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (int i = 0; i < list.size(); i++) {
                WaterSituation ws = list.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(ws.getReservoirName());
                row.createCell(1).setCellValue(ws.getDate() != null ? ws.getDate().format(dtf) : "");
                row.createCell(2).setCellValue(ws.getWaterLevel() != null ? ws.getWaterLevel().doubleValue() : 0);
                row.createCell(3).setCellValue(ws.getStorage() != null ? ws.getStorage().doubleValue() : 0);
                row.createCell(4).setCellValue(ws.getAvgInflow() != null ? ws.getAvgInflow().doubleValue() : 0);
                row.createCell(5).setCellValue(ws.getAvgOutflow() != null ? ws.getAvgOutflow().doubleValue() : 0);
                row.createCell(6).setCellValue(ws.getYoyIncrease() != null ? ws.getYoyIncrease().doubleValue() : 0);
                row.createCell(7).setCellValue(ws.getTotalCapacity() != null ? ws.getTotalCapacity().doubleValue() : 0);
                row.createCell(8).setCellValue(ws.getFloodLevel() != null ? ws.getFloodLevel().doubleValue() : 0);
            }
            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }
        }
    }

    private void exportToCsv(String filePath, List<WaterSituation> list) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            String[] headers = {"库名", "日期", "库水位(米)", "蓄水量(万立方米)", "日平均入库流量(立方米/秒)", "日平均出库流量(立方米/秒)", "比去年同期增减(万立方米)", "总库容(万立方米)", "汛限水位(米)"};
            writer.write(String.join(",", headers) + "\n");
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (WaterSituation ws : list) {
                String[] row = {
                    ws.getReservoirName(),
                    ws.getDate() != null ? ws.getDate().format(dtf) : "",
                    ws.getWaterLevel() != null ? ws.getWaterLevel().toString() : "",
                    ws.getStorage() != null ? ws.getStorage().toString() : "",
                    ws.getAvgInflow() != null ? ws.getAvgInflow().toString() : "",
                    ws.getAvgOutflow() != null ? ws.getAvgOutflow().toString() : "",
                    ws.getYoyIncrease() != null ? ws.getYoyIncrease().toString() : "",
                    ws.getTotalCapacity() != null ? ws.getTotalCapacity().toString() : "",
                    ws.getFloodLevel() != null ? ws.getFloodLevel().toString() : ""
                };
                writer.write(String.join(",", row) + "\n");
            }
        }
    }
} 