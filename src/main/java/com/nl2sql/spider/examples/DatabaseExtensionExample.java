package com.nl2sql.spider.examples;

import com.nl2sql.spider.config.DatabaseConfig;
import com.nl2sql.spider.evaluator.SpiderEvaluator;
import com.nl2sql.spider.utils.DatabaseConnectionManager;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 数据库扩展功能使用示例
 */
public class DatabaseExtensionExample {
    
    public static void main(String[] args) {
        SpiderEvaluator evaluator = new SpiderEvaluator();
        
        // 示例1: 使用SQLite数据库
        System.out.println("=== SQLite数据库示例 ===");
        sqliteExample(evaluator);
        
        // 示例2: 使用MySQL数据库
        System.out.println("\n=== MySQL数据库示例 ===");
        mysqlExample(evaluator);
        
        // 示例3: 使用PostgreSQL数据库
        System.out.println("\n=== PostgreSQL数据库示例 ===");
        postgresqlExample(evaluator);
        
        // 示例4: 使用H2内存数据库
        System.out.println("\n=== H2内存数据库示例 ===");
        h2Example(evaluator);
        
        // 示例5: 使用现有连接
        System.out.println("\n=== 使用现有连接示例 ===");
        existingConnectionExample(evaluator);
    }
    
    private static void sqliteExample(SpiderEvaluator evaluator) {
        // 创建SQLite配置
        DatabaseConfig config = new DatabaseConfig("data/spider/database/academic/academic.sqlite");
        
        System.out.println("SQLite配置: " + config);
        System.out.println("JDBC URL: " + config.buildJdbcUrl());
        System.out.println("需要认证: " + config.requiresCredentials());
        
        // 测试连接
        boolean canConnect = DatabaseConnectionManager.testConnection(config);
        System.out.println("连接测试: " + (canConnect ? "成功" : "失败"));
        
        // 使用配置进行SQL验证
        String sql = "SELECT COUNT(*) FROM course";
        boolean isValid = evaluator.isValidSql(config, sql);
        System.out.println("SQL验证 '" + sql + "': " + (isValid ? "有效" : "无效"));
        
        // 使用配置进行执行评估
        String goldSql = "SELECT COUNT(*) FROM course";
        String predSql = "SELECT count(*) FROM course"; // 大小写不同
        boolean executionMatch = evaluator.evaluateExecution(config, predSql, goldSql);
        System.out.println("执行评估: " + (executionMatch ? "匹配" : "不匹配"));
    }
    
    private static void mysqlExample(SpiderEvaluator evaluator) {
        // 创建MySQL配置
        DatabaseConfig config = new DatabaseConfig(
            DatabaseConfig.DatabaseType.MYSQL,
            "localhost",
            3306,
            "spider_db",
            "root",
            "password"
        );
        
        // 添加额外的连接属性
        config.addProperty("useSSL", "false");
        config.addProperty("allowPublicKeyRetrieval", "true");
        
        System.out.println("MySQL配置: " + config);
        System.out.println("JDBC URL: " + config.buildJdbcUrl());
        System.out.println("需要认证: " + config.requiresCredentials());
        
        // 测试连接（通常会失败，因为没有实际的MySQL服务器）
        boolean canConnect = DatabaseConnectionManager.testConnection(config);
        System.out.println("连接测试: " + (canConnect ? "成功" : "失败"));
        
        if (canConnect) {
            // 只有在连接成功时才进行SQL测试
            String sql = "SELECT COUNT(*) FROM students";
            boolean isValid = evaluator.isValidSql(config, sql);
            System.out.println("SQL验证: " + (isValid ? "有效" : "无效"));
        }
    }
    
    private static void postgresqlExample(SpiderEvaluator evaluator) {
        // 创建PostgreSQL配置
        DatabaseConfig config = new DatabaseConfig(
            DatabaseConfig.DatabaseType.POSTGRESQL,
            "localhost",
            5432,
            "spider_db",
            "postgres",
            "password"
        );
        
        System.out.println("PostgreSQL配置: " + config);
        System.out.println("JDBC URL: " + config.buildJdbcUrl());
        System.out.println("默认端口: " + DatabaseConnectionManager.getDefaultPort(DatabaseConfig.DatabaseType.POSTGRESQL));
        
        // 测试连接
        boolean canConnect = DatabaseConnectionManager.testConnection(config);
        System.out.println("连接测试: " + (canConnect ? "成功" : "失败"));
    }
    
    private static void h2Example(SpiderEvaluator evaluator) {
        // 创建H2内存数据库配置
        DatabaseConfig config = new DatabaseConfig();
        config.setType(DatabaseConfig.DatabaseType.H2);
        config.setDatabase("testdb");
        
        System.out.println("H2配置: " + config);
        System.out.println("JDBC URL: " + config.buildJdbcUrl());
        System.out.println("需要认证: " + config.requiresCredentials());
        
        // H2内存数据库通常可以连接
        boolean canConnect = DatabaseConnectionManager.testConnection(config);
        System.out.println("连接测试: " + (canConnect ? "成功" : "失败"));
        
        if (canConnect) {
            try (Connection conn = DatabaseConnectionManager.createConnection(config)) {
                // 创建测试表
                conn.createStatement().execute("CREATE TABLE IF NOT EXISTS test_table (id INT, name VARCHAR(50))");
                conn.createStatement().execute("INSERT INTO test_table VALUES (1, 'Test')");
                
                // 测试SQL验证
                String sql = "SELECT COUNT(*) FROM test_table";
                boolean isValid = evaluator.isValidSql(conn, sql);
                System.out.println("SQL验证: " + (isValid ? "有效" : "无效"));
                
                // 测试执行评估
                String goldSql = "SELECT COUNT(*) FROM test_table";
                String predSql = "SELECT count(*) FROM test_table";
                boolean executionMatch = evaluator.evaluateExecution(conn, predSql, goldSql);
                System.out.println("执行评估: " + (executionMatch ? "匹配" : "不匹配"));
                
            } catch (SQLException e) {
                System.out.println("H2数据库操作失败: " + e.getMessage());
            }
        }
    }
    
    private static void existingConnectionExample(SpiderEvaluator evaluator) {
        // 使用现有连接的示例
        DatabaseConfig config = new DatabaseConfig();
        config.setType(DatabaseConfig.DatabaseType.H2);
        config.setDatabase("existing_conn_test");
        
        try (Connection conn = DatabaseConnectionManager.createConnection(config)) {
            System.out.println("使用现有连接进行评估");
            
            // 创建测试数据
            conn.createStatement().execute("CREATE TABLE users (id INT, name VARCHAR(100))");
            conn.createStatement().execute("INSERT INTO users VALUES (1, 'Alice'), (2, 'Bob')");
            
            // 直接使用连接进行评估
            String goldSql = "SELECT COUNT(*) FROM users";
            String predSql = "SELECT count(*) FROM users";
            
            boolean isValid = evaluator.isValidSql(conn, goldSql);
            System.out.println("SQL有效性: " + (isValid ? "有效" : "无效"));
            
            boolean executionMatch = evaluator.evaluateExecution(conn, predSql, goldSql);
            System.out.println("执行匹配: " + (executionMatch ? "匹配" : "不匹配"));
            
        } catch (SQLException e) {
            System.out.println("连接操作失败: " + e.getMessage());
        }
    }
} 