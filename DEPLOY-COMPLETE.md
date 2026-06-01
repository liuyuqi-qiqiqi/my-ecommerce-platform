# 电商平台完整部署方案

> **从本地代码 → GitHub → 服务器 → 公网可访问**

---

## ⚠️ 前置说明

### 数据库已内置，无需手动建表
本项目使用 **Flyway** 自动管理数据库版本，每个微服务启动时会自动执行 `src/main/resources/db/migration/` 下的 SQL 迁移脚本，创建所有必需的数据表。

启动后 **CatalogDataInitializer** 会自动插入 27 个种子商品数据并同步到 Elasticsearch。

### 涉及的数据表（自动创建）
| 数据库 | 数据表 | 创建方式 |
|--------|--------|----------|
| product_db | category, brand, product, product_image, processed_event | Flyway 自动 |
| user_db | user_account, address, processed_event | Flyway 自动 |
| cart_db | cart, cart_item, processed_event | Flyway 自动 |
| order_db | customer_order, order_item, shipment, processed_event | Flyway 自动 |

---

## 第一步：本地同步最新代码到 GitHub

### 1.1 添加所有变更文件
在本地项目根目录（`C:\Users\liuyu\Documents\my-ONE`）执行：

```bash
cd /c/Users/liuyu/Documents/my-ONE

# 查看当前状态
git status

# 添加所有变更（修改 + 新增）
git add -A

# 再次确认
git status
```

### 1.2 提交并推送

```bash
# 提交
git commit -m "feat: 完整电商平台
- 27个真实商品数据(Unsplash真实产品图)
- 品牌色彩系统(CSS变量/红色主色调)
- 首屏Banner商业场景化(促销标语/CTAs/信任数据)
- 商品卡片(真实图/原价划线/折扣标签/hover加购)
- 粘性导航栏+促销活动栏+品牌页脚
- 全局Toast通知+骨架屏加载
- 购物车空状态插画引导
- 排序筛选功能
- 订单取消+库存恢复
- Hibernate 6.x ENUM兼容(Flyway V3/V4)
- Windows Docker Desktop端口冲突修复
- settings.xml阿里云Maven镜像"

# 推送到 GitHub
git push origin 001-ecommerce-platform
```

### 1.3 验证
浏览器打开 https://github.com/liuyuqi-qiqiqi/my-ecommerce-platform ，确认分支 `001-ecommerce-platform` 已更新。

---

## 第二步：服务器初始化 & 环境搭建

### 2.1 SSH 连接服务器

```bash
ssh root@47.95.252.177
```

### 2.2 更新系统 & 安装 Docker

```bash
# 更新系统
apt update && apt upgrade -y

# 卸载旧版本（如有）
for pkg in docker.io docker-doc docker-compose docker-compose-v2 podman-docker containerd runc; do
  apt remove -y $pkg 2>/dev/null
done

# 安装依赖
apt install -y ca-certificates curl gnupg lsb-release

# 添加 Docker 官方 GPG 密钥
install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
chmod a+r /etc/apt/keyrings/docker.gpg

# 添加 Docker 仓库
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null

# 安装 Docker Engine + Compose V2
apt update
apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# 验证安装
docker --version          # 应 >= 24.0
docker compose version    # 应 >= 2.20
```

### 2.3 配置 Docker 开机自启

```bash
systemctl enable docker
systemctl start docker
```

### 2.4 系统调优（Elasticsearch 需要）

```bash
# 虚拟内存上限
echo "vm.max_map_count=262144" | tee -a /etc/sysctl.conf
sysctl -p

# 文件描述符上限
echo "* soft nofile 65536" | tee -a /etc/security/limits.conf
echo "* hard nofile 65536" | tee -a /etc/security/limits.conf
```

### 2.5 配置时区

```bash
timedatectl set-timezone Asia/Shanghai
timedatectl status
```

### 2.6 配置防火墙

```bash
# 安装 UFW（如未安装）
apt install -y ufw

# 放行必要端口
ufw allow 22/tcp
ufw allow 80/tcp

# 开启防火墙
ufw --force enable
ufw status verbose
```

> ⚠️ **阿里云安全组**：还需登录阿里云控制台 → 安全组 → 入方向添加规则：TCP 80/0.0.0.0/0

---

## 第三步：服务器拉取最新代码

### 3.1 克隆项目

```bash
# 创建部署目录
mkdir -p /opt/ecommerce
cd /opt/ecommerce

# 克隆仓库
git clone https://github.com/liuyuqi-qiqiqi/my-ecommerce-platform.git .

# 切换到目标分支
git checkout 001-ecommerce-platform

# 确认
git branch
ls -la
```

### 3.2 创建生产环境 .env 文件

```bash
# 生成随机密钥和密码
JWT_SECRET=$(openssl rand -base64 32)
MYSQL_ROOT_PW=$(openssl rand -base64 16)
MYSQL_APP_PW=$(openssl rand -base64 16)
RABBITMQ_PW=$(openssl rand -base64 16)

# 写入 .env 文件（FRONTEND_PORT=80 用于公网访问）
cat > .env << EOF
TAG=latest

# 端口（生产环境：前端80，后端仅127.0.0.1）
FRONTEND_PORT=80
GATEWAY_PORT=8080
MYSQL_PORT=3306
REDIS_PORT=6379
RABBITMQ_AMQP_PORT=5672
RABBITMQ_MGMT_PORT=15672
ES_PORT=9200
NACOS_PORT=8848
NACOS_GRPC_PORT=9848
ZIPKIN_PORT=9411

# MySQL
MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PW}
MYSQL_USER=ecommerce
MYSQL_PASSWORD=${MYSQL_APP_PW}

# RabbitMQ
RABBITMQ_USER=ecommerce
RABBITMQ_PASSWORD=${RABBITMQ_PW}

# JWT
JWT_SECRET=${JWT_SECRET}

# Production mode — skip docker-compose.override.yml
PRODUCTION=1
EOF

# 验证
cat .env
```

---

## 第四步：数据库初始化

> ℹ️ **无需手动执行 SQL**。项目使用 Flyway 自动管理数据库版本。

### 数据库初始化流程（自动）
```
MySQL容器启动
  → init.sql 创建 4 个空数据库 (product_db, user_db, cart_db, order_db)
    → product-service 启动
      → Flyway 执行 V1~V4 迁移 (建表 + ENUM 列类型修正)
      → CatalogDataInitializer 插入 27 个种子商品 + 同步 ES
    → user-service 启动
      → Flyway 执行 V1~V4 迁移 (建表)
    → cart-service 启动
      → Flyway 执行 V1~V3 迁移 (建表)
    → order-service 启动
      → Flyway 执行 V1~V3 迁移 (建表)
```

### 如需手动查看/验证数据

```bash
# 进入 MySQL 容器
docker exec -it ecommerce-mysql mysql -u ecommerce -p

# 输入 .env 中的 MYSQL_PASSWORD 后：
USE product_db;
SHOW TABLES;
SELECT id, name, price FROM product;

USE user_db;
SHOW TABLES;

USE cart_db;
SHOW TABLES;

USE order_db;
SHOW TABLES;
EXIT;
```

---

## 第五步：启动项目 & 公网验证

### 5.1 构建镜像并启动

```bash
cd /opt/ecommerce

# 构建所有镜像（首次约 15-25 分钟，后续利用缓存 2-5 分钟）
docker compose -f docker-compose.yml build --parallel

# 启动所有服务（自动按依赖顺序）
docker compose -f docker-compose.yml up -d

# 实时查看启动日志
docker compose -f docker-compose.yml logs -f --tail=50
```

### 5.2 等待服务就绪（约 2-3 分钟）

```bash
# 循环检查健康状态
while true; do
  clear
  echo "=== 服务状态 ($(date '+%H:%M:%S')) ==="
  docker compose -f docker-compose.yml ps --format "table {{.Name}}\t{{.Status}}"
  HEALTHY=$(docker compose -f docker-compose.yml ps --format '{{.Status}}' | grep -c "healthy")
  echo ""
  echo "Healthy: $HEALTHY / 13"
  if [ "$HEALTHY" -ge 13 ]; then
    echo "✅ 全部服务就绪！"
    break
  fi
  sleep 15
done
```

### 5.3 公网验证

```bash
# 本机测试
curl -I http://localhost:80
curl http://localhost:80/health
curl http://localhost:8080/actuator/health

# 公网测试（从任何设备浏览器访问）
# http://47.95.252.177
```

浏览器打开 **http://47.95.252.177** 应看到：
- 🔥 顶部红色促销活动栏
- 🎨 品牌场景 Banner「发现你的理想好物」
- 📱 8 个分类快捷入口
- 🛍️ 27 个商品卡片（真实 Unsplash 产品图）

---

## 第六步：日常维护 & 故障排查

### 6.1 本地更新代码 → 服务器同步

```bash
# ===== 本地 =====
cd /c/Users/liuyu/Documents/my-ONE
git add -A
git commit -m "描述你的改动"
git push origin 001-ecommerce-platform

# ===== 服务器 =====
ssh root@47.95.252.177
cd /opt/ecommerce
git pull origin 001-ecommerce-platform
docker compose -f docker-compose.yml build --parallel   # 仅重建变更的服务
docker compose -f docker-compose.yml up -d              # 滚动更新
```

### 6.2 查看日志

```bash
# 所有服务日志
docker compose -f docker-compose.yml logs -f --tail=100

# 特定服务
docker compose -f docker-compose.yml logs -f --tail=200 gateway
docker compose -f docker-compose.yml logs -f --tail=200 product-service
docker compose -f docker-compose.yml logs -f --tail=200 user-service

# 最近 10 分钟的错误日志
docker compose -f docker-compose.yml logs --since 10m | grep -iE "error|exception|fail"
```

### 6.3 排查启动失败

```bash
# 1. 查看哪些服务未就绪
docker compose -f docker-compose.yml ps

# 2. 查看失败服务日志
docker logs ecommerce-product-service-1 --tail=100
docker logs ecommerce-gateway --tail=100

# 3. 检查资源
docker stats --no-stream
df -h

# 4. 重启失败的服务
docker compose -f docker-compose.yml restart product-service

# 5. 完全重置（保留代码和配置）
docker compose -f docker-compose.yml down -v
docker compose -f docker-compose.yml build --no-cache
docker compose -f docker-compose.yml up -d
```

### 6.4 数据库备份 & 恢复

```bash
# === 备份 ===
# 进入 MySQL 容器导出所有数据库
docker exec ecommerce-mysql mysqldump \
  -u ecommerce -p"$(grep MYSQL_PASSWORD .env | cut -d= -f2)" \
  --all-databases --single-transaction --routines --triggers \
  > /opt/backups/backup_$(date +%Y%m%d_%H%M%S).sql

# 仅备份商品数据
docker exec ecommerce-mysql mysqldump \
  -u ecommerce -p"$(grep MYSQL_PASSWORD .env | cut -d= -f2)" \
  product_db > /opt/backups/product_$(date +%Y%m%d).sql

# === 恢复 ===
docker exec -i ecommerce-mysql mysql \
  -u ecommerce -p"$(grep MYSQL_PASSWORD .env | cut -d= -f2)" \
  < /opt/backups/backup_20260601_120000.sql

# === 定时备份（crontab） ===
mkdir -p /opt/backups
echo "0 3 * * * docker exec ecommerce-mysql mysqldump -u ecommerce -p\"\$(grep MYSQL_PASSWORD /opt/ecommerce/.env | cut -d= -f2)\" --all-databases --single-transaction > /opt/backups/backup_\$(date +\%Y\%m\%d).sql 2>&1" | crontab -
```

### 6.5 公网无法访问排查流程

```bash
# 1. 检查容器状态
docker compose -f docker-compose.yml ps | grep frontend

# 2. 检查端口监听
ss -tlnp | grep :80

# 3. 本地测试
curl -I http://localhost:80

# 4. 检查防火墙
ufw status
# 阿里云控制台 → 安全组 → 确认 80 端口入方向已放行

# 5. 检查前端 Nginx 日志
docker logs ecommerce-frontend --tail=50

# 6. 检查网关路由
curl http://localhost:8080/actuator/health

# 7. 重启前端
docker compose -f docker-compose.yml restart frontend
```

### 6.6 接口报错排查

```bash
# 商品接口
curl http://localhost:8080/api/products/featured
curl "http://localhost:8080/api/products/search?page=1&pageSize=3"

# 注册接口
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"Test123456","displayName":"测试"}'

# 如果接口报 INTERNAL_ERROR，检查服务间网络：
docker exec ecommerce-gateway wget -qO- http://shop-bff:8085/actuator/health
docker exec ecommerce-shop-bff-1 wget -qO- http://product-service:8081/actuator/health
```

---

## 快速命令卡片

```bash
# 部署
cd /opt/ecommerce && docker compose -f docker-compose.yml up -d --build

# 状态
docker compose -f docker-compose.yml ps

# 日志
docker compose -f docker-compose.yml logs -f --tail=100

# 重启
docker compose -f docker-compose.yml restart

# 停止
docker compose -f docker-compose.yml stop

# 彻底清理
docker compose -f docker-compose.yml down -v

# 更新代码
git pull && docker compose -f docker-compose.yml build --parallel && docker compose -f docker-compose.yml up -d
```

---

## 服务端口映射（生产环境）

| 服务 | 容器端口 | 宿主机绑定 | 公网可访问 |
|------|----------|-----------|-----------|
| frontend | 80 | 0.0.0.0:80 | ✅ |
| gateway | 8080 | 127.0.0.1:8080 | ❌ |
| mysql | 3306 | 127.0.0.1:3306 | ❌ |
| redis | 6379 | 127.0.0.1:6379 | ❌ |
| rabbitmq | 5672 | 127.0.0.1:5672 | ❌ |
| elasticsearch | 9200 | 127.0.0.1:9200 | ❌ |
| nacos | 8848 | 127.0.0.1:8848 | ❌ |
| zipkin | 9411 | 127.0.0.1:9411 | ❌ |

> 内部服务仅绑定 `127.0.0.1`，公网只需暴露 80 端口。
