#!/bin/bash

# Spider Java版本示例运行脚本
# Example execution script for Spider Java version

set -e

echo "🚀 Spider NL2SQL Evaluation Framework - Example Run"
echo "=================================================="
echo ""

# 检查Java版本
echo "📋 检查环境..."
java -version 2>&1 | head -n 1
mvn -version 2>&1 | head -n 1
echo ""

# 构建项目
echo "🔨 构建项目..."
if [ ! -f "target/spider-evaluation-1.0.0.jar" ]; then
    echo "  编译项目..."
    mvn clean package -DskipTests=true
else
    echo "  JAR文件已存在，跳过编译"
fi
echo ""

# 检查数据文件
echo "📁 检查数据文件..."
if [ ! -f "data_j/test_tables.json" ]; then
    echo "❌ 缺少tables.json文件"
    exit 1
fi

if [ ! -f "data_j/department_management/department_management.sqlite" ]; then
    echo "❌ 缺少数据库文件"
    exit 1
fi

if [ ! -f "evaluation_examples_j/gold_example.txt" ]; then
    echo "❌ 缺少gold示例文件"
    exit 1
fi

if [ ! -f "evaluation_examples_j/pred_example.txt" ]; then
    echo "❌ 缺少pred示例文件"
    exit 1
fi

echo "  ✅ 所有数据文件检查通过"
echo ""

# 运行示例评估
echo "🏃 运行示例评估..."
echo "  命令: java -jar target/spider-evaluation-1.0.0.jar \\"
echo "    --gold evaluation_examples_j/gold_example.txt \\"
echo "    --pred evaluation_examples_j/pred_example.txt \\"
echo "    --db data_j/ \\"
echo "    --table data_j/test_tables.json \\"
echo "    --etype all"
echo ""

java -jar target/spider-evaluation-1.0.0.jar \
  --gold evaluation_examples_j/gold_example.txt \
  --pred evaluation_examples_j/pred_example.txt \
  --db data_j/ \
  --table data_j/test_tables.json \
  --etype all

echo ""
echo "✅ 示例运行完成！"
echo ""
echo "💡 提示："
echo "  • 上述结果显示了Java版本评估器的完整功能"
echo "  • 可以看到精确匹配、执行匹配等各项指标"
echo "  • 支持不同难度级别的SQL查询评估"
echo "  • 性能比Python版本显著提升"
echo ""
echo "📖 更多使用方法请查看 README.md"
