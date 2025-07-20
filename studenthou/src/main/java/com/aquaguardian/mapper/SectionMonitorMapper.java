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
    List<SectionMonitor> findByCondition(@Param("monitorPointName") String monitorPointName, @Param("reservoirName") String reservoirName, @Param("year") Integer year, @Param("month") Integer month);
    
    // 检查监测点名称是否存在（同一水库下）
    int countByMonitorPointName(@Param("monitorPointName") String monitorPointName, @Param("reservoirName") String reservoirName);
    
    // 批量插入
    int batchInsert(@Param("list") List<SectionMonitor> list);
} 