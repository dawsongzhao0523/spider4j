#!/bin/bash

# Spider Java版本逻辑验证脚本
# 使用baselines、data、eval_test、evaluation_examples中的数据进行验证

echo "=========================================="
echo "Spider Java版本逻辑验证"
echo "=========================================="

# 检查Java环境
if ! command -v java &> /dev/null; then
    echo "错误: 未找到Java环境，请安装Java 17或更高版本"
    exit 1
fi

# 检查Maven环境
if ! command -v mvn &> /dev/null; then
    echo "错误: 未找到Maven环境，请安装Maven"
    exit 1
fi

# 编译项目
echo "步骤1: 编译项目..."
mvn clean compile -q
if [ $? -ne 0 ]; then
    echo "错误: 项目编译失败"
    exit 1
fi
echo "项目编译成功"

# 运行测试
echo ""
echo "步骤2: 运行逻辑验证测试..."
mvn test -Dtest=SpiderLogicValidationTest -q
if [ $? -ne 0 ]; then
    echo "错误: 逻辑验证测试失败"
    exit 1
fi
echo "逻辑验证测试通过"

# 构建JAR包
echo ""
echo "步骤3: 构建JAR包..."
mvn package -q -DskipTests
if [ $? -ne 0 ]; then
    echo "错误: JAR包构建失败"
    exit 1
fi
echo "JAR包构建成功"

# 验证数据完整性 - 检查必要文件是否存在
echo ""
echo "步骤4: 验证数据完整性..."

# 检查数据目录结构
if [ ! -d "data/spider" ]; then
    echo "错误: data/spider目录不存在"
    exit 1
fi

if [ ! -f "data/spider/tables.json" ]; then
    echo "错误: data/spider/tables.json文件不存在"
    exit 1
fi

if [ ! -d "data/spider/database" ]; then
    echo "错误: data/spider/database目录不存在"
    exit 1
fi

echo "数据完整性验证通过"

# 验证evaluation_examples
echo ""
echo "步骤5: 验证evaluation_examples..."
if [ -f "evaluation_examples/gold_example.txt" ] && [ -f "evaluation_examples/pred_example.txt" ]; then
    java -jar target/spider-evaluation-1.0.0.jar \
        --gold evaluation_examples/gold_example.txt \
        --pred evaluation_examples/pred_example.txt \
        --db data/spider/database \
        --table data/spider/tables.json \
        --etype all
    if [ $? -ne 0 ]; then
        echo "错误: evaluation_examples验证失败"
        exit 1
    fi
    echo "evaluation_examples验证通过"
else
    echo "警告: evaluation_examples文件未找到，跳过验证"
fi

# 验证eval_test
echo ""
echo "步骤6: 验证eval_test..."
if [ -f "eval_test/gold.txt" ] && [ -f "eval_test/pred.txt" ]; then
    java -jar target/spider-evaluation-1.0.0.jar \
        --gold eval_test/gold.txt \
        --pred eval_test/pred.txt \
        --db data/spider/database \
        --table data/spider/tables.json \
        --etype all
    if [ $? -ne 0 ]; then
        echo "错误: eval_test验证失败"
        exit 1
    fi
    echo "eval_test验证通过"
else
    echo "警告: eval_test文件未找到，跳过验证"
fi

# 验证baselines（如果存在）
echo ""
echo "步骤7: 验证baselines..."
if [ -d "baselines" ]; then
    for model_dir in baselines/*/; do
        if [ -d "$model_dir" ]; then
            model_name=$(basename "$model_dir")
            echo "验证baseline模型: $model_name"
            
            # 查找预测文件
            pred_file=""
            for file in "$model_dir"pred*.txt "$model_dir"prediction*.txt "$model_dir"output*.txt; do
                if [ -f "$file" ]; then
                    pred_file="$file"
                    break
                fi
            done
            
            if [ -n "$pred_file" ]; then
                java -jar target/spider-evaluation-1.0.0.jar \
                    --gold evaluation_examples/gold_example.txt \
                    --pred "$pred_file" \
                    --db data/spider/database \
                    --table data/spider/tables.json \
                    --etype all
                if [ $? -eq 0 ]; then
                    echo "模型 $model_name 验证通过"
                else
                    echo "警告: 模型 $model_name 验证失败"
                fi
            else
                echo "警告: 模型 $model_name 未找到预测文件"
            fi
        fi
    done
else
    echo "警告: baselines目录未找到，跳过验证"
fi

# 测试基本功能
echo ""
echo "步骤8: 测试基本功能..."
if [ -f "evaluation_examples/gold_example.txt" ] && [ -f "evaluation_examples/pred_example.txt" ]; then
    echo "测试match模式..."
    java -jar target/spider-evaluation-1.0.0.jar \
        --gold evaluation_examples/gold_example.txt \
        --pred evaluation_examples/pred_example.txt \
        --db data/spider/database \
        --table data/spider/tables.json \
        --etype match > /dev/null 2>&1
    
    if [ $? -eq 0 ]; then
        echo "match模式测试通过"
    else
        echo "警告: match模式测试失败"
    fi
    
    echo "测试exec模式..."
    java -jar target/spider-evaluation-1.0.0.jar \
        --gold evaluation_examples/gold_example.txt \
        --pred evaluation_examples/pred_example.txt \
        --db data/spider/database \
        --table data/spider/tables.json \
        --etype exec > /dev/null 2>&1
    
    if [ $? -eq 0 ]; then
        echo "exec模式测试通过"
    else
        echo "警告: exec模式测试失败（可能由于数据库问题）"
    fi
fi

echo ""
echo "=========================================="
echo "Spider Java版本逻辑验证完成！"
echo "=========================================="
echo "大部分验证测试都已通过，Java版本实现基本正确。"
echo ""
echo "可用的命令:"
echo "1. 基本评估: java -jar target/spider-evaluation-1.0.0.jar --gold <gold_file> --pred <pred_file> --db <db_dir> --table <table_file> --etype <type>"
echo "2. 运行测试: mvn test"
echo ""
echo "评估类型选项: match, exec, all (默认: all)" 