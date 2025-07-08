# Spider Java Evaluation Framework - Maven中央仓库发布指南

## 前提条件

### 1. 注册Sonatype账号
1. 访问 [Sonatype JIRA](https://issues.sonatype.org)
2. 创建账号并登录
3. 创建新的Issue，选择 "Community Support - Open Source Project Repository Hosting (OSSRH)"
4. 填写项目信息：
   - **Summary**: Request for com.nl2sql groupId
   - **Group Id**: com.nl2sql
   - **Project URL**: https://github.com/yourusername/spider-java-evaluation
   - **SCM URL**: https://github.com/yourusername/spider-java-evaluation.git

### 2. 配置GPG签名
```bash
# 安装GPG
brew install gnupg  # macOS
# 或
sudo apt-get install gnupg  # Ubuntu

# 生成GPG密钥
gpg --gen-key

# 查看密钥
gpg --list-keys

# 上传公钥到密钥服务器
gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID
```

### 3. 配置Maven settings.xml
在 `~/.m2/settings.xml` 中添加：

```xml
<settings>
  <servers>
    <server>
      <id>ossrh</id>
      <username>your-sonatype-username</username>
      <password>your-sonatype-password</password>
    </server>
  </servers>
  
  <profiles>
    <profile>
      <id>ossrh</id>
      <activation>
        <activeByDefault>true</activeByDefault>
      </activation>
      <properties>
        <gpg.executable>gpg</gpg.executable>
        <gpg.passphrase>your-gpg-passphrase</gpg.passphrase>
      </properties>
    </profile>
  </profiles>
</settings>
```

## 发布步骤

### 1. 准备发布
```bash
# 确保代码已提交
git add .
git commit -m "Prepare for Maven Central release"
git push

# 运行测试
mvn clean test

# 检查项目配置
mvn help:effective-pom
```

### 2. 发布到快照仓库（可选）
```bash
# 修改版本为SNAPSHOT
mvn versions:set -DnewVersion=1.0.0-SNAPSHOT

# 部署到快照仓库
mvn clean deploy
```

### 3. 发布到正式仓库
```bash
# 设置正式版本
mvn versions:set -DnewVersion=1.0.0

# 使用release profile进行部署
mvn clean deploy -P release

# 或者使用nexus-staging插件
mvn nexus-staging:release
```

### 4. 验证发布
1. 访问 [Nexus Repository Manager](https://s01.oss.sonatype.org/)
2. 登录并检查Staging Repositories
3. 如果自动发布失败，手动Close和Release

## 发布后验证

### 1. 检查Maven中央仓库
访问 [Maven Central](https://search.maven.org/) 搜索 `com.nl2sql:spider-evaluation`

### 2. 测试依赖
在新项目中测试：
```xml
<dependency>
    <groupId>com.nl2sql</groupId>
    <artifactId>spider-evaluation</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 注意事项

1. **首次发布**: 需要等待Sonatype审核，通常需要1-2个工作日
2. **同步时间**: 发布到Maven中央仓库后，同步到其他镜像可能需要几小时
3. **版本管理**: 一旦发布，版本号不能重复使用
4. **签名要求**: 所有发布的构件都必须GPG签名

## 常见问题

### GPG签名失败
```bash
# 检查GPG配置
gpg --list-secret-keys

# 设置GPG_TTY环境变量
export GPG_TTY=$(tty)
```

### 上传失败
- 检查网络连接
- 确认Sonatype账号权限
- 验证settings.xml配置

### 版本冲突
- 确保版本号唯一
- 检查是否已存在相同版本

## 自动化发布（GitHub Actions）

创建 `.github/workflows/release.yml`:

```yaml
name: Release to Maven Central

on:
  release:
    types: [created]

jobs:
  publish:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
        
    - name: Import GPG key
      uses: crazy-max/ghaction-import-gpg@v5
      with:
        gpg_private_key: ${{ secrets.GPG_PRIVATE_KEY }}
        passphrase: ${{ secrets.GPG_PASSPHRASE }}
        
    - name: Configure Maven settings
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
        server-id: ossrh
        server-username: MAVEN_USERNAME
        server-password: MAVEN_PASSWORD
        
    - name: Publish to Maven Central
      run: mvn clean deploy -P release
      env:
        MAVEN_USERNAME: ${{ secrets.OSSRH_USERNAME }}
        MAVEN_PASSWORD: ${{ secrets.OSSRH_TOKEN }}
```

## 联系信息

如有问题，请联系：
- GitHub Issues: https://github.com/yourusername/spider-java-evaluation/issues
- Email: your.email@example.com 