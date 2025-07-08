#!/bin/bash

# Spider Java Evaluation Framework - Maven中央仓库发布脚本
# 自动化发布到 https://mvnrepository.com/

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 打印彩色信息
print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
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

print_header() {
    echo -e "${BLUE}"
    echo "=================================================="
    echo "  Spider Java Evaluation Framework"
    echo "  Maven中央仓库发布脚本"
    echo "=================================================="
    echo -e "${NC}"
}

# 检查先决条件
check_prerequisites() {
    print_info "检查发布先决条件..."
    
    # 检查Java版本
    if ! command -v java &> /dev/null; then
        print_error "Java未安装"
        exit 1
    fi
    
    JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | sed '/^1\./s///' | cut -d'.' -f1)
    if [[ $JAVA_VERSION -lt 17 ]]; then
        print_error "需要Java 17或更高版本，当前版本: $JAVA_VERSION"
        exit 1
    fi
    print_success "Java版本检查通过: $JAVA_VERSION"
    
    # 检查Maven
    if ! command -v mvn &> /dev/null; then
        print_error "Maven未安装"
        exit 1
    fi
    print_success "Maven检查通过"
    
    # 检查GPG
    if ! command -v gpg &> /dev/null; then
        print_error "GPG未安装，请先运行: ./setup-gpg.sh"
        exit 1
    fi
    
    # 检查GPG密钥
    if ! gpg --list-secret-keys --keyid-format=long | grep -q "sec"; then
        print_error "未找到GPG密钥，请先运行: ./setup-gpg.sh"
        exit 1
    fi
    print_success "GPG密钥检查通过"
    
    # 检查Maven settings.xml
    if [[ ! -f ~/.m2/settings.xml ]]; then
        print_error "Maven settings.xml未配置，请先运行: ./setup-gpg.sh"
        exit 1
    fi
    print_success "Maven配置检查通过"
}

# 验证项目配置
validate_project() {
    print_info "验证项目配置..."
    
    # 检查pom.xml
    if [[ ! -f pom.xml ]]; then
        print_error "pom.xml文件不存在"
        exit 1
    fi
    
    # 检查必需的pom.xml元素
    local required_elements=(
        "groupId" "artifactId" "version" "name" "description" 
        "url" "licenses" "developers" "scm"
    )
    
    for element in "${required_elements[@]}"; do
        if ! grep -q "<$element>" pom.xml; then
            print_error "pom.xml缺少必需元素: $element"
            exit 1
        fi
    done
    
    print_success "项目配置验证通过"
}

# 运行测试
run_tests() {
    print_info "运行项目测试..."
    
    if ! mvn clean test; then
        print_error "测试失败，请修复后重试"
        exit 1
    fi
    
    print_success "所有测试通过"
}

# 构建项目
build_project() {
    print_info "构建项目..."
    
    # 设置GPG环境
    export GPG_TTY=$(tty)
    
    # 使用release profile构建
    if ! mvn clean package -P release; then
        print_error "项目构建失败"
        exit 1
    fi
    
    print_success "项目构建成功"
    
    # 检查生成的文件
    local jar_file="target/spider-evaluation-1.0.0.jar"
    local sources_jar="target/spider-evaluation-1.0.0-sources.jar"
    local javadoc_jar="target/spider-evaluation-1.0.0-javadoc.jar"
    
    if [[ ! -f "$jar_file" ]] || [[ ! -f "$sources_jar" ]] || [[ ! -f "$javadoc_jar" ]]; then
        print_error "构建文件不完整"
        exit 1
    fi
    
    print_success "构建文件检查通过"
    
    # 显示文件信息
    echo ""
    print_info "生成的文件:"
    ls -lh target/*.jar | grep -E "\.(jar)$"
}

# 发布到Maven中央仓库
publish_to_central() {
    print_info "发布到Maven中央仓库..."
    
    # 设置GPG环境
    export GPG_TTY=$(tty)
    
    # 使用Central Publishing Maven Plugin发布
    if ! mvn clean deploy -P release; then
        print_error "发布失败"
        exit 1
    fi
    
    print_success "发布成功！"
}

# 验证发布结果
verify_publication() {
    print_info "验证发布结果..."
    
    local group_path="com/nl2sql"
    local artifact_id="spider-evaluation"
    local version="1.0.0"
    
    print_info "请等待几分钟，然后检查以下链接："
    echo ""
    echo "🔗 Maven中央仓库搜索:"
    echo "   https://search.maven.org/artifact/com.nl2sql/spider-evaluation/1.0.0/jar"
    echo ""
    echo "🔗 MVN Repository:"
    echo "   https://mvnrepository.com/artifact/com.nl2sql/spider-evaluation/1.0.0"
    echo ""
    echo "🔗 Central Repository:"
    echo "   https://repo1.maven.org/maven2/com/nl2sql/spider-evaluation/1.0.0/"
    echo ""
    
    print_warning "注意: 同步到所有镜像可能需要2-4小时"
}

# 生成使用示例
generate_usage_examples() {
    print_info "生成使用示例..."
    
    cat > MAVEN_USAGE_EXAMPLES.md <<EOF
# Spider Java Evaluation Framework - 使用示例

## Maven依赖

\`\`\`xml
<dependency>
    <groupId>com.nl2sql</groupId>
    <artifactId>spider-evaluation</artifactId>
    <version>1.0.0</version>
</dependency>
\`\`\`

## Gradle依赖

\`\`\`gradle
implementation 'com.nl2sql:spider-evaluation:1.0.0'
\`\`\`

## SBT依赖

\`\`\`scala
libraryDependencies += "com.nl2sql" % "spider-evaluation" % "1.0.0"
\`\`\`

## 基本使用

\`\`\`java
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
\`\`\`

## Spring Boot集成

\`\`\`java
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
\`\`\`

## 发布信息

- **GroupId**: com.nl2sql
- **ArtifactId**: spider-evaluation
- **Version**: 1.0.0
- **发布日期**: $(date +%Y-%m-%d)
- **仓库**: Maven Central Repository

## 链接

- [Maven Central](https://search.maven.org/artifact/com.nl2sql/spider-evaluation/1.0.0/jar)
- [MVN Repository](https://mvnrepository.com/artifact/com.nl2sql/spider-evaluation/1.0.0)
- [GitHub Repository](https://github.com/yourusername/spider-java-evaluation)

EOF

    print_success "使用示例已生成: MAVEN_USAGE_EXAMPLES.md"
}

# 清理临时文件
cleanup() {
    print_info "清理临时文件..."
    
    # 删除敏感文件
    if [[ -f private-key.asc ]]; then
        rm -f private-key.asc
        print_success "已删除私钥文件"
    fi
    
    if [[ -f gpg-env.sh ]]; then
        rm -f gpg-env.sh
        print_success "已删除环境变量文件"
    fi
}

# 主函数
main() {
    print_header
    
    # 解析命令行参数
    SKIP_TESTS=false
    SKIP_BUILD=false
    DRY_RUN=false
    
    while [[ $# -gt 0 ]]; do
        case $1 in
            --skip-tests)
                SKIP_TESTS=true
                shift
                ;;
            --skip-build)
                SKIP_BUILD=true
                shift
                ;;
            --dry-run)
                DRY_RUN=true
                shift
                ;;
            --help)
                echo "用法: $0 [选项]"
                echo ""
                echo "选项:"
                echo "  --skip-tests    跳过测试"
                echo "  --skip-build    跳过构建（仅发布）"
                echo "  --dry-run       模拟运行（不实际发布）"
                echo "  --help          显示帮助信息"
                exit 0
                ;;
            *)
                print_error "未知选项: $1"
                exit 1
                ;;
        esac
    done
    
    # 执行发布流程
    check_prerequisites
    validate_project
    
    if [[ "$SKIP_TESTS" != "true" ]]; then
        run_tests
    else
        print_warning "跳过测试"
    fi
    
    if [[ "$SKIP_BUILD" != "true" ]]; then
        build_project
    else
        print_warning "跳过构建"
    fi
    
    if [[ "$DRY_RUN" == "true" ]]; then
        print_warning "模拟运行模式，不会实际发布"
    else
        publish_to_central
        verify_publication
        generate_usage_examples
    fi
    
    cleanup
    
    echo ""
    print_success "发布流程完成！"
    echo ""
    print_info "后续步骤:"
    echo "1. 等待2-4小时让包同步到所有镜像"
    echo "2. 在 https://search.maven.org 搜索您的包"
    echo "3. 在 https://mvnrepository.com 查看包信息"
    echo "4. 在项目中使用新发布的依赖"
    echo ""
    print_success "🎉 恭喜！您的包已成功发布到Maven中央仓库！"
}

# 错误处理
trap 'print_error "发布过程中出现错误，请检查日志"; cleanup; exit 1' ERR

# 运行主函数
main "$@" 