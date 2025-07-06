package com.nl2sql.spider.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * FROM子句的数据模型
 * 对应Python版本的 {'table_units': [table_unit1, table_unit2, ...], 'conds': condition}
 */
public class FromClause {
    
    @JsonProperty("tableUnits")
    private List<TableUnit> tableUnits;
    
    @JsonProperty("conds")
    private List<ConditionUnit> conds;
    
    public FromClause() {}
    
    public FromClause(List<TableUnit> tableUnits, List<ConditionUnit> conds) {
        this.tableUnits = tableUnits;
        this.conds = conds;
    }
    
    public List<TableUnit> getTableUnits() {
        return tableUnits;
    }
    
    public void setTableUnits(List<TableUnit> tableUnits) {
        this.tableUnits = tableUnits;
    }
    
    public List<ConditionUnit> getConds() {
        return conds;
    }
    
    public void setConds(List<ConditionUnit> conds) {
        this.conds = conds;
    }
    
    @Override
    public String toString() {
        return "FromClause{" +
                "tableUnits=" + tableUnits +
                ", conds=" + conds +
                '}';
    }
} 