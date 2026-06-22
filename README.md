# ClkLog

ClkLog 是一套可私有化部署的用户行为分析系统。本仓库采用单仓库目录结构，
统一管理前端、管理服务、分析服务、采集、初始化和处理模块，并补齐了专业版与
企业版常用的分析、用户画像、权限和运维能力。

商业版页面与本地路由的详细对照见
[docs/commercial-feature-parity.md](docs/commercial-feature-parity.md)。

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

## 仓库结构

所有业务模块都是当前仓库直接跟踪的普通目录：

| 目录 | 作用 | 主要数据源 |
| --- | --- | --- |
| `ui` | Vue 2 + Element UI 前端 | Manage API、Analytics API |
| `manage` | 登录、权限、项目、元数据、标签、分群、密钥 | MySQL、Redis |
| `api` | 访问、事件、漏斗、用户和自定义 SQL 分析 | ClickHouse、Redis |
| `init` | ClickHouse 初始化和周期任务服务 | ClickHouse |
| `receiver` | 埋点事件接收 | Kafka、Redis、ClickHouse |
| `processing` | Flink 清洗与处理任务 | Kafka、ClickHouse |
| `deploy/local` | 本地联调种子数据和初始化脚本 | Docker Compose |

首次拉取：

```bash
git clone https://github.com/JimZhang-lab/clklog.git
cd clklog
```

后续更新：

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

## 环境要求

本地开发建议：

| 组件 | 建议版本 | 说明 |
| --- | --- | --- |
| Docker | Docker Desktop 或兼容 Docker Compose | 启动 MySQL、ClickHouse、Redis |
| JDK | Java 8 兼容源码；本地可用 JDK 17 运行 Maven | 代码保持 Java 8 兼容 |
| Maven | 3.6+ | 构建 Java 模块 |
| Node.js | 推荐 Node 16 | 旧前端依赖不兼容 Node 18 的 Yarn engine 校验 |
| Yarn | 1.x | Mac 本地前端开发统一使用 Yarn |

GitHub Actions 使用 Node 16、`npm ci` 和提交到仓库的 `ui/package-lock.json`。
Mac 本地开发使用 Yarn；如果只能使用 Node 18，可先执行
`yarn install --ignore-engines`，并在运行或打包前设置
`NODE_OPTIONS=--openssl-legacy-provider`。

## 本地启动

### 1. 启动依赖服务

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

### 2. 启动 Manage API

```bash
cd manage
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

本地地址：`http://127.0.0.1:8080`

### 3. 启动 Analytics API

```bash
cd api
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

本地地址：`http://127.0.0.1:8081`

### 4. 启动前端

推荐 Node 16：

```bash
cd ui
yarn install --frozen-lockfile
yarn dev --host 127.0.0.1
```

Node 18 环境：

```bash
cd ui
yarn install --ignore-engines
NODE_OPTIONS=--openssl-legacy-provider yarn dev --host 127.0.0.1
```

访问：`http://127.0.0.1:9527`

本地体验账号：

```text
username: clklog
password: clklog
```

前端开发代理默认连接 `8080` 和 `8081`。需要覆盖时可设置：

```bash
export CLKLOG_MANAGE_TARGET=http://127.0.0.1:8080
export CLKLOG_API_TARGET=http://127.0.0.1:8081
```

## 常用入口

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
| 标签分类 | `/#/ups/tagCategory` |
| 用户标签 | `/#/ups/userTag`、`/#/ups/userTag/list` |
| 用户分群 | `/#/ups/userGroup`、`/#/ups/userGroup/list` |
| 用户画像（CDP） | `/#/ups/personalPortrait` |
| 用户群画像 | `/#/ups/userportrait`、`/#/ups/userportrait/list` |
| 用户细查 | `/#/ups/userReview`、`/#/ups/userReview/list` |
| 自定义 SQL | `/#/tabix/query` |
| API 密钥 | `/#/apiKey/manage` |

移动端汇总是独立页面，不加载桌面侧栏，可直接在手机浏览器访问。CDP 下的
`/list` 路径兼容官方商业版示例的跳转习惯，全部落到本地实现，不跳转
`pro.clklog.com`。

## 本地健康检查

```bash
curl http://127.0.0.1:8080/actuator/health
curl http://127.0.0.1:8081/actuator/health
curl http://127.0.0.1:18123/ping
docker exec clklog-local-mysql mysqladmin ping -uroot -p123456
docker exec clklog-local-redis redis-cli ping
```

## 打包

本仓库没有根级 Maven 聚合工程，各模块从模块目录分别打包。

### Java 模块打包

```bash
cd manage && mvn clean package -DskipTests
cd ../api && mvn clean package -DskipTests
cd ../init && mvn clean package -DskipTests
cd ../receiver && mvn clean package -DskipTests
cd ../processing && mvn clean package -DskipTests
```

主要产物：

| 模块 | 产物 |
| --- | --- |
| `manage` | `manage/target/clklog-manage-1.2.0.jar` |
| `api` | `api/target/clklog-api-1.3.0.jar` |
| `init` | `init/target/clklog-init-1.2.3.jar` |
| `receiver` | `receiver/target/clklog-receiver-1.2.0.jar` |
| `processing` | `processing/target/clklog-processing-1.2.0-jar-with-dependencies.jar` |

### 前端打包

推荐 Node 16：

```bash
cd ui
yarn install --frozen-lockfile
yarn build
```

Node 18 环境：

```bash
cd ui
yarn install --ignore-engines
NODE_OPTIONS=--openssl-legacy-provider yarn build
```

前端产物位于 `ui/dist`。

## 源码方式部署

源码方式适合单机、内网机器或临时测试环境。

### 1. 准备外部依赖

生产环境至少需要：

- MySQL 8.x。
- ClickHouse 24.x 或兼容版本。
- Redis 7.x 或兼容版本。
- Nginx 或其他静态文件服务器。

如果需要真实埋点采集链路，还需要 Kafka 与 Flink；本地
`compose.dependencies.yml` 只提供管理台和分析页面联调所需的 MySQL、
ClickHouse、Redis。

### 2. 初始化数据库

全新环境执行：

```bash
mysql --default-character-set=utf8mb4 -h <mysql-host> -P <mysql-port> \
  -u <mysql-user> -p <mysql-database> \
  < init/scripts_init/mysql/mysql_clklog.sql

sed 's/${CLKLOG_LOG_DB}/clklog/g' init/scripts/init.sql \
  | clickhouse-client --host <clickhouse-host> --port <native-port> \
      --user <clickhouse-user> --password <clickhouse-password> \
      --multiquery
```

本地演示数据只在开发联调时导入：

```bash
mysql --default-character-set=utf8mb4 -h 127.0.0.1 -P 13306 \
  -uroot -p123456 clklog < deploy/local/mysql/seed.sql

clickhouse-client --host 127.0.0.1 --port 19000 \
  --user default --password 123456 --database clklog \
  --multiquery < deploy/local/clickhouse/seed.sql
```

已有环境升级：

```bash
mysql --default-character-set=utf8mb4 -h <mysql-host> -P <mysql-port> \
  -u <mysql-user> -p <mysql-database> \
  < init/scripts_init/mysql/upgrade_analytics_features.sql
```

### 3. 准备服务配置

建议将生产配置放在 `/opt/clklog/config`：

`/opt/clklog/config/manage.yml`

```yaml
server:
  port: 8080
spring:
  datasource:
    mysql:
      jdbc-url: jdbc:mysql://MYSQL_HOST:3306/clklog?characterEncoding=UTF-8&useTimezone=true&serverTimezone=GMT%2B8&allowPublicKeyRetrieval=true&useSSL=false
      username: MYSQL_USER
      password: MYSQL_PASSWORD
      driver-class-name: com.mysql.cj.jdbc.Driver
    clickhouse:
      jdbc-url: jdbc:clickhouse://CLICKHOUSE_HOST:8123/clklog
      username: default
      password: CLICKHOUSE_PASSWORD
      driver-class-name: com.clickhouse.jdbc.ClickHouseDriver
  redis:
    host: REDIS_HOST
    port: 6379
    database: 0
token:
  secret: CHANGE_ME_TO_A_LONG_RANDOM_SECRET
clklog-common:
  access-control-allow-origin-patterns: "https://your-domain.example"
```

`/opt/clklog/config/api.yml`

```yaml
server:
  port: 8081
spring:
  datasource:
    clickhouse:
      jdbc-url: jdbc:clickhouse://CLICKHOUSE_HOST:8123/clklog
      username: default
      password: CLICKHOUSE_PASSWORD
      driver-class-name: com.clickhouse.jdbc.ClickHouseDriver
      connection-timeout: 20000
      socket-timeout: 60000
      maximum-pool-size: 5
  redis:
    host: REDIS_HOST
    port: 6379
    database: 0
token:
  secret: CHANGE_ME_TO_THE_SAME_SECRET_AS_MANAGE
clklog-api:
  project-name: clklogapp
clklog-common:
  access-control-allow-origin-patterns: "https://your-domain.example"
```

### 4. 启动后端服务

```bash
java -Xms512m -Xmx512m \
  -Dspring.config.additional-location=file:/opt/clklog/config/manage.yml \
  -jar manage/target/clklog-manage-1.2.0.jar
```

```bash
java -Xms512m -Xmx512m \
  -Dspring.config.additional-location=file:/opt/clklog/config/api.yml \
  -jar api/target/clklog-api-1.3.0.jar
```

本地 profile 方式仅用于开发：

```bash
java -jar manage/target/clklog-manage-1.2.0.jar --spring.profiles.active=local
java -jar api/target/clklog-api-1.3.0.jar --spring.profiles.active=local
```

### 5. 部署前端静态文件

将 `ui/dist` 发布到 Nginx 静态目录，例如：

```bash
rsync -av --delete ui/dist/ /usr/share/nginx/html/
```

前端运行时读取 `config.js`。默认配置为同域代理：

```js
window.globalConfig = {
  BASE_API: window.location.origin + "/api",
  BASE_API_MANAGE: window.location.origin + "/manage",
};
```

如果前端和后端不同域，修改发布目录中的 `config.js`：

```js
window.globalConfig = {
  BASE_API: "https://api.example.com",
  BASE_API_MANAGE: "https://manage.example.com",
};
```

同域部署推荐 Nginx 反向代理：

```nginx
server {
  listen 80;
  server_name clklog.example.com;

  root /usr/share/nginx/html;
  index index.html;

  location / {
    try_files $uri $uri/ /index.html;
  }

  location /manage/ {
    proxy_pass http://127.0.0.1:8080/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
  }

  location /api/ {
    proxy_pass http://127.0.0.1:8081/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
  }
}
```

## Docker 镜像打包

先完成 Java 与前端打包，再构建镜像。镜像命令从仓库根目录执行。

```bash
docker build -t clklog/clklog-manage:local \
  --build-arg JAR_FILE=target/clklog-manage-1.2.0.jar manage

docker build -t clklog/clklog-api:local \
  --build-arg JAR_FILE=target/clklog-api-1.3.0.jar api

docker build -t clklog/clklog-init:local \
  --build-arg JAR_FILE=target/clklog-init-1.2.3.jar init

docker build -t clklog/clklog-receiver:local \
  --build-arg JAR_FILE=target/clklog-receiver-1.2.0.jar receiver

docker build -t clklog/clklog-processing:local \
  --build-arg JAR_FILE=target/clklog-processing-1.2.0-jar-with-dependencies.jar processing

docker build -t clklog/clklog-ui:local \
  --build-arg HTML_DIR=dist ui
```

## Docker 方式部署

当前仓库只提供本地依赖 `compose.dependencies.yml`，没有内置完整生产
Compose。生产部署可使用外部 MySQL、ClickHouse、Redis，并将配置文件挂载到
容器中。

示例：

```bash
docker network create clklog || true
```

启动 Manage：

```bash
docker run -d --name clklog-manage --restart unless-stopped \
  --network clklog \
  -p 8080:8080 \
  -v /opt/clklog/config/manage.yml:/config/application.yml:ro \
  -e JAVA_OPTS="-Xms512m -Xmx512m -Dspring.config.additional-location=file:/config/application.yml" \
  clklog/clklog-manage:local
```

启动 Analytics API：

```bash
docker run -d --name clklog-api --restart unless-stopped \
  --network clklog \
  -p 8081:8081 \
  -v /opt/clklog/config/api.yml:/config/application.yml:ro \
  -e JAVA_OPTS="-Xms512m -Xmx512m -Dspring.config.additional-location=file:/config/application.yml" \
  clklog/clklog-api:local
```

启动前端：

```bash
docker run -d --name clklog-ui --restart unless-stopped \
  --network clklog \
  -p 80:80 \
  -v /opt/clklog/config/nginx.conf:/etc/nginx/conf.d/default.conf:ro \
  -v /opt/clklog/config/config.js:/usr/share/nginx/html/config.js:ro \
  clklog/clklog-ui:local
```

如果需要埋点采集链路，额外部署：

- `receiver`：对外接收 SDK 事件，依赖 Kafka、Redis、ClickHouse。
- `processing`：Flink 作业，消费 Kafka 并写入 ClickHouse。
- `init`：ClickHouse 初始化与定时聚合服务，可按实际运维策略启用。

这些模块的默认 `application.yml` 含示例内网地址，生产部署必须提供独立配置文件
或使用 Spring Boot 环境变量覆盖。

## 部署顺序

推荐顺序：

1. 部署 MySQL、ClickHouse、Redis。
2. 初始化或升级 MySQL/ClickHouse schema。
3. 启动 Manage API。
4. 启动 Analytics API。
5. 发布 UI 静态文件并配置 Nginx。
6. 登录 `clklog` / `clklog`，检查项目 `clklogapp`。
7. 如需实时采集，再部署 Kafka、Receiver、Processing、Init。

生产首次登录后应立即：

- 修改默认账号密码。
- 修改 `token.secret`。
- 修改数据库默认密码。
- 限制 CORS 域名。
- 将 Nginx 切到 HTTPS。

## 升级与回滚

升级前：

```bash
mysqldump -h <mysql-host> -P <mysql-port> -u <mysql-user> -p clklog > clklog-backup.sql
```

ClickHouse 建议使用存储快照、`clickhouse-backup`，或已配置备份 Disk 后再执行
`BACKUP DATABASE`。升级前务必先在测试环境验证备份可恢复。

升级步骤：

1. 拉取新代码并打包。
2. 执行 `init/scripts_init/mysql/upgrade_analytics_features.sql`。
3. 替换后端 jar 或镜像。
4. 替换前端 `dist` 或 UI 镜像。
5. 重启 Manage、Analytics API、UI。
6. 检查健康接口和关键页面。

回滚：

- 后端回滚到上一个 jar 或镜像 tag。
- 前端回滚到上一个 `dist` 或镜像 tag。
- 如果升级 SQL 已修改数据结构，先确认兼容性；必要时从备份恢复。

## 轻量验证

```bash
cd manage && mvn -q -DskipTests compile
cd api && mvn -q -DskipTests compile
cd ui && yarn eslint \
  src/views/tag-system/index.vue \
  src/views/tag-system/category.vue \
  src/views/commercial/CdpList.vue \
  src/views/commercial/UserReview.vue \
  src/api/sysmanage/userTag.js \
  src/router/modules/user-portrait.js \
  src/views/commercial/CustomSqlQuery.vue \
  src/views/commercial/MobileSummary.vue \
  src/views/sys-manage/api-key.vue
```

前端全仓库仍有历史 ESLint 基线问题，迭代时优先对本次改动文件执行定向
ESLint，并确认开发构建成功。

根目录 `.github/workflows/ci.yml` 会分别编译 `api`、`manage`、`init`、
`receiver`、`processing`，并使用 Node 16 + `npm ci` 构建 `ui`。Node 18
与旧依赖 `@achrinza/node-ipc@9.2.2` 的 engine 范围不兼容。Mac 本地开发
仍统一使用 Yarn。模块目录中的工作流不会单独运行或保存发布凭据。

## 关键接口约束

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

## 常见问题

### Yarn install 在 Node 18 报 engine 不兼容

推荐切换 Node 16：

```bash
nvm install 16
nvm use 16
cd ui && yarn install --frozen-lockfile
```

必须使用 Node 18 时：

```bash
cd ui
yarn install --ignore-engines
NODE_OPTIONS=--openssl-legacy-provider yarn dev --host 127.0.0.1
```

### 前端页面能打开，但接口 404

检查 `ui/public/config.js` 或已发布目录的 `config.js`，确认：

- 同域部署时 Nginx 已代理 `/manage/` 到 Manage API。
- 同域部署时 Nginx 已代理 `/api/` 到 Analytics API。
- 不同域部署时 `BASE_API` 和 `BASE_API_MANAGE` 是完整后端地址。

### 登录成功但分析页面无数据

检查：

- 当前项目是否为 `clklogapp`。
- ClickHouse 是否已执行 `init/scripts/init.sql`。
- 本地演示是否已导入 `deploy/local/clickhouse/seed.sql`。
- Analytics API 的 ClickHouse 配置是否指向同一个库。

### API 密钥、自定义 SQL 或 CDP 数据串项目

检查请求是否携带 `projectName`。本地实现要求用户、元数据、漏斗、标签、CDP、
书签、API 密钥和自定义 SQL 都按项目隔离。

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
