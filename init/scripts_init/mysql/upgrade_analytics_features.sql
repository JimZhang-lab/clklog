-- Upgrade an existing clklog MySQL database with funnel metadata and user tags.
-- Run this script after selecting the clklog database.

DELIMITER $$

DROP PROCEDURE IF EXISTS clklog_add_column_if_missing$$
CREATE PROCEDURE clklog_add_column_if_missing(
  IN p_table_name varchar(128),
  IN p_column_name varchar(128),
  IN p_column_definition varchar(1000)
)
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = p_table_name
      AND COLUMN_NAME = p_column_name
  ) THEN
    SET @ddl = CONCAT(
      'ALTER TABLE `', p_table_name, '` ADD COLUMN `',
      p_column_name, '` ', p_column_definition
    );
    PREPARE clklog_stmt FROM @ddl;
    EXECUTE clklog_stmt;
    DEALLOCATE PREPARE clklog_stmt;
  END IF;
END$$

DELIMITER ;

CALL clklog_add_column_if_missing('tbl_funnel', 'measurement', 'varchar(16) DEFAULT NULL');
CALL clklog_add_column_if_missing('tbl_funnel', 'window_numeral', 'int DEFAULT NULL');
CALL clklog_add_column_if_missing('tbl_funnel', 'window_unit', 'varchar(16) DEFAULT NULL');
CALL clklog_add_column_if_missing('tbl_funnel', 'is_open_relation', 'bit(1) DEFAULT NULL');
CALL clklog_add_column_if_missing('tbl_funnel', 'query_json', 'longtext');

DROP PROCEDURE IF EXISTS clklog_add_column_if_missing;

CREATE TABLE IF NOT EXISTS `tbl_tag_category` (
  `id` varchar(36) NOT NULL,
  `project_name` varchar(80) DEFAULT NULL,
  `parent_id` varchar(36) DEFAULT NULL,
  `display_name` varchar(120) DEFAULT NULL,
  `sort_order` int DEFAULT 0,
  `status` varchar(32) DEFAULT NULL,
  `description` text,
  `create_time` datetime(6) DEFAULT NULL,
  `update_time` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `i_tag_category_project` (`project_name`),
  KEY `i_tag_category_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `tbl_user_tag` (
  `id` varchar(36) NOT NULL,
  `project_name` varchar(80) NOT NULL,
  `category_id` varchar(36) DEFAULT NULL,
  `display_name` varchar(120) NOT NULL,
  `tag_key` varchar(160) NOT NULL,
  `data_type` varchar(32) DEFAULT NULL,
  `create_type` varchar(32) DEFAULT NULL,
  `update_mode` varchar(32) DEFAULT NULL,
  `status` varchar(32) DEFAULT NULL,
  `description` text,
  `rule_json` longtext,
  `match_user_count` bigint DEFAULT 0,
  `last_execute_status` varchar(32) DEFAULT NULL,
  `last_execute_time` datetime(6) DEFAULT NULL,
  `create_time` datetime(6) DEFAULT NULL,
  `update_time` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `u_user_tag_project_key` (`project_name`,`tag_key`),
  KEY `i_user_tag_category` (`category_id`),
  KEY `i_user_tag_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `tbl_user_tag_assignment` (
  `id` varchar(36) NOT NULL,
  `project_name` varchar(80) NOT NULL,
  `tag_id` varchar(36) NOT NULL,
  `distinct_id` varchar(160) NOT NULL,
  `tag_value` varchar(500) DEFAULT NULL,
  `create_time` datetime(6) DEFAULT NULL,
  `update_time` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `u_user_tag_assignment` (`project_name`,`tag_id`,`distinct_id`),
  KEY `i_user_tag_assignment_user` (`project_name`,`distinct_id`),
  KEY `i_user_tag_assignment_tag` (`project_name`,`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `tbl_bookmark` (
  `id` varchar(36) NOT NULL,
  `project_name` varchar(80) DEFAULT NULL,
  `name` varchar(160) DEFAULT NULL,
  `analysis_type` varchar(64) DEFAULT NULL,
  `status` varchar(32) DEFAULT NULL,
  `description` text,
  `query_json` longtext,
  `create_user` varchar(80) DEFAULT NULL,
  `create_time` datetime(6) DEFAULT NULL,
  `update_time` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `i_bookmark_project` (`project_name`),
  KEY `i_bookmark_type` (`analysis_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `tbl_cdp_asset` (
  `id` varchar(36) NOT NULL,
  `project_name` varchar(80) DEFAULT NULL,
  `asset_type` varchar(32) DEFAULT NULL,
  `display_name` varchar(160) DEFAULT NULL,
  `asset_key` varchar(180) DEFAULT NULL,
  `status` varchar(32) DEFAULT NULL,
  `create_type` varchar(32) DEFAULT NULL,
  `update_mode` varchar(32) DEFAULT NULL,
  `distinct_ids` text,
  `rule_json` longtext,
  `description` text,
  `match_user_count` bigint DEFAULT 0,
  `last_execute_status` varchar(32) DEFAULT NULL,
  `last_execute_time` datetime(6) DEFAULT NULL,
  `create_user` varchar(80) DEFAULT NULL,
  `create_time` datetime(6) DEFAULT NULL,
  `update_time` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `u_cdp_asset_project_key` (`project_name`,`asset_type`,`asset_key`),
  KEY `i_cdp_asset_project` (`project_name`),
  KEY `i_cdp_asset_type` (`asset_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `tbl_role` (
  `id` varchar(36) NOT NULL,
  `role_name` varchar(80) DEFAULT NULL,
  `display_name` varchar(120) DEFAULT NULL,
  `role_type` varchar(64) DEFAULT NULL,
  `status` varchar(32) DEFAULT NULL,
  `sort_order` int DEFAULT 0,
  `create_time` datetime(6) DEFAULT NULL,
  `update_time` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `u_role_name` (`role_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `tbl_menu` (
  `id` varchar(36) NOT NULL,
  `title` varchar(160) DEFAULT NULL,
  `path` varchar(240) DEFAULT NULL,
  `component` varchar(240) DEFAULT NULL,
  `permissions` text,
  `roles` text,
  `status` varchar(32) DEFAULT NULL,
  `sort_order` int DEFAULT 0,
  `hidden` bit(1) DEFAULT NULL,
  `external_window` bit(1) DEFAULT NULL,
  `create_time` datetime(6) DEFAULT NULL,
  `update_time` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `i_menu_path` (`path`),
  KEY `i_menu_sort` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `tbl_api_key` (
  `id` varchar(36) NOT NULL,
  `project_name` varchar(80) DEFAULT NULL,
  `display_name` varchar(120) DEFAULT NULL,
  `key_prefix` varchar(32) DEFAULT NULL,
  `key_mask` varchar(160) DEFAULT NULL,
  `key_hash` varchar(128) DEFAULT NULL,
  `status` varchar(32) DEFAULT NULL,
  `create_user` varchar(80) DEFAULT NULL,
  `expires_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `i_api_key_project` (`project_name`),
  KEY `i_api_key_status` (`status`),
  KEY `i_api_key_prefix` (`key_prefix`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
