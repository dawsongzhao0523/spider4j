package com.nl2sql.spider.evaluator;

import com.nl2sql.spider.config.DatabaseConfig;
import com.nl2sql.spider.constants.SqlConstants;
import com.nl2sql.spider.enums.HardnessLevel;
import com.nl2sql.spider.model.*;
import com.nl2sql.spider.parser.SqlParser;
import com.nl2sql.spider.utils.DatabaseConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Spider评估器
 * 对应Python版本的evaluation.py
 */
public class SpiderEvaluator {
    
    private static final Logger logger = LoggerFactory.getLogger(SpiderEvaluator.class);
    
    private final SqlParser sqlParser;
    
    public SpiderEvaluator() {
        this.sqlParser = new SqlParser();
    }
    
    /**
     * 评估SQL难度
     * 
     * @param sql SQL结构
     * @return 难度级别
     */
    public HardnessLevel evaluateHardness(SqlStructure sql) {
        int component1Count = countComponent1(sql);
        int component2Count = countComponent2(sql);
        int othersCount = countOthers(sql);
        
        if (component1Count <= 1 && othersCount == 0 && component2Count == 0) {
            return HardnessLevel.EASY;
        } else if ((othersCount <= 2 && component1Count <= 1 && component2Count == 0) ||
                   (component1Count <= 2 && othersCount < 2 && component2Count == 0)) {
            return HardnessLevel.MEDIUM;
        } else if ((othersCount > 2 && component1Count <= 2 && component2Count == 0) ||
                   (2 < component1Count && component1Count <= 3 && othersCount <= 2 && component2Count == 0) ||
                   (component1Count <= 1 && othersCount == 0 && component2Count <= 1)) {
            return HardnessLevel.HARD;
        } else {
            return HardnessLevel.EXTRA;
        }
    }
    
    /**
     * 评估精确匹配
     * 
     * @param predicted 预测的SQL结构
     * @param gold 标准SQL结构
     * @return 是否精确匹配
     */
    public boolean evaluateExactMatch(SqlStructure predicted, SqlStructure gold) {
        EvaluationResult partialResult = evaluatePartialMatch(predicted, gold);
        
        // 检查所有部分是否都完全匹配
        for (PartialScore score : partialResult.getPartialScores().values()) {
            if (score.getF1() != 1.0) {
                return false;
            }
        }
        
        // 检查FROM子句中的表单元
        if (gold.getFrom() != null && gold.getFrom().getTableUnits() != null && 
            !gold.getFrom().getTableUnits().isEmpty()) {
            
            List<TableUnit> goldTables = new ArrayList<>(gold.getFrom().getTableUnits());
            List<TableUnit> predTables = predicted.getFrom() != null ? 
                new ArrayList<>(predicted.getFrom().getTableUnits()) : new ArrayList<>();
            
            goldTables.sort(Comparator.comparing(TableUnit::getTableId));
            predTables.sort(Comparator.comparing(TableUnit::getTableId));
            
            return goldTables.equals(predTables);
        }
        
        return true;
    }
    
    /**
     * 评估部分匹配
     * 
     * @param predicted 预测的SQL结构
     * @param gold 标准SQL结构
     * @return 评估结果
     */
    public EvaluationResult evaluatePartialMatch(SqlStructure predicted, SqlStructure gold) {
        Map<String, PartialScore> partialScores = new HashMap<>();
        
        // 评估SELECT子句
        PartialScore selectScore = evaluateSelect(predicted, gold);
        partialScores.put("select", selectScore);
        
        PartialScore selectNoAggScore = evaluateSelectNoAgg(predicted, gold);
        partialScores.put("select(no AGG)", selectNoAggScore);
        
        // 评估WHERE子句
        PartialScore whereScore = evaluateWhere(predicted, gold);
        partialScores.put("where", whereScore);
        
        PartialScore whereNoOpScore = evaluateWhereNoOp(predicted, gold);
        partialScores.put("where(no OP)", whereNoOpScore);
        
        // 评估GROUP BY子句
        PartialScore groupScore = evaluateGroup(predicted, gold);
        partialScores.put("group(no Having)", groupScore);
        
        PartialScore groupHavingScore = evaluateGroupHaving(predicted, gold);
        partialScores.put("group", groupHavingScore);
        
        // 评估ORDER BY子句
        PartialScore orderScore = evaluateOrder(predicted, gold);
        partialScores.put("order", orderScore);
        
        // 评估AND/OR
        PartialScore andOrScore = evaluateAndOr(predicted, gold);
        partialScores.put("and/or", andOrScore);
        
        // 评估IUEN (INTERSECT/UNION/EXCEPT/NESTED)
        PartialScore iuenScore = evaluateIUEN(predicted, gold);
        partialScores.put("IUEN", iuenScore);
        
        // 评估关键字
        PartialScore keywordsScore = evaluateKeywords(predicted, gold);
        partialScores.put("keywords", keywordsScore);
        
        return new EvaluationResult(partialScores);
    }
    
    /**
     * 评估执行准确性
     * 
     * @param dbPath 数据库路径
     * @param predictedSql 预测的SQL
     * @param goldSql 标准SQL
     * @return 是否执行结果相同
     */
    public boolean evaluateExecution(String dbPath, String predictedSql, String goldSql) {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath)) {
            return executeAndCompare(conn, predictedSql, goldSql);
        } catch (SQLException e) {
            logger.error("Failed to evaluate execution for SQL: {} vs {}", predictedSql, goldSql, e);
            return false;
        }
    }
    
    /**
     * 评估执行准确性（使用数据库配置）
     * 
     * @param config 数据库配置
     * @param predictedSql 预测的SQL
     * @param goldSql 标准SQL
     * @return 是否执行结果相同
     */
    public boolean evaluateExecution(DatabaseConfig config, String predictedSql, String goldSql) {
        try (Connection conn = DatabaseConnectionManager.createConnection(config)) {
            return executeAndCompare(conn, predictedSql, goldSql);
        } catch (SQLException e) {
            logger.error("Failed to evaluate execution for SQL: {} vs {} using config: {}", 
                        predictedSql, goldSql, config, e);
            return false;
        }
    }
    
    /**
     * 评估执行准确性（使用现有连接）
     * 
     * @param conn 数据库连接
     * @param predictedSql 预测的SQL
     * @param goldSql 标准SQL
     * @return 是否执行结果相同
     */
    public boolean evaluateExecution(Connection conn, String predictedSql, String goldSql) {
        return executeAndCompare(conn, predictedSql, goldSql);
    }
    
    /**
     * 验证SQL是否有效
     * 
     * @param dbPath 数据库路径
     * @param sql SQL字符串
     * @return 是否有效
     */
    public boolean isValidSql(String dbPath, String sql) {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
             Statement stmt = conn.createStatement()) {
            
            stmt.executeQuery(sql);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
    
    /**
     * 验证SQL是否有效（使用数据库配置）
     * 
     * @param config 数据库配置
     * @param sql SQL字符串
     * @return 是否有效
     */
    public boolean isValidSql(DatabaseConfig config, String sql) {
        try (Connection conn = DatabaseConnectionManager.createConnection(config);
             Statement stmt = conn.createStatement()) {
            
            stmt.executeQuery(sql);
            return true;
        } catch (SQLException e) {
            logger.debug("SQL validation failed for: {} using config: {}", sql, config);
            return false;
        }
    }
    
    /**
     * 验证SQL是否有效（使用现有连接）
     * 
     * @param conn 数据库连接
     * @param sql SQL字符串
     * @return 是否有效
     */
    public boolean isValidSql(Connection conn, String sql) {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeQuery(sql);
            return true;
        } catch (SQLException e) {
            logger.debug("SQL validation failed for: {}", sql);
            return false;
        }
    }
    
    // 私有辅助方法
    
    private int countComponent1(SqlStructure sql) {
        int count = 0;
        
        if (sql.getWhere() != null && !sql.getWhere().isEmpty()) {
            count++;
        }
        if (sql.getGroupBy() != null && !sql.getGroupBy().isEmpty()) {
            count++;
        }
        if (sql.getOrderBy() != null) {
            count++;
        }
        if (sql.getLimit() != null) {
            count++;
        }
        
        // JOIN计数
        if (sql.getFrom() != null && sql.getFrom().getTableUnits() != null) {
            count += Math.max(0, sql.getFrom().getTableUnits().size() - 1);
        }
        
        // OR和LIKE计数
        count += countOrAndLike(sql);
        
        return count;
    }
    
    private int countComponent2(SqlStructure sql) {
        int count = 0;
        
        if (sql.getIntersect() != null) count++;
        if (sql.getUnion() != null) count++;
        if (sql.getExcept() != null) count++;
        
        // 嵌套SQL计数
        count += countNestedSql(sql);
        
        return count;
    }
    
    private int countOthers(SqlStructure sql) {
        int count = 0;
        
        // 聚合函数数量
        int aggCount = countAggregations(sql);
        if (aggCount > 1) count++;
        
        // SELECT列数量
        if (sql.getSelect() != null && sql.getSelect().getSelectItems() != null &&
            sql.getSelect().getSelectItems().size() > 1) {
            count++;
        }
        
        // WHERE条件数量
        if (sql.getWhere() != null && sql.getWhere().size() > 1) {
            count++;
        }
        
        // GROUP BY子句数量
        if (sql.getGroupBy() != null && sql.getGroupBy().size() > 1) {
            count++;
        }
        
        return count;
    }
    
    private int countOrAndLike(SqlStructure sql) {
        int count = 0;
        
        // 检查WHERE条件中的LIKE
        if (sql.getWhere() != null) {
            for (ConditionUnit cond : sql.getWhere()) {
                if (cond.getOpId() >= 0 && cond.getOpId() < SqlConstants.COND_OPS.size() && 
                    SqlConstants.COND_OPS.get(cond.getOpId()).equals("like")) {
                    count++;
                }
            }
        }
        
        // 检查HAVING条件中的LIKE
        if (sql.getHaving() != null) {
            for (ConditionUnit cond : sql.getHaving()) {
                if (cond.getOpId() >= 0 && cond.getOpId() < SqlConstants.COND_OPS.size() && 
                    SqlConstants.COND_OPS.get(cond.getOpId()).equals("like")) {
                    count++;
                }
            }
        }
        
        return count;
    }
    
    private int countNestedSql(SqlStructure sql) {
        int count = 0;
        
        // 检查WHERE条件中的嵌套SQL
        if (sql.getWhere() != null) {
            for (ConditionUnit cond : sql.getWhere()) {
                if (cond.getVal1() != null && cond.getVal1() instanceof SqlStructure) {
                    count++;
                }
                if (cond.getVal2() != null && cond.getVal2() instanceof SqlStructure) {
                    count++;
                }
            }
        }
        
        // 检查HAVING条件中的嵌套SQL
        if (sql.getHaving() != null) {
            for (ConditionUnit cond : sql.getHaving()) {
                if (cond.getVal1() != null && cond.getVal1() instanceof SqlStructure) {
                    count++;
                }
                if (cond.getVal2() != null && cond.getVal2() instanceof SqlStructure) {
                    count++;
                }
            }
        }
        
        // 检查FROM子句中的嵌套SQL
        if (sql.getFrom() != null && sql.getFrom().getTableUnits() != null) {
            for (TableUnit unit : sql.getFrom().getTableUnits()) {
                if (SqlConstants.TABLE_TYPE_SQL.equals(unit.getTableType())) {
                    count++;
                }
            }
        }
        
        return count;
    }
    
    private int countAggregations(SqlStructure sql) {
        int count = 0;
        
        if (sql.getSelect() != null && sql.getSelect().getSelectItems() != null) {
            for (SelectClause.SelectItem item : sql.getSelect().getSelectItems()) {
                if (item.getAggId() > 0) { // 0表示"none"
                    count++;
                }
            }
        }
        
        return count;
    }
    
    private boolean executeAndCompare(Connection conn, String sql1, String sql2) {
        try {
            // 执行两个SQL查询并比较结果
            List<List<Object>> result1 = executeQuery(conn, sql1);
            List<List<Object>> result2 = executeQuery(conn, sql2);
            
            if (result1 == null || result2 == null) {
                return false;
            }
            
            // 比较结果集
            return compareResultSets(result1, result2);
            
        } catch (Exception e) {
            logger.error("Failed to execute and compare queries: {} vs {}", sql1, sql2, e);
            return false;
        }
    }
    
    private List<List<Object>> executeQuery(Connection conn, String sql) {
        try (Statement stmt = conn.createStatement()) {
            var rs = stmt.executeQuery(sql);
            List<List<Object>> results = new ArrayList<>();
            
            int columnCount = rs.getMetaData().getColumnCount();
            
            while (rs.next()) {
                List<Object> row = new ArrayList<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.add(rs.getObject(i));
                }
                results.add(row);
            }
            
            return results;
            
        } catch (SQLException e) {
            logger.error("Failed to execute query: {}", sql, e);
            return null;
        }
    }
    
    private boolean compareResultSets(List<List<Object>> result1, List<List<Object>> result2) {
        if (result1.size() != result2.size()) {
            return false;
        }
        
        // 对结果进行排序以便比较
        List<List<Object>> sorted1 = result1.stream()
            .map(row -> new ArrayList<>(row))
            .sorted(this::compareRows)
            .collect(Collectors.toList());
            
        List<List<Object>> sorted2 = result2.stream()
            .map(row -> new ArrayList<>(row))
            .sorted(this::compareRows)
            .collect(Collectors.toList());
        
        // 逐行比较
        for (int i = 0; i < sorted1.size(); i++) {
            List<Object> row1 = sorted1.get(i);
            List<Object> row2 = sorted2.get(i);
            
            if (row1.size() != row2.size()) {
                return false;
            }
            
            for (int j = 0; j < row1.size(); j++) {
                if (!Objects.equals(row1.get(j), row2.get(j))) {
                    // 特殊处理数字类型的比较
                    if (!compareValues(row1.get(j), row2.get(j))) {
                        return false;
                    }
                }
            }
        }
        
        return true;
    }
    
    private int compareRows(List<Object> row1, List<Object> row2) {
        for (int i = 0; i < Math.min(row1.size(), row2.size()); i++) {
            Object val1 = row1.get(i);
            Object val2 = row2.get(i);
            
            if (val1 == null && val2 == null) continue;
            if (val1 == null) return -1;
            if (val2 == null) return 1;
            
            if (val1 instanceof Comparable && val2 instanceof Comparable) {
                try {
                    @SuppressWarnings("unchecked")
                    int cmp = ((Comparable<Object>) val1).compareTo(val2);
                    if (cmp != 0) return cmp;
                } catch (ClassCastException e) {
                    // 如果类型不兼容，转换为字符串比较
                    int cmp = val1.toString().compareTo(val2.toString());
                    if (cmp != 0) return cmp;
                }
            } else {
                int cmp = val1.toString().compareTo(val2.toString());
                if (cmp != 0) return cmp;
            }
        }
        
        return Integer.compare(row1.size(), row2.size());
    }
    
    private boolean compareValues(Object val1, Object val2) {
        if (val1 == null && val2 == null) return true;
        if (val1 == null || val2 == null) return false;
        
        // 数字类型的特殊处理
        if (val1 instanceof Number && val2 instanceof Number) {
            double d1 = ((Number) val1).doubleValue();
            double d2 = ((Number) val2).doubleValue();
            return Math.abs(d1 - d2) < 1e-9; // 浮点数比较
        }
        
        // 字符串比较（忽略大小写）
        if (val1 instanceof String && val2 instanceof String) {
            return ((String) val1).equalsIgnoreCase((String) val2);
        }
        
        return Objects.equals(val1, val2);
    }
    
    // 评估方法的完整实现
    
    private PartialScore evaluateSelect(SqlStructure predicted, SqlStructure gold) {
        if (gold.getSelect() == null && predicted.getSelect() == null) {
            return new PartialScore(0, 0, 1.0, 1.0, 1.0);
        }
        
        if (gold.getSelect() == null || predicted.getSelect() == null) {
            return new PartialScore(0, 0, 0.0, 0.0, 0.0);
        }
        
        List<SelectClause.SelectItem> goldItems = gold.getSelect().getSelectItems();
        List<SelectClause.SelectItem> predItems = predicted.getSelect().getSelectItems();
        
        if (goldItems == null && predItems == null) {
            return new PartialScore(0, 0, 1.0, 1.0, 1.0);
        }
        
        if (goldItems == null || predItems == null) {
            return new PartialScore(0, 0, 0.0, 0.0, 0.0);
        }
        
        int goldCount = goldItems.size();
        int predCount = predItems.size();
        int matchCount = 0;
        
        // 简单的匹配逻辑：比较聚合函数ID
        for (SelectClause.SelectItem goldItem : goldItems) {
            for (SelectClause.SelectItem predItem : predItems) {
                if (goldItem.getAggId() == predItem.getAggId()) {
                    matchCount++;
                    break;
                }
            }
        }
        
        return calculatePartialScore(matchCount, predCount, goldCount);
    }
    
    private PartialScore evaluateSelectNoAgg(SqlStructure predicted, SqlStructure gold) {
        if (gold.getSelect() == null && predicted.getSelect() == null) {
            return new PartialScore(0, 0, 1.0, 1.0, 1.0);
        }
        
        if (gold.getSelect() == null || predicted.getSelect() == null) {
            return new PartialScore(0, 0, 0.0, 0.0, 0.0);
        }
        
        List<SelectClause.SelectItem> goldItems = gold.getSelect().getSelectItems();
        List<SelectClause.SelectItem> predItems = predicted.getSelect().getSelectItems();
        
        if (goldItems == null && predItems == null) {
            return new PartialScore(0, 0, 1.0, 1.0, 1.0);
        }
        
        if (goldItems == null || predItems == null) {
            return new PartialScore(0, 0, 0.0, 0.0, 0.0);
        }
        
        // 过滤掉聚合函数，只比较列
        List<SelectClause.SelectItem> goldNoAgg = goldItems.stream()
            .filter(item -> item.getAggId() == 0) // 0 表示 "none"
            .toList();
        List<SelectClause.SelectItem> predNoAgg = predItems.stream()
            .filter(item -> item.getAggId() == 0)
            .toList();
        
        int goldCount = goldNoAgg.size();
        int predCount = predNoAgg.size();
        int matchCount = Math.min(goldCount, predCount); // 简化匹配逻辑
        
        return calculatePartialScore(matchCount, predCount, goldCount);
    }
    
    private PartialScore evaluateWhere(SqlStructure predicted, SqlStructure gold) {
        List<ConditionUnit> goldWhere = gold.getWhere();
        List<ConditionUnit> predWhere = predicted.getWhere();
        
        if ((goldWhere == null || goldWhere.isEmpty()) && (predWhere == null || predWhere.isEmpty())) {
            return new PartialScore(0, 0, 1.0, 1.0, 1.0);
        }
        
        if (goldWhere == null || predWhere == null) {
            return new PartialScore(0, 0, 0.0, 0.0, 0.0);
        }
        
        int goldCount = goldWhere.size();
        int predCount = predWhere.size();
        int matchCount = 0;
        
        // 简单匹配：比较操作符ID
        for (ConditionUnit goldCond : goldWhere) {
            for (ConditionUnit predCond : predWhere) {
                if (goldCond.getOpId() == predCond.getOpId()) {
                    matchCount++;
                    break;
                }
            }
        }
        
        return calculatePartialScore(matchCount, predCount, goldCount);
    }
    
    private PartialScore evaluateWhereNoOp(SqlStructure predicted, SqlStructure gold) {
        // WHERE子句评估但不考虑操作符
        List<ConditionUnit> goldWhere = gold.getWhere();
        List<ConditionUnit> predWhere = predicted.getWhere();
        
        if ((goldWhere == null || goldWhere.isEmpty()) && (predWhere == null || predWhere.isEmpty())) {
            return new PartialScore(0, 0, 1.0, 1.0, 1.0);
        }
        
        if (goldWhere == null || predWhere == null) {
            return new PartialScore(0, 0, 0.0, 0.0, 0.0);
        }
        
        int goldCount = goldWhere.size();
        int predCount = predWhere.size();
        int matchCount = Math.min(goldCount, predCount); // 简化匹配
        
        return calculatePartialScore(matchCount, predCount, goldCount);
    }
    
    private PartialScore evaluateGroup(SqlStructure predicted, SqlStructure gold) {
        List<ColUnit> goldGroup = gold.getGroupBy();
        List<ColUnit> predGroup = predicted.getGroupBy();
        
        if ((goldGroup == null || goldGroup.isEmpty()) && (predGroup == null || predGroup.isEmpty())) {
            return new PartialScore(0, 0, 1.0, 1.0, 1.0);
        }
        
        if (goldGroup == null || predGroup == null) {
            return new PartialScore(0, 0, 0.0, 0.0, 0.0);
        }
        
        int goldCount = goldGroup.size();
        int predCount = predGroup.size();
        int matchCount = Math.min(goldCount, predCount); // 简化匹配
        
        return calculatePartialScore(matchCount, predCount, goldCount);
    }
    
    private PartialScore evaluateGroupHaving(SqlStructure predicted, SqlStructure gold) {
        // GROUP BY + HAVING 组合评估
        PartialScore groupScore = evaluateGroup(predicted, gold);
        
        List<ConditionUnit> goldHaving = gold.getHaving();
        List<ConditionUnit> predHaving = predicted.getHaving();
        
        if ((goldHaving == null || goldHaving.isEmpty()) && (predHaving == null || predHaving.isEmpty())) {
            return groupScore; // 只有GROUP BY分数
        }
        
        if (goldHaving == null || predHaving == null) {
            return new PartialScore(0, 0, 0.0, 0.0, 0.0);
        }
        
        int goldCount = goldHaving.size();
        int predCount = predHaving.size();
        int matchCount = Math.min(goldCount, predCount);
        
        PartialScore havingScore = calculatePartialScore(matchCount, predCount, goldCount);
        
        // 组合GROUP BY和HAVING分数
        double combinedF1 = (groupScore.getF1() + havingScore.getF1()) / 2.0;
        return new PartialScore(goldCount, predCount, combinedF1, combinedF1, combinedF1);
    }
    
    private PartialScore evaluateOrder(SqlStructure predicted, SqlStructure gold) {
        OrderByClause goldOrder = gold.getOrderBy();
        OrderByClause predOrder = predicted.getOrderBy();
        
        if (goldOrder == null && predOrder == null) {
            return new PartialScore(0, 0, 1.0, 1.0, 1.0);
        }
        
        if (goldOrder == null || predOrder == null) {
            return new PartialScore(0, 0, 0.0, 0.0, 0.0);
        }
        
        // 简单比较：排序类型是否一致
        boolean typeMatch = Objects.equals(goldOrder.getOrderType(), predOrder.getOrderType());
        
        List<ValUnit> goldVals = goldOrder.getValUnits();
        List<ValUnit> predVals = predOrder.getValUnits();
        
        int goldCount = goldVals != null ? goldVals.size() : 0;
        int predCount = predVals != null ? predVals.size() : 0;
        int matchCount = typeMatch ? Math.min(goldCount, predCount) : 0;
        
        return calculatePartialScore(matchCount, predCount, goldCount);
    }
    
    private PartialScore evaluateAndOr(SqlStructure predicted, SqlStructure gold) {
        // 评估AND/OR逻辑操作符
        int goldAndOr = countAndOrOperators(gold);
        int predAndOr = countAndOrOperators(predicted);
        
        if (goldAndOr == 0 && predAndOr == 0) {
            return new PartialScore(0, 0, 1.0, 1.0, 1.0);
        }
        
        int matchCount = Math.min(goldAndOr, predAndOr);
        return calculatePartialScore(matchCount, predAndOr, goldAndOr);
    }
    
    private PartialScore evaluateIUEN(SqlStructure predicted, SqlStructure gold) {
        // 评估INTERSECT/UNION/EXCEPT/NESTED
        int goldIuen = countIuenOperators(gold);
        int predIuen = countIuenOperators(predicted);
        
        if (goldIuen == 0 && predIuen == 0) {
            return new PartialScore(0, 0, 1.0, 1.0, 1.0);
        }
        
        int matchCount = Math.min(goldIuen, predIuen);
        return calculatePartialScore(matchCount, predIuen, goldIuen);
    }
    
    private PartialScore evaluateKeywords(SqlStructure predicted, SqlStructure gold) {
        // 评估SQL关键字使用
        Set<String> goldKeywords = extractKeywords(gold);
        Set<String> predKeywords = extractKeywords(predicted);
        
        if (goldKeywords.isEmpty() && predKeywords.isEmpty()) {
            return new PartialScore(0, 0, 1.0, 1.0, 1.0);
        }
        
        Set<String> intersection = new HashSet<>(goldKeywords);
        intersection.retainAll(predKeywords);
        
        int matchCount = intersection.size();
        int goldCount = goldKeywords.size();
        int predCount = predKeywords.size();
        
        return calculatePartialScore(matchCount, predCount, goldCount);
    }
    
    // 辅助方法
    
    private PartialScore calculatePartialScore(int matchCount, int predCount, int goldCount) {
        if (goldCount == 0 && predCount == 0) {
            return new PartialScore(0, 0, 1.0, 1.0, 1.0);
        }
        
        if (goldCount == 0 || predCount == 0) {
            return new PartialScore(goldCount, predCount, 0.0, 0.0, 0.0);
        }
        
        double precision = (double) matchCount / predCount;
        double recall = (double) matchCount / goldCount;
        double f1 = (precision + recall == 0) ? 0.0 : (2.0 * precision * recall) / (precision + recall);
        
        return new PartialScore(goldCount, predCount, precision, recall, f1);
    }
    
    private int countAndOrOperators(SqlStructure sql) {
        int count = 0;
        // 简化实现：统计WHERE和HAVING中的条件数量作为AND/OR的近似
        if (sql.getWhere() != null) {
            count += Math.max(0, sql.getWhere().size() - 1);
        }
        if (sql.getHaving() != null) {
            count += Math.max(0, sql.getHaving().size() - 1);
        }
        return count;
    }
    
    private int countIuenOperators(SqlStructure sql) {
        int count = 0;
        if (sql.getIntersect() != null) count++;
        if (sql.getUnion() != null) count++;
        if (sql.getExcept() != null) count++;
        return count;
    }
    
    private Set<String> extractKeywords(SqlStructure sql) {
        Set<String> keywords = new HashSet<>();
        
        if (sql.getSelect() != null) keywords.add("select");
        if (sql.getFrom() != null) keywords.add("from");
        if (sql.getWhere() != null && !sql.getWhere().isEmpty()) keywords.add("where");
        if (sql.getGroupBy() != null && !sql.getGroupBy().isEmpty()) keywords.add("group");
        if (sql.getHaving() != null && !sql.getHaving().isEmpty()) keywords.add("having");
        if (sql.getOrderBy() != null) keywords.add("order");
        if (sql.getLimit() != null) keywords.add("limit");
        if (sql.getIntersect() != null) keywords.add("intersect");
        if (sql.getUnion() != null) keywords.add("union");
        if (sql.getExcept() != null) keywords.add("except");
        
        return keywords;
    }
} 