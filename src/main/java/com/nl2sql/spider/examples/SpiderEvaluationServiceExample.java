package com.nl2sql.spider.examples;

import com.nl2sql.spider.config.DatabaseConfig;
import com.nl2sql.spider.enums.EvaluationType;
import com.nl2sql.spider.model.EvaluationResult;
import com.nl2sql.spider.service.SpiderEvaluationService;
import com.nl2sql.spider.utils.DatabaseConnectionManager;

import java.sql.Connection;
import java.util.Arrays;
import java.util.List;

/**
 * SpiderEvaluationService使用示例
 * 展示如何使用扩展的数据库功能进行SQL评估
 */
public class SpiderEvaluationServiceExample {
    
    public static void main(String[] args) {
        SpiderEvaluationService service = new SpiderEvaluationService();
        
        // 示例1: 创建数据库配置
        System.out.println("=== 示例1: 创建数据库配置 ===");
        demonstrateConfigCreation(service);
        
        // 示例2: 测试数据库连接
        System.out.println("\n=== 示例2: 测试数据库连接 ===");
        demonstrateConnectionTesting(service);
        
        // 示例3: SQL验证
        System.out.println("\n=== 示例3: SQL验证 ===");
        demonstrateSqlValidation(service);
        
        // 示例4: 使用DatabaseConfig进行评估
        System.out.println("\n=== 示例4: 使用DatabaseConfig进行评估 ===");
        demonstrateEvaluationWithConfig(service);
        
        // 示例5: 批量评估
        System.out.println("\n=== 示例5: 批量评估 ===");
        demonstrateBatchEvaluation(service);
        
        // 示例6: 使用现有连接进行评估
        System.out.println("\n=== 示例6: 使用现有连接进行评估 ===");
        demonstrateEvaluationWithConnection(service);
    }
    
    /**
     * 演示数据库配置创建
     */
    private static void demonstrateConfigCreation(SpiderEvaluationService service) {
        try {
            // 创建MySQL配置
            DatabaseConfig mysqlConfig = service.createDatabaseConfig(
                "mysql", "localhost", 3306, "spider_db", "root", "password"
            );
            System.out.println("MySQL配置创建成功:");
            System.out.println("  JDBC URL: " + mysqlConfig.buildJdbcUrl());
            
            // 创建PostgreSQL配置
            DatabaseConfig pgConfig = service.createDatabaseConfig(
                "postgresql", "localhost", 5432, "spider_db", "postgres", "password"
            );
            System.out.println("PostgreSQL配置创建成功:");
            System.out.println("  JDBC URL: " + pgConfig.buildJdbcUrl());
            
            // 创建SQLite配置
            DatabaseConfig sqliteConfig = new DatabaseConfig("data/spider/database/academic/academic.sqlite");
            System.out.println("SQLite配置创建成功:");
            System.out.println("  文件路径: " + sqliteConfig.getFilePath());
            
            // 添加额外属性
            mysqlConfig.addProperty("useSSL", "false");
            mysqlConfig.addProperty("serverTimezone", "UTC");
            System.out.println("MySQL配置添加属性后:");
            System.out.println("  JDBC URL: " + mysqlConfig.buildJdbcUrl());
            
        } catch (Exception e) {
            System.err.println("配置创建失败: " + e.getMessage());
        }
    }
    
    /**
     * 演示数据库连接测试
     */
    private static void demonstrateConnectionTesting(SpiderEvaluationService service) {
        // 测试H2内存数据库（应该成功）
        DatabaseConfig h2Config = new DatabaseConfig();
        h2Config.setType(DatabaseConfig.DatabaseType.H2);
        h2Config.setDatabase("test_spider_db");
        
        boolean h2Result = service.testDatabaseConnection(h2Config);
        System.out.println("H2内存数据库连接测试: " + (h2Result ? "成功" : "失败"));
        
        // 测试SQLite文件（可能成功或失败，取决于文件是否存在）
        DatabaseConfig sqliteConfig = new DatabaseConfig("data/spider/database/academic/academic.sqlite");
        boolean sqliteResult = service.testDatabaseConnection(sqliteConfig);
        System.out.println("SQLite文件连接测试: " + (sqliteResult ? "成功" : "失败"));
        
        // 测试无效的MySQL连接（应该失败）
        DatabaseConfig invalidConfig = service.createDatabaseConfig(
            "mysql", "invalid_host", 9999, "nonexistent", "user", "pass"
        );
        boolean invalidResult = service.testDatabaseConnection(invalidConfig);
        System.out.println("无效MySQL连接测试: " + (invalidResult ? "成功" : "失败"));
    }
    
    /**
     * 演示SQL验证
     */
    private static void demonstrateSqlValidation(SpiderEvaluationService service) {
        DatabaseConfig config = new DatabaseConfig();
        config.setType(DatabaseConfig.DatabaseType.H2);
        config.setDatabase("validation_test_db");
        
        String[] testSqls = {
            "SELECT 1",
            "SELECT COUNT(*) FROM users",
            "INVALID SQL SYNTAX",
            "",
            null
        };
        
        for (String sql : testSqls) {
            try {
                boolean isValid = service.validateSql(sql, "test_db", config);
                System.out.println("SQL验证 [" + (sql != null ? sql : "null") + "]: " + 
                                 (isValid ? "有效" : "无效"));
            } catch (Exception e) {
                System.out.println("SQL验证 [" + (sql != null ? sql : "null") + "]: 异常 - " + 
                                 e.getMessage());
            }
        }
    }
    
    /**
     * 演示使用DatabaseConfig进行评估
     */
    private static void demonstrateEvaluationWithConfig(SpiderEvaluationService service) {
        DatabaseConfig config = new DatabaseConfig();
        config.setType(DatabaseConfig.DatabaseType.H2);
        config.setDatabase("evaluation_test_db");
        
        String goldSql = "SELECT COUNT(*) FROM students WHERE age > 18";
        String predSql = "SELECT count(*) FROM students WHERE age > 18";
        
        try {
            // 注意：这里会抛出异常，因为没有加载数据库模式
            EvaluationResult result = service.evaluateSingle(
                goldSql, predSql, "test_db", config, EvaluationType.MATCH
            );
            
            System.out.println("评估结果:");
            System.out.println("  精确匹配: " + result.isExactMatch());
            System.out.println("  执行匹配: " + result.isExecutionMatch());
            
        } catch (Exception e) {
            System.out.println("评估失败（预期的，因为没有数据库模式）: " + e.getMessage());
        }
    }
    
    /**
     * 演示批量评估
     */
    private static void demonstrateBatchEvaluation(SpiderEvaluationService service) {
        DatabaseConfig config = new DatabaseConfig();
        config.setType(DatabaseConfig.DatabaseType.H2);
        config.setDatabase("batch_test_db");
        
        List<String> goldSqls = Arrays.asList(
            "SELECT COUNT(*) FROM users",
            "SELECT name FROM users WHERE age > 18",
            "SELECT AVG(salary) FROM employees"
        );
        
        List<String> predSqls = Arrays.asList(
            "SELECT count(*) FROM users",
            "SELECT name FROM users WHERE age > 18",
            "SELECT AVG(salary) FROM employees"
        );
        
        try {
            List<EvaluationResult> results = service.evaluateBatch(
                goldSqls, predSqls, "test_db", config, EvaluationType.MATCH
            );
            
            System.out.println("批量评估结果:");
            for (int i = 0; i < results.size(); i++) {
                EvaluationResult result = results.get(i);
                System.out.println("  SQL " + (i + 1) + " - 精确匹配: " + result.isExactMatch());
            }
            
        } catch (Exception e) {
            System.out.println("批量评估失败（预期的，因为没有数据库模式）: " + e.getMessage());
        }
        
        // 测试不同大小的列表
        List<String> shortPredSqls = Arrays.asList("SELECT 1");
        try {
            service.evaluateBatch(goldSqls, shortPredSqls, "test_db", config, EvaluationType.MATCH);
        } catch (IllegalArgumentException e) {
            System.out.println("不同大小列表的错误处理: " + e.getMessage());
        }
    }
    
    /**
     * 演示使用现有连接进行评估
     */
    private static void demonstrateEvaluationWithConnection(SpiderEvaluationService service) {
        DatabaseConfig config = new DatabaseConfig();
        config.setType(DatabaseConfig.DatabaseType.H2);
        config.setDatabase("connection_test_db");
        
        try (Connection conn = DatabaseConnectionManager.createConnection(config)) {
            System.out.println("数据库连接创建成功");
            
            // 创建测试表和数据
            conn.createStatement().execute("CREATE TABLE test_users (id INT, name VARCHAR(50), age INT)");
            conn.createStatement().execute("INSERT INTO test_users VALUES (1, 'Alice', 25)");
            conn.createStatement().execute("INSERT INTO test_users VALUES (2, 'Bob', 17)");
            conn.createStatement().execute("INSERT INTO test_users VALUES (3, 'Charlie', 30)");
            
            System.out.println("测试表创建成功，插入了3条记录");
            
            String goldSql = "SELECT COUNT(*) FROM test_users WHERE age > 18";
            String predSql = "SELECT count(*) FROM test_users WHERE age > 18";
            
            // 测试SQL验证
            boolean isValid = service.validateSql(goldSql, "test_db", conn);
            System.out.println("SQL验证结果: " + (isValid ? "有效" : "无效"));
            
            try {
                // 尝试评估（会因为没有schema而失败）
                EvaluationResult result = service.evaluateSingle(
                    goldSql, predSql, "test_db", conn, EvaluationType.EXEC
                );
                
                System.out.println("评估结果:");
                System.out.println("  精确匹配: " + result.isExactMatch());
                System.out.println("  执行匹配: " + result.isExecutionMatch());
                
            } catch (Exception e) {
                System.out.println("评估失败（预期的，因为没有数据库模式）: " + e.getMessage());
            }
            
        } catch (Exception e) {
            System.err.println("连接操作失败: " + e.getMessage());
        }
    }
} 