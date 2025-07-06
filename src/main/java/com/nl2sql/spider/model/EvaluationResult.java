package com.nl2sql.spider.model;

import java.util.Map;

/**
 * 评估结果类
 */
public class EvaluationResult {
    
    private final Map<String, PartialScore> partialScores;
    private final boolean exactMatch;
    private final boolean executionMatch;
    
    public EvaluationResult(Map<String, PartialScore> partialScores) {
        this.partialScores = partialScores;
        this.exactMatch = calculateExactMatch();
        this.executionMatch = false; // 默认值
    }
    
    public EvaluationResult(Map<String, PartialScore> partialScores, boolean exactMatch, boolean executionMatch) {
        this.partialScores = partialScores;
        this.exactMatch = exactMatch;
        this.executionMatch = executionMatch;
    }
    
    public Map<String, PartialScore> getPartialScores() {
        return partialScores;
    }
    
    public boolean isExactMatch() {
        return exactMatch;
    }
    
    public boolean isExecutionMatch() {
        return executionMatch;
    }
    
    private boolean calculateExactMatch() {
        if (partialScores == null || partialScores.isEmpty()) {
            return false;
        }
        
        for (PartialScore score : partialScores.values()) {
            if (score.getF1() != 1.0) {
                return false;
            }
        }
        
        return true;
    }
    
    @Override
    public String toString() {
        return "EvaluationResult{" +
                "partialScores=" + partialScores +
                ", exactMatch=" + exactMatch +
                ", executionMatch=" + executionMatch +
                '}';
    }
} 