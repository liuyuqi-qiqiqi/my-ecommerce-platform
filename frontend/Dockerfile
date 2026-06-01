# ============================================================
# Frontend - Multi-stage Docker Build
# Stage 1: Build the Vue/Vite app
# Stage 2: Serve with Nginx (reverse proxy to API Gateway)
# ============================================================

# ---- Stage 1: Build ----
FROM node:20-alpine AS build

WORKDIR /app

# Install dependencies first (cache layer)
COPY package.json package-lock.json* ./
RUN npm ci --silent 2>/dev/null || npm install --silent

# Copy config & source
COPY tsconfig*.json vite.config.ts index.html ./
COPY .env.production .env.production
COPY public/ public/
COPY src/ src/

# Build for production
RUN npm run build

# ---- Stage 2: Nginx Runtime ----
FROM nginx:1.27-alpine

RUN apk add --no-cache curl

# Remove default config, use ours as the only server block
RUN rm -f /etc/nginx/conf.d/default.conf

COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy built assets from build stage
COPY --from=build /app/dist /usr/share/nginx/html

# Health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
    CMD curl -sf http://localhost/health || exit 1

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
