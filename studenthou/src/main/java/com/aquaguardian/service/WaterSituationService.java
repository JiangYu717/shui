package com.aquaguardian.service;

import com.aquaguardian.entity.ImportResult;
import com.aquaguardian.entity.WaterSituation;
import com.github.pagehelper.PageInfo;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

public interface WaterSituationService {
    PageInfo<WaterSituation> getWaterSituations(int page, int pageSize, String reservoirName, String date, 
                                               Double storageMin, Double storageMax, Double totalCapacityMin, Double totalCapacityMax);
    WaterSituation createWaterSituation(WaterSituation waterSituation);
    WaterSituation updateWaterSituation(WaterSituation waterSituation);
    void deleteWaterSituation(Integer id);
    WaterSituation getWaterSituationById(Integer id);
    List<WaterSituation> importFromExcel(MultipartFile file) throws IOException;
    void exportToFile(String filePath, List<String> content, String startDate, String endDate, List<String> reservoirs, String format) throws IOException;
    
    // 检查库名是否重复
    boolean isReservoirNameExists(String reservoirName);
    
    // 批量导入（改进版）
    ImportResult<WaterSituation> batchImportFromExcel(MultipartFile file) throws IOException;
} 