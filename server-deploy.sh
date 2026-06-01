#!/usr/bin/env bash
# ============================================================
# 一键部署脚本 — 复制到服务器终端直接运行
# ============================================================
set -e

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
RED='\033[0;31m'
NC='\033[0m'
info()  { echo -e "${GREEN}[OK]${NC}  $*"; }
warn()  { echo -e "${YELLOW}[..]${NC}  $*"; }
step()  { echo -e "${CYAN}[>>]${NC} $*"; }
err()   { echo -e "${RED}[!!]${NC} $*"; }

# =============================================
# Phase 1: Docker 安装
# =============================================
step "Phase 1/4: 安装 Docker 环境"

apt update -qq && apt upgrade -y -qq
apt install -y -qq ca-certificates curl gnupg lsb-release

install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
chmod a+r /etc/apt/keyrings/docker.gpg

echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null

apt update -qq && apt install -y -qq docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

systemctl enable docker && systemctl start docker
info "Docker $(docker --version | cut -d' ' -f3 | cut -d',' -f1) 安装完成"

# ES 系统调优
echo "vm.max_map_count=262144" >> /etc/sysctl.conf && sysctl -p
echo "* soft nofile 65536" >> /etc/security/limits.conf
echo "* hard nofile 65536" >> /etc/security/limits.conf
timedatectl set-timezone Asia/Shanghai

# 防火墙
apt install -y -qq ufw
ufw allow 22/tcp
ufw allow 8088/tcp
ufw --force enable

# =============================================
# Phase 2: 项目准备
# =============================================
step "Phase 2/4: 项目代码准备"

# 如果 /root/my-ONE 不存在，从 GitHub 克隆
if [ ! -d /root/my-ONE ]; then
    info "从 GitHub 克隆项目..."
    mkdir -p /root/my-ONE
    cd /root/my-ONE
    git clone https://github.com/liuyuqi-qiqiqi/my-ecommerce-platform.git .
    git checkout 001-ecommerce-platform
else
    info "项目目录已存在，跳过克隆"
    cd /root/my-ONE
fi

# 生成 .env
step "生成生产环境 .env"
JWT_SECRET=$(openssl rand -base64 32)
MYSQL_ROOT_PW=$(openssl rand -base64 16 | tr -d '=')
MYSQL_APP_PW=$(openssl rand -base64 16 | tr -d '=')
RABBITMQ_PW=$(openssl rand -base64 16 | tr -d '=')

cat > /root/my-ONE/.env << EOF
TAG=latest
FRONTEND_PORT=8088
GATEWAY_PORT=8080
MYSQL_PORT=3306
REDIS_PORT=6379
RABBITMQ_AMQP_PORT=5672
RABBITMQ_MGMT_PORT=15672
ES_PORT=9200
NACOS_PORT=8848
NACOS_GRPC_PORT=9848
ZIPKIN_PORT=9411
MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PW}
MYSQL_USER=ecommerce
MYSQL_PASSWORD=${MYSQL_APP_PW}
RABBITMQ_USER=ecommerce
RABBITMQ_PASSWORD=${RABBITMQ_PW}
JWT_SECRET=${JWT_SECRET}
MYSQL_HOST=mysql
REDIS_HOST=redis
RABBITMQ_HOST=rabbitmq
ES_URIS=http://elasticsearch:9200
PRODUCTION=1
EOF

info ".env 已生成"

# =============================================
# Phase 3: 构建 & 启动
# =============================================
step "Phase 3/4: 构建镜像（首次 15-25 分钟）"
cd /root/my-ONE
docker compose -f docker-compose.yml build --parallel 2>&1 | tail -20
info "构建完成"

step "启动所有容器..."
docker compose -f docker-compose.yml up -d

step "等待服务就绪（约 2-3 分钟）..."
ATTEMPTS=0
while [ $ATTEMPTS -lt 120 ]; do
    HEALTHY=$(docker compose -f docker-compose.yml ps --format '{{.Status}}' 2>/dev/null | grep -c "healthy" || echo 0)
    echo "  [$ATTEMPTS] Healthy: $HEALTHY / 13"
    if [ "$HEALTHY" -ge 13 ]; then
        info "全部 13 个服务就绪！"
        break
    fi
    sleep 10
    ATTEMPTS=$((ATTEMPTS + 1))
done

# =============================================
# Phase 4: 验证
# =============================================
step "Phase 4/4: 功能验证"

echo ""
echo "--- 网关健康检查 ---"
curl -s http://localhost:8080/actuator/health
echo ""

echo "--- 前端健康检查 ---"
curl -s http://localhost:8088/health
echo ""

echo "--- 商品 API ---"
curl -s "http://localhost:8088/api/products/search?page=1&pageSize=2" | head -c 300
echo ""

echo "--- 用户注册 ---"
curl -s -X POST http://localhost:8088/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@test.com","password":"Test1234","displayName":"演示用户"}'
echo ""

echo "--- 用户登录 ---"
curl -s -X POST http://localhost:8088/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@test.com","password":"Test1234"}'
echo ""

echo ""
echo "============================================"
echo "  🎉 部署完成！"
echo "  公网地址: http://47.95.252.177:8088"
echo "============================================"
echo ""
echo "  ⚠️ 确保阿里云安全组已放行 TCP 8088 端口！"
echo ""
