package com.nl2sql.spider.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 条件单元的数据模型
 * 对应Python版本的 (not_op, op_id, val_unit, val1, val2)
 */
public class ConditionUnit {
    
    @JsonProperty("notOp")
    private boolean notOp;
    
    @JsonProperty("opId")
    private int opId;
    
    @JsonProperty("valUnit")
    private ValUnit valUnit;
    
    @JsonProperty("val1")
    private Object val1;
    
    @JsonProperty("val2")
    private Object val2;
    
    public ConditionUnit() {}
    
    public ConditionUnit(boolean notOp, int opId, ValUnit valUnit, Object val1, Object val2) {
        this.notOp = notOp;
        this.opId = opId;
        this.valUnit = valUnit;
        this.val1 = val1;
        this.val2 = val2;
    }
    
    public boolean isNotOp() {
        return notOp;
    }
    
    public void setNotOp(boolean notOp) {
        this.notOp = notOp;
    }
    
    public int getOpId() {
        return opId;
    }
    
    public void setOpId(int opId) {
        this.opId = opId;
    }
    
    public ValUnit getValUnit() {
        return valUnit;
    }
    
    public void setValUnit(ValUnit valUnit) {
        this.valUnit = valUnit;
    }
    
    public Object getVal1() {
        return val1;
    }
    
    public void setVal1(Object val1) {
        this.val1 = val1;
    }
    
    public Object getVal2() {
        return val2;
    }
    
    public void setVal2(Object val2) {
        this.val2 = val2;
    }
    
    @Override
    public String toString() {
        return "ConditionUnit{" +
                "notOp=" + notOp +
                ", opId=" + opId +
                ", valUnit=" + valUnit +
                ", val1=" + val1 +
                ", val2=" + val2 +
                '}';
    }
} 