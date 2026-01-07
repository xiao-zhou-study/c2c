#!/bin/bash

# --- 1. 配置区域 ---
# 指定需要操作的服务模块，方便后续维护
MODULES="auth/auth-service,gateway,user,item,order,notification,storage,review"

# --- 2. 预清理 ---
echo "正在停止并清理旧容器..."
# down 确保环境干净，--remove-orphans 清理不在 compose 文件中的残留容器
docker compose down --remove-orphans

# --- 3. 宿主机多线程编译 ---
echo "开始 Maven 多线程加速编译..."
# -T 1C 根据 CPU 核心数并行编译，跳过测试以加快速度
mvn clean package -DskipTests -pl $MODULES -am -T 1C

# 检查 Maven 编译状态
if [ $? -ne 0 ]; then
    echo "❌ 错误：Maven 编译失败，脚本终止。"
    exit 1
fi

echo "✅ Maven 编译完成。"

# --- 4. Docker 启动 ---
echo "正在构建镜像并启动容器..."
# --build 确保 Docker 重新读取 target 目录下新生成的 jar 包
docker compose up -d --build

# --- 5. 结果校验 ---
if [ $? -eq 0 ]; then
    echo "==============================================="
    echo "🚀 部署成功！所有服务已在后台运行。"
    echo "你可以运行 'docker compose ps' 查看服务状态。"
    echo "==============================================="
else
    echo "❌ 错误：Docker 启动失败，请检查 docker-compose.yml 配置。"
    exit 1
fi