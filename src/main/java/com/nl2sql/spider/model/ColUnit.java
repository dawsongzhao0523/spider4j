package com.nl2sql.spider.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 列单元的数据模型
 * 对应Python版本的 (agg_id, col_id, isDistinct)
 */
public class ColUnit {
    
    @JsonProperty("aggId")
    private int aggId;
    
    @JsonProperty("colId")
    private String colId;
    
    @JsonProperty("isDistinct")
    private boolean isDistinct;
    
    public ColUnit() {}
    
    public ColUnit(int aggId, String colId, boolean isDistinct) {
        this.aggId = aggId;
        this.colId = colId;
        this.isDistinct = isDistinct;
    }
    
    public int getAggId() {
        return aggId;
    }
    
    public void setAggId(int aggId) {
        this.aggId = aggId;
    }
    
    public String getColId() {
        return colId;
    }
    
    public void setColId(String colId) {
        this.colId = colId;
    }
    
    public boolean isDistinct() {
        return isDistinct;
    }
    
    public void setDistinct(boolean distinct) {
        isDistinct = distinct;
    }
    
    @Override
    public String toString() {
        return "ColUnit{" +
                "aggId=" + aggId +
                ", colId='" + colId + '\'' +
                ", isDistinct=" + isDistinct +
                '}';
    }
} 