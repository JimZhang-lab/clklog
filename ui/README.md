## 功能

```
- 登录 / 注销

- 权限验证
  - 页面权限
  - 指令权限
  - 权限配置

  - 二步登录

- 多环境发布
  - dev
  - sit
  - stage
  - prod

- 全局功能
  - 动态侧边栏（支持多级路由嵌套）
  - 快捷导航(标签页)
  - Svg Sprite 图标
  - 自适应收缩侧边栏

- Excel
  - 导出excel
  - 前端可视化excel


- 错误页面
  - 401
  - 404

- 組件
- 综合实例
- 错误日志
- 引导页
- ECharts 图表
```

## 开发

```bash
# 克隆项目
git clone https://github.com/clklog/clklog-ui.git

# 进入项目目录
cd clklog-ui

# 安装依赖
yarn install

# 可通过 Yarn registry 配置解决依赖下载速度问题
yarn config set registry https://registry.npmmirror.com

# 启动服务
yarn dev

```
## 本地环境接口服务代理配置修改

修改 `vue.config.js` 文件中的代理配置。

'/DEV-API'默认代理到 `http://127.0.0.1:8081`，可通过环境变量
`CLKLOG_API_TARGET` 覆盖。

'/DEV-API-MANAGE'默认代理到 `http://127.0.0.1:8080`，可通过环境变量
`CLKLOG_MANAGE_TARGET` 覆盖。

本地同时启动两个 Java 服务时，建议管理服务使用 `8080`，API 服务使用
`8081`：

```bash
cd ../api
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```


## 生产环境接口服务代理配置修改

方式一，先修改 `public/config.js` 后发布：

```bash
yarn build
```

方式二，发布后修改 `dist/config.js`。默认配置使用同域反向代理：

```js
BASE_API: window.location.origin + "/api"
BASE_API_MANAGE: window.location.origin + "/manage"
```

## 其它

## 预览发布环境效果
yarn preview

## 预览发布环境效果 + 静态资源分析
yarn preview --report

## 代码格式检查
yarn lint

## 代码格式检查并自动修复
yarn lint --fix

## Online Demo

[在线 Demo](https://demo.clklog.com/)

## Donate
```
