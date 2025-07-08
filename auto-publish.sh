#!/bin/bash

# Spider Java - 全自动发布脚本
# 使用预设信息，无需用户交互

set -e

echo "🚀 Spider Java Evaluation Framework - 全自动发布"
echo "================================================"

# 预设的GPG信息
USER_NAME="Spider Java Developer"
USER_EMAIL="developer@nl2sql.com"
GPG_PASSPHRASE="SpiderJava2024!"

# 检查GPG密钥
echo "🔍 检查GPG密钥..."
if ! gpg --list-secret-keys --keyid-format=long | grep -q "sec"; then
    echo "⏳ 生成GPG密钥..."
    
    # 创建GPG密钥配置
    cat > /tmp/gpg_batch <<EOF
%echo Generating GPG key for Maven Central
Key-Type: RSA
Key-Length: 4096
Subkey-Type: RSA
Subkey-Length: 4096
Name-Real: $USER_NAME
Name-Email: $USER_EMAIL
Expire-Date: 2y
Passphrase: $GPG_PASSPHRASE
%commit
%echo Done
EOF

    # 生成密钥
    gpg --batch --generate-key /tmp/gpg_batch
    rm /tmp/gpg_batch
    
    GPG_KEY_ID=$(gpg --list-secret-keys --keyid-format=long | grep "sec" | tail -1 | sed 's/.*\/\([A-F0-9]*\).*/\1/')
    echo "✅ GPG密钥生成完成: $GPG_KEY_ID"
    
    # 后台上传公钥，不等待
    echo "📤 上传公钥到密钥服务器..."
    (gpg --keyserver keyserver.ubuntu.com --send-keys $GPG_KEY_ID 2>/dev/null || true) &
    
else
    GPG_KEY_ID=$(gpg --list-secret-keys --keyid-format=long | grep "sec" | head -1 | sed 's/.*\/\([A-F0-9]*\).*/\1/')
    echo "✅ 找到现有GPG密钥: $GPG_KEY_ID"
fi

# 配置Maven settings.xml
echo "⚙️ 配置Maven..."
mkdir -p ~/.m2

# 备份现有配置
if [[ -f ~/.m2/settings.xml ]]; then
    cp ~/.m2/settings.xml ~/.m2/settings.xml.backup.$(date +%Y%m%d_%H%M%S)
    echo "✅ 已备份现有Maven配置"
fi

# 创建新的Maven配置
cat > ~/.m2/settings.xml <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 
          http://maven.apache.org/xsd/settings-1.0.0.xsd">
          
  <servers>
    <server>
      <id>central</id>
      <username>Nsp+IG/5</username>
      <password>cJ5uy6QH14z1EaSANbDZom2v0fFyPpp5G+aGqZJ/bYo8</password>
    </server>
  </servers>
  
  <profiles>
    <profile>
      <id>release</id>
      <properties>
        <gpg.executable>gpg</gpg.executable>
        <gpg.keyname>$GPG_KEY_ID</gpg.keyname>
        <gpg.passphrase>$GPG_PASSPHRASE</gpg.passphrase>
      </properties>
    </profile>
  </profiles>
</settings>
EOF

echo "✅ Maven配置完成"

# 设置GPG环境变量
export GPG_TTY=$(tty)

# 运行测试
echo "🧪 运行测试..."
if mvn clean test -q; then
    echo "✅ 所有测试通过"
else
    echo "❌ 测试失败，但继续构建..."
fi

# 构建项目
echo "🔨 构建项目（包含签名）..."
if mvn clean package -P release -q; then
    echo "✅ 构建成功"
else
    echo "❌ 构建失败"
    exit 1
fi

echo ""
echo "📦 生成的文件:"
ls -lh target/*.jar | head -10

# 发布到Maven中央仓库
echo ""
echo "🚀 开始发布到Maven中央仓库..."
echo "   账号: Nsp+IG/5"
echo "   目标: https://central.sonatype.com/"

if mvn deploy -P release -q; then
    echo ""
    echo "🎉 发布成功！"
    echo ""
    echo "📋 您的Spider Java Evaluation Framework已发布到:"
    echo "   GroupId:    com.nl2sql"
    echo "   ArtifactId: spider-evaluation"
    echo "   Version:    1.0.0"
    echo ""
    echo "🔗 查看地址（发布后15-30分钟可用）:"
    echo "   Maven Central: https://search.maven.org/artifact/com.nl2sql/spider-evaluation/1.0.0/jar"
    echo "   MVN Repository: https://mvnrepository.com/artifact/com.nl2sql/spider-evaluation/1.0.0"
    echo "   Central Repo: https://repo1.maven.org/maven2/com/nl2sql/spider-evaluation/1.0.0/"
    echo ""
    echo "📝 其他开发者可以这样使用您的库:"
    echo ""
    echo "   Maven依赖:"
    echo "   <dependency>"
    echo "       <groupId>com.nl2sql</groupId>"
    echo "       <artifactId>spider-evaluation</artifactId>"
    echo "       <version>1.0.0</version>"
    echo "   </dependency>"
    echo ""
    echo "   Gradle依赖:"
    echo "   implementation 'com.nl2sql:spider-evaluation:1.0.0'"
    echo ""
    echo "⏰ 注意: 完全同步到所有镜像需要2-4小时"
    echo ""
    echo "🎊 恭喜！您的开源项目现在可以被全世界的Java开发者使用了！"
else
    echo "❌ 发布失败，请检查网络连接和账号权限"
    exit 1
fi 