SET NAMES utf8mb4;

USE clklog;

INSERT INTO `tbl_meta_event`
(`id`, `project_name`, `event_name`, `display_name`, `group_name`, `tag`, `status`, `description`, `property_ids`, `create_time`, `update_time`)
VALUES
  ('10000000-0000-0000-0000-000000000001', 'clklogapp', '$pageview', '页面浏览', '页面事件', '访问', 'enabled', '页面浏览事件', NULL, NOW(), NOW()),
  ('10000000-0000-0000-0000-000000000002', 'clklogapp', 'signup', '完成注册', '转化事件', '转化', 'enabled', '用户完成注册', NULL, NOW(), NOW()),
  ('10000000-0000-0000-0000-000000000003', 'clklogapp', 'add_cart', '加入购物车', '转化事件', '转化', 'enabled', '用户加入购物车', NULL, NOW(), NOW()),
  ('10000000-0000-0000-0000-000000000004', 'clklogapp', 'purchase', '支付成功', '转化事件', '核心', 'enabled', '用户支付成功', NULL, NOW(), NOW());

INSERT INTO `tbl_normal_property`
(`id`, `project_name`, `property_name`, `display_name`, `group_name`, `tag`, `data_type`, `status`, `common_property`, `description`, `create_time`, `update_time`)
VALUES
  ('20000000-0000-0000-0000-000000000001', 'clklogapp', 'country', '国家', '通用属性', '地域', 'string', 'enabled', 1, '访问国家', NOW(), NOW()),
  ('20000000-0000-0000-0000-000000000002', 'clklogapp', 'province', '省份', '通用属性', '地域', 'string', 'enabled', 1, '访问省份', NOW(), NOW()),
  ('20000000-0000-0000-0000-000000000003', 'clklogapp', 'manufacturer', '设备厂商', '设备属性', '设备', 'string', 'enabled', 1, '设备制造商', NOW(), NOW());

INSERT INTO `tbl_tag_category`
(`id`, `project_name`, `parent_id`, `display_name`, `sort_order`, `status`, `description`, `create_time`, `update_time`)
VALUES
  ('30000000-0000-0000-0000-000000000001', 'clklogapp', '', '用户价值', 1, 'enabled', '用户价值类标签', NOW(), NOW()),
  ('30000000-0000-0000-0000-000000000002', 'clklogapp', '', '生命周期', 2, 'enabled', '用户生命周期标签', NOW(), NOW());

INSERT INTO `tbl_user_tag`
(`id`, `project_name`, `category_id`, `display_name`, `tag_key`, `data_type`, `create_type`, `update_mode`, `status`, `description`, `rule_json`, `match_user_count`, `last_execute_status`, `last_execute_time`, `create_time`, `update_time`)
VALUES
  ('40000000-0000-0000-0000-000000000001', 'clklogapp', '30000000-0000-0000-0000-000000000001', '价值等级', 'user_tag_value_level', 'string', 'custom', 'manual', 'enabled', '本地联调示例标签', '{}', 1, 'success', NOW(), NOW(), NOW()),
  ('40000000-0000-0000-0000-000000000002', 'clklogapp', '30000000-0000-0000-0000-000000000002', '新老用户', 'user_tag_lifecycle', 'string', 'custom', 'manual', 'enabled', '本地联调示例标签', '{}', 1, 'success', NOW(), NOW(), NOW());

INSERT INTO `tbl_user_tag_assignment`
(`id`, `project_name`, `tag_id`, `distinct_id`, `tag_value`, `create_time`, `update_time`)
VALUES
  ('50000000-0000-0000-0000-000000000001', 'clklogapp', '40000000-0000-0000-0000-000000000001', 'demo-user-001', '高价值', NOW(), NOW()),
  ('50000000-0000-0000-0000-000000000002', 'clklogapp', '40000000-0000-0000-0000-000000000002', 'demo-user-001', '新用户', NOW(), NOW());

INSERT INTO `tbl_bookmark`
(`id`, `project_name`, `name`, `analysis_type`, `status`, `description`, `query_json`, `create_user`, `create_time`, `update_time`)
VALUES
  ('60000000-0000-0000-0000-000000000001', 'clklogapp', '支付转化漏斗', 'funnelAnalysis', 'enabled', '商业版示例书签', '{"route":"/mete/funnelAnalysis"}', 'clklog', NOW(), NOW()),
  ('60000000-0000-0000-0000-000000000002', 'clklogapp', '高价值用户画像', 'userPortrait', 'enabled', '商业版示例书签', '{"route":"/ups/userportrait"}', 'clklog', NOW(), NOW());

INSERT INTO `tbl_cdp_asset`
(`id`, `project_name`, `asset_type`, `display_name`, `asset_key`, `status`, `create_type`, `update_mode`, `distinct_ids`, `rule_json`, `description`, `match_user_count`, `last_execute_status`, `last_execute_time`, `create_user`, `create_time`, `update_time`)
VALUES
  ('70000000-0000-0000-0000-000000000001', 'clklogapp', 'group', '高价值用户', 'group_high_value_users', 'enabled', 'custom', 'manual', 'demo-user-001', '{}', '商业版示例用户分群', 1, 'success', NOW(), 'clklog', NOW(), NOW()),
  ('70000000-0000-0000-0000-000000000002', 'clklogapp', 'portrait', '高价值用户画像', 'portrait_high_value_users', 'enabled', 'custom', 'manual', 'demo-user-001', '{}', '商业版示例群画像', 1, 'success', NOW(), 'clklog', NOW(), NOW());

INSERT INTO `tbl_role`
(`id`, `role_name`, `display_name`, `role_type`, `status`, `sort_order`, `create_time`, `update_time`)
VALUES
  ('80000000-0000-0000-0000-000000000001', 'sys_admin', '系统管理员', '系统管理员', 'enabled', 1, NOW(), NOW()),
  ('80000000-0000-0000-0000-000000000002', 'project_admin', '项目管理员', '项目管理员', 'enabled', 2, NOW(), NOW()),
  ('80000000-0000-0000-0000-000000000003', 'metadata_manager', '元数据管理', '元数据管理', 'enabled', 3, NOW(), NOW()),
  ('80000000-0000-0000-0000-000000000004', 'data_viewer', '数据查看', '数据查看', 'enabled', 4, NOW(), NOW());

INSERT INTO `tbl_menu`
(`id`, `title`, `path`, `component`, `permissions`, `roles`, `status`, `sort_order`, `hidden`, `external_window`, `create_time`, `update_time`)
VALUES
  ('90000000-0000-0000-0000-000000000001', '数据概览', '/index', 'views/home/index', '/index', '数据查看,项目管理员,系统管理员', 'enabled', 10, 0, 0, NOW(), NOW()),
  ('90000000-0000-0000-0000-000000000002', '实时访问', '/realtime/access', 'views/commercial/RealtimeAccess', '/visitor/getLogAnalysisList', '数据查看,项目管理员,系统管理员', 'enabled', 20, 0, 0, NOW(), NOW()),
  ('90000000-0000-0000-0000-000000000003', '事件分析', '/mete/eventAnalysis', 'views/commercial/EventWorkbench', '/meta/event/getPageList,/normal/property/getPageList', '元数据管理,数据查看,项目管理员,系统管理员', 'enabled', 30, 0, 0, NOW(), NOW()),
  ('90000000-0000-0000-0000-000000000004', '漏斗分析', '/mete/funnelAnalysis', 'views/user-behavior/funnelAnalysis', '/funnel/getPageList', '数据查看,项目管理员,系统管理员', 'enabled', 40, 0, 0, NOW(), NOW()),
  ('90000000-0000-0000-0000-000000000005', '用户画像管理(CDP）', '/ups/userGroup', 'views/commercial/CdpList', '/cdp/getPageList,/tag/getPageList', '项目管理员,系统管理员', 'enabled', 50, 0, 0, NOW(), NOW()),
  ('90000000-0000-0000-0000-000000000006', '账号管理', '/authManage/user', 'views/auth-manage/User', '/user/getlistpage,/user/add,/user/edit,/user/delete', '系统管理员', 'enabled', 60, 0, 0, NOW(), NOW());

INSERT INTO `tbl_api_key`
(`id`, `project_name`, `display_name`, `key_prefix`, `key_mask`, `key_hash`, `status`, `create_user`, `expires_at`, `created_at`, `updated_at`)
VALUES
  ('a0000000-0000-0000-0000-000000000001', 'clklogapp', '本地联调密钥', 'clk_local_', 'clk_lo************************demo', '5f70bf18a0860070169f951c68a6b06cbfe7b0604e2a134861b65848bd40f1c8', '启用', 'clklog', NULL, NOW(), NOW());
