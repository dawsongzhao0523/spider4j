#!/bin/bash

# 数据库扩展功能测试脚本

echo "=========================================="
echo "Spider Java 数据库扩展功能测试"
echo "=========================================="

# 编译项目
echo "步骤1: 编译项目..."
mvn clean compile -q
if [ $? -ne 0 ]; then
    echo "错误: 项目编译失败"
    exit 1
fi
echo "项目编译成功"

# 运行扩展功能测试
echo ""
echo "步骤2: 运行扩展功能测试..."
mvn test -Dtest=SpiderEvaluatorExtensionTest -q
if [ $? -ne 0 ]; then
    echo "错误: 扩展功能测试失败"
    exit 1
fi
echo "扩展功能测试通过"

# 运行所有测试
echo ""
echo "步骤3: 运行所有测试..."
mvn test -q
if [ $? -ne 0 ]; then
    echo "错误: 测试失败"
    exit 1
fi
echo "所有测试通过"

# 构建JAR包
echo ""
echo "步骤4: 构建JAR包..."
mvn package -DskipTests -q
if [ $? -ne 0 ]; then
    echo "错误: JAR包构建失败"
    exit 1
fi
echo "JAR包构建成功"

# 测试基本功能
echo ""
echo "步骤5: 测试基本功能..."
java -jar target/spider-evaluation-1.0.0.jar --gold evaluation_examples/gold_example.txt --pred evaluation_examples/pred_example.txt --db data/spider/database --table data/spider/tables.json --etype match > /dev/null 2>&1
if [ $? -ne 0 ]; then
    echo "错误: 基本功能测试失败"
    exit 1
fi
echo "基本功能测试通过"

echo ""
echo "=========================================="
echo "✅ 所有测试通过！"
echo "=========================================="
echo ""
echo "数据库扩展功能已成功实现，包括："
echo "1. 支持多种数据库类型: SQLite, MySQL, PostgreSQL, H2, Oracle, SQL Server"
echo "2. 数据库配置类: DatabaseConfig"
echo "3. 连接管理器: DatabaseConnectionManager"
echo "4. 扩展的评估方法: evaluateExecution() 和 isValidSql() 的重载版本"
echo "5. 向后兼容: 原有的 String dbPath 方法仍然有效"
echo ""
echo "使用示例："
echo "  // SQLite (原有方式)"
echo "  evaluator.evaluateExecution(\"path/to/db.sqlite\", predSql, goldSql);"
echo ""
echo "  // MySQL (新方式)"
echo "  DatabaseConfig config = new DatabaseConfig(DatabaseType.MYSQL, \"localhost\", 3306, \"db\", \"user\", \"pass\");"
echo "  evaluator.evaluateExecution(config, predSql, goldSql);"
echo ""
echo "  // 使用现有连接"
echo "  evaluator.evaluateExecution(connection, predSql, goldSql);"
echo "" 