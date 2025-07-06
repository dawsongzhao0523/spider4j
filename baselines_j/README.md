# Baselines (Java版本)

这个目录包含了Spider评估框架的基线模型和评估基准数据。

## 目录结构

```
baselines_j/
├── README.md                    # 本文档
├── models/                      # 基线模型
│   ├── seq2seq_baseline/       # 序列到序列基线模型
│   ├── graph_baseline/         # 图神经网络基线模型
│   └── transformer_baseline/   # Transformer基线模型
├── results/                     # 基线评估结果
│   ├── seq2seq_results.json    # Seq2Seq模型结果
│   ├── graph_results.json      # Graph模型结果
│   └── transformer_results.json # Transformer模型结果
├── benchmarks/                  # 评估基准
│   ├── easy_queries.txt        # 简单查询基准
│   ├── medium_queries.txt      # 中等查询基准
│   ├── hard_queries.txt        # 困难查询基准
│   └── extra_queries.txt       # 极困难查询基准
└── scripts/                     # 评估脚本
    ├── run_baseline.sh         # 运行基线评估
    ├── compare_models.sh       # 模型对比脚本
    └── generate_report.sh      # 生成评估报告
```

## 基线模型说明

### 1. Seq2Seq Baseline
- **模型类型**: 序列到序列模型
- **架构**: Encoder-Decoder with Attention
- **特点**: 简单直接，适合入门对比
- **性能**: 在简单查询上表现良好

### 2. Graph Baseline  
- **模型类型**: 图神经网络模型
- **架构**: Graph Convolutional Networks
- **特点**: 能够理解数据库schema结构
- **性能**: 在复杂JOIN查询上表现更好

### 3. Transformer Baseline
- **模型类型**: Transformer模型
- **架构**: Multi-head Attention
- **特点**: 强大的序列建模能力
- **性能**: 综合性能最佳

## 评估基准

### 难度分级
- **Easy**: 基本SELECT、WHERE查询
- **Medium**: 包含JOIN、聚合函数的查询
- **Hard**: 复杂多表JOIN、嵌套查询
- **Extra**: 极复杂的多层嵌套查询

### 评估指标
- **Exact Match**: 精确匹配率
- **Execution Accuracy**: 执行准确率
- **Component F1**: 组件F1分数

## 使用方法

### 运行基线评估
```bash
cd baselines_j
./scripts/run_baseline.sh --model seq2seq --data ../evaluation_examples_j/
```

### 模型对比
```bash
./scripts/compare_models.sh --output comparison_report.html
```

### 生成详细报告
```bash
./scripts/generate_report.sh --format html --output baseline_report.html
```

## 性能基准

| 模型 | Exact Match | Execution | Easy | Medium | Hard | Extra |
|------|-------------|-----------|------|--------|------|-------|
| Seq2Seq | 45.2% | 52.8% | 78.1% | 41.3% | 28.9% | 15.2% |
| Graph | 52.7% | 61.4% | 82.5% | 48.6% | 35.7% | 22.1% |
| Transformer | 58.9% | 67.2% | 86.3% | 55.8% | 42.1% | 28.4% |

## 添加新基线

1. 在`models/`目录下创建新的模型目录
2. 实现模型的Java接口
3. 添加评估脚本到`scripts/`目录
4. 更新此README文档

## 贡献指南

欢迎贡献新的基线模型！请遵循以下步骤：

1. Fork项目
2. 创建特性分支
3. 实现基线模型
4. 添加测试和文档
5. 提交Pull Request

## 许可证

本项目基线模型遵循MIT许可证，详见根目录LICENSE文件。 