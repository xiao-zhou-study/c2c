#!/bin/bash

# --- 1. 配置区域 ---
# 远程仓库地址（用于记录或手动检查）
REPO_URL="git@github.com:xiao-zhou-study/wpgx.git"
# 指定需要编译的服务模块
MODULES="auth/auth-service,gateway,user,item,order,notification,storage,review"

echo "==============================================="
echo "        🚀 开始自动化部署流程"
echo "==============================================="

# --- 2. Git 更新 ---
echo "[步骤 1/4] 正在从远程仓库拉取最新代码..."
git pull
if [ $? -eq 0 ]; then
    echo "✅ 代码拉取成功。"
else
    echo "❌ 错误：Git pull 失败，请检查网络或 SSH 密钥配置。"
    exit 1
fi

# --- 3. 预清理 ---
echo "[步骤 2/4] 正在停止并清理旧容器与孤立镜像..."
# 使用 down 确保旧镜像层不会干扰新构建
docker compose down --remove-orphans
if [ $? -eq 0 ]; then
    echo "✅ 环境清理完成。"
else
    echo "⚠️ 警告：清理过程中出现异常，尝试继续执行..."
fi

# --- 4. 宿主机多线程编译 ---
echo "[步骤 3/4] 开始 Maven 多线程加速编译 (跳过测试)..."
# -am 表示同时编译依赖的模块，-T 1C 开启并行编译
mvn clean package -DskipTests -pl $MODULES -am -T 1C

if [ $? -eq 0 ]; then
    echo "✅ Maven 编译成功，Jar 包已生成。"
else
    echo "❌ 错误：Maven 编译失败，请查看上方错误日志。"
    exit 1
fi

# --- 5. Docker 构建与启动 ---
echo "[步骤 4/4] 正在构建镜像并启动容器..."
# --build 会根据新生成的 Jar 包重新构建镜像
docker compose up -d --build

if [ $? -eq 0 ]; then
    echo "==============================================="
    echo "🎉 所有任务执行成功！"
    echo "服务状态如下："
    docker compose ps
    echo "==============================================="
else
    echo "❌ 错误：Docker Compose 启动失败，请检查 yml 文件。"
    exit 1
fi