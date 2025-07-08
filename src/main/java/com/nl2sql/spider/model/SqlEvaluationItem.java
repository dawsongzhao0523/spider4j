package com.nl2sql.spider.model;

/**
 * SQL评估项
 * 包含标准答案SQL、预测SQL和数据库ID的完整评估项
 */
public class SqlEvaluationItem {
    
    private String goldSql;      // 标准答案SQL
    private String predictionSql; // 预测SQL
    private String dbId;         // 数据库ID
    private String question;     // 可选：自然语言问题
    private String difficulty;   // 可选：难度级别
    
    public SqlEvaluationItem() {}
    
    public SqlEvaluationItem(String goldSql, String predictionSql, String dbId) {
        this.goldSql = goldSql;
        this.predictionSql = predictionSql;
        this.dbId = dbId;
    }
    
    public SqlEvaluationItem(String goldSql, String predictionSql, String dbId, String question) {
        this.goldSql = goldSql;
        this.predictionSql = predictionSql;
        this.dbId = dbId;
        this.question = question;
    }
    
    public SqlEvaluationItem(String goldSql, String predictionSql, String dbId, String question, String difficulty) {
        this.goldSql = goldSql;
        this.predictionSql = predictionSql;
        this.dbId = dbId;
        this.question = question;
        this.difficulty = difficulty;
    }
    
    public String getGoldSql() {
        return goldSql;
    }
    
    public void setGoldSql(String goldSql) {
        this.goldSql = goldSql;
    }
    
    public String getPredictionSql() {
        return predictionSql;
    }
    
    public void setPredictionSql(String predictionSql) {
        this.predictionSql = predictionSql;
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
    
    public String getDifficulty() {
        return difficulty;
    }
    
    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }
    
    @Override
    public String toString() {
        return "SqlEvaluationItem{" +
                "goldSql='" + goldSql + '\'' +
                ", predictionSql='" + predictionSql + '\'' +
                ", dbId='" + dbId + '\'' +
                ", question='" + question + '\'' +
                ", difficulty='" + difficulty + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        SqlEvaluationItem that = (SqlEvaluationItem) o;
        
        if (goldSql != null ? !goldSql.equals(that.goldSql) : that.goldSql != null) return false;
        if (predictionSql != null ? !predictionSql.equals(that.predictionSql) : that.predictionSql != null) return false;
        if (dbId != null ? !dbId.equals(that.dbId) : that.dbId != null) return false;
        if (question != null ? !question.equals(that.question) : that.question != null) return false;
        return difficulty != null ? difficulty.equals(that.difficulty) : that.difficulty == null;
    }
    
    @Override
    public int hashCode() {
        int result = goldSql != null ? goldSql.hashCode() : 0;
        result = 31 * result + (predictionSql != null ? predictionSql.hashCode() : 0);
        result = 31 * result + (dbId != null ? dbId.hashCode() : 0);
        result = 31 * result + (question != null ? question.hashCode() : 0);
        result = 31 * result + (difficulty != null ? difficulty.hashCode() : 0);
        return result;
    }
} 