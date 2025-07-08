# Spider Evaluation Framework - Spring Boot集成指南

## 1. 添加依赖

### Maven (pom.xml)
```xml
<dependency>
    <groupId>com.nl2sql</groupId>
    <artifactId>spider-evaluation</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle (build.gradle)
```gradle
implementation 'com.nl2sql:spider-evaluation:1.0.0'
```

## 2. 配置数据库连接

### application.yml
```yaml
spider:
  evaluation:
    databases:
      mysql:
        host: localhost
        port: 3306
        database: your_database
        username: your_username
        password: your_password
      postgresql:
        host: localhost
        port: 5432
        database: your_database
        username: your_username
        password: your_password
```

### application.properties
```properties
# MySQL配置
spider.evaluation.mysql.host=localhost
spider.evaluation.mysql.port=3306
spider.evaluation.mysql.database=your_database
spider.evaluation.mysql.username=your_username
spider.evaluation.mysql.password=your_password

# PostgreSQL配置
spider.evaluation.postgresql.host=localhost
spider.evaluation.postgresql.port=5432
spider.evaluation.postgresql.database=your_database
spider.evaluation.postgresql.username=your_username
spider.evaluation.postgresql.password=your_password
```

## 3. Spring Boot配置类

```java
package com.yourcompany.config;

import com.nl2sql.spider.config.DatabaseConfig;
import com.nl2sql.spider.service.SpiderEvaluationService;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpiderEvaluationConfig {
    
    @Bean
    public SpiderEvaluationService spiderEvaluationService() {
        return new SpiderEvaluationService();
    }
    
    @Bean
    @ConfigurationProperties(prefix = "spider.evaluation.mysql")
    public DatabaseConfig mysqlDatabaseConfig() {
        return new DatabaseConfig();
    }
    
    @Bean
    @ConfigurationProperties(prefix = "spider.evaluation.postgresql")
    public DatabaseConfig postgresqlDatabaseConfig() {
        return new DatabaseConfig();
    }
}
```

## 4. 数据库配置属性类

```java
package com.yourcompany.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "spider.evaluation")
public class SpiderEvaluationProperties {
    
    private DatabaseProperties mysql = new DatabaseProperties();
    private DatabaseProperties postgresql = new DatabaseProperties();
    
    public static class DatabaseProperties {
        private String host;
        private int port;
        private String database;
        private String username;
        private String password;
        
        // getters and setters
        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }
        
        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
        
        public String getDatabase() { return database; }
        public void setDatabase(String database) { this.database = database; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
    
    // getters and setters
    public DatabaseProperties getMysql() { return mysql; }
    public void setMysql(DatabaseProperties mysql) { this.mysql = mysql; }
    
    public DatabaseProperties getPostgresql() { return postgresql; }
    public void setPostgresql(DatabaseProperties postgresql) { this.postgresql = postgresql; }
}
```

## 5. Service层使用示例

```java
package com.yourcompany.service;

import com.nl2sql.spider.config.DatabaseConfig;
import com.nl2sql.spider.enums.EvaluationType;
import com.nl2sql.spider.model.EvaluationResult;
import com.nl2sql.spider.model.EvaluationStatistics;
import com.nl2sql.spider.model.SqlEvaluationItem;
import com.nl2sql.spider.service.SpiderEvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SqlEvaluationService {
    
    @Autowired
    private SpiderEvaluationService spiderEvaluationService;
    
    @Autowired
    private DatabaseConfig mysqlDatabaseConfig;
    
    /**
     * 评估SQL准确性
     */
    public EvaluationStatistics evaluateSqlAccuracy(List<SqlEvaluationItem> items) {
        return spiderEvaluationService.evaluateItems(items, mysqlDatabaseConfig, EvaluationType.MATCH);
    }
    
    /**
     * 获取详细评估结果
     */
    public List<EvaluationResult> getDetailedEvaluation(List<SqlEvaluationItem> items) {
        return spiderEvaluationService.evaluateItemsDetailed(items, mysqlDatabaseConfig, EvaluationType.MATCH);
    }
    
    /**
     * 验证SQL语法
     */
    public List<Boolean> validateSqlSyntax(List<SqlEvaluationItem> items) {
        return spiderEvaluationService.validateItems(items, mysqlDatabaseConfig);
    }
    
    /**
     * 单个SQL评估
     */
    public EvaluationResult evaluateSingleSql(String goldSql, String predSql, String dbId) {
        return spiderEvaluationService.evaluateSingleWithDynamicSchema(
            goldSql, predSql, dbId, mysqlDatabaseConfig, EvaluationType.MATCH
        );
    }
    
    /**
     * 测试数据库连接
     */
    public boolean testDatabaseConnection() {
        return spiderEvaluationService.testDatabaseConnection(mysqlDatabaseConfig);
    }
}
```

## 6. Controller层使用示例

```java
package com.yourcompany.controller;

import com.nl2sql.spider.model.EvaluationResult;
import com.nl2sql.spider.model.EvaluationStatistics;
import com.nl2sql.spider.model.SqlEvaluationItem;
import com.yourcompany.service.SqlEvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sql-evaluation")
public class SqlEvaluationController {
    
    @Autowired
    private SqlEvaluationService sqlEvaluationService;
    
    /**
     * 批量评估SQL
     */
    @PostMapping("/evaluate")
    public ResponseEntity<EvaluationStatistics> evaluateItems(
            @RequestBody List<SqlEvaluationItem> items) {
        EvaluationStatistics statistics = sqlEvaluationService.evaluateSqlAccuracy(items);
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * 获取详细评估结果
     */
    @PostMapping("/evaluate/detailed")
    public ResponseEntity<List<EvaluationResult>> evaluateItemsDetailed(
            @RequestBody List<SqlEvaluationItem> items) {
        List<EvaluationResult> results = sqlEvaluationService.getDetailedEvaluation(items);
        return ResponseEntity.ok(results);
    }
    
    /**
     * 单个SQL评估
     */
    @PostMapping("/evaluate/single")
    public ResponseEntity<EvaluationResult> evaluateSingle(
            @RequestParam String goldSql,
            @RequestParam String predSql,
            @RequestParam String dbId) {
        EvaluationResult result = sqlEvaluationService.evaluateSingleSql(goldSql, predSql, dbId);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 验证SQL语法
     */
    @PostMapping("/validate")
    public ResponseEntity<List<Boolean>> validateItems(
            @RequestBody List<SqlEvaluationItem> items) {
        List<Boolean> results = sqlEvaluationService.validateSqlSyntax(items);
        return ResponseEntity.ok(results);
    }
    
    /**
     * 测试数据库连接
     */
    @GetMapping("/test-connection")
    public ResponseEntity<Boolean> testConnection() {
        boolean connected = sqlEvaluationService.testDatabaseConnection();
        return ResponseEntity.ok(connected);
    }
}
```

## 7. DTO类定义

```java
package com.yourcompany.dto;

import com.nl2sql.spider.model.SqlEvaluationItem;
import java.util.List;

public class EvaluationRequest {
    private List<SqlEvaluationItem> items;
    private String evaluationType;
    private String databaseType;
    
    // constructors, getters and setters
    public EvaluationRequest() {}
    
    public List<SqlEvaluationItem> getItems() { return items; }
    public void setItems(List<SqlEvaluationItem> items) { this.items = items; }
    
    public String getEvaluationType() { return evaluationType; }
    public void setEvaluationType(String evaluationType) { this.evaluationType = evaluationType; }
    
    public String getDatabaseType() { return databaseType; }
    public void setDatabaseType(String databaseType) { this.databaseType = databaseType; }
}
```

## 8. 异常处理

```java
package com.yourcompany.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class SqlEvaluationExceptionHandler {
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body("参数错误: " + e.getMessage());
    }
    
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("评估过程中出现错误: " + e.getMessage());
    }
}
```

## 9. 测试用例

```java
package com.yourcompany.service;

import com.nl2sql.spider.model.SqlEvaluationItem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class SqlEvaluationServiceTest {
    
    @Autowired
    private SqlEvaluationService sqlEvaluationService;
    
    @Test
    public void testEvaluateItems() {
        List<SqlEvaluationItem> items = Arrays.asList(
            new SqlEvaluationItem(
                "SELECT COUNT(*) FROM users",
                "SELECT COUNT(*) FROM users",
                "test_db",
                "Count users"
            )
        );
        
        var statistics = sqlEvaluationService.evaluateSqlAccuracy(items);
        assertNotNull(statistics);
        assertTrue(statistics.getLevelStatistics(com.nl2sql.spider.enums.HardnessLevel.ALL).getCount() > 0);
    }
    
    @Test
    public void testDatabaseConnection() {
        boolean connected = sqlEvaluationService.testDatabaseConnection();
        assertTrue(connected, "数据库连接应该成功");
    }
}
```

## 10. 使用示例

### 基本使用
```java
// 创建评估项目
List<SqlEvaluationItem> items = Arrays.asList(
    new SqlEvaluationItem(
        "SELECT name FROM users WHERE age > 18",
        "SELECT name FROM users WHERE age > 18",
        "my_database",
        "Get adult users"
    )
);

// 执行评估
EvaluationStatistics stats = sqlEvaluationService.evaluateSqlAccuracy(items);
System.out.println("准确率: " + stats.getLevelStatistics(HardnessLevel.ALL).getExactMatchScore());
```

### REST API调用示例
```bash
# 评估SQL
curl -X POST http://localhost:8080/api/sql-evaluation/evaluate \
  -H "Content-Type: application/json" \
  -d '[{
    "goldSql": "SELECT * FROM users",
    "predictionSql": "SELECT * FROM users",
    "dbId": "test_db",
    "question": "Get all users"
  }]'

# 测试连接
curl http://localhost:8080/api/sql-evaluation/test-connection
```

## 11. 最佳实践

1. **连接池配置**: 配置合适的数据库连接池
2. **异步处理**: 对于大量评估任务，考虑使用异步处理
3. **缓存机制**: 缓存数据库Schema信息，避免重复提取
4. **监控日志**: 添加适当的日志记录和监控
5. **错误处理**: 完善的异常处理机制

## 12. 性能优化

- 使用连接池管理数据库连接
- 批量处理评估任务
- 缓存Schema信息
- 异步处理长时间运行的任务 