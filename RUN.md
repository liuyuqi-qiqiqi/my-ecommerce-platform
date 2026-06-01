# E-Commerce Platform — 运行文档

本文说明如何在本地或 Docker 中启动整套电商系统。生产/服务器部署细节见 [DEPLOY.md](./DEPLOY.md)。

---

## 目录

1. [运行方式概览](#1-运行方式概览)
2. [环境要求](#2-环境要求)
3. [方式 A：本地开发（推荐日常）](#3-方式-a本地开发推荐日常)
4. [方式 B：Docker 全栈](#4-方式-bdocker-全栈)
5. [方式 C：混合（基础设施 Docker + 应用本机）](#5-方式-c混合基础设施-docker--应用本机)
6. [访问地址与验证](#6-访问地址与验证)
7. [常用命令速查](#7-常用命令速查)
8. [Docker 部分已知问题](#8-docker-部分已知问题)

---

## 1. 运行方式概览

| 方式 | 适用场景 | 前端 | 后端 | 中间件 |
|------|----------|------|------|--------|
| **A 本地开发** | 日常改代码、调试 | `npm run dev` (5173) | 各服务 `mvn spring-boot:run` | `infra/docker-compose.yml` |
| **B Docker 全栈** | 联调、演示、接近生产 | Nginx 容器 (80) | 全部容器 | 根目录 `docker-compose.yml` |
| **C 混合** | 只调试某个微服务 | 同 A | 只跑改动的服务 | 同 A |

架构（与 [specs/001-ecommerce-platform/quickstart.md](./specs/001-ecommerce-platform/quickstart.md) 一致）：

```
浏览器 → 前端(5173 或 :80) → Gateway(:8080) → shop-bff → 微服务 → MySQL/Redis/ES/RabbitMQ/Nacos
```

**启动顺序（逻辑依赖）**：中间件健康 → Nacos → 微服务注册 → Gateway → BFF → 前端。

---

## 2. 环境要求

| 工具 | 版本 | 用途 |
|------|------|------|
| Docker Desktop | 含 Compose V2 | 中间件 / 全栈 |
| JDK | 17+ | 后端本机运行 |
| Maven | 3.9+ | 后端本机运行 |
| Node.js | 20 LTS | 前端开发 |

**资源建议**

| 场景 | 内存 |
|------|------|
| 仅 `infra` 中间件 | ≥ 4 GB |
| 根目录 Docker 全栈（含 ES、Nacos、6 个 JVM） | ≥ 12 GB（推荐 16 GB） |

**Windows 注意**

- 全栈可用 **`.\deploy.ps1`**（PowerShell），或 Git Bash/WSL 运行 `./deploy.sh`。
- Elasticsearch 在 Docker Desktop 上一般无需 `vm.max_map_count`；Linux 服务器见 [DEPLOY.md §2.3](./DEPLOY.md#23-系统调优生产环境)。

---

## 3. 方式 A：本地开发（推荐日常）

### 3.1 启动基础设施

在仓库根目录：

```powershell
cd infra
docker compose up -d mysql redis rabbitmq elasticsearch nacos zipkin
docker compose ps
```

等待 `mysql`、`redis`、`rabbitmq`、`elasticsearch`、`nacos` 状态为 **healthy**（首次 Nacos 可能需 30–60 秒）。

默认账号（仅 `infra` 编排，开发用）：

| 组件 | 用户 | 密码 |
|------|------|------|
| MySQL | `ecommerce` | `ecommerce` |
| RabbitMQ | `ecommerce` | `ecommerce` |

### 3.2 启动后端（6 个终端或 IDE）

在仓库根目录，**先微服务，再 Gateway，再 BFF**（Gateway 依赖 Nacos；BFF 通过 Feign 调下游）：

```powershell
# 微服务（8081–8084）
cd services/product-service; mvn spring-boot:run
cd services/user-service;   mvn spring-boot:run
cd services/cart-service;   mvn spring-boot:run
cd services/order-service;  mvn spring-boot:run

# API 网关（8080）
cd gateway; mvn spring-boot:run

# BFF（8085）
cd shop-bff; mvn spring-boot:run
```

各服务首次启动会通过 **Flyway** 创建 `product_db` / `user_db` / `cart_db` / `order_db` 表结构。

### 3.3 商品种子数据（可选）

在 **product-service 已启动且 Flyway 完成** 后任选其一：

```powershell
# 推荐：管理接口
curl -X POST http://localhost:8081/admin/seed

# 或 SQL（密码与 infra 一致）
Get-Content infra/seed/catalog.sql | docker exec -i ecommerce-mysql mysql -uecommerce -pecommerce
```

### 3.4 启动前端

```powershell
cd frontend
npm install
npm run dev
```

浏览器打开：**http://localhost:5173**  
Vite 将 `/api` 代理到 **http://localhost:8080**（见 `frontend/vite.config.ts`）。

---

## 4. 方式 B：Docker 全栈

使用仓库根目录的 `docker-compose.yml`（应用 + 中间件一体）。

### 4.1 准备 `.env`

```powershell
cd C:\Users\liuyu\Documents\my-ONE
copy .env.example .env
# 编辑 .env：至少修改 JWT_SECRET、MYSQL_*、RABBITMQ_PASSWORD
```

生成强密钥示例：

```powershell
# 若已安装 OpenSSL
openssl rand -base64 32
```

### 4.2 一键部署

**Windows PowerShell：**

```powershell
.\deploy.ps1
# 生产端口（仅 127.0.0.1，不用 override）
.\deploy.ps1 -Production
```

**Linux / Git Bash / WSL：**

```bash
chmod +x deploy.sh
./deploy.sh
```

等价手动步骤：

```powershell
docker compose build --parallel
docker compose up -d mysql redis rabbitmq elasticsearch nacos zipkin
# 等待 healthy 后
docker compose up -d gateway shop-bff product-service user-service cart-service order-service frontend
```

### 4.3 访问

| 入口 | URL |
|------|-----|
| 前端（Nginx） | http://localhost:80 |
| API（经 Nginx 代理） | http://localhost/api/... |
| Gateway（仅本机绑定） | http://127.0.0.1:8080 |
| Nacos 控制台 | http://127.0.0.1:8848/nacos/ |
| RabbitMQ 管理 | http://127.0.0.1:15672/ |
| Zipkin | http://127.0.0.1:9411/ |

前端构建使用 `frontend/.env.production` 中的 `VITE_API_BASE_URL=/api`，由 Nginx 转发到 `gateway:8080`。

### 4.4 部署脚本子命令

```bash
./deploy.sh build      # 仅构建镜像
./deploy.sh start      # 仅启动
./deploy.sh status     # 状态与资源
./deploy.sh logs gateway
./deploy.sh stop
./deploy.sh down
PRODUCTION=1 ./deploy.sh   # 不使用 override（当前仓库无 override 文件，行为与默认相同）
```

---

## 5. 方式 C：混合（基础设施 Docker + 应用本机）

与方式 A 相同，仅启动 `infra/docker-compose.yml`，后端与前端在本机用 Maven / `npm run dev` 启动。  
**不要** 同时启动根目录 `docker-compose.yml` 中的 `mysql`/`redis` 等，否则会因 **相同 `container_name`**（如 `ecommerce-mysql`）冲突。

本机应用连接中间件时使用 **localhost** 与默认端口（3306、6379、5672、9200、8848），与各服务 `application.yml` 中的默认值一致。

---

## 6. 访问地址与验证

### 6.1 健康检查

```powershell
# 本地开发
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health

# Docker 全栈（Gateway 绑定 127.0.0.1）
curl http://127.0.0.1:8080/actuator/health
curl http://localhost/health
```

### 6.2 核心业务冒烟

```powershell
curl "http://localhost:8080/api/products/featured"

curl -X POST http://localhost:8080/api/auth/register `
  -H "Content-Type: application/json" `
  -d '{"email":"test@example.com","password":"SecurePass123","displayName":"Test User"}'
```

更多用例见 [quickstart.md §5](./specs/001-ecommerce-platform/quickstart.md)。

### 6.3 查看容器

```powershell
docker compose ps
docker compose logs -f --tail=100 product-service
```

---

## 7. 常用命令速查

| 操作 | 命令 |
|------|------|
| 仅中间件（开发） | `cd infra && docker compose up -d` |
| 停止中间件 | `cd infra && docker compose down` |
| 全栈构建 | `docker compose build` |
| 全栈启动 | `docker compose up -d` |
| 查看日志 | `docker compose logs -f <服务名>` |
| MySQL 备份 | 见 [DEPLOY.md §7.2](./DEPLOY.md#72-数据库备份) |
| 清理数据卷 | `docker compose down -v`（**会删库**） |

---

## 8. Docker 已知问题与修复记录

### 8.1 已修复（2026-05-31）

| 问题 | 修复方式 |
|------|----------|
| MySQL 启动时执行 `catalog.sql` 失败 | 已从 `docker-compose.yml` 移除；商品数据由 **product-service** 的 `CatalogDataInitializer` 在 Flyway 后自动写入 |
| 健康检查 `wget` 不可用 | 各服务/前端 Dockerfile 安装 `curl`；Compose 健康检查改为 `curl -sf` |
| `deploy.replicas` 无效 | 已移除；扩容见 `.env.example` 中的 `--scale` 说明 |
| 缺少 `docker-compose.override.yml` | 已添加（本地端口 `0.0.0.0` 绑定） |
| `MYSQL_HOST` 等未配置 | `.env.example` 与 Compose 支持 `${MYSQL_HOST:-mysql}` 等 |
| `shop-bff` 依赖 `gateway` | 已移除多余 `depends_on` |
| `infra` ES 无持久卷 | `infra/docker-compose.yml` 已增加 `es_data` 卷 |

### 8.2 仍需注意

| # | 问题 | 说明 |
|---|------|------|
| 1 | **两套 Compose 不能同时运行** | `infra/` 与根目录共用 `ecommerce-mysql` 等容器名，只能二选一 |
| 2 | **`deploy.sh` 需 Bash** | Windows 用 Git Bash/WSL，或直接用 `docker compose` |
| 3 | **重复 Dockerfile** | 根目录通用 `Dockerfile` 与各服务目录 Dockerfile 并存；构建较慢 |
| 4 | **无资源 limits** | 全栈建议 ≥12 GB 内存 |
| 5 | **Nacos 无生产级持久化** | 重启后注册信息可能丢失 |
| 6 | **HTTPS** | Caddy 配置仅在 DEPLOY 文档中，未内置到 Compose |
| 7 | **生产端口绑定** | `PRODUCTION=1` 时 Gateway 等为 `127.0.0.1`；开发用 override 绑定 `0.0.0.0` |

### 8.3 操作提示

1. **日常开发**：优先 [§3 方式 A](#3-方式-a本地开发推荐日常)。  
2. **扩容**：`docker compose up -d --scale product-service=2`（无 `container_name` 的服务）。  
3. **不要同时** `infra` 与根目录 Compose 启动中间件。

---

## 相关文档

| 文档 | 内容 |
|------|------|
| [DEPLOY.md](./DEPLOY.md) | 服务器安装、HTTPS、备份、扩容、CI/CD |
| [specs/001-ecommerce-platform/quickstart.md](./specs/001-ecommerce-platform/quickstart.md) | 功能验收 curl 示例 |
| [.env.example](./.env.example) | Docker 全栈环境变量模板 |

---

*文档生成日期：2026-05-31，与当前仓库 Docker 编排状态一致。*
