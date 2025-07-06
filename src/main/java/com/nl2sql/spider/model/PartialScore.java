package com.nl2sql.spider.model;

/**
 * 部分评分类
 */
public class PartialScore {
    
    private final int labelTotal;
    private final int predTotal;
    private final double accuracy;
    private final double recall;
    private final double f1;
    
    public PartialScore(int labelTotal, int predTotal, double accuracy, double recall, double f1) {
        this.labelTotal = labelTotal;
        this.predTotal = predTotal;
        this.accuracy = accuracy;
        this.recall = recall;
        this.f1 = f1;
    }
    
    public int getLabelTotal() {
        return labelTotal;
    }
    
    public int getPredTotal() {
        return predTotal;
    }
    
    public double getAccuracy() {
        return accuracy;
    }
    
    public double getRecall() {
        return recall;
    }
    
    public double getF1() {
        return f1;
    }
    
    /**
     * 计算F1分数
     */
    public static double calculateF1(double accuracy, double recall) {
        if (accuracy + recall == 0) {
            return 0.0;
        }
        return (2.0 * accuracy * recall) / (accuracy + recall);
    }
    
    /**
     * 根据计数计算分数
     */
    public static PartialScore calculateScores(int count, int predTotal, int labelTotal) {
        double accuracy = 0.0;
        double recall = 0.0;
        double f1 = 0.0;
        
        if (predTotal != labelTotal) {
            return new PartialScore(labelTotal, predTotal, 0.0, 0.0, 0.0);
        } else if (count == predTotal) {
            return new PartialScore(labelTotal, predTotal, 1.0, 1.0, 1.0);
        }
        
        return new PartialScore(labelTotal, predTotal, accuracy, recall, f1);
    }
    
    @Override
    public String toString() {
        return "PartialScore{" +
                "labelTotal=" + labelTotal +
                ", predTotal=" + predTotal +
                ", accuracy=" + accuracy +
                ", recall=" + recall +
                ", f1=" + f1 +
                '}';
    }
} 