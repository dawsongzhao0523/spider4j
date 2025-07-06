package com.nl2sql.spider.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Spider数据项的模型
 * 对应train.json和dev.json中的数据格式
 */
public class SpiderDataItem {
    
    @JsonProperty("db_id")
    private String dbId;
    
    @JsonProperty("question")
    private String question;
    
    @JsonProperty("question_toks")
    private List<String> questionToks;
    
    @JsonProperty("query")
    private String query;
    
    @JsonProperty("query_toks")
    private List<String> queryToks;
    
    @JsonProperty("sql")
    private SqlStructure sql;
    
    public SpiderDataItem() {}
    
    public SpiderDataItem(String dbId, String question, String query) {
        this.dbId = dbId;
        this.question = question;
        this.query = query;
    }
    
    public String getDbId() {
        return dbId;
    }
    
    public void setDbId(String dbId) {
        this.dbId = dbId;
    }
    
    public String getQuestion() {
        return question;
    }
    
    public void setQuestion(String question) {
        this.question = question;
    }
    
    public List<String> getQuestionToks() {
        return questionToks;
    }
    
    public void setQuestionToks(List<String> questionToks) {
        this.questionToks = questionToks;
    }
    
    public String getQuery() {
        return query;
    }
    
    public void setQuery(String query) {
        this.query = query;
    }
    
    public List<String> getQueryToks() {
        return queryToks;
    }
    
    public void setQueryToks(List<String> queryToks) {
        this.queryToks = queryToks;
    }
    
    public SqlStructure getSql() {
        return sql;
    }
    
    public void setSql(SqlStructure sql) {
        this.sql = sql;
    }
    
    @Override
    public String toString() {
        return "SpiderDataItem{" +
                "dbId='" + dbId + '\'' +
                ", question='" + question + '\'' +
                ", query='" + query + '\'' +
                ", questionToks=" + questionToks +
                ", queryToks=" + queryToks +
                ", sql=" + sql +
                '}';
    }
} 