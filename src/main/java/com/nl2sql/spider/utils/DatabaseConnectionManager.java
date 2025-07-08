package com.nl2sql.spider.utils;

import com.nl2sql.spider.config.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * 数据库连接管理器
 * 负责创建和管理数据库连接
 */
public class DatabaseConnectionManager {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnectionManager.class);
    
    /**
     * 创建数据库连接
     * 
     * @param config 数据库配置
     * @return 数据库连接
     * @throws SQLException 连接异常
     */
    public static Connection createConnection(DatabaseConfig config) throws SQLException {
        try {
            // 加载数据库驱动
            Class.forName(config.getDriverClass());
            
            String jdbcUrl = config.buildJdbcUrl();
            logger.debug("Creating connection to: {}", jdbcUrl);
            
            Properties props = new Properties();
            
            // 添加用户名密码（如果需要）
            if (config.requiresCredentials()) {
                if (config.getUsername() != null) {
                    props.setProperty("user", config.getUsername());
                }
                if (config.getPassword() != null) {
                    props.setProperty("password", config.getPassword());
                }
            }
            
            // 添加额外属性
            if (config.getAdditionalProperties() != null) {
                props.putAll(config.getAdditionalProperties());
            }
            
            // 创建连接
            Connection conn = DriverManager.getConnection(jdbcUrl, props);
            logger.debug("Successfully created connection to {}", config.getType());
            
            return conn;
            
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found: " + config.getDriverClass(), e);
        } catch (SQLException e) {
            logger.error("Failed to create connection to {}: {}", config.getType(), e.getMessage());
            throw e;
        }
    }
    
    /**
     * 创建SQLite连接（向后兼容）
     * 
     * @param dbPath SQLite数据库文件路径
     * @return 数据库连接
     * @throws SQLException 连接异常
     */
    public static Connection createSqliteConnection(String dbPath) throws SQLException {
        DatabaseConfig config = new DatabaseConfig(dbPath);
        return createConnection(config);
    }
    
    /**
     * 测试数据库连接
     * 
     * @param config 数据库配置
     * @return 连接是否成功
     */
    public static boolean testConnection(DatabaseConfig config) {
        try (Connection conn = createConnection(config)) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            logger.warn("Connection test failed for {}: {}", config.getType(), e.getMessage());
            return false;
        }
    }
    
    /**
     * 安全关闭连接
     * 
     * @param conn 数据库连接
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                logger.debug("Connection closed successfully");
            } catch (SQLException e) {
                logger.warn("Failed to close connection: {}", e.getMessage());
            }
        }
    }
    
    /**
     * 获取数据库类型的默认端口
     * 
     * @param type 数据库类型
     * @return 默认端口
     */
    public static int getDefaultPort(DatabaseConfig.DatabaseType type) {
        switch (type) {
            case MYSQL:
                return 3306;
            case POSTGRESQL:
                return 5432;
            case ORACLE:
                return 1521;
            case SQLSERVER:
                return 1433;
            case H2:
                return 9092;
            default:
                return 0; // SQLite等基于文件的数据库不需要端口
        }
    }
} 