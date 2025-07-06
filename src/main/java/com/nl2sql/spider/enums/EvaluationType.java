package com.nl2sql.spider.enums;

/**
 * 评估类型枚举
 */
public enum EvaluationType {
    MATCH("match"),
    EXEC("exec"),
    ALL("all");
    
    private final String value;
    
    EvaluationType(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public static EvaluationType fromValue(String value) {
        for (EvaluationType type : EvaluationType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown evaluation type: " + value);
    }
} 