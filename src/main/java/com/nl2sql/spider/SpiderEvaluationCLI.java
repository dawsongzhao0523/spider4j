package com.nl2sql.spider;

import com.nl2sql.spider.enums.EvaluationType;
import com.nl2sql.spider.model.EvaluationStatistics;
import com.nl2sql.spider.service.SpiderEvaluationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Spider评估命令行接口
 * 对应Python版本的evaluation.py主函数
 */
public class SpiderEvaluationCLI {
    
    private static final Logger logger = LoggerFactory.getLogger(SpiderEvaluationCLI.class);
    
    public static void main(String[] args) {
        if (args.length < 8) {
            printUsage();
            System.exit(1);
        }
        
        String goldFile = null;
        String predFile = null;
        String dbDir = null;
        String tableFile = null;
        String evaluationType = "all";
        
        // 解析命令行参数
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--gold":
                    if (i + 1 < args.length) {
                        goldFile = args[++i];
                    }
                    break;
                case "--pred":
                    if (i + 1 < args.length) {
                        predFile = args[++i];
                    }
                    break;
                case "--db":
                    if (i + 1 < args.length) {
                        dbDir = args[++i];
                    }
                    break;
                case "--table":
                    if (i + 1 < args.length) {
                        tableFile = args[++i];
                    }
                    break;
                case "--etype":
                    if (i + 1 < args.length) {
                        evaluationType = args[++i];
                    }
                    break;
                default:
                    // 忽略未知参数
                    break;
            }
        }
        
        // 验证必需参数
        if (goldFile == null || predFile == null || dbDir == null || tableFile == null) {
            System.err.println("Error: Missing required parameters");
            printUsage();
            System.exit(1);
        }
        
        try {
            // 解析评估类型
            EvaluationType evalType = EvaluationType.fromValue(evaluationType);
            
            // 创建评估服务
            SpiderEvaluationService service = new SpiderEvaluationService();
            
            System.out.println("Starting Spider evaluation...");
            System.out.println("Gold file: " + goldFile);
            System.out.println("Prediction file: " + predFile);
            System.out.println("Database directory: " + dbDir);
            System.out.println("Table file: " + tableFile);
            System.out.println("Evaluation type: " + evaluationType);
            System.out.println();
            
            // 执行评估
            long startTime = System.currentTimeMillis();
            EvaluationStatistics statistics = service.evaluate(goldFile, predFile, dbDir, tableFile, evalType);
            long endTime = System.currentTimeMillis();
            
            // 打印结果
            statistics.printResults();
            
            System.out.println();
            System.out.printf("Evaluation completed in %.2f seconds%n", (endTime - startTime) / 1000.0);
            
        } catch (Exception e) {
            logger.error("Evaluation failed", e);
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }
    
    private static void printUsage() {
        System.out.println("Usage: java -jar spider-evaluation.jar [OPTIONS]");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  --gold <file>     Gold SQL file (required)");
        System.out.println("  --pred <file>     Predicted SQL file (required)");
        System.out.println("  --db <dir>        Database directory (required)");
        System.out.println("  --table <file>    Table schema file (required)");
        System.out.println("  --etype <type>    Evaluation type: match, exec, all (default: all)");
        System.out.println();
        System.out.println("Example:");
        System.out.println("  java -jar spider-evaluation.jar \\");
        System.out.println("    --gold dev_gold.sql \\");
        System.out.println("    --pred predictions.sql \\");
        System.out.println("    --db database/ \\");
        System.out.println("    --table tables.json \\");
        System.out.println("    --etype all");
    }
} 