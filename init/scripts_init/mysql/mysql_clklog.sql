

SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS clklog DEFAULT CHARSET utf8 COLLATE utf8_general_ci;


use clklog;

CREATE TABLE IF NOT EXISTS `tbl_project` (
  `id` varchar(36) NOT NULL,
  `project_name` varchar(40) DEFAULT NULL,
  `project_display_name` varchar(40) DEFAULT NULL,
  `excluded_ip` text,
  `excluded_ua` text,
  `excluded_url_params` text,
  `searchword_category_key` text,
  `searchword_key` text,
  `root_urls` text,
  `status` varchar(16) DEFAULT NULL,
  `update_time` datetime(6) DEFAULT NULL,
  `create_time` datetime(6) DEFAULT NULL,
  `token` varchar(36) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `i_status` (`status`),
  KEY `i_proj` (`project_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


LOCK TABLES `tbl_project` WRITE;
INSERT INTO `tbl_project` VALUES ('90a86ab1-614f-030e-3938-7cacdb2a7e6a','clklogapp','clklog',NULL,NULL,NULL,NULL,NULL,'','已保存',now(),now(),'ddf51db3-7c99-1310-a44f-79feb7b63c69');
UNLOCK TABLES;


CREATE TABLE IF NOT EXISTS `tbl_project_log_stat` (
  `id` varchar(36) NOT NULL,
  `project_name` varchar(40) NOT NULL,
  `stat_date` datetime DEFAULT NULL,
  `log_record_count` bigint(20) DEFAULT NULL,
  `log_space_size` bigint(20) DEFAULT NULL,
  `log_latest_time` datetime(6) DEFAULT NULL,
  `update_time` datetime(6) DEFAULT NULL,
  `db_first_time` datetime(6) DEFAULT NULL,
  `db_latest_time` datetime(6) DEFAULT NULL,
  `db_record_count` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `i_a_s` (`project_name`,`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE IF NOT EXISTS `tbl_project_stat` (
  `project_name` varchar(40) NOT NULL,
  `log_record_count` bigint(20) DEFAULT NULL,
  `log_space_size` bigint(20) DEFAULT NULL,
  `log_latest_time` datetime(6) DEFAULT NULL,
  `db_record_count` bigint(20) DEFAULT NULL,
  `db_space_size` bigint(20) DEFAULT NULL,
  `update_time` datetime(6) DEFAULT NULL,
  `log_days` int(11) DEFAULT NULL,
  `db_first_time` datetime(6) DEFAULT NULL,
  `db_latest_time` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`project_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE IF NOT EXISTS `tbl_global_setting` (
  `id` varchar(36) NOT NULL,
  `excluded_ip` text,
  `excluded_ua` text,
  `excluded_url_params` text,
  `searchword_category_key` text,
  `searchword_key` text,
  `update_time` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


LOCK TABLES `tbl_global_setting` WRITE;
INSERT INTO `tbl_global_setting` VALUES ('9d1df3ce-e8a7-ecb6-30af-33585b069c8a',NULL,NULL,NULL,NULL,NULL,now());
UNLOCK TABLES;

CREATE TABLE IF NOT EXISTS `tbl_meta_event` (
  `id` varchar(36) NOT NULL,
  `project_name` varchar(80) DEFAULT NULL,
  `event_name` varchar(120) DEFAULT NULL,
  `display_name` varchar(120) DEFAULT NULL,
  `group_name` varchar(120) DEFAULT NULL,
  `tag` varchar(120) DEFAULT NULL,
  `status` varchar(32) DEFAULT NULL,
  `description` text,
  `property_ids` text,
  `create_time` datetime(6) DEFAULT NULL,
  `update_time` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `i_meta_event_project` (`project_name`),
  KEY `i_meta_event_name` (`event_name`),
  KEY `i_meta_event_group` (`group_name`),
  KEY `i_meta_event_tag` (`tag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE IF NOT EXISTS `tbl_normal_property` (
  `id` varchar(36) NOT NULL,
  `project_name` varchar(80) DEFAULT NULL,
  `property_name` varchar(120) DEFAULT NULL,
  `display_name` varchar(120) DEFAULT NULL,
  `group_name` varchar(120) DEFAULT NULL,
  `tag` varchar(120) DEFAULT NULL,
  `data_type` varchar(32) DEFAULT NULL,
  `status` varchar(32) DEFAULT NULL,
  `common_property` bit(1) DEFAULT NULL,
  `description` text,
  `create_time` datetime(6) DEFAULT NULL,
  `update_time` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `i_normal_property_project` (`project_name`),
  KEY `i_normal_property_name` (`property_name`),
  KEY `i_normal_property_group` (`group_name`),
  KEY `i_normal_property_tag` (`tag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE IF NOT EXISTS `tbl_funnel` (
  `id` varchar(36) NOT NULL,
  `project_name` varchar(80) DEFAULT NULL,
  `name` varchar(120) DEFAULT NULL,
  `status` varchar(32) DEFAULT NULL,
  `measurement` varchar(16) DEFAULT NULL,
  `window_numeral` int DEFAULT NULL,
  `window_unit` varchar(16) DEFAULT NULL,
  `is_open_relation` bit(1) DEFAULT NULL,
  `description` text,
  `steps` text,
  `query_json` longtext,
  `create_time` datetime(6) DEFAULT NULL,
  `update_time` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `i_funnel_project` (`project_name`),
  KEY `i_funnel_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

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

CREATE TABLE IF NOT EXISTS `sys_operrecord` (
  `id` int NOT NULL,
  `action` varchar(4000) DEFAULT NULL,
  `opertime` datetime(6) DEFAULT NULL,
  `user` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;


CREATE TABLE IF NOT EXISTS `sys_user` (
  `user_id` varchar(36) NOT NULL COMMENT '主键',
  `user_name` varchar(255) DEFAULT NULL COMMENT '账号',
  `display_name` varchar(255) DEFAULT NULL COMMENT '显示名',
  `password` varchar(255) DEFAULT NULL COMMENT '密码',
  `createuser` varchar(255) DEFAULT NULL COMMENT '创建人',
  `createtime` datetime(6) DEFAULT NULL COMMENT '创建时间',
  `modifyuser` varchar(255) DEFAULT NULL COMMENT '修改人',
  `modifytime` datetime(6) DEFAULT NULL COMMENT '修改时间',
  `lastlogintime` datetime(6) DEFAULT NULL COMMENT '最新登录时间',
  PRIMARY KEY (`user_id`) USING BTREE,
  KEY `i_user_name` (`user_name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='用户表';


LOCK TABLES `sys_user` WRITE;
INSERT INTO `sys_user` VALUES ('00851690-cdc0-4702-a153-fea656d207a3','admin','管理员','$2a$10$6bdwCqDF348m1v1QsnteHuNalhKUEHBCJ8duZ1Yv8E1ur5fCYQfkS',NULL,'2024-05-10 15:22:46.000000',NULL,'2024-05-30 13:50:16.912000',NULL),('3dbb17ba-d9a8-46a7-a86d-e33aa972b8d4','test','test','$2a$10$MjKpyU.LZBlxq9oDK525vuWA.EQcNvoaljLTJSIsEAGlfslF/NwoC',NULL,'2024-06-12 11:11:57.420000',NULL,'2024-06-12 11:12:13.774000',NULL),('f57cfab7-5aaf-4b5e-96e2-706ae08c55c5','clklog','clklog','$2a$10$nUSndaWG9ky6KC75..Av.OmNIEeg2eEbx7jlwZOyQJaBQ6C7h6G3G',NULL,'2024-05-30 10:38:58.733000',NULL,'2024-05-30 10:39:41.359000',NULL);
UNLOCK TABLES;

CREATE TABLE IF NOT EXISTS `sys_userlogin` (
  `token` varchar(200) NOT NULL,
  `create_time` datetime(6) DEFAULT NULL,
  `user_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`token`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;
