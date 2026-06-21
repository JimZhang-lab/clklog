INSERT INTO log_analysis
(
  project_name, project_token, client_ip, distinct_id, log_time, stat_date,
  stat_hour, event, lib, is_first_day, country, province, city, manufacturer,
  event_session_id, raw_url, url, url_path, title, latest_referrer_host,
  latest_search_keyword, create_time
)
VALUES
  ('clklogapp', 'ddf51db3-7c99-1310-a44f-79feb7b63c69', '127.0.0.11', 'demo-user-001', now() - INTERVAL 120 MINUTE, today(), formatDateTime(now() - INTERVAL 120 MINUTE, '%H'), '$pageview', 'js', 'true', '中国', '上海', '上海', 'Apple', 'session-001', 'http://localhost/products', 'http://localhost/products', '/products', '产品列表', '直接访问', '', now()),
  ('clklogapp', 'ddf51db3-7c99-1310-a44f-79feb7b63c69', '127.0.0.11', 'demo-user-001', now() - INTERVAL 100 MINUTE, today(), formatDateTime(now() - INTERVAL 100 MINUTE, '%H'), 'signup', 'js', 'true', '中国', '上海', '上海', 'Apple', 'session-001', 'http://localhost/signup', 'http://localhost/signup', '/signup', '注册', '直接访问', '', now()),
  ('clklogapp', 'ddf51db3-7c99-1310-a44f-79feb7b63c69', '127.0.0.11', 'demo-user-001', now() - INTERVAL 70 MINUTE, today(), formatDateTime(now() - INTERVAL 70 MINUTE, '%H'), 'add_cart', 'js', 'true', '中国', '上海', '上海', 'Apple', 'session-001', 'http://localhost/cart', 'http://localhost/cart', '/cart', '购物车', '直接访问', '', now()),
  ('clklogapp', 'ddf51db3-7c99-1310-a44f-79feb7b63c69', '127.0.0.11', 'demo-user-001', now() - INTERVAL 30 MINUTE, today(), formatDateTime(now() - INTERVAL 30 MINUTE, '%H'), 'purchase', 'js', 'true', '中国', '上海', '上海', 'Apple', 'session-001', 'http://localhost/order/success', 'http://localhost/order/success', '/order/success', '支付成功', '直接访问', '', now()),
  ('clklogapp', 'ddf51db3-7c99-1310-a44f-79feb7b63c69', '127.0.0.12', 'demo-user-002', now() - INTERVAL 110 MINUTE, today(), formatDateTime(now() - INTERVAL 110 MINUTE, '%H'), '$pageview', 'js', 'true', '中国', '北京', '北京', 'Huawei', 'session-002', 'http://localhost/products', 'http://localhost/products', '/products', '产品列表', '直接访问', '', now()),
  ('clklogapp', 'ddf51db3-7c99-1310-a44f-79feb7b63c69', '127.0.0.12', 'demo-user-002', now() - INTERVAL 90 MINUTE, today(), formatDateTime(now() - INTERVAL 90 MINUTE, '%H'), 'signup', 'js', 'true', '中国', '北京', '北京', 'Huawei', 'session-002', 'http://localhost/signup', 'http://localhost/signup', '/signup', '注册', '直接访问', '', now()),
  ('clklogapp', 'ddf51db3-7c99-1310-a44f-79feb7b63c69', '127.0.0.12', 'demo-user-002', now() - INTERVAL 50 MINUTE, today(), formatDateTime(now() - INTERVAL 50 MINUTE, '%H'), 'add_cart', 'js', 'true', '中国', '北京', '北京', 'Huawei', 'session-002', 'http://localhost/cart', 'http://localhost/cart', '/cart', '购物车', '直接访问', '', now()),
  ('clklogapp', 'ddf51db3-7c99-1310-a44f-79feb7b63c69', '127.0.0.13', 'demo-user-003', now() - INTERVAL 45 MINUTE, today(), formatDateTime(now() - INTERVAL 45 MINUTE, '%H'), '$pageview', 'js', 'false', '中国', '广东', '深圳', 'Xiaomi', 'session-003', 'http://localhost/', 'http://localhost/', '/', '首页', '直接访问', '', now());

INSERT INTO flow_trend_bydate
(
  stat_date, lib, project_name, is_first_day, country, province, pv,
  visit_count, uv, new_uv, ip_count, visit_time, bounce_count, update_time
)
VALUES
  (today(), 'all', 'clklogapp', 'all', 'all', 'all', 8, 3, 3, 2, 3, 8400, 1, now()),
  (today() - 1, 'all', 'clklogapp', 'all', 'all', 'all', 4, 2, 2, 1, 2, 3600, 1, now());

INSERT INTO flow_trend_byhour
(
  stat_date, stat_hour, lib, project_name, is_first_day, country, province,
  pv, visit_count, uv, new_uv, ip_count, visit_time, bounce_count, update_time
)
VALUES
  (today(), formatDateTime(now() - INTERVAL 2 HOUR, '%H'), 'all', 'clklogapp', 'all', 'all', 'all', 3, 2, 2, 2, 2, 3600, 0, now()),
  (today(), formatDateTime(now() - INTERVAL 1 HOUR, '%H'), 'all', 'clklogapp', 'all', 'all', 'all', 3, 1, 2, 1, 2, 3000, 0, now()),
  (today(), formatDateTime(now(), '%H'), 'all', 'clklogapp', 'all', 'all', 'all', 2, 1, 1, 0, 1, 1800, 1, now());

INSERT INTO visitor_detail_bydate
(
  stat_date, lib, project_name, is_first_day, country, province, pv,
  visit_count, uv, new_uv, ip_count, visit_time, bounce_count, update_time
)
VALUES
  (today(), 'all', 'clklogapp', 'true', 'all', 'all', 7, 2, 2, 2, 2, 7200, 0, now()),
  (today(), 'all', 'clklogapp', 'false', 'all', 'all', 1, 1, 1, 0, 1, 1200, 1, now());

INSERT INTO visituri_summary_bydate
(stat_date, lib, project_name, uri, title, pv, update_time)
VALUES
  (today(), 'all', 'clklogapp', 'all', 'all', 8, now()),
  (today(), 'all', 'clklogapp', 'http://localhost/products', '产品列表', 2, now()),
  (today(), 'all', 'clklogapp', 'http://localhost/signup', '注册', 2, now()),
  (today(), 'all', 'clklogapp', 'http://localhost/cart', '购物车', 2, now()),
  (today(), 'all', 'clklogapp', 'http://localhost/order/success', '支付成功', 1, now()),
  (today(), 'all', 'clklogapp', 'http://localhost/', '首页', 1, now());

INSERT INTO sourcesite_detail_bydate
(
  stat_date, lib, project_name, is_first_day, country, province, sourcesite,
  pv, visit_count, uv, new_uv, ip_count, visit_time, bounce_count, update_time
)
VALUES
  (today(), 'all', 'clklogapp', 'all', 'all', 'all', 'all', 8, 3, 3, 2, 3, 8400, 1, now()),
  (today(), 'all', 'clklogapp', 'all', 'all', 'all', '直接访问', 8, 3, 3, 2, 3, 8400, 1, now());

INSERT INTO searchword_detail_bydate
(
  stat_date, lib, project_name, is_first_day, country, province, searchword,
  pv, visit_count, uv, new_uv, ip_count, visit_time, bounce_count, update_time
)
VALUES
  (today(), 'all', 'clklogapp', 'all', 'all', 'all', 'all', 0, 0, 0, 0, 0, 0, 0, now());

INSERT INTO area_detail_bydate
(
  stat_date, lib, project_name, is_first_day, country, province, city, pv,
  visit_count, uv, new_uv, ip_count, visit_time, bounce_count, update_time
)
VALUES
  (today(), 'all', 'clklogapp', 'all', 'all', 'all', 'all', 8, 3, 3, 2, 3, 8400, 1, now()),
  (today(), 'all', 'clklogapp', 'all', '上海', '上海', '上海', 4, 1, 1, 1, 1, 4200, 0, now()),
  (today(), 'all', 'clklogapp', 'all', '北京', '北京', '北京', 3, 1, 1, 1, 1, 3000, 0, now()),
  (today(), 'all', 'clklogapp', 'all', '广东', '广东', '深圳', 1, 1, 1, 0, 1, 1200, 1, now());

INSERT INTO visitor_detail_byinfo
(
  stat_date, lib, project_name, is_first_day, country, province, city,
  distinct_id, client_ip, manufacturer, latest_time, first_time, visit_time,
  visit_count, pv, update_time
)
VALUES
  (today(), 'js', 'clklogapp', 'true', '中国', '上海', '上海', 'demo-user-001', '127.0.0.11', 'Apple', now() - INTERVAL 30 MINUTE, now() - INTERVAL 120 MINUTE, 5400, 1, 4, now()),
  (today(), 'js', 'clklogapp', 'true', '中国', '北京', '北京', 'demo-user-002', '127.0.0.12', 'Huawei', now() - INTERVAL 50 MINUTE, now() - INTERVAL 110 MINUTE, 3600, 1, 3, now()),
  (today(), 'js', 'clklogapp', 'false', '中国', '广东', '深圳', 'demo-user-003', '127.0.0.13', 'Xiaomi', now() - INTERVAL 45 MINUTE, now() - INTERVAL 45 MINUTE, 1200, 1, 1, now());

INSERT INTO visitor_detail_bysession
(
  stat_date, project_name, country, province, client_ip, sourcesite,
  searchword, distinct_id, event_session_id, first_time, latest_time,
  visit_time, pv, update_time
)
VALUES
  (today(), 'clklogapp', '中国', '上海', '127.0.0.11', '直接访问', '', 'demo-user-001', 'session-001', now() - INTERVAL 120 MINUTE, now() - INTERVAL 30 MINUTE, 5400, 4, now()),
  (today(), 'clklogapp', '中国', '北京', '127.0.0.12', '直接访问', '', 'demo-user-002', 'session-002', now() - INTERVAL 110 MINUTE, now() - INTERVAL 50 MINUTE, 3600, 3, now()),
  (today(), 'clklogapp', '中国', '广东', '127.0.0.13', '直接访问', '', 'demo-user-003', 'session-003', now() - INTERVAL 45 MINUTE, now() - INTERVAL 45 MINUTE, 1200, 1, now());
