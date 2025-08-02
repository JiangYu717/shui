package com.aquaguardian.service;

import com.aquaguardian.entity.SectionMonitor;
import com.aquaguardian.entity.ImportResult;
import com.github.pagehelper.PageInfo;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

public interface SectionMonitorService {
    PageInfo<SectionMonitor> getSectionMonitors(int page, int pageSize, String monitorPointName, String reservoirName, 
                                               Integer yearMin, Integer yearMax, Integer monthMin, Integer monthMax,
                                               Double oxygenMin, Double oxygenMax, Double potassiumPermanganateMin, Double potassiumPermanganateMax,
                                               Double codMin, Double codMax, Double flowMin, Double flowMax,
                                               Double waterDepthMin, Double waterDepthMax, Double totalNitrogenMin, Double totalNitrogenMax,
                                               Double totalPhosphorusMin, Double totalPhosphorusMax);
    SectionMonitor createSectionMonitor(SectionMonitor sectionMonitor);
    SectionMonitor updateSectionMonitor(SectionMonitor sectionMonitor);
    void deleteSectionMonitor(Long id);
    void deleteById(Long id);
    SectionMonitor getSectionMonitorById(Long id);
    List<SectionMonitor> importFromExcel(MultipartFile file) throws IOException;
    void exportToFile(String filePath, String format) throws IOException;
    
    // 检查监测点名称是否重复（同一水库下）
    boolean isMonitorPointNameExists(String monitorPointName, String reservoirName);
    
    // 批量导入（改进版）
    ImportResult<SectionMonitor> batchImportFromExcel(MultipartFile file) throws IOException;
} 