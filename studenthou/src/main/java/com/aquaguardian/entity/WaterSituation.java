package com.aquaguardian.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class WaterSituation {
    private Integer id; // 主键
    private String reservoirName; // 库名
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime date; // 日期（年月日时）
    private BigDecimal waterLevel; // 库水位(米)
    private BigDecimal storage; // 蓄水量(万立方米)
    private BigDecimal avgInflow; // 日平均入库流量(立方米/秒)
    private BigDecimal avgOutflow; // 日平均出库流量(立方米/秒)
    private BigDecimal yoyIncrease; // 比去年同期增减(万立方米)
    private BigDecimal totalCapacity; // 总库容(万立方米)
    private BigDecimal floodLevel; // 汛限水位(米)

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getReservoirName() { return reservoirName; }
    public void setReservoirName(String reservoirName) { this.reservoirName = reservoirName; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public BigDecimal getWaterLevel() { return waterLevel; }
    public void setWaterLevel(BigDecimal waterLevel) { this.waterLevel = waterLevel; }

    public BigDecimal getStorage() { return storage; }
    public void setStorage(BigDecimal storage) { this.storage = storage; }

    public BigDecimal getAvgInflow() { return avgInflow; }
    public void setAvgInflow(BigDecimal avgInflow) { this.avgInflow = avgInflow; }

    public BigDecimal getAvgOutflow() { return avgOutflow; }
    public void setAvgOutflow(BigDecimal avgOutflow) { this.avgOutflow = avgOutflow; }

    public BigDecimal getYoyIncrease() { return yoyIncrease; }
    public void setYoyIncrease(BigDecimal yoyIncrease) { this.yoyIncrease = yoyIncrease; }

    public BigDecimal getTotalCapacity() { return totalCapacity; }
    public void setTotalCapacity(BigDecimal totalCapacity) { this.totalCapacity = totalCapacity; }

    public BigDecimal getFloodLevel() { return floodLevel; }
    public void setFloodLevel(BigDecimal floodLevel) { this.floodLevel = floodLevel; }
} 