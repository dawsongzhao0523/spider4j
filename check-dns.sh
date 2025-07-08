#!/bin/bash

echo "🔍 检查 chatdata.com 的 TXT 记录..."
echo ""

# 检查TXT记录
echo "📋 当前TXT记录："
nslookup -type=TXT chatdata.com

echo ""
echo "🎯 期望的验证记录："
echo "jqvgtloxrl"

echo ""
echo "✅ 如果看到包含 'jqvgtloxrl' 的记录，说明DNS已生效"
echo "⏰ 如果没有看到，请等待几分钟后重试（DNS传播需要时间）"
echo ""
echo "🔄 重新检查命令："
echo "   ./check-dns.sh" 