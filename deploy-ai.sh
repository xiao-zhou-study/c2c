#!/bin/bash

# --- 1. 配置区域 ---
# AI 项目根目录绝对路径
PROJECT_PATH="/usr/myprojects/backend/wpgx/ai"

echo "==============================================="
echo "        🤖 AI 模块部署流程 (强制更新版)"
echo "==============================================="

# --- 2. 切换工作目录 ---
echo "[步骤 0/4] 正在进入 AI 项目目录: $PROJECT_PATH"
if [ -d "$PROJECT_PATH" ]; then
    cd "$PROJECT_PATH" || exit 1
else
    echo "❌ 错误：目录 $PROJECT_PATH 不存在！"
    exit 1
fi

# --- 3. Git 更新 ---
echo "[步骤 1/4] 正在拉取远程代码..."
git pull
if [ $? -ne 0 ]; then
    echo "❌ 错误：Git pull 失败。"
    exit 1
fi

# --- 4. 彻底清理旧环境 ---
echo "[步骤 2/4] 正在停止旧容器并清理匿名卷..."
docker compose down --remove-orphans -v

# --- 5. 宿主机编译 (强制更新依赖) ---
echo "[步骤 3/4] 开始 Maven 编译 (强制更新 -U)..."
mvn clean package -DskipTests -U

if [ $? -eq 0 ]; then
    echo "✅ Maven 编译成功。"
else
    echo "❌ 错误：Maven 编译失败。"
    exit 1
fi

# --- 6. 强制重新构建镜像 ---
echo "[步骤 4/4] 正在强制重新构建镜像 (无缓存模式)..."
docker compose build --no-cache --pull

echo "🚀 正在启动 AI 服务..."
docker compose up -d

if [ $? -eq 0 ]; then
    echo "-----------------------------------------------"
    echo "🧹 正在执行系统资源回收..."
    docker image prune -f
    docker builder prune -f

    echo "==============================================="
    echo "🎉 AI 模块部署完成！"
    echo "==============================================="
else
    echo "❌ 错误：Docker 启动失败。"
    exit 1
fi