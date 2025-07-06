# 贡献指南

感谢您对Spider NL2SQL Evaluation Framework (Java版本)的关注！我们欢迎所有形式的贡献，无论是代码、文档、测试用例还是问题反馈。

## 📋 贡献方式

### 🐛 报告Bug

如果您发现了bug，请：

1. 在[Issues页面](https://github.com/your-username/spider-evaluation-java/issues)搜索是否已有相关问题
2. 如果没有，请创建新的Issue，包含：
   - 清晰的标题和描述
   - 重现步骤
   - 期望的行为
   - 实际的行为
   - 环境信息（Java版本、操作系统等）
   - 相关的错误日志

### 💡 功能请求

如果您有新功能的想法：

1. 先在Issues中搜索是否有类似的请求
2. 创建新的Feature Request，包含：
   - 功能的详细描述
   - 使用场景
   - 可能的实现方案
   - 是否愿意参与开发

### 🔧 代码贡献

#### 开发环境设置

1. **Fork项目**
   ```bash
   # 在GitHub上Fork项目，然后克隆到本地
   git clone https://github.com/YOUR_USERNAME/spider-evaluation-java.git
   cd spider-evaluation-java
   ```

2. **设置开发环境**
   ```bash
   # 确保Java 17+已安装
   java -version
   
   # 确保Maven 3.6+已安装
   mvn -version
   
   # 编译项目
   mvn clean compile
   
   # 运行测试
   mvn test
   ```

3. **创建功能分支**
   ```bash
   git checkout -b feature/your-feature-name
   ```

#### 代码规范

1. **Java代码风格**
   - 使用4个空格缩进，不使用Tab
   - 类名使用PascalCase
   - 方法名和变量名使用camelCase
   - 常量使用UPPER_SNAKE_CASE
   - 包名使用小写字母

2. **注释规范**
   ```java
   /**
    * 类的Javadoc注释
    * 
    * @author 作者名
    * @since 版本号
    */
   public class ExampleClass {
       
       /**
        * 方法的详细描述
        * 
        * @param param1 参数1的描述
        * @param param2 参数2的描述
        * @return 返回值的描述
        * @throws Exception 异常情况的描述
        */
       public String exampleMethod(String param1, int param2) throws Exception {
           // 实现逻辑
       }
   }
   ```

3. **命名规范**
   - 类名应该明确表达其用途
   - 方法名应该是动词或动词短语
   - 变量名应该有意义，避免缩写
   - 测试类以`Test`结尾
   - 测试方法以`test`开头

#### 测试要求

1. **单元测试**
   - 所有新功能都必须包含单元测试
   - 测试覆盖率应该达到80%以上
   - 使用JUnit 5编写测试

2. **测试示例**
   ```java
   @Test
   void testSqlTokenizer() {
       List<String> tokens = SqlTokenizer.tokenize("SELECT * FROM users");
       assertEquals(4, tokens.size());
       assertEquals("select", tokens.get(0));
       assertEquals("*", tokens.get(1));
       assertEquals("from", tokens.get(2));
       assertEquals("users", tokens.get(3));
   }
   ```

3. **集成测试**
   - 对于复杂的功能，添加集成测试
   - 确保测试数据的完整性

#### 提交规范

1. **提交信息格式**
   ```
   type(scope): subject
   
   body
   
   footer
   ```

2. **类型说明**
   - `feat`: 新功能
   - `fix`: Bug修复
   - `docs`: 文档更新
   - `style`: 代码格式修改
   - `refactor`: 代码重构
   - `test`: 测试用例修改
   - `chore`: 构建过程或辅助工具的变动

3. **提交示例**
   ```
   feat(parser): add support for WINDOW functions
   
   - Add WindowFunction model class
   - Update SqlParser to handle OVER clause
   - Add comprehensive test cases
   
   Closes #123
   ```

#### Pull Request流程

1. **提交前检查**
   ```bash
   # 运行所有测试
   mvn test
   
   # 检查代码风格
   mvn checkstyle:check
   
   # 运行集成测试
   mvn verify
   ```

2. **创建Pull Request**
   - 确保分支是最新的
   - 填写详细的PR描述
   - 关联相关的Issue
   - 添加适当的标签

3. **PR描述模板**
   ```markdown
   ## 变更描述
   简要描述这个PR的目的和内容
   
   ## 变更类型
   - [ ] Bug修复
   - [ ] 新功能
   - [ ] 重构
   - [ ] 文档更新
   - [ ] 其他
   
   ## 测试
   - [ ] 添加了新的测试用例
   - [ ] 所有现有测试通过
   - [ ] 手动测试通过
   
   ## 检查清单
   - [ ] 代码遵循项目的代码规范
   - [ ] 自我审查了代码
   - [ ] 添加了必要的注释
   - [ ] 更新了相关文档
   
   ## 相关Issue
   Closes #issue_number
   ```

### 📚 文档贡献

1. **文档类型**
   - README更新
   - API文档
   - 使用示例
   - 最佳实践指南

2. **文档规范**
   - 使用Markdown格式
   - 保持简洁明了
   - 提供实际的代码示例
   - 及时更新过时的信息

## 🎯 开发重点领域

我们特别欢迎在以下领域的贡献：

### 高优先级
- **SQL解析器增强**: 支持更多SQL语法
- **性能优化**: 提高大数据集的处理速度
- **错误处理**: 更好的错误信息和异常处理
- **测试覆盖**: 增加测试用例和边界情况

### 中优先级
- **文档完善**: 更详细的使用指南和API文档
- **示例扩展**: 更多实际使用场景的示例
- **工具集成**: 与IDE和构建工具的集成

### 低优先级
- **UI改进**: 命令行界面的用户体验优化
- **国际化**: 多语言支持
- **插件系统**: 可扩展的插件架构

## 🔍 代码审查流程

1. **自动检查**
   - 代码风格检查
   - 单元测试执行
   - 构建验证

2. **人工审查**
   - 代码逻辑正确性
   - 设计模式合理性
   - 性能考虑
   - 安全性检查

3. **审查标准**
   - 代码可读性
   - 测试充分性
   - 文档完整性
   - 向后兼容性

## 📞 社区交流

### 沟通渠道
- **GitHub Issues**: 问题报告和功能讨论
- **GitHub Discussions**: 一般性讨论和问答
- **Email**: your-email@example.com

### 行为准则
- 保持友善和专业
- 尊重不同的观点和经验水平
- 建设性地提供反馈
- 帮助新贡献者融入社区

## 🏆 贡献者认可

我们会在以下方式认可贡献者：

1. **Contributors列表**: 在README中列出所有贡献者
2. **Release Notes**: 在发布说明中感谢贡献者
3. **Special Thanks**: 对重大贡献给予特别感谢

## 📝 许可证

通过贡献代码，您同意您的贡献将在MIT许可证下发布。

## ❓ 常见问题

### Q: 我是新手，可以贡献吗？
A: 当然可以！我们欢迎所有水平的贡献者。可以从标记为"good first issue"的问题开始。

### Q: 如何选择要解决的问题？
A: 查看Issues页面，选择标记为"help wanted"或"good first issue"的问题。

### Q: 我的PR被拒绝了，怎么办？
A: 不要气馁！查看反馈意见，进行相应修改，或者在评论中讨论。

### Q: 如何跟上项目的最新进展？
A: Watch这个仓库，关注Release和Discussions页面。

---

再次感谢您的贡献！让我们一起把这个项目做得更好！ 🚀 