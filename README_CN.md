# Spider NL2SQL Evaluation Framework (Java版本)

[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.oracle.com/java/)
[![Maven](https://img.shields.io/badge/Maven-3.6+-red.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

这是Spider NL2SQL测评体系的Java版本实现，用于评估自然语言到SQL（NL2SQL）模型的性能。该项目将原始的Python版本重写为Java，提供了完整的SQL解析、评估和统计功能。

## 🚀 功能特性

- **完整的SQL解析器**: 将SQL查询解析为结构化表示
- **多维度评估**: 支持精确匹配、部分匹配和执行准确性评估
- **难度分级**: 自动评估SQL查询的难度级别（Easy/Medium/Hard/Extra）
- **详细统计**: 提供各个SQL组件的详细评估报告
- **命令行接口**: 易于集成到现有的NL2SQL产品中
- **高性能**: 基于Java 17实现，具有良好的性能表现
- **企业级**: 适合生产环境使用的稳定架构

## 📋 环境要求

- Java 17 或更高版本
- Maven 3.6 或更高版本
- 内存: 建议2GB以上
- 磁盘空间: 100MB以上

## 🛠️ 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/dawsongzhao0523/spider4j.git
cd spider-evaluation-java
```

### 2. 编译项目

```bash
# 使用Maven编译
mvn clean package

# 或者使用提供的构建脚本
./build.sh
```

### 3. 运行示例

```bash
java -jar target/spider-evaluation-1.0.0.jar \
  --gold evaluation_examples_j/gold_example.txt \
  --pred evaluation_examples_j/pred_example.txt \
  --db data_j/ \
  --table data_j/test_tables.json \
  --etype match
```

## 📖 使用说明

### 命令行参数

```bash
java -jar spider-evaluation-1.0.0.jar [OPTIONS]

选项:
  --gold <file>     标准答案文件路径 (必需)
  --pred <file>     预测结果文件路径 (必需)
  --db <dir>        数据库目录路径 (必需)
  --table <file>    表结构文件路径 (必需)
  --etype <type>    评估类型: match, exec, all (默认: all)
```

### 评估类型说明

- `match`: 仅进行SQL结构匹配评估
- `exec`: 仅进行SQL执行结果评估
- `all`: 进行完整评估（结构匹配 + 执行结果）

### 数据格式

#### 标准答案文件格式 (gold_example.txt)
```
SELECT * FROM table1	db_id1
SELECT COUNT(*) FROM table2 WHERE condition = 'value'	db_id2
```

#### 预测结果文件格式 (pred_example.txt)
```
SELECT * FROM table1
SELECT COUNT(*) FROM table2 WHERE condition = 'value'
```

#### 表结构文件格式 (tables.json)
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

## 🏗️ 项目结构

```
spider-evaluation-java/
├── pom.xml                           # Maven构建配置
├── README.md                         # 项目说明文档
├── build.sh                          # 构建脚本
├── src/
│   ├── main/
│   │   ├── java/com/nl2sql/spider/
│   │   │   ├── constants/            # 常量定义
│   │   │   ├── enums/               # 枚举类型
│   │   │   ├── model/               # 数据模型
│   │   │   ├── parser/              # SQL解析器
│   │   │   ├── evaluator/           # 评估器
│   │   │   ├── service/             # 服务层
│   │   │   ├── utils/               # 工具类
│   │   │   └── SpiderEvaluationCLI.java  # 命令行接口
│   │   └── resources/
│   │       └── logback.xml          # 日志配置
│   └── test/
│       └── java/                    # 测试代码
├── data_j/                          # 测试数据
├── evaluation_examples_j/           # 评估示例
└── target/                          # 编译输出
```

## 📊 评估指标

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

## 🔧 开发指南

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

## 🧪 测试

### 运行单元测试
```bash
mvn test
```

### 运行集成测试
```bash
mvn verify
```

### 运行特定测试
```bash
mvn test -Dtest=SqlTokenizerTest
```

## 📦 构建和部署

### 构建可执行JAR
```bash
mvn clean package
```

### Docker部署
```dockerfile
FROM openjdk:17-jre-slim
COPY target/spider-evaluation-1.0.0.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### 构建Docker镜像
```bash
docker build -t spider-evaluation:1.0.0 .
```

## 🚀 性能优化

- 使用缓存机制存储数据库schema，避免重复加载
- 并行处理多个评估任务
- 优化SQL解析算法，提高解析速度
- 内存优化，支持大规模数据集评估

## 📈 与Python版本的对比

| 特性 | Python版本 | Java版本 |
|------|------------|----------|
| 性能 | 中等 | 高 |
| 内存使用 | 较高 | 优化 |
| 并发处理 | 有限 | 良好 |
| 企业集成 | 一般 | 优秀 |
| 部署便利性 | 需要Python环境 | 单一JAR文件 |
| JDK版本 | - | Java 17 LTS |

## 🤝 贡献指南

我们欢迎所有形式的贡献！请阅读 [CONTRIBUTING.md](CONTRIBUTING.md) 了解详细信息。

### 贡献方式

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

### 代码规范

- 使用Java标准代码风格
- 添加适当的注释和文档
- 编写单元测试
- 遵循项目的架构模式

## 📄 许可证

本项目采用MIT许可证。详情请参阅 [LICENSE](LICENSE) 文件。

## 🙏 致谢

- 感谢原始Spider项目的作者
- 感谢所有贡献者的努力
- 感谢开源社区的支持

## 📚 相关资源

- [Spider官方网站](https://yale-lily.github.io/spider)
- [原始Python版本](https://github.com/taoyds/spider)
- [NL2SQL相关论文](https://arxiv.org/abs/1809.08887)

## 📞 联系我们

- 项目主页: https://github.com/dawsongzhao0523/spider4j
- 问题反馈: https://github.com/dawsongzhao0523/spider4j/issues
- 邮箱: dawsongzhao0523@gmail.com

## 🔖 引用

如果您在研究中使用了本项目，请引用原始的Spider论文：

```bibtex
@inproceedings{Yu&al.18c,
  title     = {Spider: A Large-Scale Human-Labeled Dataset for Complex and Cross-Domain Semantic Parsing and Text-to-SQL Task},
  author    = {Tao Yu and Rui Zhang and Kai Yang and Michihiro Yasunaga and Dongxu Wang and Zifan Li and James Ma and Irene Li and Qingning Yao and Shanelle Roman and Zilin Zhang and Dragomir Radev},
  booktitle = "Proceedings of the 2018 Conference on Empirical Methods in Natural Language Processing",
  year      = 2018
}
```

---

⭐ 如果这个项目对您有帮助，请给我们一个星标！ 