package com.nl2sql.spider.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * SQL结构的数据模型，对应Python版本的SQL解析结果
 * 
 * @author NL2SQL Team
 */
public class SqlStructure {
    
    @JsonProperty("select")
    private SelectClause select;
    
    @JsonProperty("from")
    private FromClause from;
    
    @JsonProperty("where")
    private List<ConditionUnit> where;
    
    @JsonProperty("groupBy")
    private List<ColUnit> groupBy;
    
    @JsonProperty("having")
    private List<ConditionUnit> having;
    
    @JsonProperty("orderBy")
    private OrderByClause orderBy;
    
    @JsonProperty("limit")
    private Integer limit;
    
    @JsonProperty("intersect")
    private SqlStructure intersect;
    
    @JsonProperty("except")
    private SqlStructure except;
    
    @JsonProperty("union")
    private SqlStructure union;
    
    // 构造函数
    public SqlStructure() {}
    
    // Getters and Setters
    public SelectClause getSelect() {
        return select;
    }
    
    public void setSelect(SelectClause select) {
        this.select = select;
    }
    
    public FromClause getFrom() {
        return from;
    }
    
    public void setFrom(FromClause from) {
        this.from = from;
    }
    
    public List<ConditionUnit> getWhere() {
        return where;
    }
    
    public void setWhere(List<ConditionUnit> where) {
        this.where = where;
    }
    
    public List<ColUnit> getGroupBy() {
        return groupBy;
    }
    
    public void setGroupBy(List<ColUnit> groupBy) {
        this.groupBy = groupBy;
    }
    
    public List<ConditionUnit> getHaving() {
        return having;
    }
    
    public void setHaving(List<ConditionUnit> having) {
        this.having = having;
    }
    
    public OrderByClause getOrderBy() {
        return orderBy;
    }
    
    public void setOrderBy(OrderByClause orderBy) {
        this.orderBy = orderBy;
    }
    
    public Integer getLimit() {
        return limit;
    }
    
    public void setLimit(Integer limit) {
        this.limit = limit;
    }
    
    public SqlStructure getIntersect() {
        return intersect;
    }
    
    public void setIntersect(SqlStructure intersect) {
        this.intersect = intersect;
    }
    
    public SqlStructure getExcept() {
        return except;
    }
    
    public void setExcept(SqlStructure except) {
        this.except = except;
    }
    
    public SqlStructure getUnion() {
        return union;
    }
    
    public void setUnion(SqlStructure union) {
        this.union = union;
    }
    
    @Override
    public String toString() {
        return "SqlStructure{" +
                "select=" + select +
                ", from=" + from +
                ", where=" + where +
                ", groupBy=" + groupBy +
                ", having=" + having +
                ", orderBy=" + orderBy +
                ", limit=" + limit +
                ", intersect=" + intersect +
                ", except=" + except +
                ", union=" + union +
                '}';
    }
} 