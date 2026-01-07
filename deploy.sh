#!/bin/bash

# --- 1. 配置区域 ---
# 定义镜像版本号（如果 Dockerfile 中引用了变量）
export VERSION=1.0.0
# 指定需要操作的服务模块
MODULES="auth/auth-service,gateway,user,item,order,notification,storage,review"

# --- 2. 预清理 ---
echo "清理旧的容器与孤立镜像..."
# down 会停止并移除容器、网络，--remove-orphans 清理配置文件中不再定义的容器
docker compose down --remove-orphans

# --- 3. 宿主机多线程编译 ---
echo "开始 Maven 多线程编译 (使用 -T 1C 加速)..."
# -T 1C 表示每个 CPU 核心启动一个线程，通常能缩短 30%-50% 的时间
mvn clean package -DskipTests -pl $MODULES -am -T 1C

# 检查 Maven 编译是否成功
if [ $? -ne 0 ]; then
    echo "❌ Maven 编译失败，请检查代码或依赖！"
    exit 1
fi

echo "✅ Maven 编译成功。"

# --- 4. Docker 启动 ---
echo "正在并行构建并启动 Docker 容器..."
# --build 强制重新构建镜像
docker compose up -d --build

# 检查 Docker 启动状态
if [ $? -eq 0 ]; then
    echo "-----------------------------------------------"
    echo "🚀 部署脚本执行完毕！服务已在后台运行。"
    echo "使用 'docker compose ps' 查看状态。"
    echo "-----------------------------------------------"
else
    echo "❌ Docker 启动失败，请检查 Docker 日志。"
    exit 1
fi