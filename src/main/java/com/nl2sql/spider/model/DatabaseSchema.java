package com.nl2sql.spider.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * 数据库Schema的数据模型
 * 对应Python版本的tables.json格式
 */
public class DatabaseSchema {
    
    @JsonProperty("db_id")
    private String dbId;
    
    @JsonProperty("table_names")
    private List<String> tableNames;
    
    @JsonProperty("table_names_original")
    private List<String> tableNamesOriginal;
    
    @JsonProperty("column_names")
    private List<List<Object>> columnNames;
    
    @JsonProperty("column_names_original")
    private List<List<Object>> columnNamesOriginal;
    
    @JsonProperty("column_types")
    private List<String> columnTypes;
    
    @JsonProperty("foreign_keys")
    private List<List<Integer>> foreignKeys;
    
    @JsonProperty("primary_keys")
    private List<Integer> primaryKeys;
    
    // 内部使用的映射表
    private Map<String, String> idMap;
    
    public DatabaseSchema() {}
    
    public String getDbId() {
        return dbId;
    }
    
    public void setDbId(String dbId) {
        this.dbId = dbId;
    }
    
    public List<String> getTableNames() {
        return tableNames;
    }
    
    public void setTableNames(List<String> tableNames) {
        this.tableNames = tableNames;
    }
    
    public List<String> getTableNamesOriginal() {
        return tableNamesOriginal;
    }
    
    public void setTableNamesOriginal(List<String> tableNamesOriginal) {
        this.tableNamesOriginal = tableNamesOriginal;
    }
    
    public List<List<Object>> getColumnNames() {
        return columnNames;
    }
    
    public void setColumnNames(List<List<Object>> columnNames) {
        this.columnNames = columnNames;
    }
    
    public List<List<Object>> getColumnNamesOriginal() {
        return columnNamesOriginal;
    }
    
    public void setColumnNamesOriginal(List<List<Object>> columnNamesOriginal) {
        this.columnNamesOriginal = columnNamesOriginal;
    }
    
    public List<String> getColumnTypes() {
        return columnTypes;
    }
    
    public void setColumnTypes(List<String> columnTypes) {
        this.columnTypes = columnTypes;
    }
    
    public List<List<Integer>> getForeignKeys() {
        return foreignKeys;
    }
    
    public void setForeignKeys(List<List<Integer>> foreignKeys) {
        this.foreignKeys = foreignKeys;
    }
    
    public List<Integer> getPrimaryKeys() {
        return primaryKeys;
    }
    
    public void setPrimaryKeys(List<Integer> primaryKeys) {
        this.primaryKeys = primaryKeys;
    }
    
    public Map<String, String> getIdMap() {
        return idMap;
    }
    
    public void setIdMap(Map<String, String> idMap) {
        this.idMap = idMap;
    }
    
    @Override
    public String toString() {
        return "DatabaseSchema{" +
                "dbId='" + dbId + '\'' +
                ", tableNames=" + tableNames +
                ", tableNamesOriginal=" + tableNamesOriginal +
                ", columnNames=" + columnNames +
                ", columnNamesOriginal=" + columnNamesOriginal +
                ", columnTypes=" + columnTypes +
                ", foreignKeys=" + foreignKeys +
                ", primaryKeys=" + primaryKeys +
                '}';
    }
} 