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
} 