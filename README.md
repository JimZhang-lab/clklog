# ClkLog

ClkLog 是一套可私有化部署的用户行为分析系统。本仓库采用单仓库目录结构，
统一管理前端、管理服务、分析服务、采集和处理模块，并补齐了专业版与企业版
常用的分析、用户画像、权限和运维能力。

## 已实现能力

- 数据概览、实时访问、数据汇总、趋势分析和移动端汇总。
- 新老访客、地域、来源、渠道、设备、受访页面、入口页和退出页分析。
- 活跃、忠诚度、留存/流失、回流/沉默用户分析。
- 元事件、事件属性、用户属性、日志查询、数据统计和自定义分析。
- 漏斗配置、UV/PV 计算、转化周期、多步骤分析、趋势和明细。
- 标签分类、用户标签、标签赋值、用户分群、用户画像和用户细查。
- 我的书签、项目配置、全局设置、系统日志汇总。
- 账号、角色、菜单和接口权限管理。
- API 密钥生命周期管理，完整密钥只在创建成功时返回一次。
- 本地只读 ClickHouse SQL 查询，限制单语句、写操作和最大返回行数。

商业版页面与本地路由的详细对照见
[docs/commercial-feature-parity.md](docs/commercial-feature-parity.md)。

## 仓库结构

所有业务模块都是当前仓库直接跟踪的普通目录：

| 目录 | 作用 | 主要数据源 |
| --- | --- | --- |
| `ui` | Vue 2 + Element UI 前端 | Manage API、Analytics API |
| `manage` | 登录、权限、项目、元数据、标签、分群、密钥 | MySQL、Redis |
| `api` | 访问、事件、漏斗、用户和自定义 SQL 分析 | ClickHouse、Redis |
| `init` | MySQL/ClickHouse 初始化与升级脚本 | MySQL、ClickHouse |
| `receiver` | 埋点事件接收 | Kafka |
| `processing` | Flink 清洗与处理任务 | Kafka、ClickHouse |
| `deploy/local` | 本地联调种子数据和初始化脚本 | Docker Compose |

首次拉取只需要克隆当前仓库：

```bash
git clone https://github.com/JimZhang-lab/clklog.git
cd clklog
```

后续更新直接在仓库根目录执行：

```bash
git pull --rebase
```

`ui`、`api`、`manage`、`init`、`receiver` 和 `processing` 不再是独立 Git
仓库，不需要也不应在这些目录中单独执行拉取、切分支或提交操作。

## 数据链路

```text
Web / App / 小程序 SDK
          |
          v
       Receiver
          |
          v
        Kafka
          |
          v
   Processing / Flink
          |
          v
      ClickHouse <------ Analytics API
                              |
                              v
MySQL <------ Manage API <--- UI ---> Redis
```

MySQL 保存账号、权限、项目、元数据、漏斗配置、标签、分群、书签和 API
密钥。ClickHouse 保存原始事件及分析聚合数据。任何用户或元数据操作都必须携带
`projectName`。

## 本地运行

### 环境要求

- Docker Desktop 或兼容的 Docker Compose 环境。
- Java 8 兼容源码环境；本地可使用较新 JDK 运行 Maven。
- Maven 3。
- Mac 本地开发使用 Node.js 与 Yarn 1.x。
- GitHub Actions 使用 Node 16、npm 和提交到仓库的 `ui/package-lock.json`。

### 1. 启动依赖

```bash
docker compose -f compose.dependencies.yml up -d
docker compose -f compose.dependencies.yml ps
```

Compose 会启动并初始化：

| 服务 | 本地地址 |
| --- | --- |
| MySQL | `127.0.0.1:13306` |
| ClickHouse HTTP | `127.0.0.1:18123` |
| ClickHouse Native | `127.0.0.1:19000` |
| Redis | `127.0.0.1:16379` |

首次启动会执行：

- `init/scripts_init/mysql/mysql_clklog.sql`
- `deploy/local/mysql/seed.sql`
- `init/scripts/init.sql`
- `deploy/local/clickhouse/seed.sql`

重新创建本地数据：

```bash
docker compose -f compose.dependencies.yml down -v
docker compose -f compose.dependencies.yml up -d
```

`down -v` 会删除本地容器数据，不要在生产环境执行。

### 2. 启动 Manage

```bash
cd manage
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

地址：`http://127.0.0.1:8080`

### 3. 启动 Analytics API

```bash
cd api
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

地址：`http://127.0.0.1:8081`

### 4. 启动前端

```bash
cd ui
yarn install
NODE_OPTIONS=--openssl-legacy-provider yarn dev --host 127.0.0.1
```

访问：`http://127.0.0.1:9527`

本地体验账号：

```text
username: clklog
password: clklog
```

前端开发代理默认连接 `8080` 和 `8081`。需要覆盖时可设置
`CLKLOG_MANAGE_TARGET` 和 `CLKLOG_API_TARGET`。

## 重点入口

| 功能 | 本地路由 |
| --- | --- |
| 数据汇总 | `/#/record/summary` |
| 移动端汇总 | `/#/mobileSummary/index` |
| 漏斗分析 | `/#/mete/funnelAnalysis` |
| App 崩溃分析 | `/#/crashAnalysis/crash` |
| 用户画像 | `/#/userbehavior/userBehavior` |
| 活跃用户分析 | `/#/userbehavior/activeUsers` |
| 忠诚度分析 | `/#/userbehavior/loyaltyAnalysis` |
| 流失/留存用户 | `/#/userbehavior/retainedUsers` |
| 回流/沉默用户 | `/#/userbehavior/silentUsers` |
| 用户细查 | `/#/ups/userReview` |
| 标签分类 | `/#/ups/tagCategory` |
| 用户标签 | `/#/ups/userTag` |
| 用户分群 | `/#/ups/userGroup` |
| 自定义 SQL | `/#/tabix/query` |
| API 密钥 | `/#/apiKey/manage` |

移动端汇总是独立页面，不加载桌面侧栏，可直接在手机浏览器访问。

## 新增接口约束

### API 密钥

Manage API：

```text
POST /apikey/list
POST /apikey/get
POST /apikey/add
POST /apikey/edit
POST /apikey/delete
```

所有请求必须携带 `projectName`。数据库只保存密钥前缀、脱敏值和 SHA-256
摘要；完整密钥只在 `/apikey/add` 成功响应中出现一次。

### 自定义 SQL

Analytics API：

```text
POST /customsql/query
```

请求必须携带 `projectName`、`sql` 和可选的 `pageSize`。服务端仅允许
`SELECT`、`SHOW`、`DESCRIBE`、`DESC`、`EXPLAIN`，拒绝多语句和数据写入，
并将返回行数限制在 1 到 1000。

## 数据库升级

已有本地或部署环境需要执行升级脚本：

```bash
docker exec -i clklog-local-mysql \
  mysql --default-character-set=utf8mb4 -uroot -p123456 clklog \
  < init/scripts_init/mysql/upgrade_analytics_features.sql
```

升级脚本包含漏斗、标签、书签、CDP、角色、菜单和 API 密钥等表结构。新增
数据库变更时，必须同时更新 fresh-install SQL 与 upgrade SQL。

## 轻量验证

```bash
cd manage && mvn -q -DskipTests compile
cd api && mvn -q -DskipTests compile
cd ui && yarn eslint \
  src/views/commercial/CustomSqlQuery.vue \
  src/views/commercial/MobileSummary.vue \
  src/views/sys-manage/api-key.vue
```

健康检查：

```bash
curl http://127.0.0.1:8080/actuator/health
curl http://127.0.0.1:8081/actuator/health
curl http://127.0.0.1:18123/ping
docker exec clklog-local-mysql mysqladmin ping -uroot -p123456
docker exec clklog-local-redis redis-cli ping
```

前端全仓库仍有历史 ESLint 基线问题，迭代时优先对本次改动文件执行定向
ESLint，并确认开发构建成功。

根目录 `.github/workflows/ci.yml` 会分别编译 `api`、`manage`、`init`、
`receiver`、`processing`，并使用 Node 16 + `npm ci` 构建 `ui`。Node 18
与旧依赖 `@achrinza/node-ipc@9.2.2` 的 engine 范围不兼容。Mac 本地开发
仍统一使用 Yarn。模块目录中的工作流不会单独运行或保存发布凭据。

## 开发约定

- 分支按变更类型使用 `feature/`、`fix/`、`chore/`、`docs/`、
  `refactor/` 或 `test/`，不使用工具或助手专属前缀。
- Mac 本地前端命令统一使用 Yarn；CI 允许使用 npm。
- Java 源码保持 Java 8 兼容。
- 不提交 `.DS_Store`、构建产物、日志、IDE 文件和本地数据卷。
- 所有模块共享当前仓库的分支、索引和提交，避免在模块目录重新初始化 Git。
- 涉及用户、事件、漏斗、标签、分群或元数据的请求必须按项目隔离。
- 跨服务修改至少验证一次 UI、Manage、MySQL、API 与 ClickHouse 数据链路。

完整协作规则见 [AGENTS.md](AGENTS.md)，其他编码助手同时遵循
[CLAUDE.md](CLAUDE.md)。

## License

社区版遵循 AGPL-3.0。闭源商业集成、商业授权和商标使用请以 ClkLog
官方授权条款为准。
