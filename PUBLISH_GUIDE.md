# Spider Java Evaluation Framework - 发布指南

## 🚀 一键发布到Maven中央仓库

您的Sonatype账号已配置完成，现在可以使用我们提供的自动化脚本将项目发布到Maven中央仓库。

## 📋 准备工作

### 1. 确认账号信息
- ✅ Sonatype账号: `Nsp+IG/5`
- ✅ 密码: `cJ5uy6QH14z1EaSANbDZom2v0fFyPpp5G+aGqZJ/bYo8`
- ✅ 账号状态: 已注册并激活

### 2. 系统要求
- Java 17+
- Maven 3.6+
- GPG (脚本会自动安装)

## 🎯 三种发布方式

### 方式一：一键发布（推荐）

```bash
# 执行一键发布脚本
./one-click-publish.sh
```

**特点：**
- 🔧 自动配置GPG密钥
- 🧪 自动运行测试
- 🔨 自动构建项目
- 🚀 自动发布到Maven中央仓库
- 📋 显示发布结果和使用说明

**首次运行时会要求输入：**
- 您的姓名（用于GPG密钥）
- 您的邮箱（用于GPG密钥）
- GPG密钥密码（请牢记）

### 方式二：分步执行

```bash
# 步骤1: 配置GPG（仅首次需要）
./setup-gpg.sh

# 步骤2: 发布项目
./publish-to-maven-central.sh
```

### 方式三：手动执行

```bash
# 1. 配置Maven settings.xml
cp maven-settings-template.xml ~/.m2/settings.xml

# 2. 生成GPG密钥
gpg --gen-key

# 3. 构建和发布
mvn clean deploy -P release
```

## 📦 发布流程详解

### 1. GPG密钥配置
- 自动生成4096位RSA密钥
- 上传公钥到多个密钥服务器
- 配置Maven使用GPG签名

### 2. 项目构建
- 编译源代码
- 运行所有测试
- 生成JAR文件
- 生成源码包
- 生成JavaDoc包
- GPG签名所有文件

### 3. 发布到中央仓库
- 上传到Sonatype Central Portal
- 自动验证和发布
- 同步到Maven中央仓库

## 📊 发布后验证

发布成功后，您可以在以下位置查看您的包：

### 🔗 官方链接
- **Maven Central Search**: https://search.maven.org/artifact/com.nl2sql/spider-evaluation/1.0.0/jar
- **MVN Repository**: https://mvnrepository.com/artifact/com.nl2sql/spider-evaluation/1.0.0
- **Central Repository**: https://repo1.maven.org/maven2/com/nl2sql/spider-evaluation/1.0.0/

### ⏰ 同步时间
- **Central Portal**: 立即可用
- **Maven Central**: 15-30分钟
- **所有镜像**: 2-4小时

## 📝 使用方法

发布成功后，其他开发者可以这样使用您的库：

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

### 基本使用示例
```java
import com.nl2sql.spider.service.SpiderEvaluationService;
import com.nl2sql.spider.config.DatabaseConfig;
import com.nl2sql.spider.model.SqlEvaluationItem;

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

// 执行评估
List<SqlEvaluationItem> items = Arrays.asList(
    new SqlEvaluationItem(
        "SELECT * FROM users WHERE age > 18",
        "SELECT * FROM users WHERE age > 18", 
        "your_database",
        "查询成年用户"
    )
);

EvaluationStatistics stats = service.evaluateItems(items, dbConfig, EvaluationType.MATCH);
System.out.println("准确率: " + stats.getLevelStatistics(HardnessLevel.ALL).getExactMatchScore());
```

## 🛠️ 脚本说明

### `one-click-publish.sh`
- **功能**: 一键完成所有发布步骤
- **适用**: 首次发布或完整发布流程
- **特点**: 自动化程度最高，用户友好

### `setup-gpg.sh`
- **功能**: 配置GPG密钥和Maven设置
- **适用**: 仅需要配置环境
- **特点**: 详细的配置过程，支持现有密钥

### `publish-to-maven-central.sh`
- **功能**: 执行构建和发布
- **适用**: 已有GPG配置，仅需发布
- **特点**: 支持多种选项，详细的验证步骤

## ⚠️ 注意事项

### 安全提醒
1. **GPG密码**: 请牢记您的GPG密码，丢失后无法恢复
2. **私钥保护**: 不要将GPG私钥提交到版本控制
3. **账号安全**: 保护好您的Sonatype账号信息

### 发布规则
1. **版本唯一性**: 每个版本号只能发布一次
2. **文件完整性**: 必须包含JAR、源码、JavaDoc和签名文件
3. **元数据要求**: 必须包含完整的POM元数据

### 故障排除
1. **GPG错误**: 确保GPG_TTY环境变量已设置
2. **网络问题**: 检查网络连接和防火墙设置
3. **权限问题**: 确保有上传权限和正确的凭据

## 🎉 发布成功

发布成功后，您将看到：

```
🎉 恭喜！您的Spider Java Evaluation Framework已成功发布到Maven中央仓库！

📋 发布信息:
   GroupId:    com.nl2sql
   ArtifactId: spider-evaluation
   Version:    1.0.0

🔗 查看链接:
   Maven Central: https://search.maven.org/artifact/com.nl2sql/spider-evaluation/1.0.0/jar
   MVN Repository: https://mvnrepository.com/artifact/com.nl2sql/spider-evaluation/1.0.0

📝 Maven依赖:
   <dependency>
       <groupId>com.nl2sql</groupId>
       <artifactId>spider-evaluation</artifactId>
       <version>1.0.0</version>
   </dependency>
```

## 📞 支持

如果在发布过程中遇到问题，请：

1. 检查脚本输出的错误信息
2. 查看Maven和GPG日志
3. 参考 [MAVEN_CENTRAL_RELEASE.md](MAVEN_CENTRAL_RELEASE.md) 详细文档
4. 提交GitHub Issue获取帮助

---

**准备好了吗？运行 `./one-click-publish.sh` 开始发布您的Spider Java Evaluation Framework！** 🚀 