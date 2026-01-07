# Docker 部署指南

## 部署流程

### 方案 A: 在线部署（推荐）

在有网络的环境中，直接构建运行：
```bash
# 1. 宿主机编译
mvn clean package -DskipTests -pl auth/auth-service,gateway,user,item,order,notification,storage,review -am

# 2. Docker 启动
docker-compose up -d --build
```

### 方案 B: 离线部署（打包镜像文件传输）

#### 步骤 1: 本地打包镜像
```bash
# Windows
save-images.bat

# 或手动执行
docker save -o images/xywpgx-gateway.tar xywpgx-gateway:latest
docker save -o images/xywpgx-auth.tar xywpgx-auth:latest
# ... 重复所有服务
```

#### 步骤 2: 传输到云服务器
将 `images/` 目录和 `docker-compose.yml` 传输到云服务器。

#### 步骤 3: 云服务器加载镜像并启动
```bash
# 加载所有镜像
chmod +x load-images.sh
./load-images.sh

# 启动服务
docker-compose up -d
```

## 服务配置

| 服务 | 镜像名 | 端口 |
|------|--------|------|
| gateway | xywpgx-gateway | 10010 |
| auth | xywpgx-auth | 8081 |
| user | xywpgx-user | 8082 |
| item | xywpgx-item | 8083 |
| order | xywpgx-order | 8084 |
| notification | xywpgx-notification | 8085 |
| storage | xywpgx-storage | 8086 |
| review | xywpgx-review | 8087 |

## JVM 配置
- `-Xms128m` 最小堆内存
- `-Xmx256m` 最大堆内存
- 时区: Asia/Shanghai

## 网络
所有服务在 `xywpgx-network` 桥接网络中，可通过服务名相互访问。
