package com.nl2sql.spider.evaluator;

import com.nl2sql.spider.constants.SqlConstants;
import com.nl2sql.spider.enums.HardnessLevel;
import com.nl2sql.spider.model.*;
import com.nl2sql.spider.parser.SqlParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

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
        // 简化实现，实际需要执行SQL并比较结果
        return false;
    }
    
    // 评估方法的简化实现
    
    private PartialScore evaluateSelect(SqlStructure predicted, SqlStructure gold) {
        // 简化实现
        return new PartialScore(0, 0, 0.0, 0.0, 0.0);
    }
    
    private PartialScore evaluateSelectNoAgg(SqlStructure predicted, SqlStructure gold) {
        // 简化实现
        return new PartialScore(0, 0, 0.0, 0.0, 0.0);
    }
    
    private PartialScore evaluateWhere(SqlStructure predicted, SqlStructure gold) {
        // 简化实现
        return new PartialScore(0, 0, 0.0, 0.0, 0.0);
    }
    
    private PartialScore evaluateWhereNoOp(SqlStructure predicted, SqlStructure gold) {
        // 简化实现
        return new PartialScore(0, 0, 0.0, 0.0, 0.0);
    }
    
    private PartialScore evaluateGroup(SqlStructure predicted, SqlStructure gold) {
        // 简化实现
        return new PartialScore(0, 0, 0.0, 0.0, 0.0);
    }
    
    private PartialScore evaluateGroupHaving(SqlStructure predicted, SqlStructure gold) {
        // 简化实现
        return new PartialScore(0, 0, 0.0, 0.0, 0.0);
    }
    
    private PartialScore evaluateOrder(SqlStructure predicted, SqlStructure gold) {
        // 简化实现
        return new PartialScore(0, 0, 0.0, 0.0, 0.0);
    }
    
    private PartialScore evaluateAndOr(SqlStructure predicted, SqlStructure gold) {
        // 简化实现
        return new PartialScore(0, 0, 0.0, 0.0, 0.0);
    }
    
    private PartialScore evaluateIUEN(SqlStructure predicted, SqlStructure gold) {
        // 简化实现
        return new PartialScore(0, 0, 0.0, 0.0, 0.0);
    }
    
    private PartialScore evaluateKeywords(SqlStructure predicted, SqlStructure gold) {
        // 简化实现
        return new PartialScore(0, 0, 0.0, 0.0, 0.0);
    }
} 