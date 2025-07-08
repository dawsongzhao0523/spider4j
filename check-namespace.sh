#!/bin/bash

# 检查Sonatype Central Portal账号和命名空间状态

echo "🔍 检查Sonatype Central Portal账号状态"
echo "=========================================="

# 用户凭据
USERNAME="Nsp+IG/5"
PASSWORD="cJ5uy6QH14z1EaSANbDZom2v0fFyPpp5G+aGqZJ/bYo8"

# 编码凭据
AUTH_HEADER=$(echo -n "$USERNAME:$PASSWORD" | base64)

echo "📋 账号信息:"
echo "   用户名: $USERNAME"
echo "   认证头: Bearer $AUTH_HEADER"
echo ""

# 检查账号状态
echo "🌐 测试API连接..."
curl -s -H "Authorization: Bearer $AUTH_HEADER" \
     "https://central.sonatype.com/api/v1/publisher/deployments" \
     | head -20

echo ""
echo ""
echo "💡 建议的命名空间选项:"
echo "1. 如果您有GitHub账号 'dszhao': io.github.dszhao"
echo "2. 如果您有域名: com.yourdomain" 
echo "3. 临时测试用: com.example (可能不被允许)"
echo ""
echo "📝 要验证io.github.dszhao命名空间，您需要:"
echo "1. 确保GitHub用户名是 'dszhao'"
echo "2. 在Sonatype Central Portal中申请该命名空间"
echo "3. 验证GitHub仓库所有权"
echo ""
echo "🔗 验证链接:"
echo "   Central Portal: https://central.sonatype.com/namespaces"
echo "   账号页面: https://central.sonatype.com/account" 