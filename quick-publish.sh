#!/bin/bash

# Spider Java - 快速发布脚本
# 解决卡住问题的简化版本

set -e

echo "🚀 Spider Java Evaluation Framework - 快速发布"
echo "================================================"

# 检查GPG密钥
echo "🔍 检查GPG密钥..."
if ! gpg --list-secret-keys --keyid-format=long | grep -q "sec"; then
    echo "📝 需要生成GPG密钥，请提供以下信息："
    read -p "👤 您的姓名: " USER_NAME
    read -p "📧 您的邮箱: " USER_EMAIL
    read -s -p "🔐 GPG密钥密码: " GPG_PASSPHRASE
    echo ""
    
    echo "⏳ 生成GPG密钥..."
    
    # 使用expect自动化GPG密钥生成
    cat > /tmp/gpg_input <<EOF
Key-Type: RSA
Key-Length: 4096
Subkey-Type: RSA
Subkey-Length: 4096
Name-Real: $USER_NAME
Name-Email: $USER_EMAIL
Expire-Date: 2y
Passphrase: $GPG_PASSPHRASE
%commit
EOF

    gpg --batch --generate-key /tmp/gpg_input
    rm /tmp/gpg_input
    
    GPG_KEY_ID=$(gpg --list-secret-keys --keyid-format=long | grep "sec" | tail -1 | sed 's/.*\/\([A-F0-9]*\).*/\1/')
    echo "✅ GPG密钥生成完成: $GPG_KEY_ID"
    
    # 上传公钥（静默处理，不等待）
    echo "📤 上传公钥到密钥服务器..."
    gpg --keyserver keyserver.ubuntu.com --send-keys $GPG_KEY_ID &
    
else
    GPG_KEY_ID=$(gpg --list-secret-keys --keyid-format=long | grep "sec" | head -1 | sed 's/.*\/\([A-F0-9]*\).*/\1/')
    echo "✅ 找到现有GPG密钥: $GPG_KEY_ID"
    read -s -p "🔐 请输入GPG密钥密码: " GPG_PASSPHRASE
    echo ""
fi

# 配置Maven settings.xml
echo "⚙️ 配置Maven..."
mkdir -p ~/.m2

# 备份现有配置
if [[ -f ~/.m2/settings.xml ]]; then
    cp ~/.m2/settings.xml ~/.m2/settings.xml.backup.$(date +%Y%m%d_%H%M%S)
fi

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

# 设置GPG环境
export GPG_TTY=$(tty)

# 运行测试
echo "🧪 运行测试..."
mvn clean test -q
echo "✅ 测试通过"

# 构建项目
echo "🔨 构建项目..."
mvn clean package -P release -q
echo "✅ 构建完成"

echo ""
echo "📦 生成的文件:"
ls -lh target/*.jar

# 发布到Maven中央仓库
echo ""
echo "🚀 发布到Maven中央仓库..."
mvn deploy -P release -q

echo ""
echo "🎉 发布成功！"
echo ""
echo "📋 您的包将在以下地址可用："
echo "   Maven Central: https://search.maven.org/artifact/com.nl2sql/spider-evaluation/1.0.0/jar"
echo "   MVN Repository: https://mvnrepository.com/artifact/com.nl2sql/spider-evaluation/1.0.0"
echo ""
echo "📝 Maven依赖:"
echo "   <dependency>"
echo "       <groupId>com.nl2sql</groupId>"
echo "       <artifactId>spider-evaluation</artifactId>"
echo "       <version>1.0.0</version>"
echo "   </dependency>"
echo ""
echo "⏰ 注意: 同步到所有镜像可能需要2-4小时" 