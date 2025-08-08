CREATE TABLE section_monitor (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    monitor_point_name VARCHAR(64) NOT NULL COMMENT '监测点名称',
    reservoir_name VARCHAR(64) NOT NULL COMMENT '水库名称',
    year INT NOT NULL COMMENT '年份',
    month INT NOT NULL COMMENT '月份',
    oxygen DECIMAL(8,3) COMMENT '氧气(mg/l)',
    potassium_permanganate DECIMAL(8,3) COMMENT '高锰酸盐(mg/l)',
    cod DECIMAL(8,3) COMMENT '化学需氧量(mg/l)',
    flow DECIMAL(8,3) COMMENT '流量(m³/s)',
    water_depth DECIMAL(8,3) COMMENT '水深(m)',
    total_nitrogen DECIMAL(8,3) COMMENT '总氮(mg/l)',
    total_phosphorus DECIMAL(8,3) COMMENT '总磷(mg/l)'
) COMMENT='监测断面数据表';

INSERT INTO section_monitor
(monitor_point_name, reservoir_name, year, month, oxygen, potassium_permanganate, cod, flow, water_depth, total_nitrogen, total_phosphorus)
VALUES
('官厅', '官厅', 2019, 1, 0.804, 5.8, 16.4, 1.54, 2.7, 2.59, 0.186),
('密云', '密云', 2019, 1, 0.773, 5.7, 17.8, 1.68, 3.5, 2.91, 0.129),
('怀柔', '怀柔', 2019, 1, 0.881, 5.8, 18.1, 2.62, 3.2, 2.97, 0.169),
('海子', '海子', 2019, 1, 0.652, 5.6, 17.0, 2.90, 2.4, 2.35, 0.156),
('白河堡', '白河堡', 2019, 1, 1.300, 15.7, 87.9, 1.42, 2.2, 27.7, 0.353),
('斋堂', '斋堂', 2019, 1, 1.480, 8.2, 28.3, 2.39, 3.5, 3.93, 0.277),
('十三陵', '十三陵', 2019, 1, 0.491, 5.5, 15.6, 1.24, 2.9, 2.02, 0.154),
('半城子', '半城子', 2019, 1, 1.440, 8.8, 27.8, 3.70, 4.7, 3.48, 0.295),
('沙厂', '沙厂', 2019, 1, 1.460, 9.5, 27.4, 3.51, 4.2, 4.42, 0.300),
('遥桥峪', '遥桥峪', 2019, 1, 0.804, 5.8, 16.4, 1.54, 2.7, 2.59, 0.186); 