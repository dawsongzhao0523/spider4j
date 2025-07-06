package com.nl2sql.spider.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * SELECT子句的数据模型
 * 对应Python版本的 (isDistinct, [(agg_id, val_unit), ...])
 */
public class SelectClause {
    
    @JsonProperty("isDistinct")
    private boolean isDistinct;
    
    @JsonProperty("selectItems")
    private List<SelectItem> selectItems;
    
    public SelectClause() {}
    
    public SelectClause(boolean isDistinct, List<SelectItem> selectItems) {
        this.isDistinct = isDistinct;
        this.selectItems = selectItems;
    }
    
    public boolean isDistinct() {
        return isDistinct;
    }
    
    public void setDistinct(boolean distinct) {
        isDistinct = distinct;
    }
    
    public List<SelectItem> getSelectItems() {
        return selectItems;
    }
    
    public void setSelectItems(List<SelectItem> selectItems) {
        this.selectItems = selectItems;
    }
    
    @Override
    public String toString() {
        return "SelectClause{" +
                "isDistinct=" + isDistinct +
                ", selectItems=" + selectItems +
                '}';
    }
    
    /**
     * SELECT项的数据模型
     */
    public static class SelectItem {
        @JsonProperty("aggId")
        private int aggId;
        
        @JsonProperty("valUnit")
        private ValUnit valUnit;
        
        public SelectItem() {}
        
        public SelectItem(int aggId, ValUnit valUnit) {
            this.aggId = aggId;
            this.valUnit = valUnit;
        }
        
        public int getAggId() {
            return aggId;
        }
        
        public void setAggId(int aggId) {
            this.aggId = aggId;
        }
        
        public ValUnit getValUnit() {
            return valUnit;
        }
        
        public void setValUnit(ValUnit valUnit) {
            this.valUnit = valUnit;
        }
        
        @Override
        public String toString() {
            return "SelectItem{" +
                    "aggId=" + aggId +
                    ", valUnit=" + valUnit +
                    '}';
        }
    }
} 