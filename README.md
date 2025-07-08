# Spider Java Evaluation Framework

一个功能强大的Java版本Spider NL2SQL评估框架，支持多种数据库和动态Schema提取。

## 🚀 快速开始

### Maven依赖

```xml
<dependency>
    <groupId>com.nl2sql</groupId>
    <artifactId>spider-evaluation</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle依赖

```gradle
implementation 'com.nl2sql:spider-evaluation:1.0.0'
```

## 📋 主要特性

- ✅ **多数据库支持**: MySQL、PostgreSQL、Oracle、SQL Server、H2、SQLite
- ✅ **动态Schema提取**: 无需tableFile，自动从数据库提取表结构
- ✅ **静态Schema支持**: 兼容传统的tableFile模式
- ✅ **Spring Boot集成**: 提供完整的Spring Boot Starter
- ✅ **POJO接口**: 支持基于Java对象的独立测试
- ✅ **CLI工具**: 命令行工具支持批量评估
- ✅ **完全向后兼容**: 保持与原有API的兼容性

## 🔧 基本使用

### 1. 简单评估

```java
// 创建服务
SpiderEvaluationService service = new SpiderEvaluationService();

// 配置数据库
DatabaseConfig dbConfig = new DatabaseConfig();
dbConfig.setDbType("mysql");
dbConfig.setHost("localhost");
dbConfig.setPort(3306);
dbConfig.setDatabase("your_database");
dbConfig.setUsername("your_username");
dbConfig.setPassword("your_password");

// 创建评估项目
List<SqlEvaluationItem> items = Arrays.asList(
    new SqlEvaluationItem(
        "SELECT * FROM users WHERE age > 18",
        "SELECT * FROM users WHERE age > 18", 
        "your_database",
        "查询成年用户"
    )
);

// 执行评估
EvaluationStatistics stats = service.evaluateItems(items, dbConfig, EvaluationType.MATCH);
System.out.println("准确率: " + stats.getLevelStatistics(HardnessLevel.ALL).getExactMatchScore());
```

### 2. Spring Boot集成

```java
@Service
public class SqlEvaluationService {
    
    @Autowired
    private SpiderEvaluationService spiderService;
    
    @Autowired
    private DatabaseConfig databaseConfig;
    
    public EvaluationStatistics evaluate(List<SqlEvaluationItem> items) {
        return spiderService.evaluateItems(items, databaseConfig, EvaluationType.MATCH);
    }
}
```

### 3. REST API

```java
@RestController
@RequestMapping("/api/sql-evaluation")
public class SqlEvaluationController {
    
    @PostMapping("/evaluate")
    public ResponseEntity<EvaluationStatistics> evaluate(@RequestBody List<SqlEvaluationItem> items) {
        EvaluationStatistics stats = sqlEvaluationService.evaluateSqlAccuracy(items);
        return ResponseEntity.ok(stats);
    }
}
```

## 📚 API文档

### 核心接口

#### 1. 基于POJO的评估接口
```java
// 基础统计评估
public EvaluationStatistics evaluateItems(List<SqlEvaluationItem> items, DatabaseConfig dbConfig, EvaluationType evaluationType)

// 详细结果评估  
public List<EvaluationResult> evaluateItemsDetailed(List<SqlEvaluationItem> items, DatabaseConfig dbConfig, EvaluationType evaluationType)

// SQL验证
public List<Boolean> validateItems(List<SqlEvaluationItem> items, DatabaseConfig dbConfig)
```

#### 2. 动态Schema接口
```java
// 无需tableFile的评估
public EvaluationStatistics evaluateWithDynamicSchema(String goldFile, String predFile, DatabaseConfig dbConfig, EvaluationType evaluationType)

// 单个SQL评估
public EvaluationResult evaluateSingleWithDynamicSchema(String goldSql, String predSql, String dbId, DatabaseConfig dbConfig, EvaluationType evaluationType)
```

#### 3. 传统文件接口
```java
// 使用tableFile的评估
public EvaluationStatistics evaluate(String goldFile, String predFile, String tableFile, DatabaseConfig dbConfig, EvaluationType evaluationType)

// SQLite数据库评估
public EvaluationStatistics evaluate(String goldFile, String predFile, String dbDir, EvaluationType evaluationType)
```

## 🗄️ 支持的数据库

| 数据库 | 驱动 | 状态 |
|--------|------|------|
| MySQL | mysql-connector-j | ✅ 完全支持 |
| PostgreSQL | postgresql | ✅ 完全支持 |
| Oracle | ojdbc8 | ✅ 完全支持 |
| SQL Server | mssql-jdbc | ✅ 完全支持 |
| H2 | h2 | ✅ 完全支持 |
| SQLite | sqlite-jdbc | ✅ 完全支持 |

## 📖 详细文档

- [Maven中央仓库发布指南](MAVEN_CENTRAL_RELEASE.md)
- [Spring Boot集成指南](SPRING_BOOT_USAGE.md)
- [MySQL测试配置指南](MYSQL_TEST_SETUP.md)
- [MySQL评估使用指南](MYSQL_EVALUATION_GUIDE.md)

## 🧪 测试

```bash
# 运行所有测试
mvn test

# 运行特定测试
mvn test -Dtest=SpiderEvaluationServiceItemsTest

# 跳过测试打包
mvn clean package -DskipTests
```

## 📦 构建

```bash
# 基本构建
mvn clean package

# 生成完整发布包（包含源码和文档）
mvn clean package -P release

# 生成CLI工具
java -jar target/spider-evaluation-1.0.0-cli.jar --help
```

## 🌟 项目结构

```
spider_j/
├── src/main/java/com/nl2sql/spider/
│   ├── config/          # 配置类
│   ├── enums/           # 枚举定义
│   ├── model/           # 数据模型
│   ├── service/         # 核心服务
│   └── utils/           # 工具类
├── src/test/java/       # 测试代码
├── MAVEN_CENTRAL_RELEASE.md    # 发布指南
├── SPRING_BOOT_USAGE.md        # Spring Boot使用指南
└── README.md           # 项目说明
```

## 🤝 贡献

欢迎提交Issue和Pull Request！

## 📄 许可证

Apache License 2.0

## 📞 联系方式

- GitHub Issues: [项目Issues](https://github.com/yourusername/spider-java-evaluation/issues)
- Email: your.email@example.com

---

**注意**: 发布到Maven中央仓库前，请先完成[发布准备工作](MAVEN_CENTRAL_RELEASE.md)。 