package com.nl2sql.spider.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nl2sql.spider.config.DatabaseConfig;
import com.nl2sql.spider.enums.EvaluationType;
import com.nl2sql.spider.enums.HardnessLevel;
import com.nl2sql.spider.evaluator.SpiderEvaluator;
import com.nl2sql.spider.model.*;
import com.nl2sql.spider.parser.SqlParser;
import com.nl2sql.spider.utils.DatabaseSchemaExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
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
     * 评估预测结果（使用DatabaseConfig）
     * 
     * @param goldFile 标准答案文件路径
     * @param predFile 预测结果文件路径
     * @param tableFile 表结构文件路径
     * @param dbConfig 数据库配置
     * @param evaluationType 评估类型
     * @return 评估结果统计
     */
    public EvaluationStatistics evaluate(String goldFile, String predFile, 
                                       String tableFile, DatabaseConfig dbConfig, EvaluationType evaluationType) {
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
                        dbConfig,
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
     * 评估预测结果（使用DatabaseConfig，无需tableFile）
     * 
     * @param goldFile 标准答案文件路径
     * @param predFile 预测结果文件路径
     * @param dbConfig 数据库配置
     * @param evaluationType 评估类型
     * @return 评估结果统计
     */
    public EvaluationStatistics evaluateWithDynamicSchema(String goldFile, String predFile, 
                                                         DatabaseConfig dbConfig, EvaluationType evaluationType) {
        try {
            // 加载数据
            List<GoldItem> goldItems = loadGoldFile(goldFile);
            List<String> predictions = loadPredictionFile(predFile);
            
            if (goldItems.size() != predictions.size()) {
                throw new IllegalArgumentException("Gold and prediction files have different number of items");
            }
            
            // 动态加载schemas
            loadSchemasFromDatabase(goldItems, dbConfig);
            
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
                        dbConfig,
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
     * 评估单个SQL对（使用DatabaseConfig）
     */
    public EvaluationResult evaluateSingle(String goldSql, String predSql, String dbId, 
                                         DatabaseConfig dbConfig, EvaluationType evaluationType) {
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
            // 使用DatabaseConfig进行执行评估
            boolean execMatch = evaluator.evaluateExecution(dbConfig, predSql, goldSql);
            result = new EvaluationResult(result.getPartialScores(), result.isExactMatch(), execMatch);
        }
        
        return result;
    }
    
    /**
     * 评估单个SQL对（使用DatabaseConfig，动态获取schema）
     */
    public EvaluationResult evaluateSingleWithDynamicSchema(String goldSql, String predSql, String dbId, 
                                                           DatabaseConfig dbConfig, EvaluationType evaluationType) {
        try {
            // 动态获取schema
            DatabaseSchema schema = DatabaseSchemaExtractor.extractSchema(dbConfig, dbId);
            schemaCache.put(dbId, schema);
            
            return evaluateSingle(goldSql, predSql, dbId, dbConfig, evaluationType);
            
        } catch (SQLException e) {
            logger.error("Failed to extract schema for database {}", dbId, e);
            throw new RuntimeException("Failed to extract schema for database " + dbId, e);
        }
    }
    
    /**
     * 评估单个SQL对（使用现有连接）
     */
    public EvaluationResult evaluateSingle(String goldSql, String predSql, String dbId, 
                                          Connection conn, EvaluationType evaluationType) {
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
            boolean execMatch = evaluator.evaluateExecution(conn, predSql, goldSql);
            result = new EvaluationResult(result.getPartialScores(), result.isExactMatch(), execMatch);
        }
        
        return result;
    }
    
    /**
     * 批量评估SQL对（使用DatabaseConfig）
     */
    public List<EvaluationResult> evaluateBatch(List<String> goldSqls, List<String> predSqls, 
                                               String dbId, DatabaseConfig dbConfig, 
                                               EvaluationType evaluationType) {
        if (goldSqls.size() != predSqls.size()) {
            throw new IllegalArgumentException("Gold and prediction lists have different sizes");
        }
        
        List<EvaluationResult> results = new ArrayList<>();
        
        for (int i = 0; i < goldSqls.size(); i++) {
            try {
                                 EvaluationResult result = evaluateSingle(
                     goldSqls.get(i), 
                     predSqls.get(i), 
                     dbId, 
                     dbConfig, 
                     evaluationType
                 );
                results.add(result);
            } catch (Exception e) {
                logger.error("Failed to evaluate SQL pair {}: gold={}, pred={}", 
                           i, goldSqls.get(i), predSqls.get(i), e);
                // 添加空结果表示失败
                results.add(createFailureResult());
            }
        }
        
        return results;
    }
    
    /**
     * 批量评估SQL（使用DatabaseConfig，动态获取schema）
     */
    public List<EvaluationResult> evaluateBatchWithDynamicSchema(List<String> goldSqls, List<String> predSqls, 
                                                               String dbId, DatabaseConfig dbConfig, 
                                                               EvaluationType evaluationType) {
        if (goldSqls.size() != predSqls.size()) {
            throw new IllegalArgumentException("Gold and prediction lists have different sizes");
        }
        
        try {
            // 动态获取schema
            DatabaseSchema schema = DatabaseSchemaExtractor.extractSchema(dbConfig, dbId);
            schemaCache.put(dbId, schema);
            
            return evaluateBatch(goldSqls, predSqls, dbId, dbConfig, evaluationType);
            
        } catch (SQLException e) {
            logger.error("Failed to extract schema for database {}", dbId, e);
            throw new RuntimeException("Failed to extract schema for database " + dbId, e);
        }
    }
    
    /**
     * 验证SQL语法（使用DatabaseConfig）
     */
    public boolean validateSql(String sql, String dbId, DatabaseConfig dbConfig) {
        try {
            DatabaseSchema schema = schemaCache.get(dbId);
            if (schema == null) {
                logger.warn("Schema not found for database: {}", dbId);
                return false;
            }
            
            // 首先尝试解析SQL结构
            sqlParser.parseSql(schema, sql);
            
            // 然后验证SQL是否可以执行
            return evaluator.isValidSql(dbConfig, sql);
            
        } catch (Exception e) {
            logger.debug("SQL validation failed for: {}", sql, e);
            return false;
        }
    }
    
    /**
     * 验证SQL语法（使用DatabaseConfig，动态获取schema）
     */
    public boolean validateSqlWithDynamicSchema(String sql, String dbId, DatabaseConfig dbConfig) {
        try {
            // 动态获取schema
            DatabaseSchema schema = DatabaseSchemaExtractor.extractSchema(dbConfig, dbId);
            schemaCache.put(dbId, schema);
            
            return validateSql(sql, dbId, dbConfig);
            
        } catch (SQLException e) {
            logger.error("Failed to extract schema for database {}", dbId, e);
            return false;
        }
    }
    
    /**
     * 验证SQL语法（使用现有连接）
     */
    public boolean validateSql(String sql, String dbId, Connection conn) {
        try {
            DatabaseSchema schema = schemaCache.get(dbId);
            if (schema == null) {
                logger.warn("Schema not found for database: {}", dbId);
                return false;
            }
            
            // 首先尝试解析SQL结构
            sqlParser.parseSql(schema, sql);
            
            // 然后验证SQL是否可以执行
            return evaluator.isValidSql(conn, sql);
            
        } catch (Exception e) {
            logger.debug("SQL validation failed for: {}", sql, e);
            return false;
        }
    }
    
    /**
     * 测试数据库连接
     */
    public boolean testDatabaseConnection(DatabaseConfig dbConfig) {
        try {
            return com.nl2sql.spider.utils.DatabaseConnectionManager.testConnection(dbConfig);
        } catch (Exception e) {
            logger.error("Database connection test failed", e);
            return false;
        }
    }
    
    /**
     * 获取数据库配置建议
     */
    public DatabaseConfig createDatabaseConfig(String dbType, String host, int port, 
                                              String database, String username, String password) {
        try {
            DatabaseConfig.DatabaseType type = DatabaseConfig.DatabaseType.valueOf(dbType.toUpperCase());
            return new DatabaseConfig(type, host, port, database, username, password);
        } catch (IllegalArgumentException e) {
            logger.error("Unsupported database type: {}", dbType);
            throw new IllegalArgumentException("Unsupported database type: " + dbType);
        }
    }
    
    /**
     * 创建失败结果
     */
    private EvaluationResult createFailureResult() {
        Map<String, PartialScore> emptyScores = new HashMap<>();
        return new EvaluationResult(emptyScores, false, false);
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
     * 从数据库动态加载schemas
     */
    private void loadSchemasFromDatabase(List<GoldItem> goldItems, DatabaseConfig dbConfig) throws SQLException {
        Set<String> dbIds = new HashSet<>();
        for (GoldItem item : goldItems) {
            dbIds.add(item.getDbId());
        }
        
        for (String dbId : dbIds) {
            if (!schemaCache.containsKey(dbId)) {
                try {
                    DatabaseSchema schema = DatabaseSchemaExtractor.extractSchema(dbConfig, dbId);
                    schemaCache.put(dbId, schema);
                    logger.info("成功加载数据库 {} 的schema", dbId);
                } catch (SQLException e) {
                    logger.warn("无法加载数据库 {} 的schema: {}", dbId, e.getMessage());
                    // 继续处理其他数据库，不中断整个流程
                }
            }
        }
        
        logger.info("共加载了 {} 个数据库的schema信息", schemaCache.size());
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

    /**
     * 评估SQL评估项集合（独立测试，无需文件）
     * 
     * @param evaluationItems SQL评估项集合
     * @param dbConfig 数据库配置
     * @param evaluationType 评估类型
     * @return 评估结果统计
     */
    public EvaluationStatistics evaluateItems(List<SqlEvaluationItem> evaluationItems, 
                                             DatabaseConfig dbConfig, EvaluationType evaluationType) {
        if (evaluationItems == null || evaluationItems.isEmpty()) {
            throw new IllegalArgumentException("Evaluation items cannot be null or empty");
        }
        
        try {
            // 动态加载schemas
            loadSchemasFromItems(evaluationItems, dbConfig);
            
            // 初始化统计
            EvaluationStatistics statistics = new EvaluationStatistics();
            
            // 逐一评估
            for (int i = 0; i < evaluationItems.size(); i++) {
                SqlEvaluationItem item = evaluationItems.get(i);
                
                try {
                    EvaluationResult result = evaluateSingle(
                        item.getGoldSql(), 
                        item.getPredictionSql(), 
                        item.getDbId(), 
                        dbConfig,
                        evaluationType
                    );
                    
                    // 更新统计
                    updateStatistics(statistics, result, item.getDbId(), item.getGoldSql());
                    
                } catch (Exception e) {
                    logger.error("Failed to evaluate item {}: gold={}, pred={}", 
                               i, item.getGoldSql(), item.getPredictionSql(), e);
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
     * 评估SQL评估项集合（返回详细结果）
     * 
     * @param evaluationItems SQL评估项集合
     * @param dbConfig 数据库配置
     * @param evaluationType 评估类型
     * @return 每个项目的详细评估结果
     */
    public List<EvaluationResult> evaluateItemsDetailed(List<SqlEvaluationItem> evaluationItems, 
                                                       DatabaseConfig dbConfig, EvaluationType evaluationType) {
        if (evaluationItems == null || evaluationItems.isEmpty()) {
            throw new IllegalArgumentException("Evaluation items cannot be null or empty");
        }
        
        try {
            // 动态加载schemas
            loadSchemasFromItems(evaluationItems, dbConfig);
            
            List<EvaluationResult> results = new ArrayList<>();
            
            // 逐一评估
            for (int i = 0; i < evaluationItems.size(); i++) {
                SqlEvaluationItem item = evaluationItems.get(i);
                
                try {
                    EvaluationResult result = evaluateSingle(
                        item.getGoldSql(), 
                        item.getPredictionSql(), 
                        item.getDbId(), 
                        dbConfig,
                        evaluationType
                    );
                    
                    results.add(result);
                    
                } catch (Exception e) {
                    logger.error("Failed to evaluate item {}: gold={}, pred={}", 
                               i, item.getGoldSql(), item.getPredictionSql(), e);
                    // 添加失败结果
                    results.add(createFailureResult());
                }
            }
            
            return results;
            
        } catch (Exception e) {
            logger.error("Evaluation failed", e);
            throw new RuntimeException("Evaluation failed", e);
        }
    }
    
    /**
     * 验证SQL评估项集合
     * 
     * @param evaluationItems SQL评估项集合
     * @param dbConfig 数据库配置
     * @return 每个项目的验证结果
     */
    public List<Boolean> validateItems(List<SqlEvaluationItem> evaluationItems, DatabaseConfig dbConfig) {
        if (evaluationItems == null || evaluationItems.isEmpty()) {
            throw new IllegalArgumentException("Evaluation items cannot be null or empty");
        }
        
        try {
            // 动态加载schemas
            loadSchemasFromItems(evaluationItems, dbConfig);
            
            List<Boolean> results = new ArrayList<>();
            
            // 逐一验证
            for (SqlEvaluationItem item : evaluationItems) {
                try {
                    boolean goldValid = validateSql(item.getGoldSql(), item.getDbId(), dbConfig);
                    boolean predValid = validateSql(item.getPredictionSql(), item.getDbId(), dbConfig);
                    
                    // 两个SQL都有效才认为该项有效
                    results.add(goldValid && predValid);
                    
                } catch (Exception e) {
                    logger.error("Failed to validate item: gold={}, pred={}", 
                               item.getGoldSql(), item.getPredictionSql(), e);
                    results.add(false);
                }
            }
            
            return results;
            
        } catch (Exception e) {
            logger.error("Validation failed", e);
            throw new RuntimeException("Validation failed", e);
        }
    }
    
    /**
     * 从评估项集合中加载schemas
     */
    private void loadSchemasFromItems(List<SqlEvaluationItem> evaluationItems, DatabaseConfig dbConfig) throws SQLException {
        Set<String> dbIds = new HashSet<>();
        for (SqlEvaluationItem item : evaluationItems) {
            if (item.getDbId() != null) {
                dbIds.add(item.getDbId());
            }
        }
        
        for (String dbId : dbIds) {
            if (!schemaCache.containsKey(dbId)) {
                try {
                    DatabaseSchema schema = DatabaseSchemaExtractor.extractSchema(dbConfig, dbId);
                    schemaCache.put(dbId, schema);
                    logger.info("成功加载数据库 {} 的schema", dbId);
                } catch (SQLException e) {
                    logger.warn("无法加载数据库 {} 的schema: {}", dbId, e.getMessage());
                    // 继续处理其他数据库，不中断整个流程
                }
            }
        }
        
        logger.info("共加载了 {} 个数据库的schema信息", schemaCache.size());
    }
} 