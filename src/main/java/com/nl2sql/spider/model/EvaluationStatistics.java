package com.nl2sql.spider.model;

import com.nl2sql.spider.enums.HardnessLevel;
import java.util.*;

/**
 * 评估统计类
 */
public class EvaluationStatistics {
    
    private final Map<HardnessLevel, LevelStatistics> levelStats;
    private int errorCount;
    
    public EvaluationStatistics() {
        this.levelStats = new HashMap<>();
        for (HardnessLevel level : HardnessLevel.values()) {
            this.levelStats.put(level, new LevelStatistics());
        }
        this.errorCount = 0;
    }
    
    /**
     * 添加评估结果
     */
    public void addResult(HardnessLevel level, EvaluationResult result) {
        LevelStatistics stats = levelStats.get(level);
        LevelStatistics allStats = levelStats.get(HardnessLevel.ALL);
        
        stats.addResult(result);
        allStats.addResult(result);
    }
    
    /**
     * 增加错误计数
     */
    public void incrementErrorCount() {
        this.errorCount++;
    }
    
    /**
     * 计算最终分数
     */
    public void calculateFinalScores() {
        for (LevelStatistics stats : levelStats.values()) {
            stats.calculateFinalScores();
        }
    }
    
    /**
     * 获取指定级别的统计
     */
    public LevelStatistics getLevelStatistics(HardnessLevel level) {
        return levelStats.get(level);
    }
    
    /**
     * 获取所有级别的统计
     */
    public Map<HardnessLevel, LevelStatistics> getAllStatistics() {
        return Collections.unmodifiableMap(levelStats);
    }
    
    /**
     * 获取错误计数
     */
    public int getErrorCount() {
        return errorCount;
    }
    
    /**
     * 打印统计结果
     */
    public void printResults() {
        System.out.println("=".repeat(80));
        System.out.println("SPIDER EVALUATION RESULTS");
        System.out.println("=".repeat(80));
        
        // 打印表头
        System.out.printf("%-20s %-10s %-15s %-15s %-15s%n", 
                         "Level", "Count", "Exact Match", "Execution", "Avg F1");
        System.out.println("-".repeat(80));
        
        // 打印各级别结果
        for (HardnessLevel level : Arrays.asList(HardnessLevel.EASY, HardnessLevel.MEDIUM, 
                                                HardnessLevel.HARD, HardnessLevel.EXTRA, HardnessLevel.ALL)) {
            LevelStatistics stats = levelStats.get(level);
            System.out.printf("%-20s %-10d %-15.3f %-15.3f %-15.3f%n",
                             level.getValue(),
                             stats.getCount(),
                             stats.getExactMatchScore(),
                             stats.getExecutionScore(),
                             stats.getAverageF1());
        }
        
        System.out.println("-".repeat(80));
        System.out.printf("Errors: %d%n", errorCount);
    }
    
    /**
     * 级别统计类
     */
    public static class LevelStatistics {
        private int count;
        private int exactMatches;
        private int executionMatches;
        private final Map<String, Double> partialScoreSum;
        private final Map<String, Integer> partialScoreCount;
        
        public LevelStatistics() {
            this.count = 0;
            this.exactMatches = 0;
            this.executionMatches = 0;
            this.partialScoreSum = new HashMap<>();
            this.partialScoreCount = new HashMap<>();
        }
        
        public void addResult(EvaluationResult result) {
            count++;
            
            if (result.isExactMatch()) {
                exactMatches++;
            }
            
            if (result.isExecutionMatch()) {
                executionMatches++;
            }
            
            // 累加部分分数
            for (Map.Entry<String, PartialScore> entry : result.getPartialScores().entrySet()) {
                String component = entry.getKey();
                PartialScore score = entry.getValue();
                
                partialScoreSum.merge(component, score.getF1(), Double::sum);
                partialScoreCount.merge(component, 1, Integer::sum);
            }
        }
        
        public void calculateFinalScores() {
            // 计算平均分数
            for (String component : partialScoreSum.keySet()) {
                double sum = partialScoreSum.get(component);
                int count = partialScoreCount.get(component);
                if (count > 0) {
                    partialScoreSum.put(component, sum / count);
                }
            }
        }
        
        public int getCount() { return count; }
        
        public double getExactMatchScore() {
            return count > 0 ? (double) exactMatches / count : 0.0;
        }
        
        public double getExecutionScore() {
            return count > 0 ? (double) executionMatches / count : 0.0;
        }
        
        public double getAverageF1() {
            if (partialScoreSum.isEmpty()) return 0.0;
            
            double sum = partialScoreSum.values().stream().mapToDouble(Double::doubleValue).sum();
            return sum / partialScoreSum.size();
        }
        
        public Map<String, Double> getPartialScores() {
            return Collections.unmodifiableMap(partialScoreSum);
        }
    }
} 