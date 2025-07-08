package com.nl2sql.spider.utils;

import com.nl2sql.spider.config.DatabaseConfig;
import com.nl2sql.spider.model.DatabaseSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

/**
 * 数据库Schema提取工具
 * 通过JDBC动态获取数据库结构信息
 */
public class DatabaseSchemaExtractor {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseSchemaExtractor.class);
    
    /**
     * 从数据库连接中提取schema信息
     */
    public static DatabaseSchema extractSchema(Connection connection, String dbId) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        
        // 获取数据库名称
        String databaseName = connection.getCatalog();
        if (databaseName == null || databaseName.isEmpty()) {
            databaseName = dbId;
        }
        
        Map<String, List<String>> tableColumns = new HashMap<>();
        Map<String, String> columnTypes = new HashMap<>();
        List<String> tableNames = new ArrayList<>();
        
        // 获取所有表
        try (ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"})) {
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                // 过滤系统表
                if (isSystemTable(tableName, metaData.getDatabaseProductName())) {
                    continue;
                }
                
                tableNames.add(tableName.toLowerCase());
                List<String> columns = new ArrayList<>();
                
                // 获取表的列信息
                try (ResultSet columnsRs = metaData.getColumns(null, null, tableName, "%")) {
                    while (columnsRs.next()) {
                        String columnName = columnsRs.getString("COLUMN_NAME");
                        String columnType = columnsRs.getString("TYPE_NAME");
                        int dataType = columnsRs.getInt("DATA_TYPE");
                        
                        columns.add(columnName.toLowerCase());
                        columnTypes.put(tableName.toLowerCase() + "." + columnName.toLowerCase(), 
                                      mapSqlTypeToString(dataType, columnType));
                    }
                }
                
                tableColumns.put(tableName.toLowerCase(), columns);
                
                logger.debug("提取表 {} 的 {} 个列", tableName, columns.size());
            }
        }
        
        logger.info("成功提取数据库 {} 的schema信息: {} 个表", dbId, tableNames.size());
        
        return buildDatabaseSchema(dbId, tableNames, tableColumns, columnTypes);
    }
    
    /**
     * 从DatabaseConfig提取schema信息
     */
    public static DatabaseSchema extractSchema(DatabaseConfig dbConfig, String dbId) throws SQLException {
        try (Connection connection = DatabaseConnectionManager.createConnection(dbConfig)) {
            return extractSchema(connection, dbId);
        }
    }
    
    /**
     * 判断是否为系统表
     */
    private static boolean isSystemTable(String tableName, String databaseProduct) {
        if (tableName == null) {
            return true;
        }
        
        String lowerTableName = tableName.toLowerCase();
        
        // SQLite系统表
        if (lowerTableName.startsWith("sqlite_")) {
            return true;
        }
        
        // MySQL系统表
        if (databaseProduct != null && databaseProduct.toLowerCase().contains("mysql")) {
            return lowerTableName.startsWith("information_schema") ||
                   lowerTableName.startsWith("performance_schema") ||
                   lowerTableName.startsWith("mysql") ||
                   lowerTableName.startsWith("sys");
        }
        
        // PostgreSQL系统表
        if (databaseProduct != null && databaseProduct.toLowerCase().contains("postgresql")) {
            return lowerTableName.startsWith("information_schema") ||
                   lowerTableName.startsWith("pg_");
        }
        
        // Oracle系统表
        if (databaseProduct != null && databaseProduct.toLowerCase().contains("oracle")) {
            return lowerTableName.startsWith("sys") ||
                   lowerTableName.startsWith("system") ||
                   lowerTableName.startsWith("ctxsys") ||
                   lowerTableName.startsWith("mdsys") ||
                   lowerTableName.startsWith("olapsys") ||
                   lowerTableName.startsWith("wmsys");
        }
        
        // SQL Server系统表
        if (databaseProduct != null && databaseProduct.toLowerCase().contains("sql server")) {
            return lowerTableName.startsWith("sys") ||
                   lowerTableName.startsWith("information_schema");
        }
        
        return false;
    }
    
    /**
     * 将SQL数据类型映射为字符串
     */
    private static String mapSqlTypeToString(int sqlType, String typeName) {
        switch (sqlType) {
            case Types.VARCHAR:
            case Types.CHAR:
            case Types.LONGVARCHAR:
            case Types.NVARCHAR:
            case Types.NCHAR:
            case Types.LONGNVARCHAR:
            case Types.CLOB:
            case Types.NCLOB:
                return "text";
                
            case Types.INTEGER:
            case Types.SMALLINT:
            case Types.TINYINT:
            case Types.BIGINT:
                return "number";
                
            case Types.DECIMAL:
            case Types.NUMERIC:
            case Types.REAL:
            case Types.FLOAT:
            case Types.DOUBLE:
                return "number";
                
            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
                return "time";
                
            case Types.BOOLEAN:
            case Types.BIT:
                return "boolean";
                
            case Types.BLOB:
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                return "others";
                
            default:
                // 如果无法映射，使用原始类型名称
                return typeName != null ? typeName.toLowerCase() : "others";
        }
    }
    
    /**
     * 提取指定表的schema信息
     */
    public static DatabaseSchema extractTableSchema(DatabaseConfig dbConfig, String dbId, String tableName) throws SQLException {
        try (Connection connection = DatabaseConnectionManager.createConnection(dbConfig)) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            String databaseName = connection.getCatalog();
            if (databaseName == null || databaseName.isEmpty()) {
                databaseName = dbId;
            }
            
            Map<String, List<String>> tableColumns = new HashMap<>();
            Map<String, String> columnTypes = new HashMap<>();
            List<String> tableNames = new ArrayList<>();
            
            // 检查表是否存在
            boolean tableExists = false;
            try (ResultSet tables = metaData.getTables(null, null, tableName, new String[]{"TABLE"})) {
                if (tables.next()) {
                    tableExists = true;
                }
            }
            
            if (!tableExists) {
                throw new SQLException("表 " + tableName + " 不存在");
            }
            
            tableNames.add(tableName.toLowerCase());
            List<String> columns = new ArrayList<>();
            
            // 获取表的列信息
            try (ResultSet columnsRs = metaData.getColumns(null, null, tableName, "%")) {
                while (columnsRs.next()) {
                    String columnName = columnsRs.getString("COLUMN_NAME");
                    String columnType = columnsRs.getString("TYPE_NAME");
                    int dataType = columnsRs.getInt("DATA_TYPE");
                    
                    columns.add(columnName.toLowerCase());
                    columnTypes.put(tableName.toLowerCase() + "." + columnName.toLowerCase(), 
                                  mapSqlTypeToString(dataType, columnType));
                }
            }
            
            tableColumns.put(tableName.toLowerCase(), columns);
            
            logger.info("成功提取表 {} 的schema信息: {} 个列", tableName, columns.size());
            
            return buildDatabaseSchema(dbId, tableNames, tableColumns, columnTypes);
        }
    }
    
    /**
     * 构建DatabaseSchema对象
     */
    private static DatabaseSchema buildDatabaseSchema(String dbId, List<String> tableNames, 
                                                     Map<String, List<String>> tableColumns, 
                                                     Map<String, String> columnTypes) {
        DatabaseSchema schema = new DatabaseSchema();
        schema.setDbId(dbId);
        schema.setTableNames(new ArrayList<>(tableNames));
        schema.setTableNamesOriginal(new ArrayList<>(tableNames)); // 使用相同的表名作为原始表名
        
        // 构建column_names格式: [[tableIndex, columnName], ...]
        List<List<Object>> columnNames = new ArrayList<>();
        List<List<Object>> columnNamesOriginal = new ArrayList<>();
        List<String> columnTypesList = new ArrayList<>();
        
        // 添加特殊的"*"列（表示所有列）
        columnNames.add(Arrays.asList(-1, "*"));
        columnNamesOriginal.add(Arrays.asList(-1, "*"));
        columnTypesList.add("text");
        
        // 为每个表添加列信息
        for (int tableIndex = 0; tableIndex < tableNames.size(); tableIndex++) {
            String tableName = tableNames.get(tableIndex);
            List<String> columns = tableColumns.get(tableName);
            
            if (columns != null) {
                for (String columnName : columns) {
                    columnNames.add(Arrays.asList(tableIndex, columnName));
                    columnNamesOriginal.add(Arrays.asList(tableIndex, columnName));
                    
                    // 获取列类型
                    String columnType = columnTypes.get(tableName + "." + columnName);
                    columnTypesList.add(columnType != null ? columnType : "text");
                }
            }
        }
        
        schema.setColumnNames(columnNames);
        schema.setColumnNamesOriginal(columnNamesOriginal);
        schema.setColumnTypes(columnTypesList);
        
        // 设置空的外键和主键信息（如果需要可以后续扩展）
        schema.setForeignKeys(new ArrayList<>());
        schema.setPrimaryKeys(new ArrayList<>());
        
        return schema;
    }
} 