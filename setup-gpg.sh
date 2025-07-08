#!/bin/bash

# GPG密钥生成和配置脚本
# 用于Maven中央仓库发布

set -e

echo "=== Spider Java Evaluation Framework - GPG配置脚本 ==="
echo ""

# 检查GPG是否已安装
if ! command -v gpg &> /dev/null; then
    echo "❌ GPG未安装，正在安装..."
    if [[ "$OSTYPE" == "darwin"* ]]; then
        # macOS
        if command -v brew &> /dev/null; then
            brew install gnupg
        else
            echo "请先安装Homebrew: https://brew.sh/"
            exit 1
        fi
    elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
        # Linux
        sudo apt-get update && sudo apt-get install -y gnupg
    else
        echo "不支持的操作系统，请手动安装GPG"
        exit 1
    fi
fi

echo "✅ GPG已安装"

# 检查是否已有GPG密钥
echo ""
echo "🔍 检查现有GPG密钥..."
if gpg --list-secret-keys --keyid-format=long | grep -q "sec"; then
    echo "📋 现有GPG密钥："
    gpg --list-secret-keys --keyid-format=long
    echo ""
    read -p "是否使用现有密钥？(y/n): " use_existing
    
    if [[ "$use_existing" == "y" || "$use_existing" == "Y" ]]; then
        # 获取现有密钥ID
        GPG_KEY_ID=$(gpg --list-secret-keys --keyid-format=long | grep "sec" | head -1 | sed 's/.*\/\([A-F0-9]*\).*/\1/')
        echo "✅ 使用现有密钥: $GPG_KEY_ID"
    else
        GENERATE_NEW=true
    fi
else
    echo "🔑 未找到GPG密钥，将生成新密钥"
    GENERATE_NEW=true
fi

# 生成新的GPG密钥
if [[ "$GENERATE_NEW" == "true" ]]; then
    echo ""
    echo "🔑 生成新的GPG密钥..."
    
    # 获取用户信息
    read -p "请输入您的姓名: " USER_NAME
    read -p "请输入您的邮箱: " USER_EMAIL
    read -s -p "请输入GPG密钥密码: " GPG_PASSPHRASE
    echo ""
    
    # 创建GPG密钥配置文件
    cat > gpg-key-config <<EOF
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
    gpg --batch --generate-key gpg-key-config
    rm gpg-key-config
    
    # 获取新生成的密钥ID
    GPG_KEY_ID=$(gpg --list-secret-keys --keyid-format=long | grep "sec" | tail -1 | sed 's/.*\/\([A-F0-9]*\).*/\1/')
    echo "✅ 新密钥已生成: $GPG_KEY_ID"
fi

# 上传公钥到密钥服务器
echo ""
echo "📤 上传公钥到密钥服务器..."
gpg --keyserver keyserver.ubuntu.com --send-keys $GPG_KEY_ID
gpg --keyserver keys.openpgp.org --send-keys $GPG_KEY_ID
gpg --keyserver pgp.mit.edu --send-keys $GPG_KEY_ID

echo "✅ 公钥已上传到密钥服务器"

# 导出私钥（用于CI/CD）
echo ""
echo "💾 导出密钥信息..."
gpg --armor --export-secret-keys $GPG_KEY_ID > private-key.asc
gpg --armor --export $GPG_KEY_ID > public-key.asc

echo "✅ 密钥已导出到当前目录"

# 更新Maven settings.xml
echo ""
echo "⚙️ 更新Maven配置..."

# 备份现有settings.xml
if [[ -f ~/.m2/settings.xml ]]; then
    cp ~/.m2/settings.xml ~/.m2/settings.xml.backup.$(date +%Y%m%d_%H%M%S)
    echo "✅ 已备份现有settings.xml"
fi

# 创建.m2目录（如果不存在）
mkdir -p ~/.m2

# 创建新的settings.xml
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

echo "✅ Maven settings.xml已更新"

# 创建环境变量文件
cat > gpg-env.sh <<EOF
#!/bin/bash
# GPG环境变量配置
export GPG_TTY=\$(tty)
export GPG_KEY_ID="$GPG_KEY_ID"
export GPG_PASSPHRASE="$GPG_PASSPHRASE"
EOF

chmod +x gpg-env.sh

echo ""
echo "🎉 GPG配置完成！"
echo ""
echo "📝 配置信息："
echo "   GPG密钥ID: $GPG_KEY_ID"
echo "   私钥文件: private-key.asc"
echo "   公钥文件: public-key.asc"
echo "   环境变量: gpg-env.sh"
echo ""
echo "⚠️  重要提醒："
echo "   1. 请妥善保管private-key.asc文件"
echo "   2. 不要将GPG密码提交到版本控制"
echo "   3. 在CI/CD中使用时，请将密钥和密码设置为环境变量"
echo ""
echo "🚀 现在可以运行发布脚本了！" 