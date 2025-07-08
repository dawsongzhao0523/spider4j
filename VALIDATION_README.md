# Spider Java版本逻辑验证框架

## 概述

这是一个完整的Spider NL2SQL数据集Java版本逻辑验证框架，用于验证您重写的Spider逻辑是否正确。该框架使用项目中的`baselines`、`data`、`eval_test`、`evaluation_examples`数据进行全面的验证测试。

## 项目结构

```
spider_j/
├── src/
│   ├── main/java/com/nl2sql/spider/
│   │   ├── SpiderEvaluationCLI.java          # 原始评估CLI
│   │   ├── SpiderValidationCLI.java          # 新增验证CLI
│   │   ├── evaluator/                        # 评估器
│   │   ├── service/                          # 服务层
│   │   ├── model/                            # 数据模型
│   │   ├── utils/
│   │   │   └── DataValidator.java            # 数据验证工具
│   │   └── ...
│   └── test/java/com/nl2sql/spider/
│       └── SpiderLogicValidationTest.java    # 逻辑验证测试
├── data/spider/                              # Spider数据集
├── baselines/                                # 基线模型结果
├── eval_test/                                # 评估测试数据
├── evaluation_examples/                      # 评估示例数据
├── validate_spider.sh                        # 验证脚本
└── pom.xml                                   # Maven配置
```

## 快速开始

### 1. 环境要求

- Java 17或更高版本
- Maven 3.6或更高版本
- 确保项目中包含以下数据目录：
  - `data/spider/` - Spider数据集
  - `evaluation_examples/` - 评估示例
  - `eval_test/` - 评估测试数据
  - `baselines/` - 基线模型结果（可选）

### 2. 一键验证

运行完整的验证流程：

```bash
chmod +x validate_spider.sh
./validate_spider.sh
```

这将执行以下步骤：
1. 编译项目
2. 运行逻辑验证测试
3. 构建JAR包
4. 验证数据完整性
5. 验证evaluation_examples
6. 验证eval_test
7. 验证baselines模型
8. 运行完整验证

### 3. 手动验证

#### 编译项目

```bash
mvn clean compile
```

#### 运行测试

```bash
mvn test -Dtest=SpiderLogicValidationTest
```

#### 构建JAR包

```bash
mvn package -DskipTests
```

## 验证命令

构建完成后，您可以使用以下命令进行验证：

### 1. 数据完整性验证

```bash
java -jar target/spider-evaluation-1.0.0.jar validate-data data/spider
```

验证：
- 目录结构完整性
- JSON文件格式正确性
- SQL文件格式正确性
- 数据库文件存在性
- 数据一致性

### 2. 逻辑正确性验证

```bash
java -jar target/spider-evaluation-1.0.0.jar validate-logic \
    evaluation_examples/gold_example.txt \
    evaluation_examples/pred_example.txt \
    data/spider/database \
    data/spider/tables.json \
    all
```

参数说明：
- `gold_example.txt` - 标准答案文件
- `pred_example.txt` - 预测结果文件
- `data/spider/database` - 数据库目录
- `data/spider/tables.json` - 表结构文件
- `all` - 评估类型（match/exec/all）

### 3. 完整验证

```bash
java -jar target/spider-evaluation-1.0.0.jar validate-all data/spider all
```

这将运行：
1. 数据完整性验证
2. evaluation_examples逻辑验证
3. eval_test数据验证

## 验证测试详情

### SpiderLogicValidationTest

该测试类包含6个测试方法：

1. **testEvaluationExamples** - 验证evaluation_examples中的示例数据
2. **testEvalTestData** - 验证eval_test中的测试数据
3. **testDevDataset** - 验证dev数据集
4. **testBaselinesModels** - 验证baselines中的不同模型结果
5. **testEvaluationTypeConsistency** - 验证不同评估类型的一致性
6. **testErrorHandling** - 验证错误处理机制

### 数据验证器

`DataValidator`类验证以下内容：

- **目录结构**：检查必需的文件和目录是否存在
- **JSON格式**：验证tables.json、dev.json、train_spider.json格式
- **SQL格式**：验证SQL文件的格式正确性
- **数据库文件**：检查SQLite数据库文件是否存在
- **数据一致性**：验证JSON中的db_id与schema的一致性

## 评估指标

验证框架支持以下评估指标：

### 1. SQL难度分级
- **Easy** - 简单查询
- **Medium** - 中等复杂度查询
- **Hard** - 复杂查询
- **Extra Hard** - 极复杂查询

### 2. 评估类型
- **match** - 仅结构匹配
- **exec** - 仅执行准确性
- **all** - 完整评估（结构+执行）

### 3. 评估组件
- SELECT列匹配
- WHERE条件匹配
- GROUP BY匹配
- ORDER BY匹配
- 聚合函数匹配
- 嵌套查询匹配
- 等等...

## 输出示例

### 验证成功输出

```
================================================================================
SPIDER EVALUATION RESULTS
================================================================================
Level                Count      Exact Match     Execution       Avg F1         
--------------------------------------------------------------------------------
Easy                 248        0.823           0.831           0.856          
Medium               446        0.654           0.668           0.712          
Hard                 174        0.471           0.488           0.534          
Extra Hard           167        0.287           0.299           0.342          
All                  1035       0.596           0.611           0.634          
--------------------------------------------------------------------------------
Errors: 0
```

### 数据验证输出

```
============================================================
VALIDATION SUMMARY
============================================================
Status: PASSED
Errors: 0
Warnings: 2
Infos: 15
============================================================
```

## 故障排除

### 常见问题

1. **编译错误**
   - 检查Java版本是否为17或更高
   - 检查Maven版本是否为3.6或更高

2. **数据文件未找到**
   - 确保data/spider目录存在
   - 检查必需的JSON和SQL文件是否存在

3. **测试失败**
   - 查看详细的错误日志
   - 检查数据库连接是否正常

4. **内存不足**
   - 增加JVM内存：`java -Xmx4g -jar ...`

### 调试模式

启用详细日志：

```bash
java -Dlogging.level.com.nl2sql.spider=DEBUG -jar target/spider-evaluation-1.0.0.jar validate-all data/spider
```

## 扩展功能

### 添加新的验证测试

1. 在`SpiderLogicValidationTest`中添加新的测试方法
2. 使用`@Test`和`@Order`注解
3. 遵循现有的测试模式

### 添加新的评估指标

1. 在`EvaluationStatistics`中添加新的统计字段
2. 在`SpiderEvaluator`中实现新的评估逻辑
3. 更新输出格式

### 支持新的数据格式

1. 在`DataValidator`中添加新的验证方法
2. 在相应的模型类中添加新的字段
3. 更新JSON解析逻辑

## 贡献指南

1. Fork本项目
2. 创建特性分支
3. 提交更改
4. 创建Pull Request

## 许可证

本项目采用与原Spider项目相同的许可证。

## 联系方式

如有问题或建议，请创建Issue或联系项目维护者。

---

**注意**：这个验证框架是为了确保您的Java版本Spider实现与原始Python版本保持一致。通过所有验证测试意味着您的实现是正确的。 