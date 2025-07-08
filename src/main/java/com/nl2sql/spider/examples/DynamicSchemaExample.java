package com.nl2sql.spider.examples;

import com.nl2sql.spider.config.DatabaseConfig;
import com.nl2sql.spider.enums.EvaluationType;
import com.nl2sql.spider.model.EvaluationResult;
import com.nl2sql.spider.service.SpiderEvaluationService;
import com.nl2sql.spider.utils.DatabaseConnectionManager;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

/**
 * 动态Schema功能使用示例
 * 演示如何在不需要tableFile的情况下进行SQL评估
 */
public class DynamicSchemaExample {
    
    public static void main(String[] args) {
        SpiderEvaluationService service = new SpiderEvaluationService();
        
        // 示例1: 创建测试数据库并进行动态schema评估
        System.out.println("=== 示例1: 动态Schema评估 ===");
        demonstrateDynamicSchemaEvaluation(service);
        
        // 示例2: 批量动态schema评估
        System.out.println("\n=== 示例2: 批量动态Schema评估 ===");
        demonstrateBatchDynamicEvaluation(service);
        
        // 示例3: 动态schema SQL验证
        System.out.println("\n=== 示例3: 动态Schema SQL验证 ===");
        demonstrateDynamicSchemaValidation(service);
        
        // 示例4: 不同数据库类型的动态schema
        System.out.println("\n=== 示例4: 不同数据库类型的动态Schema ===");
        demonstrateDifferentDatabaseTypes(service);
    }
    
    /**
     * 演示动态schema评估
     */
    private static void demonstrateDynamicSchemaEvaluation(SpiderEvaluationService service) {
        // 创建H2内存数据库
        DatabaseConfig config = createTestDatabase();
        
        if (config == null) {
            System.out.println("测试数据库创建失败，跳过示例");
            return;
        }
        
        // 进行动态schema评估
        String goldSql = "SELECT COUNT(*) FROM employees WHERE salary > 50000";
        String predSql = "SELECT count(*) FROM employees WHERE salary > 50000";
        
        try {
            EvaluationResult result = service.evaluateSingleWithDynamicSchema(
                goldSql, predSql, "company_db", config, EvaluationType.MATCH
            );
            
            System.out.println("评估结果:");
            System.out.println("  Gold SQL: " + goldSql);
            System.out.println("  Pred SQL: " + predSql);
            System.out.println("  精确匹配: " + result.isExactMatch());
            System.out.println("  执行匹配: " + result.isExecutionMatch());
            System.out.println("  部分分数: " + result.getPartialScores());
            
        } catch (Exception e) {
            System.out.println("动态schema评估失败: " + e.getMessage());
        }
    }
    
    /**
     * 演示批量动态schema评估
     */
    private static void demonstrateBatchDynamicEvaluation(SpiderEvaluationService service) {
        DatabaseConfig config = createTestDatabase();
        
        if (config == null) {
            return;
        }
        
        List<String> goldSqls = Arrays.asList(
            "SELECT name FROM employees WHERE department = 'IT'",
            "SELECT AVG(salary) FROM employees",
            "SELECT COUNT(*) FROM departments",
            "SELECT d.name, COUNT(e.id) FROM departments d LEFT JOIN employees e ON d.id = e.department_id GROUP BY d.id, d.name"
        );
        
        List<String> predSqls = Arrays.asList(
            "SELECT name FROM employees WHERE department = 'IT'",
            "SELECT avg(salary) FROM employees",
            "SELECT count(*) FROM departments",
            "SELECT d.name, COUNT(e.id) FROM departments d LEFT JOIN employees e ON d.id = e.department_id GROUP BY d.name"
        );
        
        try {
            List<EvaluationResult> results = service.evaluateBatchWithDynamicSchema(
                goldSqls, predSqls, "company_db", config, EvaluationType.MATCH
            );
            
            System.out.println("批量评估结果:");
            for (int i = 0; i < results.size(); i++) {
                EvaluationResult result = results.get(i);
                System.out.println("  SQL " + (i + 1) + ":");
                System.out.println("    精确匹配: " + result.isExactMatch());
                System.out.println("    执行匹配: " + result.isExecutionMatch());
            }
            
        } catch (Exception e) {
            System.out.println("批量动态schema评估失败: " + e.getMessage());
        }
    }
    
    /**
     * 演示动态schema SQL验证
     */
    private static void demonstrateDynamicSchemaValidation(SpiderEvaluationService service) {
        DatabaseConfig config = createTestDatabase();
        
        if (config == null) {
            return;
        }
        
        String[] testSqls = {
            "SELECT * FROM employees",
            "SELECT name, salary FROM employees WHERE salary > 40000",
            "SELECT d.name, COUNT(e.id) FROM departments d JOIN employees e ON d.id = e.department_id GROUP BY d.id",
            "INVALID SQL SYNTAX",
            "SELECT * FROM non_existent_table",
            "SELECT name FROM employees WHERE invalid_column = 'test'"
        };
        
        System.out.println("SQL验证结果:");
        for (String sql : testSqls) {
            try {
                boolean isValid = service.validateSqlWithDynamicSchema(sql, "company_db", config);
                System.out.println("  [" + (isValid ? "✓" : "✗") + "] " + sql);
            } catch (Exception e) {
                System.out.println("  [✗] " + sql + " (验证失败: " + e.getMessage() + ")");
            }
        }
    }
    
    /**
     * 演示不同数据库类型的动态schema
     */
    private static void demonstrateDifferentDatabaseTypes(SpiderEvaluationService service) {
        // 演示SQLite
        System.out.println("SQLite动态Schema:");
        demonstrateSQLiteSchema(service);
        
        // 演示H2
        System.out.println("\nH2动态Schema:");
        demonstrateH2Schema(service);
    }
    
    /**
     * 演示SQLite动态schema
     */
    private static void demonstrateSQLiteSchema(SpiderEvaluationService service) {
        DatabaseConfig sqliteConfig = new DatabaseConfig();
        sqliteConfig.setType(DatabaseConfig.DatabaseType.SQLITE);
        sqliteConfig.setDatabase("data/spider/database/academic/academic.sqlite");
        
        try {
            boolean isValid = service.validateSqlWithDynamicSchema(
                "SELECT COUNT(*) FROM author", "academic", sqliteConfig
            );
            System.out.println("  SQLite schema验证: " + (isValid ? "成功" : "失败"));
        } catch (Exception e) {
            System.out.println("  SQLite schema验证失败: " + e.getMessage());
        }
    }
    
    /**
     * 演示H2动态schema
     */
    private static void demonstrateH2Schema(SpiderEvaluationService service) {
        DatabaseConfig h2Config = new DatabaseConfig();
        h2Config.setType(DatabaseConfig.DatabaseType.H2);
        h2Config.setDatabase("mem:demo_h2;DB_CLOSE_DELAY=-1");
        h2Config.setUsername("sa");
        h2Config.setPassword("");
        
        // 创建简单的测试表
        try (Connection conn = DatabaseConnectionManager.createConnection(h2Config)) {
            Statement stmt = conn.createStatement();
            stmt.execute("CREATE TABLE test_table (id INT PRIMARY KEY, name VARCHAR(50))");
            stmt.execute("INSERT INTO test_table VALUES (1, 'Test')");
            
            boolean isValid = service.validateSqlWithDynamicSchema(
                "SELECT * FROM test_table", "test_db", h2Config
            );
            System.out.println("  H2 schema验证: " + (isValid ? "成功" : "失败"));
            
        } catch (Exception e) {
            System.out.println("  H2 schema验证失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建测试数据库
     */
    private static DatabaseConfig createTestDatabase() {
        DatabaseConfig config = new DatabaseConfig();
        config.setType(DatabaseConfig.DatabaseType.H2);
        config.setDatabase("mem:company_example;DB_CLOSE_DELAY=-1");
        config.setUsername("sa");
        config.setPassword("");
        
        try (Connection conn = DatabaseConnectionManager.createConnection(config)) {
            Statement stmt = conn.createStatement();
            
            // 创建测试表
            stmt.execute("CREATE TABLE departments (id INT PRIMARY KEY, name VARCHAR(50))");
            stmt.execute("CREATE TABLE employees (id INT PRIMARY KEY, name VARCHAR(50), salary DECIMAL(10,2), department_id INT, department VARCHAR(50))");
            
            // 插入测试数据
            stmt.execute("INSERT INTO departments VALUES (1, 'IT'), (2, 'HR'), (3, 'Finance')");
            stmt.execute("INSERT INTO employees VALUES " +
                        "(1, 'Alice', 60000, 1, 'IT'), " +
                        "(2, 'Bob', 45000, 2, 'HR'), " +
                        "(3, 'Charlie', 70000, 1, 'IT'), " +
                        "(4, 'Diana', 55000, 3, 'Finance')");
            
            System.out.println("测试数据库创建成功");
            return config;
            
        } catch (Exception e) {
            System.out.println("测试数据库创建失败: " + e.getMessage());
            return null;
        }
    }
} 