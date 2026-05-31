# 电商商城项目宪法文档（Constitution）
> **项目名称**: E-Commerce Platform (PC Web)
> **版本**: 1.0
> **生效日期**: 2025-11-21
> **适用范围**: 所有微服务模块、前端应用、基础设施及自动化流程

---

## 一、架构原则
1.  **微服务边界清晰**
    - 每个业务域（商品、订单、购物车、用户）独立为一个微服务，禁止跨服务直接访问数据库。
    - 服务间通信仅通过 **RESTful API（同步）** 或 **RabbitMQ（异步事件）**。
2.  **前后端分离**
    - 前端（Vue3 + Element Plus）通过调用后端 BFF（Backend For Frontend）或聚合 API 获取数据。
    - 禁止前端直连微服务，需通过网关（Spring Cloud Gateway）统一入口。
3.  **数据一致性**
    - 跨服务操作（如下单减库存）必须通过 **Saga 模式** 或 **可靠消息最终一致性** 实现。

---

## 二、技术栈规范

| 层级       | 技术选型                | 版本要求       | 约束说明                                 |
| :--------- | :---------------------- | :------------- | :--------------------------------------- |
| 后端语言   | Java                    | ≥ 17           | 使用 Records、Pattern Matching 等现代特性 |
| 微服务框架 | Spring Boot + Spring Cloud | 2023.x+      | 统一注册中心 (Nacos)、配置中心           |
| 数据库     | MySQL                   | 8.0+           | 主从读写分离，每个服务独享 schema        |
| 搜索引擎   | Elasticsearch           | 8.x            | 商品信息实时同步，支持关键词+属性过滤    |
| 消息队列   | RabbitMQ                | 3.12+          | 用于订单创建、库存扣减、通知等异步解耦   |
| 前端框架   | Vue 3                   | ≥ 3.4          | Composition API + TypeScript             |
| UI 组件库  | Element Plus            | ≥ 2.7          | 禁止自定义主题覆盖核心样式               |
| 构建工具   | Vite                    | ≥ 5.0          | 开启生产环境压缩与 Tree-shaking          |

---

## 三、代码质量标准
### 3.1 编码规范
- **后端**: 遵循 [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- **前端**: TypeScript 严格模式 (`strict: true`)，禁用 `any` 类型，组件命名采用 PascalCase

### 3.2 测试要求
- 单元测试覆盖率 ≥ 70% (Jacoco 统计)
- 核心链路（如下单、支付）必须包含集成测试（使用 Testcontainers 模拟 MySQL/ES/RabbitMQ）
- 前端关键路径需包含：
  - Vitest 单元测试（组件逻辑）
  - Cypress E2E 测试（用户主流程）

### 3.3 API 设计
- 所有 RESTful 接口遵循 [OpenAPI 3.0](https://swagger.io/specification/) 规范
- 接口必须包含明确的请求/响应 Schema，并通过 Swagger UI 可视化
- 错误码统一格式: `{ "code": "ORDER_NOT_FOUND", "message": "订单不存在" }`

---

## 四、安全与运维
### 4.1 安全基线
- 所有用户输入必须校验（后端使用 Hibernate Validator + 自定义注解）
- 敏感操作（如修改密码、下单）需记录操作日志并支持审计追溯
- JWT Token 有效期 ≤ 2 小时，Refresh Token 存入 Redis 并设置滑动过期策略

### 4.2 可观测性
- 集成 Micrometer + Prometheus + Grafana 监控系统指标（QPS、延迟、错误率）
- 关键链路（搜索、下单、支付）接入 Sleuth + Zipkin 实现全链路追踪
- 应用日志统一采集至 ELK，格式为结构化 JSON

### 4.3 部署要求
- 所有服务容器化（Docker），提供标准 `Dockerfile`
- 支持 Docker Compose 本地开发部署，生产环境运行于 Kubernetes
- 每个微服务拥有独立 CI/CD 流水线（推荐 GitHub Actions）

---

## 五、AI 辅助开发约束（Cursor Agent 使用规范）
1.  **生成代码必须遵守本宪法**
    - AI 不得绕过输入校验、不得硬编码配置、不得使用已废弃或非标 API。
2.  **规范优先于实现**
    - 在执行 `/implement` 前，必须先完成 `/specify`（功能契约）和 `/plan`（技术方案）。
3.  **可解释性要求**
    - 所有 AI 生成的复杂逻辑（如状态机、并发控制）必须附带注释，说明设计意图、边界条件。
4.  **禁止“黑盒生成”**
    - 任何由 AI 生成的代码必须经过人工审查，确保符合本宪法第 1-4 章要求。

---

> ✅ **宪法效力说明**
> 本文件是项目所有开发活动的强制性依据。任何偏离本宪法的行为（包括 AI 自动生成内容）都必须经过团队评审并记录在案。