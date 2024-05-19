#!/bin/bash

# 目标目录
TARGET_DIR="./scripts/scripts"

# 遍历目录中的所有文件
find "$TARGET_DIR" -type f | while read -r file; do
    # 使用 file 命令检测文件编码
    encoding=$(uchardet "$file")

    # 如果检测到的编码不是 utf-8，使用 iconv 转换
    if [ "$encoding" != "UTF-8" ] && [ ! -z "$encoding" ]; then
        # 输出当前处理的文件和编码
        echo "Converting $file from $encoding to UTF-8"
        # 使用 iconv 转换编码
        iconv -f "$encoding" -t UTF-8 "$file" -o "${file}.utf8"
        # 重命名新文件，覆盖旧文件（可选）
        mv "${file}.utf8" "$file"
    fi
done
