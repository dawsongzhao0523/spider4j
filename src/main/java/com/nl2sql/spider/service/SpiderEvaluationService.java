package com.nl2sql.spider.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nl2sql.spider.enums.EvaluationType;
import com.nl2sql.spider.enums.HardnessLevel;
import com.nl2sql.spider.evaluator.SpiderEvaluator;
import com.nl2sql.spider.model.*;
import com.nl2sql.spider.parser.SqlParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Spider评估服务类
 * 提供高级API进行NL2SQL评估
 */
public class SpiderEvaluationService {
    
    private static final Logger logger = LoggerFactory.getLogger(SpiderEvaluationService.class);
    
    private final ObjectMapper objectMapper;
    private final SqlParser sqlParser;
    private final SpiderEvaluator evaluator;
    private final Map<String, DatabaseSchema> schemaCache;
    
    public SpiderEvaluationService() {
        this.objectMapper = new ObjectMapper();
        this.sqlParser = new SqlParser();
        this.evaluator = new SpiderEvaluator();
        this.schemaCache = new HashMap<>();
    }
    
    /**
     * 评估预测结果
     * 
     * @param goldFile 标准答案文件路径
     * @param predFile 预测结果文件路径
     * @param dbDir 数据库目录路径
     * @param tableFile 表结构文件路径
     * @param evaluationType 评估类型
     * @return 评估结果统计
     */
    public EvaluationStatistics evaluate(String goldFile, String predFile, String dbDir, 
                                       String tableFile, EvaluationType evaluationType) {
        try {
            // 加载数据
            List<GoldItem> goldItems = loadGoldFile(goldFile);
            List<String> predictions = loadPredictionFile(predFile);
            loadSchemas(tableFile);
            
            if (goldItems.size() != predictions.size()) {
                throw new IllegalArgumentException("Gold and prediction files have different number of items");
            }
            
            // 初始化统计
            EvaluationStatistics statistics = new EvaluationStatistics();
            
            // 逐一评估
            for (int i = 0; i < goldItems.size(); i++) {
                GoldItem goldItem = goldItems.get(i);
                String prediction = predictions.get(i);
                
                try {
                    EvaluationResult result = evaluateSingle(
                        goldItem.getSql(), 
                        prediction, 
                        goldItem.getDbId(), 
                        dbDir, 
                        evaluationType
                    );
                    
                    // 更新统计
                    updateStatistics(statistics, result, goldItem.getDbId(), goldItem.getSql());
                    
                } catch (Exception e) {
                    logger.error("Failed to evaluate item {}: gold={}, pred={}", 
                               i, goldItem.getSql(), prediction, e);
                    statistics.incrementErrorCount();
                }
            }
            
            // 计算最终分数
            statistics.calculateFinalScores();
            
            return statistics;
            
        } catch (Exception e) {
            logger.error("Evaluation failed", e);
            throw new RuntimeException("Evaluation failed", e);
        }
    }
    
    /**
     * 评估单个SQL对
     */
    public EvaluationResult evaluateSingle(String goldSql, String predSql, String dbId, 
                                         String dbDir, EvaluationType evaluationType) {
        DatabaseSchema schema = schemaCache.get(dbId);
        if (schema == null) {
            throw new IllegalArgumentException("Schema not found for database: " + dbId);
        }
        
        // 解析SQL
        SqlStructure goldStructure = sqlParser.parseSql(schema, goldSql);
        SqlStructure predStructure;
        
        try {
            predStructure = sqlParser.parseSql(schema, predSql);
        } catch (Exception e) {
            // 如果解析失败，使用空的SQL结构
            predStructure = createEmptySqlStructure();
        }
        
        // 评估
        EvaluationResult result = evaluator.evaluatePartialMatch(predStructure, goldStructure);
        
        // 根据评估类型添加额外评估
        if (evaluationType == EvaluationType.EXEC || evaluationType == EvaluationType.ALL) {
            String dbPath = Paths.get(dbDir, dbId, dbId + ".sqlite").toString();
            boolean execMatch = evaluator.evaluateExecution(dbPath, predSql, goldSql);
            result = new EvaluationResult(result.getPartialScores(), result.isExactMatch(), execMatch);
        }
        
        return result;
    }
    
    /**
     * 加载标准答案文件
     */
    private List<GoldItem> loadGoldFile(String goldFile) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(goldFile));
        List<GoldItem> goldItems = new ArrayList<>();
        
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            
            String[] parts = line.split("\t");
            if (parts.length >= 2) {
                goldItems.add(new GoldItem(parts[0].trim(), parts[1].trim()));
            }
        }
        
        return goldItems;
    }
    
    /**
     * 加载预测结果文件
     */
    private List<String> loadPredictionFile(String predFile) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(predFile));
        List<String> predictions = new ArrayList<>();
        
        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                predictions.add(line.trim());
            }
        }
        
        return predictions;
    }
    
    /**
     * 加载表结构文件
     */
    private void loadSchemas(String tableFile) throws IOException {
        List<DatabaseSchema> schemas = objectMapper.readValue(
            new File(tableFile), 
            new TypeReference<List<DatabaseSchema>>() {}
        );
        
        for (DatabaseSchema schema : schemas) {
            schemaCache.put(schema.getDbId(), schema);
        }
        
        logger.info("Loaded {} database schemas", schemas.size());
    }
    
    /**
     * 创建空的SQL结构
     */
    private SqlStructure createEmptySqlStructure() {
        SqlStructure sql = new SqlStructure();
        sql.setSelect(new SelectClause(false, new ArrayList<>()));
        sql.setFrom(new FromClause(new ArrayList<>(), new ArrayList<>()));
        sql.setWhere(new ArrayList<>());
        sql.setGroupBy(new ArrayList<>());
        sql.setHaving(new ArrayList<>());
        return sql;
    }
    
    /**
     * 更新统计信息
     */
    private void updateStatistics(EvaluationStatistics statistics, EvaluationResult result, String dbId, String goldSql) {
        DatabaseSchema schema = schemaCache.get(dbId);
        if (schema == null) return;
        
        // 计算难度级别
        HardnessLevel hardness = calculateHardness(goldSql, schema);
        
        statistics.addResult(hardness, result);
    }
    
    /**
     * 计算SQL难度级别
     */
    private HardnessLevel calculateHardness(String goldSql, DatabaseSchema schema) {
        try {
            SqlStructure sqlStructure = sqlParser.parseSql(schema, goldSql);
            return evaluator.evaluateHardness(sqlStructure);
        } catch (Exception e) {
            logger.error("Failed to calculate hardness for SQL: {}", goldSql, e);
            return HardnessLevel.EASY; // 默认为简单级别
        }
    }
    
    /**
     * 标准答案项
     */
    public static class GoldItem {
        private final String sql;
        private final String dbId;
        
        public GoldItem(String sql, String dbId) {
            this.sql = sql;
            this.dbId = dbId;
        }
        
        public String getSql() { return sql; }
        public String getDbId() { return dbId; }
    }
} 