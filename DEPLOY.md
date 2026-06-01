# E-Commerce Platform — 线上部署文档

## 目录

1. [架构概览](#1-架构概览)
2. [服务器环境准备](#2-服务器环境准备)
3. [代码上传](#3-代码上传)
4. [配置环境变量](#4-配置环境变量)
5. [一键部署](#5-一键部署)
6. [HTTPS / TLS 配置](#6-https--tls-配置)
7. [日常运维](#7-日常运维)
8. [故障排查](#8-故障排查)
9. [扩容与高可用](#9-扩容与高可用)

---

## 1. 架构概览

```
                    Internet
                       │
                ┌──────▼──────┐
                │   Nginx :80 │  ← 前端静态资源 + API 反向代理
                │  (frontend) │
                └──────┬──────┘
                       │ /api/* → Gateway
                ┌──────▼──────┐
                │   Gateway   │  ← Spring Cloud Gateway (8080)
                │   (8080)    │     + JWT 鉴权
                └──────┬──────┘
                       │ Nacos 服务发现
          ┌────────────┼────────────┬────────────┐
   ┌──────▼──┐  ┌──────▼──┐  ┌─────▼───┐  ┌─────▼───┐
   │Shop-BFF │  │Product  │  │  User   │  │  Cart   │  │ Order  │
   │ (8085)  │  │ (8081)  │  │ (8082)  │  │ (8083)  │  │ (8084) │
   └─────────┘  └──┬───┬──┘  └──┬───┬──┘  └──┬───┬──┘  └───┬────┘
                   │   │        │   │        │   │        │
                   ▼   ▼        ▼   ▼        ▼   ▼        ▼
          ┌──────┐ ┌──────┐ ┌──────┐ ┌──────────┐ ┌──────────┐
          │MySQL │ │  ES  │ │Redis │ │RabbitMQ  │ │  Nacos   │
          │(3306)│ │(9200)│ │(6379)│ │ (5672)   │ │ (8848)   │
          └──────┘ └──────┘ └──────┘ └──────────┘ └──────────┘
```

| 服务 | 端口 | 技术栈 | Dockerfile 位置 |
|------|------|--------|-----------------|
| frontend | 80 | Vue 3 + Vite + Nginx | `frontend/Dockerfile` |
| gateway | 8080 | Spring Cloud Gateway | `gateway/Dockerfile` |
| shop-bff | 8085 | Spring Boot + OpenFeign | `shop-bff/Dockerfile` |
| product-service | 8081 | Spring Boot + JPA + ES | `services/product-service/Dockerfile` |
| user-service | 8082 | Spring Boot + JPA + Redis | `services/user-service/Dockerfile` |
| cart-service | 8083 | Spring Boot + JPA + Redis | `services/cart-service/Dockerfile` |
| order-service | 8084 | Spring Boot + JPA + MQ | `services/order-service/Dockerfile` |
| mysql | 3306 | MySQL 8.0 | 官方镜像 |
| redis | 6379 | Redis 7 | 官方镜像 |
| rabbitmq | 5672/15672 | RabbitMQ 3 | 官方镜像 |
| elasticsearch | 9200 | ES 8.13 | 官方镜像 |
| nacos | 8848/9848 | Nacos 2.3 | 官方镜像 |
| zipkin | 9411 | Zipkin 3.1 | 官方镜像 |

### 关键文件

| 文件 | 用途 |
|------|------|
| `docker-compose.yml` | 生产环境 Docker Compose 编排 |
| `docker-compose.override.yml` | 本地开发覆盖（端口绑定 `0.0.0.0`；`PRODUCTION=1` 时跳过） |
| `deploy.sh` | 一键部署脚本 |
| `.env` | 环境变量（从 `.env.example` 复制） |
| `frontend/nginx.conf` | 前端 Nginx 配置（静态资源 + API 代理） |
| `infra/mysql/init.sql` | 数据库初始化脚本 |
| `infra/seed/catalog.sql` | 商品种子 SQL（本地手动导入；Docker 全栈由 product-service 启动时自动种子） |

---

## 2. 服务器环境准备

### 2.1 最低硬件配置

| 资源 | 开发/测试 | 生产推荐 |
|------|-----------|----------|
| CPU | 4 核 | 8 核+ |
| 内存 | 8 GB | 16 GB+ |
| 磁盘 | 20 GB | 50 GB+ SSD |

### 2.2 安装 Docker

**Windows：** 安装 [Docker Desktop](https://docs.docker.com/desktop/setup/install/windows-install/)，全栈部署在 **Git Bash** 或 **WSL** 中运行 `./deploy.sh`；也可使用 `docker compose` 命令（见 [RUN.md](./RUN.md)）。

**Ubuntu / Debian：**

```bash
# 卸载旧版本
sudo apt-get remove docker docker-engine docker.io containerd runc 2>/dev/null

# 安装依赖
sudo apt-get update
sudo apt-get install -y ca-certificates curl gnupg

# 添加 Docker 官方 GPG key
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg

# 添加仓库
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# 安装 Docker Engine + Compose V2
sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# 将当前用户加入 docker 组（免 sudo）
sudo usermod -aG docker $USER
newgrp docker
```

**验证安装：**

```bash
docker --version          # >= 24.0
docker compose version    # >= 2.20
```

### 2.3 系统调优（生产环境）

```bash
# 虚拟内存上限（Elasticsearch 需要）
echo "vm.max_map_count=262144" | sudo tee -a /etc/sysctl.conf
sudo sysctl -p

# 文件描述符上限
echo "* soft nofile 65536" | sudo tee -a /etc/security/limits.conf
echo "* hard nofile 65536" | sudo tee -a /etc/security/limits.conf

# 减少 swap 使用
echo "vm.swappiness=1" | sudo tee -a /etc/sysctl.conf
sudo sysctl -p
```

### 2.4 配置防火墙

```bash
# UFW 示例
sudo ufw allow 22/tcp         # SSH
sudo ufw allow 80/tcp         # HTTP
sudo ufw allow 443/tcp        # HTTPS
# 内部服务端口不对外暴露
sudo ufw deny 3306/tcp        # MySQL
sudo ufw deny 6379/tcp        # Redis
sudo ufw enable
```

---

## 3. 代码上传

### 方式 A：Git Clone（推荐）

```bash
cd /opt
git clone <your-repo-url> ecommerce
cd ecommerce
```

### 方式 B：rsync 上传

```bash
# 在本地执行（排除构建产物）
rsync -avz --delete \
  --exclude 'frontend/node_modules' \
  --exclude 'frontend/dist' \
  --exclude '**/target' \
  --exclude '.git' \
  --exclude '*.log' \
  ./ user@your-server:/opt/ecommerce/
```

### 方式 C：tar 打包上传

```bash
# 本地打包
tar --exclude='frontend/node_modules' \
    --exclude='**/target' \
    --exclude='.git' \
    -czf ecommerce.tar.gz .

# 上传并解压
scp ecommerce.tar.gz user@your-server:/opt/
ssh user@your-server "mkdir -p /opt/ecommerce && cd /opt/ecommerce && tar xzf ../ecommerce.tar.gz"
```

---

## 4. 配置环境变量

### 4.1 自动生成（推荐）

```bash
cd /opt/ecommerce
chmod +x deploy.sh
./deploy.sh help    # 仅生成 .env，不部署
# 或
cp .env.example .env    # 手动复制
```

首次运行 `./deploy.sh` 会自动从 `.env.example` 生成 `.env` 并自动填充随机密码。

### 4.2 手动编辑

```bash
nano .env
```

**必须修改的敏感值：**

```ini
# JWT 密钥（≥ 256 bit，用 openssl rand -base64 32 生成）
JWT_SECRET=<你的强密钥>

# MySQL 密码
MYSQL_ROOT_PASSWORD=<你的强密码>
MYSQL_PASSWORD=<你的强密码>

# RabbitMQ 密码
RABBITMQ_PASSWORD=<你的强密码>
```

**生成随机密码：**

```bash
openssl rand -base64 32   # JWT 密钥
openssl rand -base64 16   # 数据库/消息队列密码
```

### 4.3 环境变量参考

| 变量 | 说明 | 默认值 |
|------|------|--------|
| `TAG` | Docker 镜像标签 | `latest` |
| `FRONTEND_PORT` | 前端对外端口 | `80` |
| `GATEWAY_PORT` | API 网关端口 | `8080` |
| `MYSQL_ROOT_PASSWORD` | MySQL root 密码 | **必须修改** |
| `MYSQL_USER` | MySQL 应用用户 | `ecommerce` |
| `MYSQL_PASSWORD` | MySQL 应用密码 | **必须修改** |
| `RABBITMQ_USER` | RabbitMQ 用户 | `ecommerce` |
| `RABBITMQ_PASSWORD` | RabbitMQ 密码 | **必须修改** |
| `JWT_SECRET` | JWT 签名密钥 | **必须修改** |
| `MYSQL_HOST` | MySQL 主机（云 RDS 时修改） | `mysql` |
| `PRODUCTION` | 生产模式（跳过 override，端口仅 127.0.0.1） | `0` |

扩容使用 `docker compose up -d --scale product-service=N`（勿使用已移除的 `deploy.replicas`）。

---

## 5. 一键部署

### 5.1 部署命令

```bash
cd /opt/ecommerce
chmod +x deploy.sh
./deploy.sh
```

脚本自动执行：
1. ✅ 检查 Docker / Docker Compose 版本
2. ✅ 自动生成 `.env`（首次）并填充随机密码
3. ✅ 构建所有 Docker 镜像（Maven 多阶段构建）
4. ✅ 启动基础设施（MySQL → Redis → RabbitMQ → ES → Nacos → Zipkin）
5. ✅ 等待基础设施健康检查通过
6. ✅ 启动后端微服务
7. ✅ 启动前端 Nginx

**预计耗时：**
- 首次：10-20 分钟（下载基础镜像 + Maven 依赖）
- 后续：2-5 分钟（利用 Docker 层缓存）

### 5.2 生产环境部署

```bash
# 跳过开发覆盖文件
PRODUCTION=1 ./deploy.sh

# 等同于
docker compose -f docker-compose.yml up -d --build
```

### 5.3 部署脚本命令

```bash
./deploy.sh                 # 完整部署（构建 + 启动）
./deploy.sh build           # 仅构建镜像
./deploy.sh start           # 仅启动（不重新构建）
./deploy.sh stop            # 停止所有服务
./deploy.sh restart         # 重启所有服务
./deploy.sh logs            # 查看所有日志
./deploy.sh logs gateway    # 查看特定服务日志
./deploy.sh status          # 查看服务状态
./deploy.sh down            # 停止并移除容器
./deploy.sh clean           # 彻底清理（含数据卷）
```

### 5.4 手动分步部署

```bash
# 1. 构建镜像
docker compose build --parallel

# 2. 启动基础设施
docker compose up -d mysql redis rabbitmq elasticsearch nacos zipkin

# 3. 等待基础设施健康
watch "docker compose ps"

# 4. 启动应用服务
docker compose up -d gateway shop-bff product-service user-service cart-service order-service

# 5. 启动前端
docker compose up -d frontend
```

### 5.5 验证部署

```bash
# 前端
curl -I http://localhost:80

# 健康检查
curl http://localhost:8080/actuator/health   # Gateway
curl http://localhost:8081/actuator/health   # Product Service

# 查看所有容器状态
docker compose ps
```

---

## 6. HTTPS / TLS 配置

### 方式 A：Nginx + Certbot（宿主机，推荐）

在宿主机安装 Nginx 作为 SSL 终端：

```bash
# 安装
sudo apt install -y nginx certbot python3-certbot-nginx

# 创建配置
sudo tee /etc/nginx/sites-available/ecommerce <<'EOF'
server {
    listen 80;
    server_name your-domain.com;

    location /.well-known/acme-challenge/ {
        root /var/www/certbot;
    }

    location / {
        proxy_pass http://127.0.0.1:80;  # → Docker frontend
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
EOF

sudo ln -s /etc/nginx/sites-available/ecommerce /etc/nginx/sites-enabled/
sudo nginx -t && sudo systemctl reload nginx

# 申请 SSL 证书
sudo certbot --nginx -d your-domain.com

# 测试自动续期
sudo certbot renew --dry-run
```

### 方式 B：Caddy（Docker 内，自动 HTTPS）

在 `docker-compose.yml` 中添加 Caddy 服务：

```yaml
caddy:
  image: caddy:2-alpine
  container_name: ecommerce-caddy
  restart: unless-stopped
  ports:
    - "80:80"
    - "443:443"
  volumes:
    - ./Caddyfile:/etc/caddy/Caddyfile:ro
    - caddy_data:/data
    - caddy_config:/config
  networks:
    - ecommerce-net
```

`Caddyfile`：

```caddyfile
your-domain.com {
    reverse_proxy frontend:80
    header {
        X-Frame-Options "SAMEORIGIN"
        X-Content-Type-Options "nosniff"
        Referrer-Policy "strict-origin-when-cross-origin"
    }
}
```

Caddy 自动从 Let's Encrypt 申请并续签证书。

### 方式 C：Cloudflare / CDN SSL

如果使用 CDN（Cloudflare 等）：
1. DNS 解析到 CDN
2. CDN SSL/TLS 模式：Full 或 Full (Strict)
3. CDN 回源到服务器 80 端口

这是最简单的方案，无需在服务器配置证书。

### HTTPS 启用后的 Nginx 配置调整

编辑 `frontend/nginx.conf`，取消注释：

```nginx
add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
add_header Content-Security-Policy "default-src 'self'; ..." always;
```

---

## 7. 日常运维

### 7.1 更新部署

```bash
cd /opt/ecommerce
git pull
./deploy.sh
```

### 7.2 数据库备份

```bash
# 备份全部数据库
docker exec ecommerce-mysql mysqldump \
  -u ecommerce -p"${MYSQL_PASSWORD}" \
  --all-databases --single-transaction \
  > backup_$(date +%Y%m%d_%H%M%S).sql

# 备份单个数据库
docker exec ecommerce-mysql mysqldump \
  -u ecommerce -p"${MYSQL_PASSWORD}" \
  product_db > product_backup.sql
```

### 7.3 扩缩容

```bash
# 扩容商品服务到 3 个实例
docker compose up -d --scale product-service=3

# 缩减
docker compose up -d --scale product-service=1
```

### 7.4 查看日志

```bash
docker compose logs -f --tail=100 gateway
docker compose logs -f --tail=100 product-service
```

### 7.5 资源监控

```bash
docker stats
docker system df
docker system prune -a   # 清理未使用的镜像和卷
```

---

## 8. 故障排查

### 8.1 常见问题

| 问题 | 可能原因 | 解决方法 |
|------|----------|----------|
| 构建失败 | Maven 依赖下载超时 | 重试 `docker compose build --no-cache` |
| 前端 502 | Gateway 未就绪 | 等待 60s，`docker compose logs gateway` |
| Nacos 连接超时 | Nacos 启动慢 | Nacos 首次需 60s，等待健康检查通过 |
| MySQL 拒绝连接 | 密码不匹配 | 检查 `.env` 中 `MYSQL_PASSWORD` |
| ES 启动失败 | `vm.max_map_count` 太小 | `sudo sysctl -w vm.max_map_count=262144` |
| 端口冲突 | 端口已被占用 | `lsof -i :80`，修改 `.env` 端口映射 |
| 服务注册失败 | 网络不通 | 确保所有服务在 `ecommerce-net` 网络上 |

### 8.2 诊断命令

```bash
# 查看容器日志
docker logs ecommerce-gateway --tail=50 -f

# 进入容器排查
docker exec -it ecommerce-gateway sh

# 检查网络连通性
docker exec ecommerce-gateway wget -qO- http://nacos:8848/nacos/

# 检查 JVM 内存
docker stats --format "table {{.Name}}\t{{.MemUsage}}\t{{.MemPerc}}"

# 查看健康状态
curl http://localhost:8080/actuator/health
```

### 8.3 完全重置

```bash
./deploy.sh clean
# 或手动：
docker compose down -v --remove-orphans
docker images | grep ecommerce- | awk '{print $1":"$2}' | xargs -r docker rmi
```

---

## 9. 扩容与高可用

### 9.1 无状态服务扩容

```bash
docker compose up -d \
  --scale product-service=4 \
  --scale user-service=2 \
  --scale cart-service=2 \
  --scale order-service=2 \
  --scale shop-bff=2
```

Nacos 自动服务发现和负载均衡。

### 9.2 数据库高可用

- **云数据库**：推荐使用云厂商 RDS，修改 `.env` 中 `MYSQL_HOST` 为外部地址
- **MySQL 主从**：配置 MySQL Replication
- **Redis 哨兵**：生产环境建议使用 Redis Sentinel 或云 Redis 服务

### 9.3 多机部署

```bash
# Docker Swarm 快速多机
docker swarm init
docker stack deploy -c docker-compose.yml ecommerce
```

---

## 附录 A：端口映射

| 服务 | 容器端口 | 宿主机 | 环境变量 |
|------|----------|--------|----------|
| frontend | 80 | 80 | `FRONTEND_PORT` |
| gateway | 8080 | 127.0.0.1:8080 | `GATEWAY_PORT` |
| mysql | 3306 | 127.0.0.1:3306 | `MYSQL_PORT` |
| redis | 6379 | 127.0.0.1:6379 | `REDIS_PORT` |
| rabbitmq | 5672/15672 | 127.0.0.1:5672/15672 | `RABBITMQ_*_PORT` |
| elasticsearch | 9200 | 127.0.0.1:9200 | `ES_PORT` |
| nacos | 8848/9848 | 127.0.0.1:8848/9848 | `NACOS_*_PORT` |
| zipkin | 9411 | 127.0.0.1:9411 | `ZIPKIN_PORT` |

> **生产安全**：后端端口绑定 `127.0.0.1` 仅本地可访问。公网只需暴露 80/443。

## 附录 B：CI/CD 示例（GitHub Actions）

```yaml
# .github/workflows/deploy.yml
name: Deploy
on:
  push:
    branches: [main]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - name: Deploy to server
        uses: appleboy/ssh-action@v1
        with:
          host: ${{ secrets.SERVER_HOST }}
          username: ${{ secrets.SERVER_USER }}
          key: ${{ secrets.SERVER_SSH_KEY }}
          script: |
            cd /opt/ecommerce
            git pull
            docker compose build --parallel
            docker compose up -d
```
