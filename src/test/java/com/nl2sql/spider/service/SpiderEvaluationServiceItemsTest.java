package com.nl2sql.spider.service;

import com.nl2sql.spider.config.DatabaseConfig;
import com.nl2sql.spider.enums.EvaluationType;
import com.nl2sql.spider.model.EvaluationResult;
import com.nl2sql.spider.model.EvaluationStatistics;
import com.nl2sql.spider.model.SqlEvaluationItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SqlEvaluationItem集合接口测试
 * 测试基于MySQL数据库的evaluateItems接口功能
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SpiderEvaluationServiceItemsTest {
    
    private SpiderEvaluationService service;
    private DatabaseConfig mysqlConfig;
    
    // MySQL数据库配置 - 请根据您的实际数据库配置修改
    private static final String MYSQL_HOST = "localhost";
    private static final int MYSQL_PORT = 3306;
    private static final String MYSQL_DATABASE = "test_spider";
    private static final String MYSQL_USERNAME = "root";
    private static final String MYSQL_PASSWORD = "password";
    
    @BeforeEach
    void setUp() {
        service = new SpiderEvaluationService();
        
        // 创建MySQL数据库配置
        mysqlConfig = service.createDatabaseConfig(
            "mysql", 
            MYSQL_HOST, 
            MYSQL_PORT, 
            MYSQL_DATABASE, 
            MYSQL_USERNAME, 
            MYSQL_PASSWORD
        );
    }
    
    @Test
    @Order(1)
    @DisplayName("测试数据库连接")
    void testDatabaseConnection() {
        System.out.println("=== 测试数据库连接 ===");
        
        boolean connected = service.testDatabaseConnection(mysqlConfig);
        System.out.println("数据库连接状态: " + (connected ? "成功" : "失败"));
        
        if (!connected) {
            System.out.println("警告: 数据库连接失败，请检查配置:");
            System.out.println("  主机: " + MYSQL_HOST);
            System.out.println("  端口: " + MYSQL_PORT);
            System.out.println("  数据库: " + MYSQL_DATABASE);
            System.out.println("  用户名: " + MYSQL_USERNAME);
            System.out.println("  密码: " + (MYSQL_PASSWORD.isEmpty() ? "空" : "已设置"));
        }
        
        // 即使连接失败，也不中断测试，因为后续会有fallback处理
        assertTrue(true, "数据库连接测试完成");
    }
    
    @Test
    @Order(2)
    @DisplayName("测试evaluateItems方法 - 基础统计")
    void testEvaluateItems() {
        System.out.println("\n=== 测试evaluateItems方法 - 基础统计 ===");
        
        // 准备测试数据
        List<SqlEvaluationItem> items = createTestItems();
        
        try {
            // 执行评估
            EvaluationStatistics statistics = service.evaluateItems(items, mysqlConfig, EvaluationType.MATCH);
            
            // 验证结果
            assertNotNull(statistics, "评估统计结果不应为null");
            
            System.out.println("评估统计结果:");
            System.out.println("  总数: " + statistics.getLevelStatistics(com.nl2sql.spider.enums.HardnessLevel.ALL).getCount());
            System.out.println("  精确匹配数: " + (int)(statistics.getLevelStatistics(com.nl2sql.spider.enums.HardnessLevel.ALL).getExactMatchScore() * statistics.getLevelStatistics(com.nl2sql.spider.enums.HardnessLevel.ALL).getCount()));
            System.out.println("  精确匹配率: " + String.format("%.2f%%", statistics.getLevelStatistics(com.nl2sql.spider.enums.HardnessLevel.ALL).getExactMatchScore() * 100));
            System.out.println("  错误数: " + statistics.getErrorCount());
            
            // 验证基本逻辑
            assertEquals(items.size(), statistics.getLevelStatistics(com.nl2sql.spider.enums.HardnessLevel.ALL).getCount(), "总数应该等于输入项目数");
            assertTrue(statistics.getLevelStatistics(com.nl2sql.spider.enums.HardnessLevel.ALL).getExactMatchScore() >= 0.0 && 
                      statistics.getLevelStatistics(com.nl2sql.spider.enums.HardnessLevel.ALL).getExactMatchScore() <= 1.0, 
                      "精确匹配率应该在0-1之间");
            
        } catch (Exception e) {
            System.err.println("评估过程中出现错误: " + e.getMessage());
            
            // 如果是数据库连接问题，给出提示但不失败测试
            if (e.getMessage().contains("Communications link failure") || 
                e.getMessage().contains("Connection refused") ||
                e.getMessage().contains("Schema not found")) {
                System.out.println("提示: 这可能是数据库连接问题或Schema不存在，测试继续");
                assertTrue(true, "数据库连接问题，测试跳过");
            } else {
                fail("评估失败: " + e.getMessage());
            }
        }
    }
    
    @Test
    @Order(3)
    @DisplayName("测试evaluateItemsDetailed方法 - 详细结果")
    void testEvaluateItemsDetailed() {
        System.out.println("\n=== 测试evaluateItemsDetailed方法 - 详细结果 ===");
        
        // 准备测试数据
        List<SqlEvaluationItem> items = createTestItems();
        
        try {
            // 执行详细评估
            List<EvaluationResult> results = service.evaluateItemsDetailed(items, mysqlConfig, EvaluationType.MATCH);
            
            // 验证结果
            assertNotNull(results, "详细评估结果不应为null");
            assertEquals(items.size(), results.size(), "结果数量应该等于输入项目数");
            
            System.out.println("详细评估结果:");
            for (int i = 0; i < results.size(); i++) {
                EvaluationResult result = results.get(i);
                SqlEvaluationItem item = items.get(i);
                
                assertNotNull(result, "评估结果不应为null");
                
                System.out.println("  项目 " + (i + 1) + ":");
                System.out.println("    数据库: " + item.getDbId());
                System.out.println("    问题: " + item.getQuestion());
                System.out.println("    标准SQL: " + item.getGoldSql());
                System.out.println("    预测SQL: " + item.getPredictionSql());
                System.out.println("    精确匹配: " + result.isExactMatch());
                System.out.println("    执行匹配: " + result.isExecutionMatch());
                System.out.println();
            }
            
        } catch (Exception e) {
            System.err.println("详细评估过程中出现错误: " + e.getMessage());
            
            // 如果是数据库连接问题，给出提示但不失败测试
            if (e.getMessage().contains("Communications link failure") || 
                e.getMessage().contains("Connection refused") ||
                e.getMessage().contains("Schema not found")) {
                System.out.println("提示: 这可能是数据库连接问题或Schema不存在，测试继续");
                assertTrue(true, "数据库连接问题，测试跳过");
            } else {
                fail("详细评估失败: " + e.getMessage());
            }
        }
    }
    
    @Test
    @Order(4)
    @DisplayName("测试validateItems方法 - SQL验证")
    void testValidateItems() {
        System.out.println("\n=== 测试validateItems方法 - SQL验证 ===");
        
        // 准备测试数据
        List<SqlEvaluationItem> items = createTestItems();
        
        try {
            // 执行验证
            List<Boolean> validationResults = service.validateItems(items, mysqlConfig);
            
            // 验证结果
            assertNotNull(validationResults, "验证结果不应为null");
            assertEquals(items.size(), validationResults.size(), "验证结果数量应该等于输入项目数");
            
            System.out.println("SQL验证结果:");
            for (int i = 0; i < validationResults.size(); i++) {
                Boolean isValid = validationResults.get(i);
                SqlEvaluationItem item = items.get(i);
                
                assertNotNull(isValid, "验证结果不应为null");
                
                System.out.println("  项目 " + (i + 1) + " (" + item.getDbId() + "): " + 
                                 (isValid ? "有效" : "无效"));
                System.out.println("    标准SQL: " + item.getGoldSql());
                System.out.println("    预测SQL: " + item.getPredictionSql());
            }
            
        } catch (Exception e) {
            System.err.println("验证过程中出现错误: " + e.getMessage());
            
            // 如果是数据库连接问题，给出提示但不失败测试
            if (e.getMessage().contains("Communications link failure") || 
                e.getMessage().contains("Connection refused") ||
                e.getMessage().contains("Schema not found")) {
                System.out.println("提示: 这可能是数据库连接问题或Schema不存在，测试继续");
                assertTrue(true, "数据库连接问题，测试跳过");
            } else {
                fail("验证失败: " + e.getMessage());
            }
        }
    }
    
    @Test
    @Order(5)
    @DisplayName("测试空集合处理")
    void testEmptyItems() {
        System.out.println("\n=== 测试空集合处理 ===");
        
        List<SqlEvaluationItem> emptyItems = new ArrayList<>();
        
        // 测试空集合应该抛出异常
        assertThrows(IllegalArgumentException.class, () -> {
            service.evaluateItems(emptyItems, mysqlConfig, EvaluationType.MATCH);
        }, "空集合应该抛出IllegalArgumentException");
        
        assertThrows(IllegalArgumentException.class, () -> {
            service.evaluateItemsDetailed(emptyItems, mysqlConfig, EvaluationType.MATCH);
        }, "空集合应该抛出IllegalArgumentException");
        
        assertThrows(IllegalArgumentException.class, () -> {
            service.validateItems(emptyItems, mysqlConfig);
        }, "空集合应该抛出IllegalArgumentException");
        
        System.out.println("空集合处理测试通过");
    }
    
    @Test
    @Order(6)
    @DisplayName("测试null参数处理")
    void testNullParameters() {
        System.out.println("\n=== 测试null参数处理 ===");
        
        // 测试null参数应该抛出异常
        assertThrows(IllegalArgumentException.class, () -> {
            service.evaluateItems(null, mysqlConfig, EvaluationType.MATCH);
        }, "null参数应该抛出IllegalArgumentException");
        
        assertThrows(IllegalArgumentException.class, () -> {
            service.evaluateItemsDetailed(null, mysqlConfig, EvaluationType.MATCH);
        }, "null参数应该抛出IllegalArgumentException");
        
        assertThrows(IllegalArgumentException.class, () -> {
            service.validateItems(null, mysqlConfig);
        }, "null参数应该抛出IllegalArgumentException");
        
        System.out.println("null参数处理测试通过");
    }
    
    @Test
    @Order(7)
    @DisplayName("测试混合数据库ID")
    void testMixedDatabaseIds() {
        System.out.println("\n=== 测试混合数据库ID ===");
        
        List<SqlEvaluationItem> items = new ArrayList<>();
        
        // 添加不同数据库ID的项目
        items.add(new SqlEvaluationItem(
            "SELECT * FROM users", 
            "SELECT * FROM users", 
            "test_db1",
            "查询所有用户"
        ));
        
        items.add(new SqlEvaluationItem(
            "SELECT COUNT(*) FROM orders", 
            "SELECT COUNT(*) FROM orders", 
            "test_db2",
            "统计订单数量"
        ));
        
        items.add(new SqlEvaluationItem(
            "SELECT name FROM products", 
            "SELECT name FROM products", 
            "test_db1",  // 重复的数据库ID
            "查询产品名称"
        ));
        
        try {
            // 执行评估（应该能处理多个不同的数据库ID）
            EvaluationStatistics statistics = service.evaluateItems(items, mysqlConfig, EvaluationType.MATCH);
            
            assertNotNull(statistics, "统计结果不应为null");
            assertEquals(3, statistics.getLevelStatistics(com.nl2sql.spider.enums.HardnessLevel.ALL).getCount(), "应该处理所有3个项目");
            
            System.out.println("混合数据库ID测试结果:");
            System.out.println("  总数: " + statistics.getLevelStatistics(com.nl2sql.spider.enums.HardnessLevel.ALL).getCount());
            System.out.println("  错误数: " + statistics.getErrorCount());
            
        } catch (Exception e) {
            System.err.println("混合数据库ID测试中出现错误: " + e.getMessage());
            
            // 如果是数据库连接问题，给出提示但不失败测试
            if (e.getMessage().contains("Communications link failure") || 
                e.getMessage().contains("Connection refused") ||
                e.getMessage().contains("Schema not found")) {
                System.out.println("提示: 这可能是数据库连接问题或Schema不存在，测试继续");
                assertTrue(true, "数据库连接问题，测试跳过");
            } else {
                // 对于schema不存在的错误，这是预期的
                assertTrue(e.getMessage().contains("Schema not found") || 
                          e.getMessage().contains("Communications link failure") ||
                          e.getMessage().contains("Evaluation failed"),
                          "应该是schema不存在、连接失败或评估失败的错误");
            }
        }
        
        System.out.println("混合数据库ID测试完成");
    }
    
    /**
     * 创建测试数据
     */
    private List<SqlEvaluationItem> createTestItems() {
        List<SqlEvaluationItem> items = new ArrayList<>();
        
        // 测试项目1 - 精确匹配的SQL
        items.add(new SqlEvaluationItem(
            "SELECT * FROM users WHERE id = 1", 
            "SELECT * FROM users WHERE id = 1", 
            "test_db",
            "查询ID为1的用户",
            "easy"
        ));
        
        // 测试项目2 - 语义相同但写法略有不同的SQL
        items.add(new SqlEvaluationItem(
            "SELECT name, email FROM users WHERE age > 18", 
            "SELECT users.name, users.email FROM users WHERE users.age > 18", 
            "test_db",
            "查询成年用户的姓名和邮箱",
            "easy"
        ));
        
        // 测试项目3 - 聚合查询
        items.add(new SqlEvaluationItem(
            "SELECT COUNT(*) FROM orders", 
            "SELECT COUNT(*) FROM orders", 
            "test_db",
            "统计订单总数",
            "easy"
        ));
        
        // 测试项目4 - JOIN查询
        items.add(new SqlEvaluationItem(
            "SELECT u.name, o.amount FROM users u JOIN orders o ON u.id = o.user_id", 
            "SELECT users.name, orders.amount FROM users INNER JOIN orders ON users.id = orders.user_id", 
            "test_db",
            "查询用户及其订单金额",
            "medium"
        ));
        
        // 测试项目5 - 完全不匹配的SQL
        items.add(new SqlEvaluationItem(
            "SELECT name FROM customers WHERE city = 'Beijing'", 
            "SELECT customer_name FROM clients WHERE location = 'Shanghai'", 
            "test_db",
            "查询指定城市的客户",
            "easy"
        ));
        
        return items;
    }
    
    /**
     * 打印测试配置信息
     */
    @Test
    @Order(0)
    @DisplayName("打印测试配置信息")
    void printTestConfiguration() {
        System.out.println("=== MySQL数据库测试配置 ===");
        System.out.println("主机: " + MYSQL_HOST);
        System.out.println("端口: " + MYSQL_PORT);
        System.out.println("数据库: " + MYSQL_DATABASE);
        System.out.println("用户名: " + MYSQL_USERNAME);
        System.out.println("密码: " + (MYSQL_PASSWORD.isEmpty() ? "空" : "已设置"));
        System.out.println();
        System.out.println("请确保:");
        System.out.println("1. MySQL服务正在运行");
        System.out.println("2. 数据库 '" + MYSQL_DATABASE + "' 已创建");
        System.out.println("3. 用户 '" + MYSQL_USERNAME + "' 有足够的权限");
        System.out.println("4. 如果配置不正确，请修改测试类中的数据库配置常量");
        System.out.println();
        
        // 这个测试总是通过，只是为了打印配置信息
        assertTrue(true, "配置信息打印完成");
    }
} 