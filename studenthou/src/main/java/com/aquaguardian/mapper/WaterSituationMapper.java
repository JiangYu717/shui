package com.aquaguardian.mapper;

import com.aquaguardian.entity.WaterSituation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface WaterSituationMapper {
    List<WaterSituation> findAll();
    WaterSituation findById(@Param("id") Integer id);
    int insert(WaterSituation waterSituation);
    int update(WaterSituation waterSituation);
    int deleteById(@Param("id") Integer id);
    List<WaterSituation> findByReservoirNameAndDate(@Param("reservoirName") String reservoirName, @Param("date") String date);
    List<WaterSituation> findByConditions(@Param("reservoirName") String reservoirName, @Param("date") String date,
                                         @Param("storageMin") Double storageMin, @Param("storageMax") Double storageMax,
                                         @Param("totalCapacityMin") Double totalCapacityMin, @Param("totalCapacityMax") Double totalCapacityMax);
    
    // 检查库名是否存在
    int countByReservoirName(@Param("reservoirName") String reservoirName);
    
    // 批量插入
    int batchInsert(@Param("list") List<WaterSituation> list);
} 