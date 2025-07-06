package com.nl2sql.spider.utils;

import com.nl2sql.spider.constants.SqlConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SQL分词器
 * 将SQL字符串分解为tokens
 */
public class SqlTokenizer {
    
    private static final Pattern TOKEN_PATTERN = Pattern.compile(
        "\\s*(\\(|\\)|,|;|\\.|\\*|=|!=|<>|<=|>=|<|>|\\+|-|/|\\||\\b(?:SELECT|FROM|WHERE|GROUP|BY|HAVING|ORDER|LIMIT|DISTINCT|ALL|AND|OR|NOT|IN|LIKE|BETWEEN|NULL|IS|EXISTS|UNION|INTERSECT|EXCEPT|INNER|LEFT|RIGHT|FULL|OUTER|JOIN|ON|AS|ASC|DESC|COUNT|SUM|AVG|MAX|MIN)\\b|'[^']*'|\"[^\"]*\"|`[^`]*`|\\w+)\\s*",
        Pattern.CASE_INSENSITIVE
    );
    
    /**
     * 将SQL字符串分词
     * 
     * @param sql SQL字符串
     * @return token列表
     */
    public static List<String> tokenize(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        List<String> tokens = new ArrayList<>();
        Matcher matcher = TOKEN_PATTERN.matcher(sql);
        
        while (matcher.find()) {
            String token = matcher.group(1);
            if (token != null && !token.trim().isEmpty()) {
                tokens.add(token.toLowerCase());
            }
        }
        
        return tokens;
    }
    
    /**
     * 标准化SQL字符串
     * 
     * @param sql 原始SQL
     * @return 标准化后的SQL
     */
    public static String normalize(String sql) {
        if (sql == null) {
            return "";
        }
        
        // 移除多余的空格
        sql = sql.replaceAll("\\s+", " ");
        
        // 移除开头和结尾的空格
        sql = sql.trim();
        
        // 移除结尾的分号
        if (sql.endsWith(";")) {
            sql = sql.substring(0, sql.length() - 1);
        }
        
        return sql;
    }
    
    /**
     * 检查token是否为关键字
     * 
     * @param token 要检查的token
     * @return 是否为关键字
     */
    public static boolean isKeyword(String token) {
        return SqlConstants.RESERVED_WORDS.contains(token.toLowerCase());
    }
    
    /**
     * 检查token是否为聚合函数
     * 
     * @param token 要检查的token
     * @return 是否为聚合函数
     */
    public static boolean isAggregateFunction(String token) {
        return SqlConstants.AGG_OPS.contains(token.toLowerCase());
    }
    
    /**
     * 检查token是否为条件操作符
     * 
     * @param token 要检查的token
     * @return 是否为条件操作符
     */
    public static boolean isConditionOperator(String token) {
        return SqlConstants.COND_OPS.contains(token.toLowerCase());
    }
    
    /**
     * 提取SQL中的表名
     * 
     * @param tokens token列表
     * @return 表名列表
     */
    public static List<String> extractTableNames(List<String> tokens) {
        List<String> tableNames = new ArrayList<>();
        
        for (int i = 0; i < tokens.size(); i++) {
            if ("from".equals(tokens.get(i)) || "join".equals(tokens.get(i))) {
                if (i + 1 < tokens.size()) {
                    String tableName = tokens.get(i + 1);
                    if (!isKeyword(tableName) && !"(".equals(tableName)) {
                        tableNames.add(tableName);
                    }
                }
            }
        }
        
        return tableNames;
    }
    
    /**
     * 提取SQL中的列名
     * 
     * @param tokens token列表
     * @return 列名列表
     */
    public static List<String> extractColumnNames(List<String> tokens) {
        List<String> columnNames = new ArrayList<>();
        
        boolean inSelect = false;
        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            
            if ("select".equals(token)) {
                inSelect = true;
                continue;
            }
            
            if (inSelect && ("from".equals(token) || "where".equals(token) || 
                           "group".equals(token) || "order".equals(token))) {
                inSelect = false;
                continue;
            }
            
            if (inSelect && !isKeyword(token) && !",".equals(token) && 
                !"*".equals(token) && !"(".equals(token) && !")".equals(token)) {
                columnNames.add(token);
            }
        }
        
        return columnNames;
    }
} 