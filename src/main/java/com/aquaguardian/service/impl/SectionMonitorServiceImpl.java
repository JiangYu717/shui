package com.aquaguardian.service.impl;

import com.aquaguardian.entity.SectionMonitor;
import com.aquaguardian.entity.ImportResult;
import com.aquaguardian.mapper.SectionMonitorMapper;
import com.aquaguardian.service.SectionMonitorService;
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
import java.util.ArrayList;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import java.util.Objects;

@Service
public class SectionMonitorServiceImpl implements SectionMonitorService {
    @Autowired
    private SectionMonitorMapper sectionMonitorMapper;

    @Override
    public PageInfo<SectionMonitor> getSectionMonitors(int page, int pageSize, String monitorPointName, String reservoirName, 
                                                      Integer yearMin, Integer yearMax, Integer monthMin, Integer monthMax,
                                                      Double oxygenMin, Double oxygenMax, Double potassiumPermanganateMin, Double potassiumPermanganateMax,
                                                      Double codMin, Double codMax, Double flowMin, Double flowMax,
                                                      Double waterDepthMin, Double waterDepthMax, Double totalNitrogenMin, Double totalNitrogenMax,
                                                      Double totalPhosphorusMin, Double totalPhosphorusMax) {
        PageHelper.startPage(page, pageSize);
        List<SectionMonitor> list = sectionMonitorMapper.findByConditions(monitorPointName, reservoirName, 
                yearMin, yearMax, monthMin, monthMax, oxygenMin, oxygenMax, potassiumPermanganateMin, potassiumPermanganateMax,
                codMin, codMax, flowMin, flowMax, waterDepthMin, waterDepthMax, 
                totalNitrogenMin, totalNitrogenMax, totalPhosphorusMin, totalPhosphorusMax);
        return new PageInfo<>(list);
    }

    @Override
    public SectionMonitor createSectionMonitor(SectionMonitor sectionMonitor) {
        // 检查监测点名称是否重复（同一水库下）
        if (isMonitorPointNameExists(sectionMonitor.getMonitorPointName(), sectionMonitor.getReservoirName())) {
            throw new RuntimeException("监测点名称 '" + sectionMonitor.getMonitorPointName() + "' 在该水库下已存在，请使用其他名称！");
        }
        sectionMonitorMapper.insert(sectionMonitor);
        return sectionMonitor;
    }

    @Override
    public SectionMonitor updateSectionMonitor(SectionMonitor sectionMonitor) {
        // 更新时检查监测点名称是否重复（同一水库下，排除自己）
        SectionMonitor existing = sectionMonitorMapper.findById(sectionMonitor.getId());
        if (existing != null && (!existing.getMonitorPointName().equals(sectionMonitor.getMonitorPointName()) || !existing.getReservoirName().equals(sectionMonitor.getReservoirName()))) {
            if (isMonitorPointNameExists(sectionMonitor.getMonitorPointName(), sectionMonitor.getReservoirName())) {
                throw new RuntimeException("监测点名称 '" + sectionMonitor.getMonitorPointName() + "' 在该水库下已存在，请使用其他名称！");
            }
        }
        sectionMonitorMapper.update(sectionMonitor);
        return sectionMonitor;
    }

    @Override
    public void deleteSectionMonitor(Long id) {
        sectionMonitorMapper.deleteById(id);
    }

    @Override
    public void deleteById(Long id) {
        sectionMonitorMapper.deleteById(id);
    }

    @Override
    public SectionMonitor getSectionMonitorById(Long id) {
        return sectionMonitorMapper.findById(id);
    }

    @Override
    public boolean isMonitorPointNameExists(String monitorPointName, String reservoirName) {
        return sectionMonitorMapper.countByMonitorPointName(monitorPointName, reservoirName) > 0;
    }

    @Override
    public List<SectionMonitor> importFromExcel(MultipartFile file) throws IOException {
        List<SectionMonitor> list = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            // 从第1行开始遍历（含表头），但只处理真实数据
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                String monitorPointName = getStringCellValue(row.getCell(0));
                // 跳过监测点名称为空、为"示例名称"或"实例名称"的行
                if (monitorPointName == null || monitorPointName.trim().isEmpty() ||
                    "示例名称".equals(monitorPointName.trim()) || "实例名称".equals(monitorPointName.trim())) {
                    continue;
                }
                SectionMonitor sm = new SectionMonitor();
                sm.setMonitorPointName(monitorPointName);
                sm.setReservoirName(getStringCellValue(row.getCell(1)));
                sm.setYear(getIntCellValue(row.getCell(2)));
                sm.setMonth(getIntCellValue(row.getCell(3)));
                sm.setOxygen(getBigDecimalCellValue(row.getCell(4)));
                sm.setPotassiumPermanganate(getBigDecimalCellValue(row.getCell(5)));
                sm.setCod(getBigDecimalCellValue(row.getCell(6)));
                sm.setFlow(getBigDecimalCellValue(row.getCell(7)));
                sm.setWaterDepth(getBigDecimalCellValue(row.getCell(8)));
                sm.setTotalNitrogen(getBigDecimalCellValue(row.getCell(9)));
                sm.setTotalPhosphorus(getBigDecimalCellValue(row.getCell(10)));
                list.add(sm);
            }
        }
        // 校验
        for (int i = 0; i < list.size(); i++) {
            SectionMonitor sm = list.get(i);
            if (sm.getMonitorPointName() == null || sm.getMonitorPointName().trim().isEmpty()) {
                throw new RuntimeException("第" + (i + 2) + "行监测点名称不能为空，导入失败！");
            }
            // 可继续加其它字段校验
        }
        // 批量插入
        for (SectionMonitor sm : list) {
            sectionMonitorMapper.insert(sm);
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportResult<SectionMonitor> batchImportFromExcel(MultipartFile file) throws IOException {
        List<SectionMonitor> list = new ArrayList<>();
        List<String> duplicateRecords = new ArrayList<>();
        
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            // 从第1行开始遍历（含表头），但只处理真实数据
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                String monitorPointName = getStringCellValue(row.getCell(0));
                String reservoirName = getStringCellValue(row.getCell(1));
                // 跳过监测点名称为空、为"示例名称"或"实例名称"的行
                if (monitorPointName == null || monitorPointName.trim().isEmpty() ||
                    "示例名称".equals(monitorPointName.trim()) || "实例名称".equals(monitorPointName.trim())) {
                    continue;
                }
                
                SectionMonitor sm = new SectionMonitor();
                sm.setMonitorPointName(monitorPointName);
                sm.setReservoirName(reservoirName);
                sm.setYear(getIntCellValue(row.getCell(2)));
                sm.setMonth(getIntCellValue(row.getCell(3)));
                sm.setOxygen(getBigDecimalCellValue(row.getCell(4)));
                sm.setPotassiumPermanganate(getBigDecimalCellValue(row.getCell(5)));
                sm.setCod(getBigDecimalCellValue(row.getCell(6)));
                sm.setFlow(getBigDecimalCellValue(row.getCell(7)));
                sm.setWaterDepth(getBigDecimalCellValue(row.getCell(8)));
                sm.setTotalNitrogen(getBigDecimalCellValue(row.getCell(9)));
                sm.setTotalPhosphorus(getBigDecimalCellValue(row.getCell(10)));
                
                // 检查是否与数据库中的记录重复
                if (isAllFieldsDuplicate(sm)) {
                    duplicateRecords.add(monitorPointName + "（" + reservoirName + "）- 所有字段重复");
                    continue; // 跳过所有字段都重复的记录
                }
                
                // 检查是否与当前批次中的记录重复
                if (isDuplicateInCurrentBatch(sm, list)) {
                    duplicateRecords.add(monitorPointName + "（" + reservoirName + "）- 当前批次重复");
                    continue; // 跳过当前批次中重复的记录
                }
                
                list.add(sm);
            }
        }
        
        // 校验
        for (int i = 0; i < list.size(); i++) {
            SectionMonitor sm = list.get(i);
            if (sm.getMonitorPointName() == null || sm.getMonitorPointName().trim().isEmpty()) {
                throw new RuntimeException("第" + (i + 2) + "行监测点名称不能为空，导入失败！");
            }
        }
        
        // 批量插入
        if (!list.isEmpty()) {
            sectionMonitorMapper.batchInsert(list);
        }
        
        // 构建导入结果
        String message = "导入成功";
        if (!duplicateRecords.isEmpty()) {
            String duplicateNames = String.join(", ", duplicateRecords);
            message = "重复记录（所有字段相同）：" + duplicateNames;
            System.out.println("批量导入时跳过重复记录（所有字段相同）：" + duplicateNames);
        }
        
        return new ImportResult<>(list, duplicateRecords, message, true);
    }

    /**
     * 检查监测断面记录的所有字段是否都重复
     */
    private boolean isAllFieldsDuplicate(SectionMonitor newRecord) {
        // 获取所有现有记录
        List<SectionMonitor> existingRecords = sectionMonitorMapper.findAll();
        
        for (SectionMonitor existing : existingRecords) {
            // 比较所有字段
            if (Objects.equals(existing.getMonitorPointName(), newRecord.getMonitorPointName()) &&
                Objects.equals(existing.getReservoirName(), newRecord.getReservoirName()) &&
                Objects.equals(existing.getYear(), newRecord.getYear()) &&
                Objects.equals(existing.getMonth(), newRecord.getMonth()) &&
                compareBigDecimal(existing.getOxygen(), newRecord.getOxygen()) &&
                compareBigDecimal(existing.getPotassiumPermanganate(), newRecord.getPotassiumPermanganate()) &&
                compareBigDecimal(existing.getCod(), newRecord.getCod()) &&
                compareBigDecimal(existing.getFlow(), newRecord.getFlow()) &&
                compareBigDecimal(existing.getWaterDepth(), newRecord.getWaterDepth()) &&
                compareBigDecimal(existing.getTotalNitrogen(), newRecord.getTotalNitrogen()) &&
                compareBigDecimal(existing.getTotalPhosphorus(), newRecord.getTotalPhosphorus())) {
                return true; // 所有字段都相同
            }
        }
        return false; // 没有找到完全相同的记录
    }

    /**
     * 检查是否与当前批次中的记录重复
     */
    private boolean isDuplicateInCurrentBatch(SectionMonitor newRecord, List<SectionMonitor> currentBatch) {
        for (SectionMonitor existing : currentBatch) {
            // 比较所有字段
            if (Objects.equals(existing.getMonitorPointName(), newRecord.getMonitorPointName()) &&
                Objects.equals(existing.getReservoirName(), newRecord.getReservoirName()) &&
                Objects.equals(existing.getYear(), newRecord.getYear()) &&
                Objects.equals(existing.getMonth(), newRecord.getMonth()) &&
                compareBigDecimal(existing.getOxygen(), newRecord.getOxygen()) &&
                compareBigDecimal(existing.getPotassiumPermanganate(), newRecord.getPotassiumPermanganate()) &&
                compareBigDecimal(existing.getCod(), newRecord.getCod()) &&
                compareBigDecimal(existing.getFlow(), newRecord.getFlow()) &&
                compareBigDecimal(existing.getWaterDepth(), newRecord.getWaterDepth()) &&
                compareBigDecimal(existing.getTotalNitrogen(), newRecord.getTotalNitrogen()) &&
                compareBigDecimal(existing.getTotalPhosphorus(), newRecord.getTotalPhosphorus())) {
                return true; // 所有字段都相同
            }
        }
        return false; // 没有找到完全相同的记录
    }

    /**
     * 比较两个BigDecimal，忽略精度差异
     */
    private boolean compareBigDecimal(BigDecimal bd1, BigDecimal bd2) {
        if (bd1 == null && bd2 == null) return true;
        if (bd1 == null || bd2 == null) return false;
        return bd1.compareTo(bd2) == 0;
    }

    private String getStringCellValue(Cell cell) {
        if (cell == null) return null;
        DataFormatter formatter = new DataFormatter();
        String value = formatter.formatCellValue(cell);
        return value.trim().isEmpty() ? null : value.trim();
    }

    private Integer getIntCellValue(Cell cell) {
        if (cell == null) return null;
        try {
            DataFormatter formatter = new DataFormatter();
            String value = formatter.formatCellValue(cell).trim();
            return value.isEmpty() ? null : Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
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

    @Override
    public void exportToFile(String filePath, String format) throws IOException {
        List<SectionMonitor> dataList = sectionMonitorMapper.findAll();
        if ("xlsx".equalsIgnoreCase(format) || "excel".equalsIgnoreCase(format)) {
            exportToExcel(filePath, dataList);
        } else {
            exportToCsv(filePath, dataList);
        }
    }

    private void exportToExcel(String filePath, List<SectionMonitor> list) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("监测断面数据");
            String[] headers = {"监测点名称", "水库名称", "年份", "月份", "氧气(mg/l)", "高锰酸盐(mg/l)", "化学需氧量(mg/l)", "流量(m³/s)", "水深(m)", "总氮(mg/l)", "总磷(mg/l)"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                sheet.setColumnWidth(i, 20 * 256);
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }
            for (int i = 0; i < list.size(); i++) {
                SectionMonitor sm = list.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(sm.getMonitorPointName());
                row.createCell(1).setCellValue(sm.getReservoirName());
                row.createCell(2).setCellValue(sm.getYear() != null ? sm.getYear() : 0);
                row.createCell(3).setCellValue(sm.getMonth() != null ? sm.getMonth() : 0);
                row.createCell(4).setCellValue(sm.getOxygen() != null ? sm.getOxygen().doubleValue() : 0);
                row.createCell(5).setCellValue(sm.getPotassiumPermanganate() != null ? sm.getPotassiumPermanganate().doubleValue() : 0);
                row.createCell(6).setCellValue(sm.getCod() != null ? sm.getCod().doubleValue() : 0);
                row.createCell(7).setCellValue(sm.getFlow() != null ? sm.getFlow().doubleValue() : 0);
                row.createCell(8).setCellValue(sm.getWaterDepth() != null ? sm.getWaterDepth().doubleValue() : 0);
                row.createCell(9).setCellValue(sm.getTotalNitrogen() != null ? sm.getTotalNitrogen().doubleValue() : 0);
                row.createCell(10).setCellValue(sm.getTotalPhosphorus() != null ? sm.getTotalPhosphorus().doubleValue() : 0);
            }
            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }
        }
    }

    private void exportToCsv(String filePath, List<SectionMonitor> list) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            String[] headers = {"监测点名称", "水库名称", "年份", "月份", "氧气(mg/l)", "高锰酸盐(mg/l)", "化学需氧量(mg/l)", "流量(m³/s)", "水深(m)", "总氮(mg/l)", "总磷(mg/l)"};
            writer.write(String.join(",", headers) + "\n");
            for (SectionMonitor sm : list) {
                String[] row = {
                    sm.getMonitorPointName(),
                    sm.getReservoirName(),
                    sm.getYear() != null ? sm.getYear().toString() : "",
                    sm.getMonth() != null ? sm.getMonth().toString() : "",
                    sm.getOxygen() != null ? sm.getOxygen().toString() : "",
                    sm.getPotassiumPermanganate() != null ? sm.getPotassiumPermanganate().toString() : "",
                    sm.getCod() != null ? sm.getCod().toString() : "",
                    sm.getFlow() != null ? sm.getFlow().toString() : "",
                    sm.getWaterDepth() != null ? sm.getWaterDepth().toString() : "",
                    sm.getTotalNitrogen() != null ? sm.getTotalNitrogen().toString() : "",
                    sm.getTotalPhosphorus() != null ? sm.getTotalPhosphorus().toString() : ""
                };
                writer.write(String.join(",", row) + "\n");
            }
        }
    }
} 