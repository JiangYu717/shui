-- 水情数据表
CREATE TABLE IF NOT EXISTS `water_situation` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `reservoir_name` varchar(100) NOT NULL COMMENT '库名',
  `date` datetime NOT NULL COMMENT '日期时间',
  `water_level` decimal(8,2) COMMENT '库水位(米)',
  `storage` decimal(10,2) COMMENT '蓄水量(万立方米)',
  `avg_inflow` decimal(8,2) COMMENT '日平均入库流量(立方米/秒)',
  `avg_outflow` decimal(8,2) COMMENT '日平均出库流量(立方米/秒)',
  `yoy_increase` decimal(10,2) COMMENT '比去年同期增减(万立方米)',
  `total_capacity` decimal(10,2) COMMENT '总库容(万立方米)',
  `flood_level` decimal(8,2) COMMENT '汛限水位(米)',
  PRIMARY KEY (`id`),
  KEY `idx_reservoir_name` (`reservoir_name`),
  KEY `idx_date` (`date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='水情数据表';

-- 插入测试数据
INSERT INTO `water_situation` (`reservoir_name`, `date`, `water_level`, `storage`, `avg_inflow`, `avg_outflow`, `yoy_increase`, `total_capacity`, `flood_level`) VALUES
('密云水库', '2024-01-15 08:00:00', 145.20, 12500.50, 85.30, 65.20, 1200.50, 43700.00, 147.00),
('官厅水库', '2024-01-15 08:00:00', 475.80, 8900.30, 45.60, 38.90, 850.20, 22700.00, 479.00),
('怀柔水库', '2024-01-15 08:00:00', 89.50, 3200.80, 25.40, 18.70, 450.30, 9800.00, 91.00),
('十三陵水库', '2024-01-15 08:00:00', 95.30, 1800.60, 15.20, 12.40, 280.10, 8100.00, 97.00),
('密云水库', '2024-01-16 08:00:00', 145.45, 12600.20, 87.10, 66.80, 1250.30, 43700.00, 147.00),
('官厅水库', '2024-01-16 08:00:00', 475.95, 8950.10, 46.20, 39.50, 870.40, 22700.00, 479.00),
('怀柔水库', '2024-01-16 08:00:00', 89.65, 3250.40, 26.10, 19.20, 460.80, 9800.00, 91.00),
('十三陵水库', '2024-01-16 08:00:00', 95.55, 1820.30, 15.80, 12.90, 285.60, 8100.00, 97.00),
('密云水库', '2024-01-17 08:00:00', 145.70, 12700.80, 88.90, 68.40, 1300.10, 43700.00, 147.00),
('官厅水库', '2024-01-17 08:00:00', 476.10, 9000.50, 46.80, 40.10, 890.70, 22700.00, 479.00); 