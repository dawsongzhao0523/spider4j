# SqlEvaluationItem 集合接口使用指南

## 概述

`SqlEvaluationItem` 是一个新的POJO类，用于独立的基于文件的测试。它将GoldItem和Predictions合并为一个Java对象，支持更灵活的SQL评估。

## 核心类介绍

### SqlEvaluationItem

```java
public class SqlEvaluationItem {
    private String goldSql;      // 标准答案SQL
    private String predictionSql; // 预测SQL
    private String dbId;         // 数据库ID
    private String question;     // 可选：自然语言问题
    private String difficulty;   // 可选：难度级别
    
    // 构造函数和getter/setter方法
}
```

## 主要接口

### 1. evaluateItems() - 基础评估
返回评估统计结果

```java
public EvaluationStatistics evaluateItems(
    List<SqlEvaluationItem> evaluationItems, 
    DatabaseConfig dbConfig, 
    EvaluationType evaluationType
)
```

### 2. evaluateItemsDetailed() - 详细评估  
返回每个项目的详细评估结果

```java
public List<EvaluationResult> evaluateItemsDetailed(
    List<SqlEvaluationItem> evaluationItems, 
    DatabaseConfig dbConfig, 
    EvaluationType evaluationType
)
```

### 3. validateItems() - SQL验证
验证SQL语法和表结构的有效性

```java
public List<Boolean> validateItems(
    List<SqlEvaluationItem> evaluationItems, 
    DatabaseConfig dbConfig
)
```

## 使用示例

### 创建评估项集合

```java
List<SqlEvaluationItem> items = new ArrayList<>();

// 精确匹配的SQL
items.add(new SqlEvaluationItem(
    "SELECT * FROM users", 
    "SELECT * FROM users", 
    "test_db",
    "查询所有用户"
));

// 不匹配的SQL
items.add(new SqlEvaluationItem(
    "SELECT id, name FROM users", 
    "SELECT name, id FROM users", 
    "test_db",
    "查询用户ID和姓名"
));
```

### 执行评估

```java
SpiderEvaluationService service = new SpiderEvaluationService();
DatabaseConfig dbConfig = service.createDatabaseConfig("h2", "mem", 0, "testdb", "sa", "");

// 基础评估
EvaluationStatistics statistics = service.evaluateItems(items, dbConfig, EvaluationType.EXACT_MATCH);

// 详细评估
List<EvaluationResult> results = service.evaluateItemsDetailed(items, dbConfig, EvaluationType.EXACT_MATCH);

// SQL验证
List<Boolean> validationResults = service.validateItems(items, dbConfig);
```

## 优势

1. **独立测试**: 无需依赖外部文件，直接在代码中定义测试数据
2. **类型安全**: 使用强类型POJO，避免字符串解析错误
3. **灵活配置**: 支持不同数据库配置和评估类型
4. **动态Schema**: 自动从数据库连接中提取表结构信息
5. **批量处理**: 一次性处理多个SQL评估项
6. **详细结果**: 可选择返回统计结果或详细的每项结果

## 注意事项

1. 确保数据库配置正确，系统会自动连接并提取schema信息
2. 不同的dbId会导致系统尝试连接不同的数据库
3. 如果数据库连接失败，相关的评估项会被跳过
4. 建议使用内存数据库（如H2）进行测试，避免外部依赖

## 测试验证

项目包含完整的测试套件，验证了：
- 基础评估功能
- 详细评估功能  
- SQL验证功能
- 错误处理机制
- 混合数据库ID支持

所有38个测试用例均通过，确保功能稳定可靠。 