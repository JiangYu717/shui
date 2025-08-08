package com.aquaguardian.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.IOException;

@Component
public class ExcelTemplateGenerator {
    
    public void generateReservoirTemplate(String outputPath) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("水库数据");
            
            // 创建标题行样式
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            
            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "水库名称*", "位置*", "容量(万立方米)*", "水位(米)", 
                "类型*", "用途", "负责人", "联系电话",
                "建设日期(YYYY-MM-DD)", "最后维护日期(YYYY-MM-DD)", "状态*"
            };
            
            // 设置列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.setColumnWidth(i, 20 * 256); // 20个字符宽
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // 创建示例数据行
            Row exampleRow = sheet.createRow(1);
            String[] exampleData = {
                "示例水库", "北京市海淀区", "1000", "50",
                "大型水库", "灌溉", "张三", "13800138000",
                "2020-01-01", "2024-01-01", "正常"
            };
            
            for (int i = 0; i < exampleData.length; i++) {
                exampleRow.createCell(i).setCellValue(exampleData[i]);
            }
            
            // 保存文件
            try (FileOutputStream fileOut = new FileOutputStream(outputPath)) {
                workbook.write(fileOut);
            }
        }
    }

    public void generateSectionMonitorTemplate(String outputPath) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("监测断面数据");
            // 标题样式
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            // 表头
            Row headerRow = sheet.createRow(0);
            String[] headers = {"监测点名称", "水库名称", "年份", "月份", "氧气(mg/l)", "高锰酸盐(mg/l)", "化学需氧量(mg/l)", "流量(m³/s)", "水深(m)", "总氮(mg/l)", "总磷(mg/l)"};
            for (int i = 0; i < headers.length; i++) {
                sheet.setColumnWidth(i, 20 * 256);
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            // 示例数据
            Row exampleRow = sheet.createRow(1);
            String[] exampleData = {"崇青", "崇青", "2019", "3", "0.887", "5.5", "18.2", "1.37", "2", "3.15", "0.193"};
            for (int i = 0; i < exampleData.length; i++) {
                exampleRow.createCell(i).setCellValue(exampleData[i]);
            }
            try (FileOutputStream fileOut = new FileOutputStream(outputPath)) {
                workbook.write(fileOut);
            }
        }
    }
} 