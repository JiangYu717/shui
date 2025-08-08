/*
 Navicat MySQL Data Transfer

 Source Server         : jiedan
 Source Server Type    : MySQL
 Source Server Version : 50726
 Source Host           : localhost:3306
 Source Schema         : aquaguardian

 Target Server Type    : MySQL
 Target Server Version : 50726
 File Encoding         : 65001

 Date: 06/06/2025 21:24:43
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for data_import_export_log
-- ----------------------------
DROP TABLE IF EXISTS `data_import_export_log`;
CREATE TABLE `data_import_export_log`  (
  `log_id` int(11) NOT NULL AUTO_INCREMENT,
  `operation_type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'import/export',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `file_size` int(11) NULL DEFAULT NULL COMMENT 'Unit: bytes',
  `record_count` int(11) NULL DEFAULT NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'success/failed',
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  `operator` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `operation_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`log_id`) USING BTREE,
  INDEX `idx_operation_time`(`operation_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of data_import_export_log
-- ----------------------------

-- ----------------------------
-- Table structure for issue_report
-- ----------------------------
DROP TABLE IF EXISTS `issue_report`;
CREATE TABLE `issue_report`  (
  `issue_id` int(11) NOT NULL AUTO_INCREMENT,
  `inspector_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `phone_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `reservoir_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `issue_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `issue_report_date` date NOT NULL,
  `is_resolved` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`issue_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of issue_report
-- ----------------------------

-- ----------------------------
-- Table structure for reservoir_info
-- ----------------------------
DROP TABLE IF EXISTS `reservoir_info`;
CREATE TABLE `reservoir_info`  (
  `reservoir_id` int(11) NOT NULL AUTO_INCREMENT,
  `reservoir_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `capacity` decimal(10, 2) NULL DEFAULT NULL COMMENT 'Unit: million cubic meters',
  `water_level` decimal(6, 2) NULL DEFAULT NULL COMMENT 'Unit: meters',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'Type of reservoir',
  `purpose` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `responsible_person` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `construction_date` date NULL DEFAULT NULL,
  `last_maintenance_date` date NULL DEFAULT NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'normal' COMMENT 'normal/maintenance/risk',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`reservoir_id`) USING BTREE,
  UNIQUE INDEX `idx_reservoir_name`(`reservoir_name`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of reservoir_info
-- ----------------------------
INSERT INTO `reservoir_info` VALUES (6, '122', '222', 22.00, 22.00, '大型水库', '灌溉', '张三', '13800138003', '2020-01-01', '2024-01-01', '正常', '2025-06-06 18:42:24', '2025-06-06 18:42:24');
INSERT INTO `reservoir_info` VALUES (7, '112', '12', 2.00, 2.00, '大型水库', '2', '2', '15100024455', '2025-06-04', '2025-06-11', 'normal', '2025-06-06 19:26:05', '2025-06-06 19:26:05');
INSERT INTO `reservoir_info` VALUES (9, '222', '22', 22.00, 22.00, '22', '22', '22', '22', '2020-01-01', '2024-01-01', '正常', '2025-06-06 19:36:38', '2025-06-06 19:36:38');
INSERT INTO `reservoir_info` VALUES (12, '11116', '11116', 4.00, 4.00, '大型水库', '22233', '44455', '15100222455', '2025-06-02', '2025-06-19', 'normal', '2025-06-06 19:43:17', '2025-06-06 19:43:26');
INSERT INTO `reservoir_info` VALUES (13, '333322', '33332', 3332.00, 32.00, '大型水库', '灌溉', '张三', '13800138000', '2020-01-01', '2024-01-01', '正常', '2025-06-06 19:44:16', '2025-06-06 19:44:16');

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `user_id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `phone_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `role` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'user',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  `last_login` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `idx_username`(`username`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, '111', '96e79218965eb72c92a549dd5a330112', '15122224444', 'user', '2025-06-06 11:13:49', '2025-06-06 11:25:02');

-- ----------------------------
-- Table structure for water_quality_data
-- ----------------------------
DROP TABLE IF EXISTS `water_quality_data`;
CREATE TABLE `water_quality_data`  (
  `data_id` int(11) NOT NULL AUTO_INCREMENT,
  `reservoir_id` int(11) NOT NULL,
  `measurement_date` datetime(0) NOT NULL,
  `water_temperature` decimal(4, 1) NULL DEFAULT NULL COMMENT 'Unit: °C',
  `ph_value` decimal(3, 1) NULL DEFAULT NULL,
  `dissolved_oxygen` decimal(4, 2) NULL DEFAULT NULL COMMENT 'Unit: mg/L',
  `turbidity` decimal(5, 2) NULL DEFAULT NULL COMMENT 'Unit: NTU',
  `conductivity` decimal(6, 2) NULL DEFAULT NULL COMMENT 'Unit: μS/cm',
  `total_phosphorus` decimal(5, 3) NULL DEFAULT NULL COMMENT 'Unit: mg/L',
  `total_nitrogen` decimal(5, 3) NULL DEFAULT NULL COMMENT 'Unit: mg/L',
  `algae_density` decimal(8, 2) NULL DEFAULT NULL COMMENT 'Unit: cells/L',
  `risk_level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'normal' COMMENT 'normal/warning/danger',
  `inspector` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `notes` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`data_id`) USING BTREE,
  INDEX `idx_reservoir_date`(`reservoir_id`, `measurement_date`) USING BTREE,
  CONSTRAINT `fk_water_quality_reservoir` FOREIGN KEY (`reservoir_id`) REFERENCES `reservoir_info` (`reservoir_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of water_quality_data
-- ----------------------------

-- ----------------------------
-- Table structure for water_quality_inspection
-- ----------------------------
DROP TABLE IF EXISTS `water_quality_inspection`;
CREATE TABLE `water_quality_inspection`  (
  `inspection_id` int(11) NOT NULL AUTO_INCREMENT,
  `inspector_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `reservoir_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `inspection_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `inspection_date` date NOT NULL,
  `nh3_n` double NULL DEFAULT NULL,
  `tp` double NULL DEFAULT NULL,
  `codmn` double NULL DEFAULT NULL,
  `cod` double NULL DEFAULT NULL,
  `inspection_result` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`inspection_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of water_quality_inspection
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
