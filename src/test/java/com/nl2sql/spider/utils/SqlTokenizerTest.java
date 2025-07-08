package com.nl2sql.spider.utils;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * SQL分词器测试
 */
public class SqlTokenizerTest {
    
    @Test
    public void testBasicTokenization() {
        String sql = "SELECT name FROM users WHERE age > 18";
        List<String> tokens = SqlTokenizer.tokenize(sql);
        
        assertEquals(8, tokens.size());
        assertEquals("select", tokens.get(0));
        assertEquals("name", tokens.get(1));
        assertEquals("from", tokens.get(2));
        assertEquals("users", tokens.get(3));
        assertEquals("where", tokens.get(4));
        assertEquals("age", tokens.get(5));
        assertEquals(">", tokens.get(6));
        assertEquals("18", tokens.get(7));
    }
    
    @Test
    public void testStringValues() {
        String sql = "SELECT * FROM users WHERE name = 'John Doe'";
        List<String> tokens = SqlTokenizer.tokenize(sql);
        
        // 字符串值会被转换为小写
        assertTrue(tokens.contains("'john doe'"));
    }
    
    @Test
    public void testCompoundOperators() {
        String sql = "SELECT * FROM users WHERE age >= 18 AND status != 'inactive'";
        List<String> tokens = SqlTokenizer.tokenize(sql);
        
        assertTrue(tokens.contains(">="));
        assertTrue(tokens.contains("!="));
    }
    
    @Test
    public void testEmptyInput() {
        List<String> tokens = SqlTokenizer.tokenize("");
        assertTrue(tokens.isEmpty());
        
        tokens = SqlTokenizer.tokenize(null);
        assertTrue(tokens.isEmpty());
    }
    
    @Test
    public void testComplexQuery() {
        String sql = "SELECT u.name, COUNT(*) FROM users u JOIN orders o ON u.id = o.user_id WHERE u.age BETWEEN 18 AND 65 GROUP BY u.name HAVING COUNT(*) > 5";
        List<String> tokens = SqlTokenizer.tokenize(sql);
        
        assertFalse(tokens.isEmpty());
        assertTrue(tokens.contains("select"));
        assertTrue(tokens.contains("join"));
        assertTrue(tokens.contains("between"));
        assertTrue(tokens.contains("group"));
        assertTrue(tokens.contains("having"));
    }
} 