#!/bin/bash

# Spider Java版本演示脚本

echo "🚀 Spider NL2SQL Evaluation Framework (Java版本) 演示"
echo "=================================================="
echo ""

echo "📁 项目结构预览:"
echo "spider_j/"
echo "├── pom.xml                      # Maven构建配置 (JDK 17)"
echo "├── README_J.md                  # Java版本文档"
echo "├── build.sh                     # 构建脚本"
echo "├── demo.sh                      # 演示脚本 (本文件)"
echo "├── src/main/java/              # 主要源代码"
echo "│   └── com/nl2sql/spider/"
echo "│       ├── model/              # 数据模型层"
echo "│       ├── parser/             # SQL解析器"
echo "│       ├── evaluator/          # 评估器"
echo "│       ├── service/            # 服务层"
echo "│       ├── utils/              # 工具类"
echo "│       └── SpiderEvaluationCLI.java"
echo "├── src/test/java/              # 测试代码"
echo "├── baselines_j/                # Java版本基线模型"
echo "├── data_j/                     # 测试数据"
echo "├── eval_test_j/                # 评估测试"
echo "└── evaluation_examples_j/      # 评估示例"
echo ""

echo "🔧 技术栈:"
echo "• Java 17 (LTS版本)"
echo "• Maven 3.6+"
echo "• Jackson 2.15.2 (JSON处理)"
echo "• SQLite JDBC 3.42.0.0"
echo "• SLF4J + Logback (日志)"
echo "• JUnit 5 (测试)"
echo ""

echo "⚡ 核心功能:"
echo "• SQL解析器 - 将SQL字符串解析为结构化对象"
echo "• 多维度评估 - 精确匹配、部分匹配、执行准确性"
echo "• 难度分级 - Easy/Medium/Hard/Extra自动分级"
echo "• 命令行接口 - 与Python版本兼容的CLI"
echo "• 编程接口 - 易于集成的Java API"
echo ""

echo "🚀 快速开始:"
echo "1. 构建项目:"
echo "   cd spider_j && ./build.sh"
echo ""
echo "2. 运行示例:"
echo "   ./build.sh run"
echo ""
echo "3. 命令行使用:"
echo "   java -jar target/spider-evaluation-1.0.0.jar \\"
echo "     --gold gold.sql \\"
echo "     --pred pred.sql \\"
echo "     --db database/ \\"
echo "     --table tables.json \\"
echo "     --etype all"
echo ""

echo "📊 与Python版本对比:"
echo "┌─────────────────┬─────────────┬─────────────┐"
echo "│ 特性            │ Python版本  │ Java版本    │"
echo "├─────────────────┼─────────────┼─────────────┤"
echo "│ 性能            │ 中等        │ 高          │"
echo "│ 内存使用        │ 较高        │ 优化        │"
echo "│ 并发处理        │ 有限        │ 良好        │"
echo "│ 企业集成        │ 一般        │ 优秀        │"
echo "│ 部署便利性      │ 需Python环境│ 单一JAR文件 │"
echo "│ JDK版本         │ -           │ Java 17 LTS │"
echo "└─────────────────┴─────────────┴─────────────┘"
echo ""

echo "📖 详细文档请查看: spider_j/README_J.md"
echo ""

if [[ "$1" == "--build" ]]; then
    echo "🔨 开始构建项目..."
    ./build.sh package
elif [[ "$1" == "--run" ]]; then
    echo "🏃 构建并运行示例..."
    ./build.sh run
else
    echo "💡 提示:"
    echo "  运行 './demo.sh --build' 来构建项目"
    echo "  运行 './demo.sh --run' 来构建并运行示例"
    echo "  运行 './build.sh help' 查看所有构建选项"
fi

echo ""
echo "✨ Spider Java版本已准备就绪！" 