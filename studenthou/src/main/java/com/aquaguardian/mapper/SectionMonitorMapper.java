package com.aquaguardian.mapper;

import com.aquaguardian.entity.SectionMonitor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface SectionMonitorMapper {
    List<SectionMonitor> findAll();
    SectionMonitor findById(@Param("id") Long id);
    int insert(SectionMonitor sectionMonitor);
    int update(SectionMonitor sectionMonitor);
    int deleteById(Long id);
    List<SectionMonitor> findByConditions(@Param("monitorPointName") String monitorPointName, @Param("reservoirName") String reservoirName,
                                         @Param("yearMin") Integer yearMin, @Param("yearMax") Integer yearMax,
                                         @Param("monthMin") Integer monthMin, @Param("monthMax") Integer monthMax,
                                         @Param("oxygenMin") Double oxygenMin, @Param("oxygenMax") Double oxygenMax,
                                         @Param("potassiumPermanganateMin") Double potassiumPermanganateMin, @Param("potassiumPermanganateMax") Double potassiumPermanganateMax,
                                         @Param("codMin") Double codMin, @Param("codMax") Double codMax,
                                         @Param("flowMin") Double flowMin, @Param("flowMax") Double flowMax,
                                         @Param("waterDepthMin") Double waterDepthMin, @Param("waterDepthMax") Double waterDepthMax,
                                         @Param("totalNitrogenMin") Double totalNitrogenMin, @Param("totalNitrogenMax") Double totalNitrogenMax,
                                         @Param("totalPhosphorusMin") Double totalPhosphorusMin, @Param("totalPhosphorusMax") Double totalPhosphorusMax);
    
    // 检查监测点名称是否存在（同一水库下）
    int countByMonitorPointName(@Param("monitorPointName") String monitorPointName, @Param("reservoirName") String reservoirName);
    
    // 批量插入
    int batchInsert(@Param("list") List<SectionMonitor> list);
} 