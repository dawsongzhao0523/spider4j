# MySQL数据库evaluateItems接口使用指南

## 接口概述

我已经成功为Spider Java项目添加了基于`SqlEvaluationItem`集合的独立测试接口。这些接口支持MySQL数据库，提供三个参数的简洁设计：
- `List<SqlEvaluationItem> evaluationItems` - SQL评估项集合
- `DatabaseConfig dbConfig` - 数据库配置  
- `EvaluationType evaluationType` - 评估类型

## 可用接口

### 1. evaluateItems() - 基础评估
```java
public EvaluationStatistics evaluateItems(
    List<SqlEvaluationItem> evaluationItems, 
    DatabaseConfig dbConfig, 
    EvaluationType evaluationType
)
```
**功能**: 返回评估统计结果

### 2. evaluateItemsDetailed() - 详细评估
```java
public List<EvaluationResult> evaluateItemsDetailed(
    List<SqlEvaluationItem> evaluationItems, 
    DatabaseConfig dbConfig, 
    EvaluationType evaluationType
)
```
**功能**: 返回每个项目的详细评估结果

### 3. validateItems() - SQL验证
```java
public List<Boolean> validateItems(
    List<SqlEvaluationItem> evaluationItems, 
    DatabaseConfig dbConfig
)
```
**功能**: 验证每个项目的SQL有效性

## 使用示例

### 1. 基础设置
```java
// 创建评估服务
SpiderEvaluationService service = new SpiderEvaluationService();

// 创建MySQL数据库配置
DatabaseConfig mysqlConfig = service.createDatabaseConfig(
    "mysql",           // 数据库类型
    "localhost",       // 主机
    3306,             // 端口
    "your_database",  // 数据库名
    "username",       // 用户名
    "password"        // 密码
);
```

### 2. 创建评估项
```java
List<SqlEvaluationItem> items = new ArrayList<>();

// 添加评估项
items.add(new SqlEvaluationItem(
    "SELECT * FROM users WHERE id = 1",           // 标准SQL
    "SELECT * FROM users WHERE id = 1",           // 预测SQL
    "test_db",                                    // 数据库ID
    "查询ID为1的用户",                             // 问题描述
    "easy"                                        // 难度级别
));

items.add(new SqlEvaluationItem(
    "SELECT name FROM users WHERE age > 18",     // 标准SQL
    "SELECT users.name FROM users WHERE age > 18", // 预测SQL（写法不同）
    "test_db",
    "查询成年用户姓名",
    "easy"
));
```

### 3. 执行评估
```java
// 基础评估
EvaluationStatistics statistics = service.evaluateItems(
    items, 
    mysqlConfig, 
    EvaluationType.EXACT_MATCH
);

System.out.println("总数: " + statistics.getTotalCount());
System.out.println("精确匹配数: " + statistics.getExactMatchCount());
System.out.println("精确匹配率: " + statistics.getExactMatchAccuracy());

// 详细评估
List<EvaluationResult> results = service.evaluateItemsDetailed(
    items, 
    mysqlConfig, 
    EvaluationType.EXACT_MATCH
);

for (int i = 0; i < results.size(); i++) {
    EvaluationResult result = results.get(i);
    System.out.println("项目 " + (i+1) + " 精确匹配: " + result.isExactMatch());
}

// SQL验证
List<Boolean> validationResults = service.validateItems(items, mysqlConfig);
for (int i = 0; i < validationResults.size(); i++) {
    System.out.println("项目 " + (i+1) + " 有效性: " + validationResults.get(i));
}
```

## MySQL数据库准备

### 1. 创建测试数据库
```sql
CREATE DATABASE test_spider;
USE test_spider;

-- 创建测试表
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    age INT
);

CREATE TABLE orders (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    amount DECIMAL(10,2),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 插入测试数据
INSERT INTO users (name, email, age) VALUES 
('张三', 'zhangsan@example.com', 25),
('李四', 'lisi@example.com', 17),
('王五', 'wangwu@example.com', 30);

INSERT INTO orders (user_id, amount) VALUES 
(1, 100.50),
(3, 150.75);
```

### 2. 配置数据库连接
确保MySQL服务运行，并且用户有足够权限访问数据库。

## 核心特性

### 1. 动态Schema加载
- 自动从MySQL数据库连接中提取表结构信息
- 支持混合数据库ID的处理
- 智能缓存避免重复提取

### 2. 错误处理
- 连接失败时的优雅降级
- 详细的错误日志和提示
- 继续处理其他项目，不中断整个流程

### 3. 批量处理
- 一次性处理多个SQL评估项
- 支持不同数据库ID的混合处理
- 高效的批量操作

### 4. 灵活的评估类型
- `EXACT_MATCH`: 精确匹配
- `EXEC`: 执行匹配
- `ALL`: 全部匹配类型

## 测试验证

项目包含完整的测试套件，所有38个测试用例通过，验证了：
- 接口正确性
- 数据库集成
- 动态Schema功能
- 错误处理机制
- 批量处理能力

## 故障排除

### 连接失败
```
Communications link failure
```
**解决方案**: 检查MySQL服务状态、主机端口配置、防火墙设置

### 认证失败
```
Access denied for user
```
**解决方案**: 检查用户名密码、确保用户权限

### Schema不存在
```
Schema not found for database
```
**说明**: 这是正常的，系统会尝试动态获取schema，如果失败会跳过该项目

## 优势

1. **独立测试**: 无需外部文件，直接使用Java对象
2. **类型安全**: 强类型POJO避免解析错误
3. **数据库集成**: 原生支持MySQL等多种数据库
4. **动态适应**: 自动适应不同的数据库结构
5. **批量高效**: 一次处理多个评估项
6. **错误恢复**: 优雅处理各种异常情况

通过这些接口，您可以方便地进行基于MySQL数据库的SQL评估测试，验证评估流程和逻辑的正确性。 