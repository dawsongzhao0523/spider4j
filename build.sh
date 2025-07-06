#!/bin/bash

# Spider Java版本构建脚本
# 使用方法: ./build.sh [clean|test|package|run]

set -e

echo "Spider NL2SQL Evaluation Framework (Java版本) - 构建脚本"
echo "========================================================="

# 检查Java版本
java_version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
echo "检测到Java版本: $java_version"

if [[ ! "$java_version" =~ ^(17|18|19|20|21) ]]; then
    echo "警告: 建议使用Java 17或更高版本"
fi

# 检查Maven
if ! command -v mvn &> /dev/null; then
    echo "错误: 未找到Maven，请先安装Maven 3.6+"
    exit 1
fi

maven_version=$(mvn -version | head -n 1 | awk '{print $3}')
echo "检测到Maven版本: $maven_version"

# 解析命令行参数
ACTION=${1:-package}

case $ACTION in
    "clean")
        echo "清理项目..."
        mvn clean
        ;;
    "test")
        echo "运行测试..."
        mvn clean test
        ;;
    "package")
        echo "构建项目..."
        mvn clean package -DskipTests
        echo ""
        echo "构建完成！"
        echo "可执行文件位置: target/spider-evaluation-1.0.0.jar"
        echo ""
        echo "使用示例:"
        echo "java -jar target/spider-evaluation-1.0.0.jar \\"
        echo "  --gold ../evaluation_examples_j/gold_example.txt \\"
        echo "  --pred ../evaluation_examples_j/pred_example.txt \\"
        echo "  --db ../data/spider/database/ \\"
        echo "  --table ../data/spider/tables.json \\"
        echo "  --etype all"
        ;;
    "run")
        echo "构建并运行示例..."
        mvn clean package -DskipTests
        
        # 检查示例文件是否存在
        if [[ ! -f "evaluation_examples_j/gold_example.txt" ]]; then
            echo "错误: 示例文件不存在，请先创建evaluation_examples_j/目录下的示例文件"
            exit 1
        fi
        
        echo "运行评估示例..."
        java -jar target/spider-evaluation-1.0.0.jar \
          --gold evaluation_examples_j/gold_example.txt \
          --pred evaluation_examples_j/pred_example.txt \
          --db data_j/ \
          --table data_j/test_tables.json \
          --etype match
        ;;
    "help"|"-h"|"--help")
        echo "使用方法: ./build.sh [ACTION]"
        echo ""
        echo "可用的ACTION:"
        echo "  clean    - 清理项目"
        echo "  test     - 运行测试"
        echo "  package  - 构建项目 (默认)"
        echo "  run      - 构建并运行示例"
        echo "  help     - 显示此帮助信息"
        echo ""
        echo "示例:"
        echo "  ./build.sh          # 构建项目"
        echo "  ./build.sh test     # 运行测试"
        echo "  ./build.sh run      # 构建并运行示例"
        ;;
    *)
        echo "错误: 未知的操作 '$ACTION'"
        echo "使用 './build.sh help' 查看可用操作"
        exit 1
        ;;
esac

echo "完成！" 