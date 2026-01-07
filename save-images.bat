@echo off
echo ========================================
echo 打包 Docker 镜像到文件
echo ========================================

set IMAGES=^
xywpgx-gateway:latest ^
xywpgx-auth:latest ^
xywpgx-user:latest ^
xywpgx-item:latest ^
xywpgx-order:latest ^
xywpgx-notification:latest ^
xywpgx-storage:latest ^
xywpgx-review:latest

for %%i in (%IMAGES%) do (
    echo 正在保存镜像: %%i
    docker save -o images/%%i.tar %%i
)

echo.
echo ========================================
echo 所有镜像已保存到 images/ 目录
echo ========================================
pause
