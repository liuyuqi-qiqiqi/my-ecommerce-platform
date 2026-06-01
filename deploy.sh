#!/usr/bin/env bash
# ============================================================
# E-Commerce Platform — One-Click Deploy Script
# Usage:
#   ./deploy.sh              # Build & start all services
#   ./deploy.sh start        # Start (without rebuild)
#   ./deploy.sh build        # Build images only
#   ./deploy.sh stop         # Stop all services
#   ./deploy.sh restart      # Restart all services
#   ./deploy.sh logs <svc>   # Tail logs (one or all services)
#   ./deploy.sh status       # Show container status & health
#   ./deploy.sh down         # Stop & remove containers
#   ./deploy.sh clean        # Remove everything (volumes too)
#   ./deploy.sh help         # Show this help
# ============================================================

set -euo pipefail

cd "$(dirname "$0")"
PROJECT_ROOT="$(pwd)"

# ── Config ──────────────────────────────────────────────────
ENV_FILE=".env"
COMPOSE_FILE="docker-compose.yml"
OVERRIDE_FILE="docker-compose.override.yml"

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
CYAN='\033[0;36m'
NC='\033[0m'

log_info()  { echo -e "${GREEN}[INFO]${NC}  $*"; }
log_warn()  { echo -e "${YELLOW}[WARN]${NC}  $*"; }
log_error() { echo -e "${RED}[ERROR]${NC} $*"; }
log_step()  { echo -e "${CYAN}[STEP]${NC}  $*"; }

die() { log_error "$*"; exit 1; }

# ── Build compose args ──────────────────────────────────────
compose_args() {
    local args="-f ${COMPOSE_FILE}"
    if [ "${PRODUCTION:-0}" != "1" ] && [ -f "$OVERRIDE_FILE" ]; then
        args="$args -f ${OVERRIDE_FILE}"
    fi
    echo "$args"
}

COMPOSE_ARGS=$(compose_args)

# ── Pre-flight ──────────────────────────────────────────────
check_prereqs() {
    log_step "Checking prerequisites..."

    if ! command -v docker &>/dev/null; then
        die "Docker is not installed. Install: https://docs.docker.com/engine/install/"
    fi
    log_info "Docker: $(docker --version)"

    if ! docker compose version &>/dev/null; then
        die "Docker Compose V2 is required. Install: https://docs.docker.com/compose/install/"
    fi
    log_info "Docker Compose: $(docker compose version)"

    # .env setup
    if [ ! -f "$ENV_FILE" ]; then
        log_warn ".env not found. Generating from .env.example..."

        if [ ! -f .env.example ]; then
            die ".env.example is missing — cannot generate .env"
        fi

        cp .env.example "$ENV_FILE"

        # Auto-generate secure secrets
        JWT_GEN=$(openssl rand -base64 32 2>/dev/null || echo "jwt-$(date +%s)$$-$RANDOM")
        ROOT_PW=$(openssl rand -base64 16 2>/dev/null || echo "root-$(date +%s)$$")
        APP_PW=$(openssl rand -base64 16 2>/dev/null || echo "app-$(date +%s)$$")
        RABBIT_PW=$(openssl rand -base64 16 2>/dev/null || echo "rabbit-$(date +%s)$$")

        if [[ "$(uname -s)" == "Darwin" ]]; then
            sed -i '' "s|^JWT_SECRET=.*|JWT_SECRET=$JWT_GEN|" .env
            sed -i '' "s|^MYSQL_ROOT_PASSWORD=.*|MYSQL_ROOT_PASSWORD=$ROOT_PW|" .env
            sed -i '' "s|^MYSQL_PASSWORD=.*|MYSQL_PASSWORD=$APP_PW|" .env
            sed -i '' "s|^RABBITMQ_PASSWORD=.*|RABBITMQ_PASSWORD=$RABBIT_PW|" .env
        else
            sed -i "s|^JWT_SECRET=.*|JWT_SECRET=$JWT_GEN|" .env
            sed -i "s|^MYSQL_ROOT_PASSWORD=.*|MYSQL_ROOT_PASSWORD=$ROOT_PW|" .env
            sed -i "s|^MYSQL_PASSWORD=.*|MYSQL_PASSWORD=$APP_PW|" .env
            sed -i "s|^RABBITMQ_PASSWORD=.*|RABBITMQ_PASSWORD=$RABBIT_PW|" .env
        fi

        log_info ".env generated with random secrets."
        log_warn "Review .env before deploying to production!"
    fi

    # Check for default passwords
    if grep -q "change-me" "$ENV_FILE" 2>/dev/null; then
        log_warn "Default passwords detected in .env — change them before production use!"
    fi

    log_info "Prerequisites OK."
}

# ── Commands ────────────────────────────────────────────────

cmd_build() {
    log_step "Building Docker images (this may take a while on first run)..."
    # shellcheck disable=SC2086
    docker compose ${COMPOSE_ARGS} build --parallel
    log_info "Build complete."
}

cmd_start() {
    log_step "Starting infrastructure (MySQL, Redis, RabbitMQ, ES, Nacos, Zipkin)..."
    # shellcheck disable=SC2086
    docker compose ${COMPOSE_ARGS} up -d --wait \
        mysql redis rabbitmq elasticsearch nacos zipkin 2>/dev/null || \
    docker compose ${COMPOSE_ARGS} up -d \
        mysql redis rabbitmq elasticsearch nacos zipkin

    log_info "Waiting for infrastructure to become healthy..."
    local waited=0
    local max_wait=120
    while [ $waited -lt $max_wait ]; do
        local mysql_ok
        local nacos_ok
        mysql_ok=$(docker inspect --format='{{.State.Health.Status}}' ecommerce-mysql 2>/dev/null || echo "starting")
        nacos_ok=$(docker inspect --format='{{.State.Health.Status}}' ecommerce-nacos 2>/dev/null || echo "starting")
        if [ "$mysql_ok" = "healthy" ] && [ "$nacos_ok" = "healthy" ]; then
            log_info "Infrastructure healthy! (MySQL: $mysql_ok, Nacos: $nacos_ok)"
            break
        fi
        sleep 5
        waited=$((waited + 5))
        echo "       Waiting... ${waited}s (MySQL: $mysql_ok, Nacos: $nacos_ok)"
    done

    if [ $waited -ge $max_wait ]; then
        log_warn "Health check timed out after ${max_wait}s. Proceeding anyway..."
    fi

    log_step "Starting backend services..."
    # shellcheck disable=SC2086
    docker compose ${COMPOSE_ARGS} up -d \
        gateway shop-bff product-service user-service cart-service order-service

    log_step "Starting frontend..."
    # shellcheck disable=SC2086
    docker compose ${COMPOSE_ARGS} up -d frontend

    log_info "Catalog data is seeded automatically by product-service on startup."
    log_info "Deployment complete!"
    echo ""
    echo "  ┌──────────────────────────────────────────────┐"
    echo "  │  Frontend:  http://localhost:${FRONTEND_PORT:-80}              │"
    echo "  │  API:       http://localhost:${GATEWAY_PORT:-8080}/api/        │"
    echo "  │  Nacos:     http://localhost:${NACOS_PORT:-8848}/nacos/       │"
    echo "  │  Zipkin:    http://localhost:${ZIPKIN_PORT:-9411}/            │"
    echo "  │  RabbitMQ:  http://localhost:${RABBITMQ_MGMT_PORT:-15672}/    │"
    echo "  └──────────────────────────────────────────────┘"
    echo ""
    echo "  Useful commands:"
    echo "    ./deploy.sh status        # Check all services"
    echo "    ./deploy.sh logs gateway  # View gateway logs"
    echo "    ./deploy.sh stop          # Stop all services"
    echo ""
}

cmd_stop() {
    log_step "Stopping all services..."
    # shellcheck disable=SC2086
    docker compose ${COMPOSE_ARGS} stop
    log_info "All services stopped."
}

cmd_down() {
    log_step "Stopping and removing containers..."
    # shellcheck disable=SC2086
    docker compose ${COMPOSE_ARGS} down --remove-orphans
    log_info "Containers removed. (Volumes preserved — use 'clean' to remove volumes too)"
}

cmd_restart() {
    log_step "Restarting all services..."
    # shellcheck disable=SC2086
    docker compose ${COMPOSE_ARGS} restart
    log_info "All services restarted."
}

cmd_logs() {
    local svc="${1:-}"
    if [ -n "$svc" ]; then
        # shellcheck disable=SC2086
        docker compose ${COMPOSE_ARGS} logs -f --tail=100 "$svc"
    else
        # shellcheck disable=SC2086
        docker compose ${COMPOSE_ARGS} logs -f --tail=100
    fi
}

cmd_status() {
    echo ""
    echo "============================================"
    echo "  Service Status"
    echo "============================================"
    # shellcheck disable=SC2086
    docker compose ${COMPOSE_ARGS} ps
    echo ""
    echo "============================================"
    echo "  Resource Usage"
    echo "============================================"
    docker stats --no-stream --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.MemPerc}}" \
        $(docker ps --filter "network=ecommerce-net" -q) 2>/dev/null || true
}

cmd_clean() {
    log_warn "This will remove ALL containers, volumes, networks, and project images!"
    read -rp "Are you sure? [y/N] " confirm
    if [ "$confirm" != "y" ] && [ "$confirm" != "Y" ]; then
        log_info "Aborted."
        return
    fi

    log_step "Stopping and removing containers & volumes..."
    # shellcheck disable=SC2086
    docker compose ${COMPOSE_ARGS} down -v --remove-orphans

    log_step "Removing project images..."
    docker images --format '{{.Repository}}:{{.Tag}}' 2>/dev/null | \
        grep 'ecommerce-' | xargs -r docker rmi 2>/dev/null || true

    log_info "Cleanup complete."
}

cmd_help() {
    echo "E-Commerce Platform — Deploy Script"
    echo ""
    echo "Usage: ./deploy.sh [COMMAND] [ARGS]"
    echo ""
    echo "Commands:"
    echo "  (none)      Full deploy: build + start"
    echo "  build       Build all Docker images"
    echo "  start       Start services (no rebuild)"
    echo "  stop        Stop all services"
    echo "  down        Stop & remove containers"
    echo "  restart     Restart all services"
    echo "  logs [svc]  Tail logs (optionally one service)"
    echo "  status      Show container status & health"
    echo "  clean       Remove everything (containers, volumes, images)"
    echo "  help        This help message"
    echo ""
    echo "Environment:"
    echo "  PRODUCTION=1   Skip docker-compose.override.yml"
    echo ""
    echo "Examples:"
    echo "  ./deploy.sh                  # Full production deploy"
    echo "  ./deploy.sh build            # Build images only"
    echo "  ./deploy.sh logs gateway     # Tail gateway logs"
    echo "  PRODUCTION=1 ./deploy.sh     # Deploy without override"
}

# ── Main ────────────────────────────────────────────────────
main() {
    local cmd="${1:-deploy}"

    case "$cmd" in
        deploy|"")
            check_prereqs
            cmd_build
            cmd_start
            ;;
        build)
            check_prereqs
            cmd_build
            ;;
        start)
            check_prereqs
            cmd_start
            ;;
        stop)
            cmd_stop
            ;;
        down)
            cmd_down
            ;;
        restart)
            cmd_restart
            ;;
        logs)
            cmd_logs "${2:-}"
            ;;
        status)
            cmd_status
            ;;
        clean)
            cmd_clean
            ;;
        help|--help|-h)
            cmd_help
            ;;
        *)
            log_error "Unknown command: $cmd"
            cmd_help
            exit 1
            ;;
    esac
}

main "$@"
