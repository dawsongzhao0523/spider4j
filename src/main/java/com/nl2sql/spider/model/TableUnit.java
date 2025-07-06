package com.nl2sql.spider.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 表单元的数据模型
 * 对应Python版本的 (table_type, col_unit/sql)
 */
public class TableUnit {
    
    @JsonProperty("tableType")
    private String tableType; // "table_unit" or "sql"
    
    @JsonProperty("tableId")
    private String tableId; // 当tableType为"table_unit"时使用
    
    @JsonProperty("sql")
    private SqlStructure sql; // 当tableType为"sql"时使用
    
    public TableUnit() {}
    
    public TableUnit(String tableType, String tableId) {
        this.tableType = tableType;
        this.tableId = tableId;
    }
    
    public TableUnit(String tableType, SqlStructure sql) {
        this.tableType = tableType;
        this.sql = sql;
    }
    
    public String getTableType() {
        return tableType;
    }
    
    public void setTableType(String tableType) {
        this.tableType = tableType;
    }
    
    public String getTableId() {
        return tableId;
    }
    
    public void setTableId(String tableId) {
        this.tableId = tableId;
    }
    
    public SqlStructure getSql() {
        return sql;
    }
    
    public void setSql(SqlStructure sql) {
        this.sql = sql;
    }
    
    @Override
    public String toString() {
        return "TableUnit{" +
                "tableType='" + tableType + '\'' +
                ", tableId='" + tableId + '\'' +
                ", sql=" + sql +
                '}';
    }
} 