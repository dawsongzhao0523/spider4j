package com.nl2sql.spider.config;

import java.util.Properties;

/**
 * 数据库配置类
 * 支持多种数据库类型的连接配置
 */
public class DatabaseConfig {
    
    public enum DatabaseType {
        SQLITE("org.sqlite.JDBC", "jdbc:sqlite:"),
        MYSQL("com.mysql.cj.jdbc.Driver", "jdbc:mysql://"),
        POSTGRESQL("org.postgresql.Driver", "jdbc:postgresql://"),
        ORACLE("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@"),
        H2("org.h2.Driver", "jdbc:h2:"),
        SQLSERVER("com.microsoft.sqlserver.jdbc.SQLServerDriver", "jdbc:sqlserver://");
        
        private final String driverClass;
        private final String urlPrefix;
        
        DatabaseType(String driverClass, String urlPrefix) {
            this.driverClass = driverClass;
            this.urlPrefix = urlPrefix;
        }
        
        public String getDriverClass() {
            return driverClass;
        }
        
        public String getUrlPrefix() {
            return urlPrefix;
        }
    }
    
    private DatabaseType type;
    private String host;
    private int port;
    private String database;
    private String username;
    private String password;
    private String filePath; // 用于SQLite等基于文件的数据库
    private Properties additionalProperties;
    
    // 构造函数
    public DatabaseConfig() {
        this.additionalProperties = new Properties();
    }
    
    // SQLite构造函数
    public DatabaseConfig(String filePath) {
        this();
        this.type = DatabaseType.SQLITE;
        this.filePath = filePath;
    }
    
    // 网络数据库构造函数
    public DatabaseConfig(DatabaseType type, String host, int port, String database, 
                         String username, String password) {
        this();
        this.type = type;
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }
    
    /**
     * 构建JDBC URL
     */
    public String buildJdbcUrl() {
        switch (type) {
            case SQLITE:
                return DatabaseType.SQLITE.getUrlPrefix() + filePath;
            case MYSQL:
                return DatabaseType.MYSQL.getUrlPrefix() + host + ":" + port + "/" + database + 
                       "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
            case POSTGRESQL:
                return DatabaseType.POSTGRESQL.getUrlPrefix() + host + ":" + port + "/" + database;
            case ORACLE:
                return DatabaseType.ORACLE.getUrlPrefix() + host + ":" + port + ":" + database;
            case H2:
                return DatabaseType.H2.getUrlPrefix() + (filePath != null ? filePath : "mem:" + database);
            case SQLSERVER:
                return DatabaseType.SQLSERVER.getUrlPrefix() + host + ":" + port + ";databaseName=" + database;
            default:
                throw new IllegalArgumentException("Unsupported database type: " + type);
        }
    }
    
    /**
     * 获取驱动类名
     */
    public String getDriverClass() {
        return type.getDriverClass();
    }
    
    /**
     * 是否需要用户名密码
     */
    public boolean requiresCredentials() {
        return type != DatabaseType.SQLITE && type != DatabaseType.H2;
    }
    
    // Getters and Setters
    public DatabaseType getType() {
        return type;
    }
    
    public void setType(DatabaseType type) {
        this.type = type;
    }
    
    public String getHost() {
        return host;
    }
    
    public void setHost(String host) {
        this.host = host;
    }
    
    public int getPort() {
        return port;
    }
    
    public void setPort(int port) {
        this.port = port;
    }
    
    public String getDatabase() {
        return database;
    }
    
    public void setDatabase(String database) {
        this.database = database;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public Properties getAdditionalProperties() {
        return additionalProperties;
    }
    
    public void setAdditionalProperties(Properties additionalProperties) {
        this.additionalProperties = additionalProperties;
    }
    
    public void addProperty(String key, String value) {
        this.additionalProperties.setProperty(key, value);
    }
    
    @Override
    public String toString() {
        return "DatabaseConfig{" +
                "type=" + type +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", database='" + database + '\'' +
                ", username='" + username + '\'' +
                ", filePath='" + filePath + '\'' +
                '}';
    }
} 