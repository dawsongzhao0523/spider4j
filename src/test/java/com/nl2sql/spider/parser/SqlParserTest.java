package com.nl2sql.spider.parser;

import com.nl2sql.spider.model.*;
import com.nl2sql.spider.utils.SqlTokenizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

/**
 * SqlParser的单元测试
 */
public class SqlParserTest {
    
    private SqlParser parser;
    private DatabaseSchema testSchema;
    
    @BeforeEach
    void setUp() {
        parser = new SqlParser();
        testSchema = createTestSchema();
    }
    
    @Test
    @DisplayName("测试简单SELECT语句解析")
    void testSimpleSelectParsing() {
        String sql = "SELECT name FROM student";
        
        SqlStructure result = parser.parseSql(testSchema, sql);
        
        assertNotNull(result);
        assertNotNull(result.getSelect());
        assertNotNull(result.getFrom());
    }
    
    @Test
    @DisplayName("测试带WHERE条件的SELECT语句解析")
    void testSelectWithWhereParsing() {
        String sql = "SELECT name FROM student WHERE age > 18";
        
        SqlStructure result = parser.parseSql(testSchema, sql);
        
        assertNotNull(result);
        assertNotNull(result.getSelect());
        assertNotNull(result.getFrom());
        assertNotNull(result.getWhere());
        assertFalse(result.getWhere().isEmpty());
    }
    
    @Test
    @DisplayName("测试SQL分词器")
    void testSqlTokenizer() {
        String sql = "SELECT name, age FROM student WHERE age > 18";
        
        List<String> tokens = SqlTokenizer.tokenize(sql);
        
        assertNotNull(tokens);
        assertTrue(tokens.size() > 0);
        assertEquals("select", tokens.get(0));
        assertTrue(tokens.contains("name"));
        assertTrue(tokens.contains("age"));
        assertTrue(tokens.contains("from"));
        assertTrue(tokens.contains("student"));
        assertTrue(tokens.contains("where"));
    }
    
    @Test
    @DisplayName("测试复杂SQL解析")
    void testComplexSqlParsing() {
        String sql = "SELECT COUNT(*) FROM student WHERE age > 18 GROUP BY grade ORDER BY grade ASC";
        
        SqlStructure result = parser.parseSql(testSchema, sql);
        
        assertNotNull(result);
        assertNotNull(result.getSelect());
        assertNotNull(result.getFrom());
        assertNotNull(result.getWhere());
        assertNotNull(result.getGroupBy());
        assertNotNull(result.getOrderBy());
    }
    
    @Test
    @DisplayName("测试JOIN语句解析")
    void testJoinParsing() {
        String sql = "SELECT s.name, c.title FROM student s JOIN course c ON s.id = c.student_id";
        
        SqlStructure result = parser.parseSql(testSchema, sql);
        
        assertNotNull(result);
        assertNotNull(result.getSelect());
        assertNotNull(result.getFrom());
    }
    
    @Test
    @DisplayName("测试数字识别")
    void testNumericValueRecognition() {
        // 测试数字识别的逻辑
        assertTrue(isNumericValue("123"));
        assertTrue(isNumericValue("12.34"));
        assertTrue(isNumericValue("-5"));
        assertFalse(isNumericValue("abc"));
        assertFalse(isNumericValue("12a"));
    }
    
    @Test
    @DisplayName("测试字符串识别")
    void testStringValueRecognition() {
        // 测试字符串识别的逻辑
        assertTrue(isStringValue("'hello'"));
        assertTrue(isStringValue("\"world\""));
        assertFalse(isStringValue("hello"));
        assertFalse(isStringValue("'unclosed"));
    }
    
    // 辅助方法：数字识别
    private boolean isNumericValue(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    // 辅助方法：字符串识别
    private boolean isStringValue(String str) {
        if (str == null || str.length() < 2) {
            return false;
        }
        return (str.startsWith("'") && str.endsWith("'")) || 
               (str.startsWith("\"") && str.endsWith("\""));
    }
    
    // 辅助方法
    
    private DatabaseSchema createTestSchema() {
        DatabaseSchema schema = new DatabaseSchema();
        schema.setDbId("test_db");
        return schema;
    }
} 