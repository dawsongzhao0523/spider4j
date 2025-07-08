package com.nl2sql.spider;

import com.nl2sql.spider.enums.EvaluationType;
import com.nl2sql.spider.model.EvaluationStatistics;
import com.nl2sql.spider.service.SpiderEvaluationService;
import com.nl2sql.spider.utils.DataValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Spider验证命令行工具
 * 提供数据验证和逻辑验证功能
 */
public class SpiderValidationCLI {
    
    private static final Logger logger = LoggerFactory.getLogger(SpiderValidationCLI.class);
    
    public static void main(String[] args) {
        if (args.length < 1) {
            printUsage();
            System.exit(1);
        }
        
        String command = args[0];
        
        switch (command) {
            case "validate-data":
                validateData(args);
                break;
            case "validate-logic":
                validateLogic(args);
                break;
            case "validate-all":
                validateAll(args);
                break;
            default:
                System.err.println("Unknown command: " + command);
                printUsage();
                System.exit(1);
        }
    }
    
    /**
     * 验证数据完整性
     */
    private static void validateData(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java -jar spider-validation.jar validate-data <data-dir>");
            System.exit(1);
        }
        
        String dataDir = args[1];
        
        System.out.println("=".repeat(80));
        System.out.println("SPIDER DATA VALIDATION");
        System.out.println("=".repeat(80));
        System.out.println("Data directory: " + dataDir);
        System.out.println();
        
        DataValidator validator = new DataValidator();
        DataValidator.ValidationResult result = validator.validateDataset(dataDir);
        
        result.printSummary();
        
        if (!result.isValid()) {
            System.exit(1);
        }
    }
    
    /**
     * 验证逻辑正确性
     */
    private static void validateLogic(String[] args) {
        if (args.length < 6) {
            System.err.println("Usage: java -jar spider-validation.jar validate-logic <gold-file> <pred-file> <db-dir> <table-file> [eval-type]");
            System.exit(1);
        }
        
        String goldFile = args[1];
        String predFile = args[2];
        String dbDir = args[3];
        String tableFile = args[4];
        String evalType = args.length > 5 ? args[5] : "all";
        
        System.out.println("=".repeat(80));
        System.out.println("SPIDER LOGIC VALIDATION");
        System.out.println("=".repeat(80));
        System.out.println("Gold file: " + goldFile);
        System.out.println("Prediction file: " + predFile);
        System.out.println("Database directory: " + dbDir);
        System.out.println("Table file: " + tableFile);
        System.out.println("Evaluation type: " + evalType);
        System.out.println();
        
        // 验证文件存在
        if (!Files.exists(Paths.get(goldFile))) {
            System.err.println("Error: Gold file not found: " + goldFile);
            System.exit(1);
        }
        
        if (!Files.exists(Paths.get(predFile))) {
            System.err.println("Error: Prediction file not found: " + predFile);
            System.exit(1);
        }
        
        if (!Files.exists(Paths.get(dbDir))) {
            System.err.println("Error: Database directory not found: " + dbDir);
            System.exit(1);
        }
        
        if (!Files.exists(Paths.get(tableFile))) {
            System.err.println("Error: Table file not found: " + tableFile);
            System.exit(1);
        }
        
        try {
            EvaluationType evaluationType = EvaluationType.fromValue(evalType);
            SpiderEvaluationService service = new SpiderEvaluationService();
            
            long startTime = System.currentTimeMillis();
            EvaluationStatistics statistics = service.evaluate(goldFile, predFile, dbDir, tableFile, evaluationType);
            long endTime = System.currentTimeMillis();
            
            statistics.printResults();
            
            System.out.println();
            System.out.printf("Validation completed in %.2f seconds%n", (endTime - startTime) / 1000.0);
            
        } catch (Exception e) {
            logger.error("Logic validation failed", e);
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }
    
    /**
     * 完整验证（数据+逻辑）
     */
    private static void validateAll(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java -jar spider-validation.jar validate-all <data-dir> [eval-type]");
            System.exit(1);
        }
        
        String dataDir = args[1];
        String evalType = args.length > 2 ? args[2] : "all";
        
        System.out.println("=".repeat(80));
        System.out.println("SPIDER COMPLETE VALIDATION");
        System.out.println("=".repeat(80));
        System.out.println("Data directory: " + dataDir);
        System.out.println("Evaluation type: " + evalType);
        System.out.println();
        
        // 步骤1: 验证数据完整性
        System.out.println("Step 1: Validating data integrity...");
        DataValidator validator = new DataValidator();
        DataValidator.ValidationResult dataResult = validator.validateDataset(dataDir);
        
        if (!dataResult.isValid()) {
            System.err.println("Data validation failed. Cannot proceed with logic validation.");
            dataResult.printSummary();
            System.exit(1);
        }
        
        System.out.println("Data validation passed!");
        System.out.println();
        
        // 步骤2: 验证逻辑正确性（使用evaluation_examples作为测试数据）
        System.out.println("Step 2: Validating logic correctness...");
        
        String goldFile = "evaluation_examples/gold_example.txt";
        String predFile = "evaluation_examples/pred_example.txt";
        String dbDir = dataDir + "/database";
        String tableFile = dataDir + "/tables.json";
        
        if (!Files.exists(Paths.get(goldFile)) || !Files.exists(Paths.get(predFile))) {
            System.err.println("Warning: evaluation_examples not found. Skipping logic validation.");
            System.err.println("To run logic validation, ensure evaluation_examples directory exists with gold_example.txt and pred_example.txt");
            return;
        }
        
        try {
            EvaluationType evaluationType = EvaluationType.fromValue(evalType);
            SpiderEvaluationService service = new SpiderEvaluationService();
            
            long startTime = System.currentTimeMillis();
            EvaluationStatistics statistics = service.evaluate(goldFile, predFile, dbDir, tableFile, evaluationType);
            long endTime = System.currentTimeMillis();
            
            statistics.printResults();
            
            System.out.println();
            System.out.printf("Logic validation completed in %.2f seconds%n", (endTime - startTime) / 1000.0);
            
            // 步骤3: 验证eval_test数据
            System.out.println();
            System.out.println("Step 3: Validating with eval_test data...");
            
            String evalGoldFile = "eval_test/gold.txt";
            String evalPredFile = "eval_test/pred.txt";
            
            if (Files.exists(Paths.get(evalGoldFile)) && Files.exists(Paths.get(evalPredFile))) {
                startTime = System.currentTimeMillis();
                EvaluationStatistics evalStats = service.evaluate(evalGoldFile, evalPredFile, dbDir, tableFile, evaluationType);
                endTime = System.currentTimeMillis();
                
                System.out.println("Eval Test Results:");
                evalStats.printResults();
                
                System.out.printf("Eval test validation completed in %.2f seconds%n", (endTime - startTime) / 1000.0);
            } else {
                System.out.println("eval_test data not found. Skipping eval_test validation.");
            }
            
            System.out.println();
            System.out.println("=".repeat(80));
            System.out.println("VALIDATION COMPLETED SUCCESSFULLY!");
            System.out.println("=".repeat(80));
            
        } catch (Exception e) {
            logger.error("Complete validation failed", e);
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }
    
    private static void printUsage() {
        System.out.println("Spider Validation Tool");
        System.out.println();
        System.out.println("Usage: java -jar spider-validation.jar <command> [options]");
        System.out.println();
        System.out.println("Commands:");
        System.out.println("  validate-data <data-dir>");
        System.out.println("    Validate data integrity and format");
        System.out.println();
        System.out.println("  validate-logic <gold-file> <pred-file> <db-dir> <table-file> [eval-type]");
        System.out.println("    Validate logic correctness");
        System.out.println("    eval-type: match, exec, all (default: all)");
        System.out.println();
        System.out.println("  validate-all <data-dir> [eval-type]");
        System.out.println("    Run complete validation (data + logic)");
        System.out.println("    eval-type: match, exec, all (default: all)");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  java -jar spider-validation.jar validate-data data/spider");
        System.out.println("  java -jar spider-validation.jar validate-logic \\");
        System.out.println("    evaluation_examples/gold_example.txt \\");
        System.out.println("    evaluation_examples/pred_example.txt \\");
        System.out.println("    data/spider/database \\");
        System.out.println("    data/spider/tables.json \\");
        System.out.println("    all");
        System.out.println("  java -jar spider-validation.jar validate-all data/spider all");
    }
} 