# Evaluation Tests (Java版本)

这个目录包含了Spider Java版本评估框架的测试用例和验证脚本。

## 测试结构

```
eval_test_j/
├── unit_tests/              # 单元测试
│   ├── parser_test/        # SQL解析器测试
│   ├── evaluator_test/     # 评估器测试
│   └── service_test/       # 服务层测试
├── integration_tests/       # 集成测试
│   ├── end_to_end_test/    # 端到端测试
│   └── performance_test/   # 性能测试
├── validation/              # 验证脚本
│   ├── compare_with_python.py  # 与Python版本对比
│   └── accuracy_check.java     # 准确性检查
└── README.md               # 本文件
```

## 运行测试

### 单元测试
```bash
cd spider_j
mvn test
```

### 集成测试
```bash
cd spider_j
mvn verify
```

### 特定测试类
```bash
mvn test -Dtest=SqlTokenizerTest
mvn test -Dtest=SpiderEvaluatorTest
mvn test -Dtest=SpiderEvaluationServiceTest
```

### 性能测试
```bash
cd eval_test_j/integration_tests/performance_test
mvn clean package
java -jar target/performance-test-1.0.0.jar
```

## 验证脚本

### 与Python版本对比
```bash
cd eval_test_j/validation

# 运行Python版本
python3 ../../../evaluation.py \
  --gold ../../evaluation_examples_j/gold_example.txt \
  --pred ../../evaluation_examples_j/pred_example.txt \
  --db ../../../data/spider/database/ \
  --table ../../../data/spider/tables.json \
  --etype all > python_result.txt

# 运行Java版本
cd ../../
java -jar target/spider-evaluation-1.0.0.jar \
  --gold evaluation_examples_j/gold_example.txt \
  --pred evaluation_examples_j/pred_example.txt \
  --db ../data/spider/database/ \
  --table ../data/spider/tables.json \
  --etype all > eval_test_j/validation/java_result.txt

# 对比结果
cd eval_test_j/validation
python3 compare_with_python.py python_result.txt java_result.txt
```

## 测试数据

### 测试用例分类

1. **基础功能测试**
   - SQL分词测试
   - SQL解析测试
   - 基本评估测试

2. **边界情况测试**
   - 空SQL处理
   - 错误SQL处理
   - 特殊字符处理

3. **复杂查询测试**
   - 嵌套查询
   - 多表JOIN
   - 复杂聚合

4. **性能测试**
   - 大数据集处理
   - 内存使用测试
   - 并发处理测试

### 测试数据生成

```bash
# 生成测试数据
cd eval_test_j/validation
java -cp ../../target/spider-evaluation-1.0.0.jar \
  com.nl2sql.spider.test.TestDataGenerator \
  --output test_cases.json \
  --count 1000
```

## 持续集成

### GitHub Actions配置
```yaml
# .github/workflows/java-ci.yml
name: Java CI

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v2
    
    - name: Set up JDK 17
      uses: actions/setup-java@v2
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Cache Maven packages
      uses: actions/cache@v2
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
    
    - name: Run tests
      run: |
        cd spider_j
        mvn clean verify
    
    - name: Run integration tests
      run: |
        cd spider_j/eval_test_j
        ./run_all_tests.sh
```

## 测试覆盖率

目标测试覆盖率：
- 单元测试：>= 80%
- 集成测试：>= 70%
- 端到端测试：>= 90%

查看覆盖率报告：
```bash
cd spider_j
mvn jacoco:report
open target/site/jacoco/index.html
```

## 基准测试

### 性能基准
- SQL解析速度：>= 1000 queries/second
- 评估速度：>= 500 evaluations/second
- 内存使用：<= 2GB for 10K queries

### 准确性基准
- 与Python版本结果一致性：>= 99.9%
- 精确匹配准确性：>= 95%
- 执行准确性：>= 90%

## 故障排除

### 常见问题

1. **内存不足**
```bash
# 增加JVM内存
export MAVEN_OPTS="-Xmx4g"
mvn test
```

2. **SQLite数据库锁定**
```bash
# 清理临时文件
find . -name "*.db-journal" -delete
```

3. **字符编码问题**
```bash
# 设置UTF-8编码
export JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8"
```

## 贡献测试用例

欢迎贡献新的测试用例！请确保：

1. 测试用例有明确的目的
2. 包含预期结果
3. 有适当的文档说明
4. 遵循命名约定

### 测试用例模板
```java
@Test
public void testSpecificFeature() {
    // Given
    String input = "test input";
    String expected = "expected output";
    
    // When
    String actual = methodUnderTest(input);
    
    // Then
    assertEquals(expected, actual);
}
``` 