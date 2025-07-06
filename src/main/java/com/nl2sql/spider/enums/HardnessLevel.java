package com.nl2sql.spider.enums;

/**
 * 难度级别枚举
 */
public enum HardnessLevel {
    EASY("easy"),
    MEDIUM("medium"),
    HARD("hard"),
    EXTRA("extra"),
    ALL("all");
    
    private final String value;
    
    HardnessLevel(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public static HardnessLevel fromValue(String value) {
        for (HardnessLevel level : HardnessLevel.values()) {
            if (level.value.equals(value)) {
                return level;
            }
        }
        throw new IllegalArgumentException("Unknown hardness level: " + value);
    }
} 