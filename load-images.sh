#!/bin/bash
echo "========================================"
echo "加载 Docker 镜像"
echo "========================================"

for tar in images/*.tar; do
    if [ -f "$tar" ]; then
        echo "正在加载: $tar"
        docker load -i "$tar"
    fi
done

echo ""
echo "========================================"
echo "所有镜像已加载完成"
echo "========================================"
docker images | grep xywpgx
