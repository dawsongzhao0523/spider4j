#!/bin/bash

# Spider Java Evaluation Framework - 一键发布脚本
# 自动配置GPG并发布到Maven中央仓库

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
NC='\033[0m'

print_banner() {
    echo -e "${PURPLE}"
    echo "╔════════════════════════════════════════════════════════════════╗"
    echo "║                                                                ║"
    echo "║          Spider Java Evaluation Framework                      ║"
    echo "║              一键发布到Maven中央仓库                            ║"
    echo "║                                                                ║"
    echo "║  🚀 自动配置GPG + 构建项目 + 发布到 mvnrepository.com          ║"
    echo "║                                                                ║"
    echo "╚════════════════════════════════════════════════════════════════╝"
    echo -e "${NC}"
}

print_step() {
    echo -e "${BLUE}📋 步骤 $1: $2${NC}"
    echo "─────────────────────────────────────────────────────────────────"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

# 检查是否为首次运行
check_first_run() {
    if [[ ! -f ./.gpg-configured ]]; then
        echo "true"
    else
        echo "false"
    fi
}

# 配置GPG（首次运行）
setup_gpg_first_time() {
    print_step "1" "首次GPG配置"
    
    echo "请提供以下信息用于生成GPG密钥："
    echo ""
    
    read -p "👤 您的姓名: " USER_NAME
    read -p "📧 您的邮箱: " USER_EMAIL
    
    while true; do
        read -s -p "🔐 GPG密钥密码: " GPG_PASSPHRASE
        echo ""
        read -s -p "🔐 确认密码: " GPG_PASSPHRASE_CONFIRM
        echo ""
        
        if [[ "$GPG_PASSPHRASE" == "$GPG_PASSPHRASE_CONFIRM" ]]; then
            break
        else
            print_error "密码不匹配，请重新输入"
        fi
    done
    
    echo ""
    echo "⏳ 正在生成GPG密钥，这可能需要几分钟..."
    
    # 生成GPG密钥
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

    gpg --batch --generate-key gpg-key-config
    rm gpg-key-config
    
    # 获取密钥ID
    GPG_KEY_ID=$(gpg --list-secret-keys --keyid-format=long | grep "sec" | tail -1 | sed 's/.*\/\([A-F0-9]*\).*/\1/')
    
    print_success "GPG密钥已生成: $GPG_KEY_ID"
    
    # 上传公钥
    echo "📤 上传公钥到密钥服务器..."
    gpg --keyserver keyserver.ubuntu.com --send-keys $GPG_KEY_ID || true
    gpg --keyserver keys.openpgp.org --send-keys $GPG_KEY_ID || true
    
    # 保存配置
    echo "$GPG_KEY_ID" > .gpg-key-id
    echo "$GPG_PASSPHRASE" > .gpg-passphrase
    touch .gpg-configured
    
    print_success "GPG配置完成"
}

# 使用现有GPG配置
use_existing_gpg() {
    print_step "1" "使用现有GPG配置"
    
    if [[ -f .gpg-key-id ]] && [[ -f .gpg-passphrase ]]; then
        GPG_KEY_ID=$(cat .gpg-key-id)
        GPG_PASSPHRASE=$(cat .gpg-passphrase)
        print_success "已加载GPG配置: $GPG_KEY_ID"
    else
        print_error "GPG配置文件丢失，请删除 .gpg-configured 文件重新配置"
        exit 1
    fi
}

# 配置Maven settings.xml
configure_maven() {
    print_step "2" "配置Maven"
    
    # 备份现有配置
    if [[ -f ~/.m2/settings.xml ]]; then
        cp ~/.m2/settings.xml ~/.m2/settings.xml.backup.$(date +%Y%m%d_%H%M%S)
        print_success "已备份现有Maven配置"
    fi
    
    # 创建目录
    mkdir -p ~/.m2
    
    # 创建新配置
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

    print_success "Maven配置已更新"
}

# 运行测试
run_tests() {
    print_step "3" "运行测试"
    
    echo "🧪 执行项目测试..."
    if mvn clean test -q; then
        print_success "所有测试通过"
    else
        print_error "测试失败，请检查代码"
        exit 1
    fi
}

# 构建项目
build_project() {
    print_step "4" "构建项目"
    
    export GPG_TTY=$(tty)
    
    echo "🔨 构建项目（包含签名）..."
    if mvn clean package -P release -q; then
        print_success "项目构建成功"
        
        echo ""
        echo "📦 生成的文件:"
        ls -lh target/*.jar | grep -E "\.(jar)$"
    else
        print_error "项目构建失败"
        exit 1
    fi
}

# 发布到Maven中央仓库
publish_to_maven() {
    print_step "5" "发布到Maven中央仓库"
    
    export GPG_TTY=$(tty)
    
    echo "🚀 发布到Maven中央仓库..."
    if mvn deploy -P release -q; then
        print_success "发布成功！"
    else
        print_error "发布失败"
        exit 1
    fi
}

# 显示发布结果
show_results() {
    print_step "6" "发布完成"
    
    echo ""
    echo "🎉 恭喜！您的Spider Java Evaluation Framework已成功发布到Maven中央仓库！"
    echo ""
    echo "📋 发布信息:"
    echo "   GroupId:    com.nl2sql"
    echo "   ArtifactId: spider-evaluation"
    echo "   Version:    1.0.0"
    echo ""
    echo "🔗 查看链接:"
    echo "   Maven Central: https://search.maven.org/artifact/com.nl2sql/spider-evaluation/1.0.0/jar"
    echo "   MVN Repository: https://mvnrepository.com/artifact/com.nl2sql/spider-evaluation/1.0.0"
    echo "   Central Repo: https://repo1.maven.org/maven2/com/nl2sql/spider-evaluation/1.0.0/"
    echo ""
    echo "📝 Maven依赖:"
    echo "   <dependency>"
    echo "       <groupId>com.nl2sql</groupId>"
    echo "       <artifactId>spider-evaluation</artifactId>"
    echo "       <version>1.0.0</version>"
    echo "   </dependency>"
    echo ""
    echo "⏰ 注意: 同步到所有镜像可能需要2-4小时"
    echo ""
    print_success "发布流程完成！"
}

# 清理敏感文件
cleanup() {
    if [[ -f .gpg-passphrase ]]; then
        rm -f .gpg-passphrase
        print_success "已清理敏感文件"
    fi
}

# 主函数
main() {
    print_banner
    
    # 检查先决条件
    if ! command -v java &> /dev/null; then
        print_error "需要Java 17+，请先安装Java"
        exit 1
    fi
    
    if ! command -v mvn &> /dev/null; then
        print_error "需要Maven，请先安装Maven"
        exit 1
    fi
    
    if ! command -v gpg &> /dev/null; then
        print_error "需要GPG，正在安装..."
        if [[ "$OSTYPE" == "darwin"* ]]; then
            brew install gnupg
        else
            sudo apt-get update && sudo apt-get install -y gnupg
        fi
    fi
    
    # 检查是否首次运行
    FIRST_RUN=$(check_first_run)
    
    if [[ "$FIRST_RUN" == "true" ]]; then
        echo "🔧 检测到首次运行，将进行完整配置..."
        echo ""
        setup_gpg_first_time
    else
        echo "✅ 检测到已有配置，使用现有GPG密钥..."
        echo ""
        use_existing_gpg
    fi
    
    # 执行发布流程
    configure_maven
    run_tests
    build_project
    publish_to_maven
    show_results
    cleanup
}

# 错误处理
trap 'print_error "发布过程中出现错误"; cleanup; exit 1' ERR

# 运行主函数
main "$@" 