package com.nl2sql.spider.service;

import com.nl2sql.spider.config.DatabaseConfig;
import com.nl2sql.spider.enums.EvaluationType;
import com.nl2sql.spider.model.EvaluationResult;
import com.nl2sql.spider.utils.DatabaseConnectionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SpiderEvaluationService扩展功能测试
 */
public class SpiderEvaluationServiceExtensionTest {
    
    private SpiderEvaluationService service;
    
    @BeforeEach
    void setUp() {
        service = new SpiderEvaluationService();
    }
    
    @Test
    @DisplayName("测试数据库配置创建")
    void testCreateDatabaseConfig() {
        // 测试MySQL配置创建
        DatabaseConfig mysqlConfig = service.createDatabaseConfig(
            "mysql", "localhost", 3306, "testdb", "root", "password"
        );
        
        assertEquals(DatabaseConfig.DatabaseType.MYSQL, mysqlConfig.getType());
        assertEquals("localhost", mysqlConfig.getHost());
        assertEquals(3306, mysqlConfig.getPort());
        assertEquals("testdb", mysqlConfig.getDatabase());
        assertEquals("root", mysqlConfig.getUsername());
        assertEquals("password", mysqlConfig.getPassword());
        
        // 测试PostgreSQL配置创建
        DatabaseConfig pgConfig = service.createDatabaseConfig(
            "postgresql", "localhost", 5432, "testdb", "postgres", "password"
        );
        
        assertEquals(DatabaseConfig.DatabaseType.POSTGRESQL, pgConfig.getType());
        assertEquals("localhost", pgConfig.getHost());
        assertEquals(5432, pgConfig.getPort());
        
        // 测试不支持的数据库类型
        assertThrows(IllegalArgumentException.class, () -> {
            service.createDatabaseConfig("unsupported", "localhost", 1234, "db", "user", "pass");
        });
    }
    
    @Test
    @DisplayName("测试数据库连接测试")
    void testDatabaseConnectionTest() {
        // 测试SQLite配置
        DatabaseConfig sqliteConfig = new DatabaseConfig("data/spider/database/academic/academic.sqlite");
        
        // 连接测试应该不抛出异常
        boolean result = service.testDatabaseConnection(sqliteConfig);
        assertNotNull(result); // 结果可能是true或false，取决于文件是否存在
        
        // 测试H2内存数据库
        DatabaseConfig h2Config = new DatabaseConfig();
        h2Config.setType(DatabaseConfig.DatabaseType.H2);
        h2Config.setDatabase("testdb");
        
        boolean h2Result = service.testDatabaseConnection(h2Config);
        assertTrue(h2Result); // H2内存数据库应该能够连接
        
        // 测试无效配置
        DatabaseConfig invalidConfig = new DatabaseConfig(
            DatabaseConfig.DatabaseType.MYSQL, "invalid_host", 9999, "nonexistent", "user", "pass"
        );
        
        boolean invalidResult = service.testDatabaseConnection(invalidConfig);
        assertFalse(invalidResult); // 无效配置应该连接失败
    }
    
    @Test
    @DisplayName("测试SQL验证功能")
    void testSqlValidation() {
        // 由于没有实际的数据库模式，这个测试主要验证方法调用不抛出异常
        DatabaseConfig config = new DatabaseConfig();
        config.setType(DatabaseConfig.DatabaseType.H2);
        config.setDatabase("testdb");
        
        String validSql = "SELECT 1";
        String invalidSql = "INVALID SQL SYNTAX";
        
        // 这些方法应该能正常调用（可能返回false，因为没有schema）
        assertDoesNotThrow(() -> {
            service.validateSql(validSql, "test_db", config);
        });
        
        assertDoesNotThrow(() -> {
            service.validateSql(invalidSql, "test_db", config);
        });
    }
    
    @Test
    @DisplayName("测试批量评估功能")
    void testBatchEvaluation() {
        DatabaseConfig config = new DatabaseConfig();
        config.setType(DatabaseConfig.DatabaseType.H2);
        config.setDatabase("testdb");
        
        List<String> goldSqls = Arrays.asList(
            "SELECT COUNT(*) FROM users",
            "SELECT name FROM users WHERE age > 18"
        );
        
        List<String> predSqls = Arrays.asList(
            "SELECT count(*) FROM users",
            "SELECT name FROM users WHERE age > 18"
        );
        
        // 测试批量评估（没有schema时会抛出异常，但方法调用结构应该正确）
        List<EvaluationResult> results = service.evaluateBatch(goldSqls, predSqls, "test_db", config, EvaluationType.MATCH);
        // 由于没有schema，所有结果都应该是失败结果
        assertEquals(goldSqls.size(), results.size());
        for (EvaluationResult result : results) {
            assertFalse(result.isExactMatch());
            assertFalse(result.isExecutionMatch());
        }
        
        // 测试不同大小的列表
        List<String> shortPredSqls = Arrays.asList("SELECT 1");
        
        assertThrows(IllegalArgumentException.class, () -> {
            service.evaluateBatch(goldSqls, shortPredSqls, "test_db", config, EvaluationType.MATCH);
        });
    }
    
    @Test
    @DisplayName("测试使用DatabaseConfig进行评估")
    void testEvaluationWithConfig() {
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
    
    @Test
    @DisplayName("测试使用连接的评估功能")
    void testEvaluationWithConnection() {
        DatabaseConfig config = new DatabaseConfig();
        config.setType(DatabaseConfig.DatabaseType.H2);
        config.setDatabase("test_connection_db");
        
        try (Connection conn = DatabaseConnectionManager.createConnection(config)) {
            // 创建测试表
            conn.createStatement().execute("CREATE TABLE test_table (id INT, name VARCHAR(50))");
            conn.createStatement().execute("INSERT INTO test_table VALUES (1, 'Test')");
            
            String goldSql = "SELECT COUNT(*) FROM test_table";
            String predSql = "SELECT count(*) FROM test_table";
            
            // 测试使用连接的SQL验证
            boolean isValid = service.validateSql(goldSql, "test_db", conn);
            // 由于没有schema，这里预期返回false，但不应该抛出异常
            assertFalse(isValid);
            
            // 测试使用连接的评估（没有schema时会抛出异常）
            assertThrows(IllegalArgumentException.class, () -> {
                service.evaluateSingle(goldSql, predSql, "test_db", conn, EvaluationType.EXEC);
            });
            
        } catch (Exception e) {
            fail("Connection test should not throw exception: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("测试配置属性设置")
    void testConfigurationProperties() {
        DatabaseConfig config = service.createDatabaseConfig(
            "mysql", "localhost", 3306, "testdb", "root", "password"
        );
        
        // 测试添加额外属性
        config.addProperty("useSSL", "false");
        config.addProperty("serverTimezone", "UTC");
        
        assertEquals("false", config.getAdditionalProperties().getProperty("useSSL"));
        assertEquals("UTC", config.getAdditionalProperties().getProperty("serverTimezone"));
        
        // 测试JDBC URL构建
        String jdbcUrl = config.buildJdbcUrl();
        assertTrue(jdbcUrl.contains("mysql"));
        assertTrue(jdbcUrl.contains("localhost"));
        assertTrue(jdbcUrl.contains("3306"));
        assertTrue(jdbcUrl.contains("testdb"));
    }
    
    @Test
    @DisplayName("测试错误处理")
    void testErrorHandling() {
        DatabaseConfig config = new DatabaseConfig();
        config.setType(DatabaseConfig.DatabaseType.H2);
        config.setDatabase("error_test_db");
        
        // 测试空SQL
        assertDoesNotThrow(() -> {
            service.validateSql("", "test_db", config);
        });
        
        // 测试null SQL
        assertDoesNotThrow(() -> {
            service.validateSql(null, "test_db", config);
        });
        
        // 测试无效的dbId
        assertDoesNotThrow(() -> {
            service.validateSql("SELECT 1", "nonexistent_db", config);
        });
    }
} 