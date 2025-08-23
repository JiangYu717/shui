package com.aquaguardian.entity;

import java.math.BigDecimal;

public class SectionMonitor {
    private Long id;
    private String monitorPointName;
    private String reservoirName;
    private Integer year;
    private Integer month;
    private BigDecimal oxygen;
    private BigDecimal potassiumPermanganate;
    private BigDecimal cod;
    private BigDecimal flow;
    private BigDecimal waterDepth;
    private BigDecimal totalNitrogen;
    private BigDecimal totalPhosphorus;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMonitorPointName() { return monitorPointName; }
    public void setMonitorPointName(String monitorPointName) { this.monitorPointName = monitorPointName; }
    public String getReservoirName() { return reservoirName; }
    public void setReservoirName(String reservoirName) { this.reservoirName = reservoirName; }
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }
    public BigDecimal getOxygen() { return oxygen; }
    public void setOxygen(BigDecimal oxygen) { this.oxygen = oxygen; }
    public BigDecimal getPotassiumPermanganate() { return potassiumPermanganate; }
    public void setPotassiumPermanganate(BigDecimal potassiumPermanganate) { this.potassiumPermanganate = potassiumPermanganate; }
    public BigDecimal getCod() { return cod; }
    public void setCod(BigDecimal cod) { this.cod = cod; }
    public BigDecimal getFlow() { return flow; }
    public void setFlow(BigDecimal flow) { this.flow = flow; }
    public BigDecimal getWaterDepth() { return waterDepth; }
    public void setWaterDepth(BigDecimal waterDepth) { this.waterDepth = waterDepth; }
    public BigDecimal getTotalNitrogen() { return totalNitrogen; }
    public void setTotalNitrogen(BigDecimal totalNitrogen) { this.totalNitrogen = totalNitrogen; }
    public BigDecimal getTotalPhosphorus() { return totalPhosphorus; }
    public void setTotalPhosphorus(BigDecimal totalPhosphorus) { this.totalPhosphorus = totalPhosphorus; }
} 