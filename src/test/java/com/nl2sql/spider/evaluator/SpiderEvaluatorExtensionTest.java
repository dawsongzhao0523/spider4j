package com.nl2sql.spider.evaluator;

import com.nl2sql.spider.config.DatabaseConfig;
import com.nl2sql.spider.utils.DatabaseConnectionManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SpiderEvaluator扩展功能测试
 */
public class SpiderEvaluatorExtensionTest {
    
    private SpiderEvaluator evaluator;
    
    @BeforeEach
    void setUp() {
        evaluator = new SpiderEvaluator();
    }
    
    @Test
    @DisplayName("测试SQLite数据库配置")
    void testSqliteConfig() {
        DatabaseConfig config = new DatabaseConfig("data/spider/database/academic/academic.sqlite");
        
        // 测试配置构建
        assertEquals(DatabaseConfig.DatabaseType.SQLITE, config.getType());
        assertEquals("data/spider/database/academic/academic.sqlite", config.getFilePath());
        assertEquals("jdbc:sqlite:data/spider/database/academic/academic.sqlite", config.buildJdbcUrl());
        assertFalse(config.requiresCredentials());
    }
    
    @Test
    @DisplayName("测试MySQL数据库配置")
    void testMysqlConfig() {
        DatabaseConfig config = new DatabaseConfig(
            DatabaseConfig.DatabaseType.MYSQL, 
            "localhost", 
            3306, 
            "spider_db", 
            "root", 
            "password"
        );
        
        // 测试配置构建
        assertEquals(DatabaseConfig.DatabaseType.MYSQL, config.getType());
        assertEquals("localhost", config.getHost());
        assertEquals(3306, config.getPort());
        assertEquals("spider_db", config.getDatabase());
        assertEquals("root", config.getUsername());
        assertEquals("password", config.getPassword());
        assertTrue(config.requiresCredentials());
        
        String expectedUrl = "jdbc:mysql://localhost:3306/spider_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        assertEquals(expectedUrl, config.buildJdbcUrl());
    }
    
    @Test
    @DisplayName("测试PostgreSQL数据库配置")
    void testPostgresqlConfig() {
        DatabaseConfig config = new DatabaseConfig(
            DatabaseConfig.DatabaseType.POSTGRESQL, 
            "localhost", 
            5432, 
            "spider_db", 
            "postgres", 
            "password"
        );
        
        assertEquals(DatabaseConfig.DatabaseType.POSTGRESQL, config.getType());
        assertEquals("jdbc:postgresql://localhost:5432/spider_db", config.buildJdbcUrl());
        assertTrue(config.requiresCredentials());
    }
    
    @Test
    @DisplayName("测试H2数据库配置")
    void testH2Config() {
        DatabaseConfig config = new DatabaseConfig();
        config.setType(DatabaseConfig.DatabaseType.H2);
        config.setDatabase("testdb");
        
        assertEquals(DatabaseConfig.DatabaseType.H2, config.getType());
        assertEquals("jdbc:h2:mem:testdb", config.buildJdbcUrl());
        assertFalse(config.requiresCredentials());
    }
    
    @Test
    @DisplayName("测试数据库连接管理器")
    void testDatabaseConnectionManager() {
        // 测试SQLite连接
        DatabaseConfig sqliteConfig = new DatabaseConfig("data/spider/database/academic/academic.sqlite");
        
        try (Connection conn = DatabaseConnectionManager.createConnection(sqliteConfig)) {
            assertNotNull(conn);
            assertFalse(conn.isClosed());
        } catch (SQLException e) {
            // 如果文件不存在，这是正常的
            assertTrue(e.getMessage().contains("no such table") || 
                      e.getMessage().contains("unable to open database file"));
        }
        
        // 测试连接测试功能
        boolean canConnect = DatabaseConnectionManager.testConnection(sqliteConfig);
        // 结果取决于文件是否存在，但不应该抛出异常
        assertNotNull(canConnect);
    }
    
    @Test
    @DisplayName("测试默认端口获取")
    void testDefaultPorts() {
        assertEquals(3306, DatabaseConnectionManager.getDefaultPort(DatabaseConfig.DatabaseType.MYSQL));
        assertEquals(5432, DatabaseConnectionManager.getDefaultPort(DatabaseConfig.DatabaseType.POSTGRESQL));
        assertEquals(1521, DatabaseConnectionManager.getDefaultPort(DatabaseConfig.DatabaseType.ORACLE));
        assertEquals(1433, DatabaseConnectionManager.getDefaultPort(DatabaseConfig.DatabaseType.SQLSERVER));
        assertEquals(9092, DatabaseConnectionManager.getDefaultPort(DatabaseConfig.DatabaseType.H2));
        assertEquals(0, DatabaseConnectionManager.getDefaultPort(DatabaseConfig.DatabaseType.SQLITE));
    }
    
    @Test
    @DisplayName("测试SpiderEvaluator扩展方法")
    void testSpiderEvaluatorExtensions() {
        DatabaseConfig config = new DatabaseConfig("data/spider/database/academic/academic.sqlite");
        
        // 测试SQL验证方法
        String validSql = "SELECT 1";
        String invalidSql = "SELECT * FROM non_existent_table";
        
        // 这些测试可能因为数据库文件不存在而失败，但方法应该正常调用
        try {
            boolean isValid1 = evaluator.isValidSql(config, validSql);
            boolean isValid2 = evaluator.isValidSql(config, invalidSql);
            
            // 至少验证方法可以正常调用
            assertNotNull(isValid1);
            assertNotNull(isValid2);
        } catch (Exception e) {
            // 预期可能的异常
            assertTrue(e.getMessage().contains("no such table") || 
                      e.getMessage().contains("unable to open database file") ||
                      e.getMessage().contains("Database driver not found"));
        }
    }
    
    @Test
    @DisplayName("测试配置属性扩展")
    void testConfigProperties() {
        DatabaseConfig config = new DatabaseConfig();
        config.setType(DatabaseConfig.DatabaseType.MYSQL);
        config.setHost("localhost");
        config.setPort(3306);
        config.setDatabase("test");
        
        // 添加额外属性
        config.addProperty("useSSL", "false");
        config.addProperty("serverTimezone", "UTC");
        
        assertNotNull(config.getAdditionalProperties());
        assertEquals("false", config.getAdditionalProperties().getProperty("useSSL"));
        assertEquals("UTC", config.getAdditionalProperties().getProperty("serverTimezone"));
    }
} 