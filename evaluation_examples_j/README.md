# Evaluation Examples (Java版本)

这个目录包含了一些Spider评估的示例文件，用于演示Java版本的评估功能。

## 文件说明

### gold_example.txt
标准答案文件示例，每行包含一个SQL查询和对应的数据库ID，用制表符分隔。

格式：`SQL查询 \t 数据库ID`

### pred_example.txt  
预测结果文件示例，每行包含一个预测的SQL查询。

### eval_result_example.txt
评估结果示例，展示了Java版本评估器的输出格式。

## 使用示例

```bash
cd spider_j

# 编译项目
mvn clean package

# 运行评估示例
java -jar target/spider-evaluation-1.0.0.jar \
  --gold evaluation_examples_j/gold_example.txt \
  --pred evaluation_examples_j/pred_example.txt \
  --db ../data/spider/database/ \
  --table ../data/spider/tables.json \
  --etype all
```

## 评估指标说明

### 难度级别
- **easy**: 简单查询，通常只包含基本的SELECT、WHERE子句
- **medium**: 中等复杂度，可能包含JOIN、聚合函数等
- **hard**: 复杂查询，包含多个JOIN、嵌套查询等
- **extra**: 极复杂查询，包含多层嵌套、复杂的逻辑等

### 评估类型
- **Exact Match**: 预测SQL与标准SQL完全匹配的比例
- **Execution**: 预测SQL执行结果与标准SQL执行结果相同的比例
- **Avg F1**: 各个SQL组件F1分数的平均值

### 组件评估
- **select**: SELECT子句评估
- **select(no AGG)**: 不考虑聚合函数的SELECT子句评估
- **where**: WHERE子句评估
- **where(no OP)**: 不考虑操作符的WHERE子句评估
- **group(no Having)**: 不考虑HAVING的GROUP BY子句评估
- **group**: 包含HAVING的GROUP BY子句评估
- **order**: ORDER BY子句评估
- **and/or**: AND/OR逻辑操作符评估
- **IUEN**: INTERSECT/UNION/EXCEPT/NESTED查询评估
- **keywords**: SQL关键字评估 