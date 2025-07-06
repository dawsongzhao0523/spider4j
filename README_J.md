# Spider NL2SQL Evaluation Framework (Java版本)

这是Spider NL2SQL测评体系的Java版本实现，用于评估自然语言到SQL（NL2SQL）模型的性能。该项目将原始的Python版本重写为Java，提供了完整的SQL解析、评估和统计功能。

## 功能特性

- **SQL解析器**: 将SQL查询解析为结构化表示
- **多维度评估**: 支持精确匹配、部分匹配和执行准确性评估
- **难度分级**: 自动评估SQL查询的难度级别（Easy/Medium/Hard/Extra）
- **详细统计**: 提供各个SQL组件的详细评估报告
- **命令行接口**: 易于集成到现有的NL2SQL产品中
- **高性能**: 基于Java 17实现，具有良好的性能表现

## 技术栈

- **Java**: 17 (LTS版本)
- **构建工具**: Maven 3.6+
- **JSON处理**: Jackson 2.15.2
- **数据库**: SQLite JDBC 3.42.0.0
- **日志**: SLF4J + Logback
- **测试**: JUnit 5

## 项目结构

```
spider_j/
├── pom.xml                           # Maven构建配置
├── README_J.md                       # Java版本说明文档
├── src/
│   ├── main/
│   │   ├── java/com/nl2sql/spider/
│   │   │   ├── constants/            # 常量定义
│   │   │   │   └── SqlConstants.java
│   │   │   ├── enums/               # 枚举类型
│   │   │   │   ├── EvaluationType.java
│   │   │   │   └── HardnessLevel.java
│   │   │   ├── model/               # 数据模型
│   │   │   │   ├── SqlStructure.java
│   │   │   │   ├── SelectClause.java
│   │   │   │   ├── FromClause.java
│   │   │   │   ├── TableUnit.java
│   │   │   │   ├── ColUnit.java
│   │   │   │   ├── ValUnit.java
│   │   │   │   ├── ConditionUnit.java
│   │   │   │   ├── OrderByClause.java
│   │   │   │   ├── DatabaseSchema.java
│   │   │   │   ├── SpiderDataItem.java
│   │   │   │   ├── EvaluationResult.java
│   │   │   │   ├── PartialScore.java
│   │   │   │   └── EvaluationStatistics.java
│   │   │   ├── parser/              # SQL解析器
│   │   │   │   └── SqlParser.java
│   │   │   ├── evaluator/           # 评估器
│   │   │   │   └── SpiderEvaluator.java
│   │   │   ├── service/             # 服务层
│   │   │   │   └── SpiderEvaluationService.java
│   │   │   ├── utils/               # 工具类
│   │   │   │   └── SqlTokenizer.java
│   │   │   └── SpiderEvaluationCLI.java  # 命令行接口
│   │   └── resources/
│   │       └── logback.xml          # 日志配置
│   └── test/
│       └── java/com/nl2sql/spider/
│           ├── utils/
│           │   └── SqlTokenizerTest.java
│           ├── parser/
│           │   └── SqlParserTest.java
│           ├── evaluator/
│           │   └── SpiderEvaluatorTest.java
│           └── service/
│               └── SpiderEvaluationServiceTest.java
├── baselines_j/                     # Java版本基线模型
├── data_j/                          # 测试数据
├── eval_test_j/                     # 评估测试
└── evaluation_examples_j/           # 评估示例
```

## 快速开始

### 环境要求

- Java 17 或更高版本
- Maven 3.6 或更高版本

### 编译项目

```bash
cd spider_j
mvn clean package
```

### 使用方法

#### 命令行使用

```bash
java -jar target/spider-evaluation-1.0.0.jar \
  --gold evaluation_examples_j/gold_example.txt \
  --pred evaluation_examples_j/pred_example.txt \
  --db data_j/ \
  --table data_j/test_tables.json \
  --etype match
```

#### 参数说明

- `--gold`: 标准答案文件路径（必需）
- `--pred`: 预测结果文件路径（必需）
- `--db`: 数据库目录路径（必需）
- `--table`: 表结构文件路径（必需）
- `--etype`: 评估类型，可选值：`match`、`exec`、`all`（默认：`all`）

#### 编程接口使用

```java
import com.nl2sql.spider.service.SpiderEvaluationService;
import com.nl2sql.spider.enums.EvaluationType;
import com.nl2sql.spider.model.EvaluationStatistics;

// 创建评估服务
SpiderEvaluationService service = new SpiderEvaluationService();

// 执行评估
EvaluationStatistics statistics = service.evaluate(
    "../data/spider/dev_gold.sql",
    "predictions.sql", 
    "../data/spider/database/",
    "../data/spider/tables.json",
    EvaluationType.ALL
);

// 打印结果
statistics.printResults();
```

## 评估指标

### 1. 精确匹配 (Exact Match)
评估预测SQL与标准SQL的完全匹配度

### 2. 执行准确性 (Execution Accuracy)
评估预测SQL在数据库上的执行结果是否与标准SQL一致

### 3. 部分匹配 (Partial Match)
分别评估SQL各个组件的匹配度：
- SELECT子句
- WHERE子句
- GROUP BY子句
- ORDER BY子句
- 聚合函数
- 条件操作符
- 关键字等

### 4. 难度分级
根据SQL复杂度自动分为四个级别：
- **Easy**: 简单查询
- **Medium**: 中等复杂度查询
- **Hard**: 复杂查询
- **Extra**: 极复杂查询

## 数据格式

### 标准答案文件格式
每行包含一个SQL查询和对应的数据库ID，用制表符分隔：
```
SELECT * FROM table1	db_id1
SELECT COUNT(*) FROM table2 WHERE condition = 'value'	db_id2
```

### 预测结果文件格式
每行包含一个预测的SQL查询：
```
SELECT * FROM table1
SELECT COUNT(*) FROM table2 WHERE condition = 'value'
```

### 表结构文件格式
JSON格式的数据库schema信息：
```json
[
  {
    "db_id": "database1",
    "table_names": ["table1", "table2"],
    "column_names": [[0, "id"], [0, "name"], [1, "value"]],
    "column_types": ["number", "text", "number"],
    "foreign_keys": [[2, 0]],
    "primary_keys": [0]
  }
]
```

## 性能优化

- 使用缓存机制存储数据库schema，避免重复加载
- 并行处理多个评估任务
- 优化SQL解析算法，提高解析速度
- 内存优化，支持大规模数据集评估

## 与Python版本的对比

| 特性 | Python版本 | Java版本 |
|------|------------|----------|
| 性能 | 中等 | 高 |
| 内存使用 | 较高 | 优化 |
| 并发处理 | 有限 | 良好 |
| 企业集成 | 一般 | 优秀 |
| 部署便利性 | 需要Python环境 | 单一JAR文件 |
| JDK版本 | - | Java 17 LTS |

## 开发指南

### 添加新的评估指标

1. 在`SpiderEvaluator`类中添加新的评估方法
2. 更新`EvaluationResult`模型以包含新指标
3. 修改统计输出格式

### 扩展SQL解析器

1. 在`SqlParser`类中添加新的解析规则
2. 更新对应的数据模型
3. 添加相应的测试用例

### 自定义输出格式

继承`EvaluationStatistics`类并重写`printResults()`方法。

## 测试

运行单元测试：
```bash
mvn test
```

运行集成测试：
```bash
mvn verify
```

运行特定测试：
```bash
mvn test -Dtest=SqlTokenizerTest
```

## 构建和部署

### 构建可执行JAR
```bash
mvn clean package
```

### 运行JAR文件
```bash
java -jar target/spider-evaluation-1.0.0.jar --help
```

### Docker部署
```dockerfile
FROM openjdk:17-jre-slim
COPY target/spider-evaluation-1.0.0.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 示例用法

### 基本评估
```bash
cd spider_j
java -jar target/spider-evaluation-1.0.0.jar \
  --gold ../evaluation_examples_j/gold_example.txt \
  --pred ../evaluation_examples_j/pred_example.txt \
  --db ../data_j/test_database/ \
  --table ../data_j/test_tables.json \
  --etype all
```

### 仅匹配评估
```bash
java -jar target/spider-evaluation-1.0.0.jar \
  --gold gold.sql \
  --pred pred.sql \
  --db database/ \
  --table tables.json \
  --etype match
```

### 仅执行评估
```bash
java -jar target/spider-evaluation-1.0.0.jar \
  --gold gold.sql \
  --pred pred.sql \
  --db database/ \
  --table tables.json \
  --etype exec
```

## 许可证

本项目采用与原始Spider项目相同的许可证。

## 贡献

欢迎提交Issue和Pull Request来改进项目。

## 引用

如果您在研究中使用了本项目，请引用原始的Spider论文：

```
@inproceedings{Yu&al.18c,
  title     = {Spider: A Large-Scale Human-Labeled Dataset for Complex and Cross-Domain Semantic Parsing and Text-to-SQL Task},
  author    = {Tao Yu and Rui Zhang and Kai Yang and Michihiro Yasunaga and Dongxu Wang and Zifan Li and James Ma and Irene Li and Qingning Yao and Shanelle Roman and Zilin Zhang and Dragomir Radev},
  booktitle = "Proceedings of the 2018 Conference on Empirical Methods in Natural Language Processing",
  year      = 2018
}
```

## 更新日志

### v1.0.0 (2025-07-06)
- 初始Java版本发布
- 支持完整的Spider评估功能
- 基于Java 17开发
- 提供命令行和编程接口
- 包含完整的测试套件 