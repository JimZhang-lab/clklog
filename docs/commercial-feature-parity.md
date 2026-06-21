# ClkLog 商业版功能对照

调研对象：`https://pro.clklog.com/`

调研日期：2026-06-21

本次以官方示例账号可见的菜单、页面结构、筛选条件、列表字段、弹窗和主要操作
为基准，并验证本地前端、Manage、MySQL、Analytics API 和 ClickHouse 数据链路。

## 覆盖结论

官方示例中可见的 44 个菜单与子菜单入口均已在本地路由中提供。原来跳转到官方
站点的“自定义 SQL 查询”和“数据汇总（移动端展示）”已经改为本地实现。

| 能力域 | 官方主要入口 | 本地状态 |
| --- | --- | --- |
| 数据概览与实时访问 | `#/index`、`#/realtime/access` | 已实现 |
| 数据汇总与趋势 | `#/record/summary`、趋势分析 | 已实现 |
| 移动端汇总 | `/mobile/` | 已实现为独立响应式页面 |
| 访客分析 | 新老访客、地域、来源、渠道、设备 | 已实现 |
| 访问分析 | 受访页、结构页、入口页、退出页、搜索词 | 已实现 |
| 用户分析 | 画像、活跃、忠诚、留存/流失、回流/沉默 | 已实现 |
| App 崩溃分析 | `#/crashAnalysis/crash` | 已实现并连接 ClickHouse |
| 事件分析 | 元事件、属性、日志、统计、书签、自定义分析 | 已实现 |
| 漏斗分析 | `#/mete/funnelAnalysis` | 已实现并连接 ClickHouse |
| CDP | 标签分类、用户标签、分群、画像、用户细查 | 已实现并持久化 MySQL |
| 系统设置 | 项目、全局设置、日志汇总 | 已实现 |
| 权限管理 | 账号、角色、菜单 | 已实现 |
| API 密钥 | `#/apiKey/manage` | 已实现完整生命周期 |
| 自定义 SQL | `/tabix/` | 已实现本地只读查询 |

## 用户分析

官方入口：

- `#/userbehavior/userBehavior`：用户画像。
- `#/userbehavior/activeUsers`：活跃用户分析。
- `#/userbehavior/loyaltyAnalysis`：忠诚度分析。
- `#/userbehavior/retainedUsers`：流失/留存用户。
- `#/userbehavior/silentUsers`：回流/沉默用户。

官方页面能力：

- 用户画像：按时间、渠道、访客类型和地域筛选，展示用户 ID、访客类型、浏览量、
  访问次数、平均访问页数、停留时长、上次访问时间和访问详情。
- 活跃用户分析：日活跃、周活跃和月活跃切换，近 7 天、近 14 天和近 30 天范围，
  展示活跃趋势和日期、用户总数、活跃数表格。
- 忠诚度分析：访问页数、访问深度、访问时长、上次访问时间和访问频次五个页签，
  每个页签包含分布图和占比表格。
- 流失/留存用户：按日、按周、按月，过去 7 天、过去 14 天和过去 30 天范围，
  支持流失与留存切换，展示留存矩阵和流失趋势。
- 回流/沉默用户：按日、按周、按月，近 7 天、近 14 天和近 30 天范围，展示
  累计用户数、回流用户数、沉默用户数、老用户数、新用户数和用户数。

本地实现：

- 用户画像保留 ClickHouse 用户列表与用户细查弹窗，并补齐官方“访问详情”列。
- 新增 Analytics API `/user/getUserActiveTrend`、`/user/getUserRemainTrend`、
  `/user/getUserChurnTrend`、`/user/getUserRevisitAndSilentTrend`。
- 活跃用户读取 `visitor_detail_bydate`，回流、沉默和流失读取 `visitor_life_bydate`，
  留存矩阵基于 `visitor_detail_byinfo` 的 `projectName + distinctId + stat_date`
  计算后续 1 到 7 天留存。
- 忠诚度前端接入 `/uservisit/getUserPv`、`getUserDepth`、`getUserVisitTime`、
  `getUserLatestTime` 和 `getUserVisit`。其中访问深度基于 `log_analysis`
  按会话统计去重页面数。
- 本地 ClickHouse seed 补充 `visitor_life_bydate` 演示数据，确保前端、API 和
  ClickHouse 联通后有可见结果。

## App 崩溃分析

官方入口：`#/crashAnalysis/crash`

官方页面能力：

- 今日、昨日、过去 7 天、过去 30 天和自定义时间范围。
- 按日、按周、按月粒度。
- 流量概览：访问次数、崩溃触发次数、崩溃率、访问用户数、崩溃触发用户数、
  崩溃触发用户数占比。
- 趋势图：崩溃率、iOS 应用版本崩溃率、Android 应用版本崩溃率，并支持选择
  崩溃触发次数、崩溃率、崩溃用户数等指标。
- 数据汇总页签：按应用版本与操作系统汇总设备型号数、访问次数、崩溃次数、
  崩溃率、访问用户数、崩溃触发用户数、崩溃触发用户占比和崩溃数占比。
- 崩溃日志页签：展示崩溃时间、应用版本、操作系统、操作系统版本、设备型号、
  崩溃详情信息和会话 ID。

本地实现：

- 本地入口：`/#/crashAnalysis/crash`。
- Analytics API：`/appCrashed/totalSummary`、`trendSummary`、
  `groupedSummary`、`getPagedSummary`、`getPage`。
- ClickHouse 表：`crashed_detail_bydate` 和 `log_analysis` 中的
  `AppCrashed` 原始事件。
- 主页面默认展示“数据汇总”页签，并可继续下钻查看设备型号汇总与崩溃日志。
- 崩溃详情弹窗保留崩溃时间、应用版本、操作系统、系统版本、设备型号和堆栈信息。

## 漏斗分析

官方页面能力：

- 今日、昨日、过去 7 天、过去 30 天和自定义时间范围。
- 按日、按周、按月粒度。
- 人数（UV）与次数（PV）两种计算方式。
- 多步骤事件与转化周期。
- 保存、编辑、删除漏斗和分析书签。
- 结果、趋势和明细展示。

本地实现：

- 漏斗定义与完整查询 JSON 持久化到 MySQL。
- 使用 ClickHouse `windowFunnel` 按事件时间顺序计算。
- 支持 UV/PV、转化周期、多步骤事件、趋势和明细。
- 元事件未配置时从 ClickHouse 原始事件回退生成事件选项。
- 漏斗、事件和用户查询均按 `projectName` 隔离。

## 用户细查

官方页面能力：

- 用户 ID、业务 ID、匿名 ID 和用户分群筛选。
- 用户列表与身份信息。
- 用户画像、访问概览、浏览历史和行为轨迹。
- 用户标签查看与维护。

本地实现：

- 本地入口：`/#/ups/userReview`，兼容 `/#/ups/userReview/list`。
- 页面提供“按ID筛选”和“按分群筛选”两种模式，分群读取 MySQL 中的 CDP
  group 资产，用户列表读取 ClickHouse 访客明细。
- 列表字段对齐官方：用户 ID（user_Id）、业务 ID（login_id）、匿名 ID
  （anonymous_id）、更新时间、入库时间和详情。
- 用户详情复用本地用户细查弹窗，包含基本信息、访问汇总、地域、设备、会话和
  页面历史。
- 详情查询使用 `projectName + distinctId`，避免跨项目串数据。
- 用户细查弹窗可查看、添加和移除标签。
- 标签赋值写入 MySQL，行为详情读取 ClickHouse。

## 标签与 CDP

官方页面能力：

- `#/ups/tagCategory`：多级标签分类，支持新建分类、分类显示名、顺序号和描述。
- `#/ups/userTag/list`：标签分类、数据类型、更新方式、标签状态、创建方式、
  最新版本计算时间筛选；表格展示标签状态、分类、创建方式、更新方式、标签类型、
  覆盖人数、最新版本计算状态、最新版本计算时间、创建人、创建时间和修改时间。
- `#/ups/userGroup/list`：更新方式、分群状态、最新版本计算时间和创建方式筛选；
  展示分群状态、创建方式、更新方式、覆盖人数、计算状态、计算时间、创建人和
  创建/修改时间。
- `#/ups/personalPortrait`：个人画像复用用户画像分析页，支持访客详情下钻。
- `#/ups/userportrait/list`：用户群画像管理，展示更新方式、最新版本计算时间、
  创建时间、更新时间和创建人。
- `#/ups/userReview/list`：用户 ID、业务 ID、匿名 ID 和分群筛选，支持查看用户详情。

本地实现：

- 标签分类的新增、编辑、删除、排序和启停按 `projectName` 隔离，删除前校验同项目
  子分类与用户标签引用。
- 用户标签页面补齐官方筛选项，支持标签定义、规则 JSON、状态、创建方式、更新
  方式和数据类型持久化。
- 用户标签支持单用户和批量用户赋值，覆盖人数自动维护；详情抽屉展示覆盖用户、
  标签值分布和标签配置，可移除单个用户标签。
- 用户分群和用户群画像共用 CDP 资产模型，支持服务端分页、关键字、更新方式、
  创建方式、状态和最新版本计算时间筛选。
- CDP 资产详情展示覆盖用户、标签画像和规则配置；新增/编辑时可维护状态、创建人、
  更新方式、规则 JSON、描述和覆盖用户。
- 本地兼容官方 `/list` 路径：`/#/ups/userTag/list`、`/#/ups/userGroup/list`、
  `/#/ups/userportrait/list`、`/#/ups/userReview/list`，并且不再跳转官方站点。
- Manage API 增强：`/tag/getPageList` 支持 `dataType` 和最新计算时间筛选；
  `/tag/getUserPageList` 与 `/tag/getUserDistributeStats` 用于覆盖用户和标签值分布；
  `/cdp/getPageList` 支持更新方式、创建方式、计算状态和最新计算时间筛选。

## API 密钥

官方页面字段：

- 显示名称。
- 脱敏 API 密钥。
- 状态。
- 创建时间。
- 过期时间。
- 新建、编辑和删除。

本地实现：

- MySQL 表：`tbl_api_key`。
- Manage API：`/apikey/list`、`get`、`add`、`edit`、`delete`。
- 所有操作携带 `projectName`。
- 数据库仅保存前缀、脱敏值和 SHA-256 摘要。
- 完整密钥仅在创建成功响应中返回一次。

## 自定义 SQL

官方示例原入口指向 Tabix。当前本地入口为 `/#/tabix/query`：

- 提供 SQL 编辑器、返回行数、执行、重置和动态结果表。
- 查询直接读取本地 ClickHouse。
- 仅允许 `SELECT`、`SHOW`、`DESCRIBE`、`DESC`、`EXPLAIN`。
- 禁止多语句和写操作。
- 最大返回 1000 行。
- 返回字段、行数、耗时和实际执行 SQL。

## 移动端汇总

本地入口：`/#/mobileSummary/index`

- 独立于桌面 Layout，不加载桌面侧栏。
- 支持日、周、月和自定义日期范围。
- 展示 PV、访问次数、UV、新用户数、IP 和跳出率。
- 读取与 Web 数据汇总相同的 Analytics API 与 ClickHouse 数据。
- 已在 390 x 844 视口验证无横向溢出。

## 权限与系统能力

- 账号、角色和菜单配置持久化到 MySQL。
- 角色维护菜单与接口权限集合。
- 书签、漏斗、标签、CDP 和 API 密钥均为真实持久化数据。
- 系统日志汇总和项目配置使用 Manage API。
- 本地路由不再依赖 `pro.clklog.com` 的功能页面。

## 联调验收

本次完成的实际验证：

- Manage 与 Analytics API Maven 编译通过。
- 前端开发构建成功。
- 本轮前端文件定向 ESLint 通过。
- 用户分析 5 个菜单逐页对照官方示例并在浏览器验证无控制台错误。
- 用户分析新增 API 使用本地 token 验证通过，并返回 ClickHouse 种子数据。
- CDP 6 个菜单逐页对照官方示例并在浏览器验证无本地控制台错误：
  标签分类、用户标签、用户分群、用户画像、用户群画像、用户细查。
- CDP `/list` 兼容路由已验证：用户标签、用户分群、用户群画像、用户细查。
- CDP Manage 接口使用本地 token 验证通过：标签列表筛选、标签覆盖用户、标签值
  分布和分群列表筛选。
- App 崩溃分析主页面补齐官方“数据汇总”页签，并通过定向 ESLint。
- API 密钥完成列表、新建、一次性回显、编辑和删除。
- 自定义 SQL 返回 ClickHouse 事件统计。
- `DROP TABLE` 等写操作被服务端拒绝。
- 移动端汇总显示本地种子数据，并通过窄屏检查。

官方示例没有向体验账号公开的授权后台、计费、集群管理等内部能力不属于可见
页面对照范围。
