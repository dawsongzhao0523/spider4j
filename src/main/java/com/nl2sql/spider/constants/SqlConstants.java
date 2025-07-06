package com.nl2sql.spider.constants;

import java.util.Arrays;
import java.util.List;

/**
 * SQL常量定义
 * 对应Python版本的常量定义
 */
public class SqlConstants {
    
    // 聚合操作符
    public static final List<String> AGG_OPS = Arrays.asList(
        "none", "max", "min", "count", "sum", "avg"
    );
    
    // 条件操作符
    public static final List<String> COND_OPS = Arrays.asList(
        "=", ">", "<", ">=", "<=", "!=", "in", "like", "is", "exists"
    );
    
    // 单元操作符
    public static final List<String> UNIT_OPS = Arrays.asList(
        "none", "-", "+", "*", "/"
    );
    
    // SQL操作符
    public static final List<String> SQL_OPS = Arrays.asList(
        "intersect", "union", "except"
    );
    
    // 关键字
    public static final List<String> CLAUSE_KEYWORDS = Arrays.asList(
        "select", "from", "where", "group", "order", "limit", "intersect", "union", "except"
    );
    
    // JOIN关键字
    public static final List<String> JOIN_KEYWORDS = Arrays.asList(
        "join", "inner", "left", "right", "full", "outer"
    );
    
    // 表类型
    public static final String TABLE_TYPE_TABLE_UNIT = "table_unit";
    public static final String TABLE_TYPE_SQL = "sql";
    
    // 值单元类型
    public static final String UNIT_TYPE_COLUMN = "column";
    public static final String UNIT_TYPE_NUMBER = "number";
    public static final String UNIT_TYPE_STRING = "string";
    public static final String UNIT_TYPE_SQL = "sql";
    
    // 难度级别阈值
    public static final int HARDNESS_EASY_THRESHOLD = 1;
    public static final int HARDNESS_MEDIUM_THRESHOLD = 2;
    public static final int HARDNESS_HARD_THRESHOLD = 3;
    
    // 保留字
    public static final List<String> RESERVED_WORDS = Arrays.asList(
        "select", "from", "where", "group", "by", "having", "order", "limit",
        "distinct", "all", "and", "or", "not", "in", "like", "between",
        "null", "is", "exists", "union", "intersect", "except",
        "inner", "left", "right", "full", "outer", "join", "on",
        "as", "asc", "desc", "count", "sum", "avg", "max", "min"
    );
    
    // 数据类型
    public static final List<String> DATA_TYPES = Arrays.asList(
        "text", "number", "time", "boolean", "others"
    );
    
    private SqlConstants() {
        // 私有构造函数，防止实例化
    }
} 