package com.nl2sql.spider.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 值单元的数据模型
 * 对应Python版本的 (unit_op, col_unit1, col_unit2)
 * 扩展支持不同类型的值
 */
public class ValUnit {
    
    @JsonProperty("unitOp")
    private int unitOp;
    
    @JsonProperty("colUnit1")
    private ColUnit colUnit1;
    
    @JsonProperty("colUnit2")
    private ColUnit colUnit2;
    
    // 扩展字段：值类型和值内容
    @JsonProperty("valueType")
    private String valueType;
    
    @JsonProperty("value")
    private Object value;
    
    public ValUnit() {}
    
    public ValUnit(int unitOp, ColUnit colUnit1, ColUnit colUnit2) {
        this.unitOp = unitOp;
        this.colUnit1 = colUnit1;
        this.colUnit2 = colUnit2;
    }
    
    // 新的构造函数，支持不同类型的值
    public ValUnit(String valueType, Object value) {
        this.valueType = valueType;
        this.value = value;
        this.unitOp = 0; // 默认为"none"
    }
    
    public int getUnitOp() {
        return unitOp;
    }
    
    public void setUnitOp(int unitOp) {
        this.unitOp = unitOp;
    }
    
    public ColUnit getColUnit1() {
        return colUnit1;
    }
    
    public void setColUnit1(ColUnit colUnit1) {
        this.colUnit1 = colUnit1;
    }
    
    public ColUnit getColUnit2() {
        return colUnit2;
    }
    
    public void setColUnit2(ColUnit colUnit2) {
        this.colUnit2 = colUnit2;
    }
    
    public String getValueType() {
        return valueType;
    }
    
    public void setValueType(String valueType) {
        this.valueType = valueType;
    }
    
    public Object getValue() {
        return value;
    }
    
    public void setValue(Object value) {
        this.value = value;
    }
    
    @Override
    public String toString() {
        return "ValUnit{" +
                "unitOp=" + unitOp +
                ", colUnit1=" + colUnit1 +
                ", colUnit2=" + colUnit2 +
                ", valueType='" + valueType + '\'' +
                ", value=" + value +
                '}';
    }
} 