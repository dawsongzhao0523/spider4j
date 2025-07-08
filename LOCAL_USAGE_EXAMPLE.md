# 本地Maven仓库使用指南

## ✅ 安装状态
您的Spider Evaluation Framework已成功安装到本地Maven仓库：

**本地仓库路径：** `/Users/dszhao/Downloads/repo/com/chatdata/spider-evaluation/1.0.0/`

**已安装的文件：**
- `spider-evaluation-1.0.0.jar` - 主要库文件
- `spider-evaluation-1.0.0-sources.jar` - 源码包
- `spider-evaluation-1.0.0-javadoc.jar` - 文档包
- `spider-evaluation-1.0.0-cli.jar` - CLI工具包

## 🚀 在新项目中使用

### 1. Maven项目配置

在您的新项目的 `pom.xml` 中添加依赖：

```xml
<dependency>
    <groupId>com.chatdata</groupId>
    <artifactId>spider-evaluation</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. 基本使用示例

```java
import com.chatdata.spider.evaluation.SpiderEvaluationService;
import com.chatdata.spider.evaluation.model.SqlEvaluationItem;
import com.chatdata.spider.evaluation.model.EvaluationResult;

public class SpiderEvaluationExample {
    public static void main(String[] args) {
        // 创建评估服务
        SpiderEvaluationService service = new SpiderEvaluationService();
        
        // 创建评估项
        SqlEvaluationItem item = new SqlEvaluationItem();
        item.setDbId("chinook");
        item.setQuestion("List all customers from USA");
        item.setPredictedSql("SELECT * FROM customers WHERE country = 'USA'");
        item.setGoldSql("SELECT * FROM customers WHERE country = 'USA'");
        
        // 数据库连接配置
        item.setJdbcUrl("jdbc:sqlite:path/to/chinook.db");
        item.setUsername("");
        item.setPassword("");
        
        // 执行评估
        EvaluationResult result = service.evaluateSql(item);
        
        // 输出结果
        System.out.println("执行匹配: " + result.isExecutionMatch());
        System.out.println("准确率: " + result.getAccuracy());
        System.out.println("详细信息: " + result.getDetails());
    }
}
```

### 3. Spring Boot集成示例

```java
@RestController
@RequestMapping("/api/evaluation")
public class EvaluationController {
    
    private final SpiderEvaluationService evaluationService;
    
    public EvaluationController() {
        this.evaluationService = new SpiderEvaluationService();
    }
    
    @PostMapping("/sql")
    public ResponseEntity<EvaluationResult> evaluateSql(@RequestBody SqlEvaluationItem item) {
        try {
            EvaluationResult result = evaluationService.evaluateSql(item);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
```

## 🔧 支持的数据库

- ✅ **SQLite** - `jdbc:sqlite:database.db`
- ✅ **MySQL** - `jdbc:mysql://localhost:3306/database`
- ✅ **PostgreSQL** - `jdbc:postgresql://localhost:5432/database`
- ✅ **H2** - `jdbc:h2:mem:testdb`
- ✅ **Oracle** - `jdbc:oracle:thin:@localhost:1521:xe`
- ✅ **SQL Server** - `jdbc:sqlserver://localhost:1433;databaseName=database`

## 📊 CLI工具使用

您也可以直接使用CLI工具：

```bash
java -jar target/spider-evaluation-1.0.0-cli.jar \
  --db-id chinook \
  --question "List all customers" \
  --predicted-sql "SELECT * FROM customers" \
  --gold-sql "SELECT * FROM customers" \
  --jdbc-url "jdbc:sqlite:chinook.db"
```

## 🎯 快速测试

1. **创建测试项目：**
   ```bash
   mvn archetype:generate -DgroupId=com.example -DartifactId=spider-test -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false
   cd spider-test
   ```

2. **添加依赖到pom.xml**

3. **创建测试类并运行**

## 📚 更多信息

- **GitHub仓库：** https://github.com/dszhao/spider_j
- **文档：** 查看项目中的 `SPRING_BOOT_USAGE.md`
- **API文档：** 已包含在javadoc包中 