package com.nl2sql.spider.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * ORDER BY子句的数据模型
 * 对应Python版本的 ('asc'/'desc', [val_unit1, val_unit2, ...])
 */
public class OrderByClause {
    
    @JsonProperty("orderType")
    private String orderType; // "asc" or "desc"
    
    @JsonProperty("valUnits")
    private List<ValUnit> valUnits;
    
    public OrderByClause() {}
    
    public OrderByClause(String orderType, List<ValUnit> valUnits) {
        this.orderType = orderType;
        this.valUnits = valUnits;
    }
    
    public String getOrderType() {
        return orderType;
    }
    
    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }
    
    public List<ValUnit> getValUnits() {
        return valUnits;
    }
    
    public void setValUnits(List<ValUnit> valUnits) {
        this.valUnits = valUnits;
    }
    
    @Override
    public String toString() {
        return "OrderByClause{" +
                "orderType='" + orderType + '\'' +
                ", valUnits=" + valUnits +
                '}';
    }
} 