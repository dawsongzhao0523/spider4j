package com.nl2sql.spider.parser;

import com.nl2sql.spider.constants.SqlConstants;
import com.nl2sql.spider.model.*;
import com.nl2sql.spider.utils.SqlTokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * SQL解析器
 * 对应Python版本的process_sql.py
 */
public class SqlParser {
    
    private static final Logger logger = LoggerFactory.getLogger(SqlParser.class);
    
    /**
     * 解析SQL字符串为结构化对象
     * 
     * @param schema 数据库schema
     * @param query SQL查询字符串
     * @return 解析后的SQL结构
     */
    public SqlStructure parseSql(DatabaseSchema schema, String query) {
        try {
            List<String> tokens = SqlTokenizer.tokenize(query);
            Map<String, String> tablesWithAlias = getTablesWithAlias(schema, tokens);
            
            return parseSql(tokens, 0, tablesWithAlias, schema).getRight();
        } catch (Exception e) {
            logger.error("Failed to parse SQL: {}", query, e);
            throw new RuntimeException("SQL parsing failed", e);
        }
    }
    
    /**
     * 获取表名和别名的映射
     */
    private Map<String, String> getTablesWithAlias(DatabaseSchema schema, List<String> tokens) {
        Map<String, String> tables = scanAlias(tokens);
        
        // 添加所有表名
        for (String tableName : schema.getTableNames()) {
            if (!tables.containsKey(tableName)) {
                tables.put(tableName, tableName);
            }
        }
        
        return tables;
    }
    
    /**
     * 扫描别名
     */
    private Map<String, String> scanAlias(List<String> tokens) {
        Map<String, String> alias = new HashMap<>();
        
        for (int i = 0; i < tokens.size(); i++) {
            if ("as".equals(tokens.get(i)) && i > 0 && i < tokens.size() - 1) {
                alias.put(tokens.get(i + 1), tokens.get(i - 1));
            }
        }
        
        return alias;
    }
    
    /**
     * 解析SQL结构
     */
    private Pair<Integer, SqlStructure> parseSql(List<String> tokens, int startIdx, 
                                                 Map<String, String> tablesWithAlias, 
                                                 DatabaseSchema schema) {
        boolean isBlock = false;
        int idx = startIdx;
        
        SqlStructure sql = new SqlStructure();
        
        if (idx < tokens.size() && "(".equals(tokens.get(idx))) {
            isBlock = true;
            idx++;
        }
        
        // 解析FROM子句以获取默认表
        Triple<Integer, List<TableUnit>, List<ConditionUnit>> fromResult = 
            parseFrom(tokens, startIdx, tablesWithAlias, schema);
        int fromEndIdx = fromResult.getLeft();
        List<TableUnit> tableUnits = fromResult.getMiddle();
        List<ConditionUnit> conds = fromResult.getRight();
        
        sql.setFrom(new FromClause(tableUnits, conds));
        
        // 获取默认表名列表
        List<String> defaultTables = getDefaultTableNames(tableUnits);
        
        // 解析SELECT子句
        Pair<Integer, SelectClause> selectResult = parseSelect(tokens, idx, tablesWithAlias, schema, defaultTables);
        sql.setSelect(selectResult.getRight());
        
        idx = fromEndIdx;
        
        // 解析WHERE子句
        Pair<Integer, List<ConditionUnit>> whereResult = parseWhere(tokens, idx, tablesWithAlias, schema, defaultTables);
        idx = whereResult.getLeft();
        sql.setWhere(whereResult.getRight());
        
        // 解析GROUP BY子句
        Pair<Integer, List<ColUnit>> groupByResult = parseGroupBy(tokens, idx, tablesWithAlias, schema, defaultTables);
        idx = groupByResult.getLeft();
        sql.setGroupBy(groupByResult.getRight());
        
        // 解析HAVING子句
        Pair<Integer, List<ConditionUnit>> havingResult = parseHaving(tokens, idx, tablesWithAlias, schema, defaultTables);
        idx = havingResult.getLeft();
        sql.setHaving(havingResult.getRight());
        
        // 解析ORDER BY子句
        Pair<Integer, OrderByClause> orderByResult = parseOrderBy(tokens, idx, tablesWithAlias, schema, defaultTables);
        idx = orderByResult.getLeft();
        sql.setOrderBy(orderByResult.getRight());
        
        // 解析LIMIT子句
        Pair<Integer, Integer> limitResult = parseLimit(tokens, idx);
        idx = limitResult.getLeft();
        sql.setLimit(limitResult.getRight());
        
        idx = skipSemicolon(tokens, idx);
        
        if (isBlock && idx < tokens.size() && ")".equals(tokens.get(idx))) {
            idx++;
        }
        
        idx = skipSemicolon(tokens, idx);
        
        // 解析INTERSECT/UNION/EXCEPT子句
        if (idx < tokens.size() && SqlConstants.SQL_OPS.contains(tokens.get(idx))) {
            String sqlOp = tokens.get(idx);
            idx++;
            Pair<Integer, SqlStructure> iueResult = parseSql(tokens, idx, tablesWithAlias, schema);
            idx = iueResult.getLeft();
            
            switch (sqlOp) {
                case "intersect":
                    sql.setIntersect(iueResult.getRight());
                    break;
                case "union":
                    sql.setUnion(iueResult.getRight());
                    break;
                case "except":
                    sql.setExcept(iueResult.getRight());
                    break;
            }
        }
        
        return new Pair<>(idx, sql);
    }
    
    /**
     * 解析SELECT子句
     */
    private Pair<Integer, SelectClause> parseSelect(List<String> tokens, int startIdx, 
                                                   Map<String, String> tablesWithAlias, 
                                                   DatabaseSchema schema, 
                                                   List<String> defaultTables) {
        int idx = startIdx;
        
        if (idx >= tokens.size() || !"select".equals(tokens.get(idx))) {
            throw new RuntimeException("'select' not found");
        }
        
        idx++;
        boolean isDistinct = false;
        
        if (idx < tokens.size() && "distinct".equals(tokens.get(idx))) {
            idx++;
            isDistinct = true;
        }
        
        List<SelectClause.SelectItem> selectItems = new ArrayList<>();
        
        while (idx < tokens.size() && !SqlConstants.CLAUSE_KEYWORDS.contains(tokens.get(idx))) {
            int aggId = SqlConstants.AGG_OPS.indexOf("none");
            
            if (idx < tokens.size() && SqlConstants.AGG_OPS.contains(tokens.get(idx))) {
                aggId = SqlConstants.AGG_OPS.indexOf(tokens.get(idx));
                idx++;
            }
            
            Pair<Integer, ValUnit> valUnitResult = parseValUnit(tokens, idx, tablesWithAlias, schema, defaultTables);
            idx = valUnitResult.getLeft();
            
            selectItems.add(new SelectClause.SelectItem(aggId, valUnitResult.getRight()));
            
            if (idx < tokens.size() && ",".equals(tokens.get(idx))) {
                idx++;
            }
        }
        
        return new Pair<>(idx, new SelectClause(isDistinct, selectItems));
    }
    
    /**
     * 解析FROM子句
     */
    private Triple<Integer, List<TableUnit>, List<ConditionUnit>> parseFrom(List<String> tokens, int startIdx, 
                                                                           Map<String, String> tablesWithAlias, 
                                                                           DatabaseSchema schema) {
        int fromIdx = -1;
        for (int i = startIdx; i < tokens.size(); i++) {
            if ("from".equals(tokens.get(i))) {
                fromIdx = i;
                break;
            }
        }
        
        if (fromIdx == -1) {
            throw new RuntimeException("'from' not found");
        }
        
        int idx = fromIdx + 1;
        List<TableUnit> tableUnits = new ArrayList<>();
        List<ConditionUnit> conds = new ArrayList<>();
        
        while (idx < tokens.size()) {
            boolean isBlock = false;
            
            if (idx < tokens.size() && "(".equals(tokens.get(idx))) {
                isBlock = true;
                idx++;
            }
            
            if (idx < tokens.size() && "select".equals(tokens.get(idx))) {
                Pair<Integer, SqlStructure> sqlResult = parseSql(tokens, idx, tablesWithAlias, schema);
                idx = sqlResult.getLeft();
                tableUnits.add(new TableUnit(SqlConstants.TABLE_TYPE_SQL, sqlResult.getRight()));
            } else {
                if (idx < tokens.size() && "join".equals(tokens.get(idx))) {
                    idx++;
                }
                
                Triple<Integer, String, String> tableResult = parseTableUnit(tokens, idx, tablesWithAlias, schema);
                idx = tableResult.getLeft();
                tableUnits.add(new TableUnit(SqlConstants.TABLE_TYPE_TABLE_UNIT, tableResult.getMiddle()));
            }
            
            if (idx < tokens.size() && "on".equals(tokens.get(idx))) {
                idx++;
                // 解析JOIN条件
                // TODO: 实现条件解析
            }
            
            if (isBlock && idx < tokens.size() && ")".equals(tokens.get(idx))) {
                idx++;
            }
            
            if (idx < tokens.size() && (SqlConstants.CLAUSE_KEYWORDS.contains(tokens.get(idx)) || 
                                      ")".equals(tokens.get(idx)) || ";".equals(tokens.get(idx)))) {
                break;
            }
        }
        
        return new Triple<>(idx, tableUnits, conds);
    }
    
    // 其他解析方法的简化实现
    
    private Pair<Integer, List<ConditionUnit>> parseWhere(List<String> tokens, int startIdx, 
                                                         Map<String, String> tablesWithAlias, 
                                                         DatabaseSchema schema, 
                                                         List<String> defaultTables) {
        // 简化实现
        return new Pair<>(startIdx, new ArrayList<>());
    }
    
    private Pair<Integer, List<ColUnit>> parseGroupBy(List<String> tokens, int startIdx, 
                                                     Map<String, String> tablesWithAlias, 
                                                     DatabaseSchema schema, 
                                                     List<String> defaultTables) {
        // 简化实现
        return new Pair<>(startIdx, new ArrayList<>());
    }
    
    private Pair<Integer, List<ConditionUnit>> parseHaving(List<String> tokens, int startIdx, 
                                                          Map<String, String> tablesWithAlias, 
                                                          DatabaseSchema schema, 
                                                          List<String> defaultTables) {
        // 简化实现
        return new Pair<>(startIdx, new ArrayList<>());
    }
    
    private Pair<Integer, OrderByClause> parseOrderBy(List<String> tokens, int startIdx, 
                                                     Map<String, String> tablesWithAlias, 
                                                     DatabaseSchema schema, 
                                                     List<String> defaultTables) {
        // 简化实现
        return new Pair<>(startIdx, null);
    }
    
    private Pair<Integer, Integer> parseLimit(List<String> tokens, int startIdx) {
        int idx = startIdx;
        
        if (idx < tokens.size() && "limit".equals(tokens.get(idx))) {
            idx += 2;
            return new Pair<>(idx, Integer.parseInt(tokens.get(idx - 1)));
        }
        
        return new Pair<>(idx, null);
    }
    
    private Pair<Integer, ValUnit> parseValUnit(List<String> tokens, int startIdx, 
                                               Map<String, String> tablesWithAlias, 
                                               DatabaseSchema schema, 
                                               List<String> defaultTables) {
        // 简化实现
        return new Pair<>(startIdx + 1, new ValUnit());
    }
    
    private Triple<Integer, String, String> parseTableUnit(List<String> tokens, int startIdx, 
                                                          Map<String, String> tablesWithAlias, 
                                                          DatabaseSchema schema) {
        int idx = startIdx;
        String tableId = tablesWithAlias.get(tokens.get(idx));
        String tableName = tokens.get(idx);
        
        if (idx + 1 < tokens.size() && "as".equals(tokens.get(idx + 1))) {
            idx += 3;
        } else {
            idx += 1;
        }
        
        return new Triple<>(idx, tableId, tableName);
    }
    
    private List<String> getDefaultTableNames(List<TableUnit> tableUnits) {
        List<String> defaultTables = new ArrayList<>();
        for (TableUnit unit : tableUnits) {
            if (SqlConstants.TABLE_TYPE_TABLE_UNIT.equals(unit.getTableType())) {
                defaultTables.add(unit.getTableId());
            }
        }
        return defaultTables;
    }
    
    private int skipSemicolon(List<String> tokens, int startIdx) {
        int idx = startIdx;
        if (idx < tokens.size() && ";".equals(tokens.get(idx))) {
            idx++;
        }
        return idx;
    }
    
    // 辅助类
    public static class Pair<L, R> {
        private final L left;
        private final R right;
        
        public Pair(L left, R right) {
            this.left = left;
            this.right = right;
        }
        
        public L getLeft() { return left; }
        public R getRight() { return right; }
    }
    
    public static class Triple<L, M, R> {
        private final L left;
        private final M middle;
        private final R right;
        
        public Triple(L left, M middle, R right) {
            this.left = left;
            this.middle = middle;
            this.right = right;
        }
        
        public L getLeft() { return left; }
        public M getMiddle() { return middle; }
        public R getRight() { return right; }
    }
} 