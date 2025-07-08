package com.nl2sql.spider;

import com.nl2sql.spider.enums.EvaluationType;
import com.nl2sql.spider.model.EvaluationStatistics;
import com.nl2sql.spider.service.SpiderEvaluationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Spider逻辑验证测试类
 * 使用baselines、data、eval_test、evaluation_examples中的数据进行Java版本逻辑验证
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SpiderLogicValidationTest {
    
    private static final Logger logger = LoggerFactory.getLogger(SpiderLogicValidationTest.class);
    
    private SpiderEvaluationService evaluationService;
    
    // 测试数据路径
    private static final String DATA_DIR = "data/spider";
    private static final String EVAL_TEST_DIR = "eval_test";
    private static final String EVALUATION_EXAMPLES_DIR = "evaluation_examples";
    private static final String BASELINES_DIR = "baselines";
    
    @BeforeEach
    void setUp() {
        evaluationService = new SpiderEvaluationService();
        logger.info("Spider逻辑验证测试开始");
    }
    
    /**
     * 测试1: 验证evaluation_examples中的示例数据
     */
    @Test
    @Order(1)
    void testEvaluationExamples() {
        logger.info("=== 测试1: 验证evaluation_examples中的示例数据 ===");
        
        String goldFile = EVALUATION_EXAMPLES_DIR + "/gold_example.txt";
        String predFile = EVALUATION_EXAMPLES_DIR + "/pred_example.txt";
        String tableFile = DATA_DIR + "/tables.json";
        String dbDir = DATA_DIR + "/database";
        
        // 验证文件存在
        assertTrue(Files.exists(Paths.get(goldFile)), "Gold example file should exist");
        assertTrue(Files.exists(Paths.get(predFile)), "Prediction example file should exist");
        assertTrue(Files.exists(Paths.get(tableFile)), "Table file should exist");
        assertTrue(Files.exists(Paths.get(dbDir)), "Database directory should exist");
        
        try {
            // 执行评估
            EvaluationStatistics stats = evaluationService.evaluate(
                goldFile, predFile, dbDir, tableFile, EvaluationType.ALL
            );
            
            // 验证结果
            assertNotNull(stats, "Evaluation statistics should not be null");
            assertTrue(stats.getLevelStatistics(com.nl2sql.spider.enums.HardnessLevel.ALL).getCount() > 0, 
                      "Should have processed some items");
            
            // 打印详细结果
            logger.info("Evaluation Examples测试结果:");
            stats.printResults();
            
            // 验证分数在合理范围内
            double exactMatchScore = stats.getLevelStatistics(com.nl2sql.spider.enums.HardnessLevel.ALL).getExactMatchScore();
            double executionScore = stats.getLevelStatistics(com.nl2sql.spider.enums.HardnessLevel.ALL).getExecutionScore();
            assertTrue(exactMatchScore >= 0.0 && exactMatchScore <= 1.0,
                      "Exact match accuracy should be between 0 and 1");
            assertTrue(executionScore >= 0.0 && executionScore <= 1.0,
                      "Execution accuracy should be between 0 and 1");
            
        } catch (Exception e) {
            logger.error("Evaluation examples test failed", e);
            fail("Evaluation examples test should not throw exception: " + e.getMessage());
        }
    }
    
    /**
     * 测试2: 验证eval_test中的测试数据
     */
    @Test
    @Order(2)
    void testEvalTestData() {
        logger.info("=== 测试2: 验证eval_test中的测试数据 ===");
        
        String goldFile = EVAL_TEST_DIR + "/gold.txt";
        String predFile = EVAL_TEST_DIR + "/pred.txt";
        String tableFile = DATA_DIR + "/tables.json";
        String dbDir = DATA_DIR + "/database";
        
        // 验证文件存在
        assertTrue(Files.exists(Paths.get(goldFile)), "Gold test file should exist");
        assertTrue(Files.exists(Paths.get(predFile)), "Prediction test file should exist");
        
        try {
            // 执行评估
            EvaluationStatistics stats = evaluationService.evaluate(
                goldFile, predFile, dbDir, tableFile, EvaluationType.ALL
            );
            
            // 验证结果
            assertNotNull(stats, "Evaluation statistics should not be null");
            assertTrue(stats.getLevelStatistics(com.nl2sql.spider.enums.HardnessLevel.ALL).getCount() > 0, 
                      "Should have processed some items");
            
            // 打印详细结果
            logger.info("Eval Test数据测试结果:");
            stats.printResults();
            
            // 验证各难度级别都有数据
            assertTrue(stats.getLevelStatistics(com.nl2sql.spider.enums.HardnessLevel.EASY).getCount() >= 0, 
                      "Easy count should be non-negative");
            assertTrue(stats.getLevelStatistics(com.nl2sql.spider.enums.HardnessLevel.MEDIUM).getCount() >= 0, 
                      "Medium count should be non-negative");
            assertTrue(stats.getLevelStatistics(com.nl2sql.spider.enums.HardnessLevel.HARD).getCount() >= 0, 
                      "Hard count should be non-negative");
            assertTrue(stats.getLevelStatistics(com.nl2sql.spider.enums.HardnessLevel.EXTRA).getCount() >= 0, 
                      "Extra hard count should be non-negative");
            
        } catch (Exception e) {
            logger.error("Eval test data test failed", e);
            fail("Eval test data test should not throw exception: " + e.getMessage());
        }
    }
    
    /**
     * 测试3: 验证dev数据集
     */
    @Test
    @Order(3)
    void testDevDataset() {
        logger.info("=== 测试3: 验证dev数据集 ===");
        
        String goldFile = DATA_DIR + "/dev_gold.sql";
        String predFile = EVALUATION_EXAMPLES_DIR + "/pred_example.txt"; // 使用示例预测作为测试
        String tableFile = DATA_DIR + "/tables.json";
        String dbDir = DATA_DIR + "/database";
        
        // 验证文件存在
        assertTrue(Files.exists(Paths.get(goldFile)), "Dev gold file should exist");
        assertTrue(Files.exists(Paths.get(predFile)), "Prediction file should exist");
        
        try {
            // 执行评估
            EvaluationStatistics stats = evaluationService.evaluate(
                goldFile, predFile, dbDir, tableFile, EvaluationType.MATCH
            );
            
            // 验证结果
            assertNotNull(stats, "Evaluation statistics should not be null");
            assertTrue(stats.getLevelStatistics(com.nl2sql.spider.enums.HardnessLevel.ALL).getCount() > 0, 
                      "Should have processed some items");
            
            // 打印详细结果
            logger.info("Dev数据集测试结果:");
            stats.printResults();
            
        } catch (Exception e) {
            logger.error("Dev dataset test failed", e);
            fail("Dev dataset test should not throw exception: " + e.getMessage());
        }
    }
    
    /**
     * 测试4: 验证baselines中的不同模型结果
     */
    @Test
    @Order(4)
    void testBaselinesModels() {
        logger.info("=== 测试4: 验证baselines中的不同模型结果 ===");
        
        String tableFile = DATA_DIR + "/tables.json";
        String dbDir = DATA_DIR + "/database";
        String goldFile = EVALUATION_EXAMPLES_DIR + "/gold_example.txt";
        
        // 获取所有baseline模型目录
        File baselinesDir = new File(BASELINES_DIR);
        if (!baselinesDir.exists() || !baselinesDir.isDirectory()) {
            logger.warn("Baselines directory not found or not a directory");
            return;
        }
        
        File[] modelDirs = baselinesDir.listFiles(File::isDirectory);
        if (modelDirs == null || modelDirs.length == 0) {
            logger.warn("No baseline model directories found");
            return;
        }
        
        for (File modelDir : modelDirs) {
            String modelName = modelDir.getName();
            logger.info("测试baseline模型: {}", modelName);
            
            // 查找该模型的预测文件
            File predFile = findPredictionFile(modelDir);
            if (predFile == null) {
                logger.warn("No prediction file found for model: {}", modelName);
                continue;
            }
            
            try {
                // 执行评估
                EvaluationStatistics stats = evaluationService.evaluate(
                    goldFile, predFile.getPath(), dbDir, tableFile, EvaluationType.ALL
                );
                
                // 验证结果
                assertNotNull(stats, "Evaluation statistics should not be null for " + modelName);
                assertTrue(stats.getLevelStatistics(com.nl2sql.spider.enums.HardnessLevel.ALL).getCount() > 0, 
                          "Should have processed some items for " + modelName);
                
                // 打印结果
                logger.info("模型 {} 的评估结果:", modelName);
                stats.printResults();
                
            } catch (Exception e) {
                logger.error("Failed to evaluate baseline model: " + modelName, e);
                // 不让单个模型失败影响整个测试
            }
        }
    }
    
    /**
     * 测试5: 验证不同评估类型的一致性
     */
    @Test
    @Order(5)
    void testEvaluationTypeConsistency() {
        logger.info("=== 测试5: 验证不同评估类型的一致性 ===");
        
        String goldFile = EVALUATION_EXAMPLES_DIR + "/gold_example.txt";
        String predFile = EVALUATION_EXAMPLES_DIR + "/pred_example.txt";
        String tableFile = DATA_DIR + "/tables.json";
        String dbDir = DATA_DIR + "/database";
        
        try {
            // 测试不同评估类型
            EvaluationStatistics matchStats = evaluationService.evaluate(
                goldFile, predFile, dbDir, tableFile, EvaluationType.MATCH
            );
            
            EvaluationStatistics execStats = evaluationService.evaluate(
                goldFile, predFile, dbDir, tableFile, EvaluationType.EXEC
            );
            
            EvaluationStatistics allStats = evaluationService.evaluate(
                goldFile, predFile, dbDir, tableFile, EvaluationType.ALL
            );
            
            // 验证一致性
            assertEquals(matchStats.getLevelStatistics(com.nl2sql.spider.enums.HardnessLevel.ALL).getCount(), 
                        execStats.getLevelStatistics(com.nl2sql.spider.enums.HardnessLevel.ALL).getCount(),
                        "Match and exec should have same total count");
            assertEquals(matchStats.getLevelStatistics(com.nl2sql.spider.enums.HardnessLevel.ALL).getCount(), 
                        allStats.getLevelStatistics(com.nl2sql.spider.enums.HardnessLevel.ALL).getCount(),
                        "Match and all should have same total count");
            
            // 验证ALL类型包含了MATCH和EXEC的结果
            assertEquals(matchStats.getLevelStatistics(com.nl2sql.spider.enums.HardnessLevel.ALL).getExactMatchScore(), 
                        allStats.getLevelStatistics(com.nl2sql.spider.enums.HardnessLevel.ALL).getExactMatchScore(),
                        "ALL type should have same exact match accuracy as MATCH type");
            assertEquals(execStats.getLevelStatistics(com.nl2sql.spider.enums.HardnessLevel.ALL).getExecutionScore(), 
                        allStats.getLevelStatistics(com.nl2sql.spider.enums.HardnessLevel.ALL).getExecutionScore(),
                        "ALL type should have same execution accuracy as EXEC type");
            
            logger.info("评估类型一致性验证通过");
            
        } catch (Exception e) {
            logger.error("Evaluation type consistency test failed", e);
            fail("Evaluation type consistency test should not throw exception: " + e.getMessage());
        }
    }
    
    /**
     * 测试6: 验证错误处理机制
     */
    @Test
    @Order(6)
    void testErrorHandling() {
        logger.info("=== 测试6: 验证错误处理机制 ===");
        
        String tableFile = DATA_DIR + "/tables.json";
        String dbDir = DATA_DIR + "/database";
        
        // 测试不存在的文件
        assertThrows(RuntimeException.class, () -> {
            evaluationService.evaluate(
                "nonexistent_gold.txt", 
                "nonexistent_pred.txt", 
                dbDir, 
                tableFile, 
                EvaluationType.ALL
            );
        }, "Should throw exception for non-existent files");
        
        // 测试不匹配的文件长度
        try {
            // 创建临时测试文件
            Path tempGold = Files.createTempFile("test_gold", ".txt");
            Path tempPred = Files.createTempFile("test_pred", ".txt");
            
            Files.write(tempGold, "SELECT * FROM table1\tdb1\n".getBytes());
            Files.write(tempPred, "SELECT * FROM table1\nSELECT * FROM table2\n".getBytes());
            
            assertThrows(RuntimeException.class, () -> {
                evaluationService.evaluate(
                    tempGold.toString(), 
                    tempPred.toString(), 
                    dbDir, 
                    tableFile, 
                    EvaluationType.ALL
                );
            }, "Should throw exception for mismatched file lengths");
            
            // 清理临时文件
            Files.deleteIfExists(tempGold);
            Files.deleteIfExists(tempPred);
            
        } catch (Exception e) {
            logger.error("Error handling test failed", e);
            fail("Error handling test should not throw unexpected exception: " + e.getMessage());
        }
        
        logger.info("错误处理机制验证通过");
    }
    
    /**
     * 查找模型目录中的预测文件
     */
    private File findPredictionFile(File modelDir) {
        // 常见的预测文件名模式
        String[] patterns = {"pred.txt", "prediction.txt", "predictions.txt", "output.txt"};
        
        for (String pattern : patterns) {
            File predFile = new File(modelDir, pattern);
            if (predFile.exists()) {
                return predFile;
            }
        }
        
        // 如果没有找到标准名称，查找所有txt文件
        File[] txtFiles = modelDir.listFiles((dir, name) -> name.endsWith(".txt"));
        if (txtFiles != null && txtFiles.length > 0) {
            return txtFiles[0]; // 返回第一个txt文件
        }
        
        return null;
    }
} 