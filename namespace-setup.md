# 🎯 Sonatype Central Portal 命名空间设置指南

## 当前问题
您的发布失败是因为命名空间权限问题：
```
Namespace 'io.github.dszhao' is not allowed
```

## 🔧 解决方案

### 步骤1: 验证GitHub用户名
首先确认您的GitHub用户名是什么。如果是`dszhao`，那么您可以申请`io.github.dszhao`命名空间。

### 步骤2: 在Sonatype Central Portal申请命名空间
1. 访问：https://central.sonatype.com/namespaces
2. 登录您的账号 (Nsp+IG/5)
3. 点击 "Add Namespace"
4. 输入：`io.github.dszhao`
5. 按照提示验证GitHub仓库所有权

### 步骤3: 验证GitHub仓库
您需要：
1. 在GitHub上创建一个公开仓库（如果还没有的话）
2. 按照Sonatype的指示在仓库中添加验证文件
3. 等待验证通过

### 步骤4: 使用已验证的命名空间
一旦命名空间被验证，您就可以使用它来发布。

## 🚀 临时解决方案

如果您想立即测试发布流程，我们可以：

### 选项1: 使用您现有的域名
如果您拥有任何域名，可以使用：
- `com.yourdomain`
- 需要在域名的DNS中添加TXT记录验证

### 选项2: 申请临时测试命名空间
某些特殊的命名空间可能可以直接使用，但这不是推荐的长期解决方案。

## 📋 推荐操作

1. **立即操作**：
   - 访问 https://central.sonatype.com/namespaces
   - 申请 `io.github.dszhao` 命名空间（假设您的GitHub用户名是dszhao）

2. **验证GitHub**：
   - 确保您有一个名为 `dszhao` 的GitHub账号
   - 创建一个公开仓库用于验证

3. **等待验证**：
   - 通常需要几分钟到几小时
   - 验证通过后您就可以发布了

## 🔗 有用的链接
- [Central Portal](https://central.sonatype.com/)
- [命名空间管理](https://central.sonatype.com/namespaces)
- [账号页面](https://central.sonatype.com/account)
- [GitHub验证指南](https://central.sonatype.org/register/central-portal/)

## ❓ 如果您的GitHub用户名不是 'dszhao'
请告诉我您的实际GitHub用户名，我会相应更新配置。 