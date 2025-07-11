package com.aquaguardian.service;

import com.aquaguardian.entity.WaterSituation;
import com.github.pagehelper.PageInfo;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

public interface WaterSituationService {
    PageInfo<WaterSituation> getWaterSituations(int page, int pageSize, String reservoirName, String date);
    WaterSituation createWaterSituation(WaterSituation waterSituation);
    WaterSituation updateWaterSituation(WaterSituation waterSituation);
    void deleteWaterSituation(Integer id);
    WaterSituation getWaterSituationById(Integer id);
    List<WaterSituation> importFromExcel(MultipartFile file) throws IOException;
    void exportToFile(String filePath, List<String> content, String startDate, String endDate, List<String> reservoirs, String format) throws IOException;
} 