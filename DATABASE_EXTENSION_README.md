# Spider Java 数据库扩展功能

## 概述

Spider Java项目已成功扩展，现在支持多种数据库类型，不再局限于SQLite文件路径格式。新的扩展功能提供了灵活的数据库连接配置，支持主流数据库系统。

## 🚀 新功能特性

### 1. 支持的数据库类型

- **SQLite** - 文件数据库（原有功能，保持向后兼容）
- **MySQL** - 世界上最流行的开源数据库
- **PostgreSQL** - 功能强大的开源对象关系数据库
- **Oracle** - 企业级关系数据库
- **SQL Server** - Microsoft的关系数据库管理系统
- **H2** - 轻量级内存/文件数据库

### 2. 核心组件

#### DatabaseConfig 类
- 统一的数据库配置管理
- 支持不同数据库类型的连接参数
- 自动构建JDBC URL
- 支持额外连接属性

#### DatabaseConnectionManager 类
- 数据库连接创建和管理
- 驱动自动加载
- 连接测试功能
- 安全的连接关闭

#### SpiderEvaluator 扩展方法
- `evaluateExecution(DatabaseConfig config, String predictedSql, String goldSql)`
- `evaluateExecution(Connection conn, String predictedSql, String goldSql)`
- `isValidSql(DatabaseConfig config, String sql)`
- `isValidSql(Connection conn, String sql)`

## 📝 使用示例

### 1. SQLite 数据库（向后兼容）

```java
// 原有方式仍然有效
SpiderEvaluator evaluator = new SpiderEvaluator();
boolean result = evaluator.evaluateExecution(
    "path/to/database.sqlite", 
    predictedSql, 
    goldSql
);

// 新方式
DatabaseConfig config = new DatabaseConfig("path/to/database.sqlite");
boolean result = evaluator.evaluateExecution(config, predictedSql, goldSql);
```

### 2. MySQL 数据库

```java
DatabaseConfig config = new DatabaseConfig(
    DatabaseConfig.DatabaseType.MYSQL,
    "localhost",        // 主机
    3306,              // 端口
    "spider_db",       // 数据库名
    "root",            // 用户名
    "password"         // 密码
);

// 添加额外连接属性
config.addProperty("useSSL", "false");
config.addProperty("serverTimezone", "UTC");

SpiderEvaluator evaluator = new SpiderEvaluator();
boolean result = evaluator.evaluateExecution(config, predictedSql, goldSql);
```

### 3. PostgreSQL 数据库

```java
DatabaseConfig config = new DatabaseConfig(
    DatabaseConfig.DatabaseType.POSTGRESQL,
    "localhost",
    5432,
    "spider_db",
    "postgres",
    "password"
);

boolean result = evaluator.evaluateExecution(config, predictedSql, goldSql);
```

### 4. H2 内存数据库

```java
DatabaseConfig config = new DatabaseConfig();
config.setType(DatabaseConfig.DatabaseType.H2);
config.setDatabase("testdb");  // 内存数据库

// 测试连接
boolean canConnect = DatabaseConnectionManager.testConnection(config);

if (canConnect) {
    boolean result = evaluator.evaluateExecution(config, predictedSql, goldSql);
}
```

### 5. 使用现有连接

```java
DatabaseConfig config = new DatabaseConfig(
    DatabaseConfig.DatabaseType.MYSQL,
    "localhost", 3306, "spider_db", "root", "password"
);

try (Connection conn = DatabaseConnectionManager.createConnection(config)) {
    // 直接使用连接进行多次评估
    boolean result1 = evaluator.evaluateExecution(conn, sql1, gold1);
    boolean result2 = evaluator.evaluateExecution(conn, sql2, gold2);
    
    // SQL有效性验证
    boolean isValid = evaluator.isValidSql(conn, testSql);
}
```

## 🔧 配置选项

### 数据库类型和默认端口

| 数据库类型 | 默认端口 | JDBC URL 前缀 |
|-----------|---------|---------------|
| SQLite    | N/A     | jdbc:sqlite: |
| MySQL     | 3306    | jdbc:mysql:// |
| PostgreSQL| 5432    | jdbc:postgresql:// |
| Oracle    | 1521    | jdbc:oracle:thin:@ |
| SQL Server| 1433    | jdbc:sqlserver:// |
| H2        | 9092    | jdbc:h2: |

### 连接属性示例

```java
DatabaseConfig config = new DatabaseConfig(/*...*/);

// MySQL 特定属性
config.addProperty("useSSL", "false");
config.addProperty("allowPublicKeyRetrieval", "true");
config.addProperty("serverTimezone", "UTC");

// PostgreSQL 特定属性
config.addProperty("ssl", "false");
config.addProperty("loggerLevel", "OFF");

// 通用属性
config.addProperty("connectTimeout", "30000");
config.addProperty("socketTimeout", "60000");
```

## 🧪 测试

### 运行扩展功能测试

```bash
# 运行扩展功能测试
mvn test -Dtest=SpiderEvaluatorExtensionTest

# 运行所有测试
mvn test

# 编译和打包
mvn clean package
```

### 测试覆盖范围

- 数据库配置类功能测试
- 连接管理器测试
- 多数据库类型支持验证
- SpiderEvaluator扩展方法测试
- 向后兼容性测试

## 📦 依赖管理

项目已添加以下数据库驱动依赖（标记为optional，按需使用）：

```xml
<!-- MySQL -->
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.33</version>
    <optional>true</optional>
</dependency>

<!-- PostgreSQL -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.6.0</version>
    <optional>true</optional>
</dependency>

<!-- H2 -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <version>2.2.220</version>
    <optional>true</optional>
</dependency>

<!-- Oracle -->
<dependency>
    <groupId>com.oracle.database.jdbc</groupId>
    <artifactId>ojdbc8</artifactId>
    <version>21.9.0.0</version>
    <optional>true</optional>
</dependency>

<!-- SQL Server -->
<dependency>
    <groupId>com.microsoft.sqlserver</groupId>
    <artifactId>mssql-jdbc</artifactId>
    <version>12.4.0.jre11</version>
    <optional>true</optional>
</dependency>
```

## 🔄 向后兼容性

所有原有的API仍然有效：

```java
// 这些方法仍然正常工作
evaluator.evaluateExecution(String dbPath, String predictedSql, String goldSql)
evaluator.isValidSql(String dbPath, String sql)
```

新的重载方法提供了额外的灵活性，但不会破坏现有代码。

## 🚨 注意事项

1. **驱动依赖**: 确保目标数据库的JDBC驱动在classpath中
2. **连接权限**: 确保数据库用户有足够的权限执行SQL查询
3. **网络连接**: 远程数据库需要确保网络连通性
4. **资源管理**: 使用try-with-resources语句确保连接正确关闭
5. **错误处理**: 适当处理SQL异常和连接异常

## 📈 性能建议

1. **连接复用**: 对于批量评估，使用现有连接而不是每次创建新连接
2. **连接池**: 在生产环境中考虑使用连接池
3. **超时设置**: 为长时间运行的查询设置适当的超时
4. **批处理**: 对于大量SQL评估，考虑批处理方式

## 🎯 示例项目

查看 `src/main/java/com/nl2sql/spider/examples/DatabaseExtensionExample.java` 获取完整的使用示例。

## 📞 支持

如果遇到问题或需要帮助，请：

1. 检查数据库连接配置
2. 确认JDBC驱动版本兼容性
3. 查看日志输出获取详细错误信息
4. 参考测试用例了解正确用法 