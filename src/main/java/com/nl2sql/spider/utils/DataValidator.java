package com.nl2sql.spider.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nl2sql.spider.model.DatabaseSchema;
import com.nl2sql.spider.model.SpiderDataItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * Spider数据验证工具类
 * 用于验证数据集的完整性和格式正确性
 */
public class DataValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(DataValidator.class);
    private final ObjectMapper objectMapper;
    
    public DataValidator() {
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * 验证Spider数据集的完整性
     * 
     * @param dataDir 数据目录路径
     * @return 验证结果
     */
    public ValidationResult validateDataset(String dataDir) {
        ValidationResult result = new ValidationResult();
        
        try {
            // 验证目录结构
            validateDirectoryStructure(dataDir, result);
            
            // 验证JSON文件格式
            validateJsonFiles(dataDir, result);
            
            // 验证SQL文件格式
            validateSqlFiles(dataDir, result);
            
            // 验证数据库文件
            validateDatabaseFiles(dataDir, result);
            
            // 验证数据一致性
            validateDataConsistency(dataDir, result);
            
        } catch (Exception e) {
            logger.error("Dataset validation failed", e);
            result.addError("Dataset validation failed: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 验证目录结构
     */
    private void validateDirectoryStructure(String dataDir, ValidationResult result) {
        String[] requiredFiles = {
            "tables.json",
            "dev.json",
            "dev_gold.sql",
            "train_spider.json",
            "train_gold.sql"
        };
        
        String[] requiredDirs = {
            "database"
        };
        
        for (String file : requiredFiles) {
            Path filePath = Paths.get(dataDir, file);
            if (!Files.exists(filePath)) {
                result.addError("Required file missing: " + file);
            } else {
                result.addInfo("Found required file: " + file);
            }
        }
        
        for (String dir : requiredDirs) {
            Path dirPath = Paths.get(dataDir, dir);
            if (!Files.exists(dirPath) || !Files.isDirectory(dirPath)) {
                result.addError("Required directory missing: " + dir);
            } else {
                result.addInfo("Found required directory: " + dir);
            }
        }
    }
    
    /**
     * 验证JSON文件格式
     */
    private void validateJsonFiles(String dataDir, ValidationResult result) {
        // 验证tables.json
        validateTablesJson(Paths.get(dataDir, "tables.json"), result);
        
        // 验证dev.json
        validateDevJson(Paths.get(dataDir, "dev.json"), result);
        
        // 验证train_spider.json
        validateTrainJson(Paths.get(dataDir, "train_spider.json"), result);
    }
    
    /**
     * 验证tables.json文件
     */
    private void validateTablesJson(Path filePath, ValidationResult result) {
        if (!Files.exists(filePath)) {
            result.addError("tables.json file not found");
            return;
        }
        
        try {
            List<DatabaseSchema> schemas = objectMapper.readValue(
                filePath.toFile(), 
                new TypeReference<List<DatabaseSchema>>() {}
            );
            
            result.addInfo("Successfully parsed tables.json with " + schemas.size() + " schemas");
            
            // 验证每个schema的完整性
            Set<String> dbIds = new HashSet<>();
            for (DatabaseSchema schema : schemas) {
                if (schema.getDbId() == null || schema.getDbId().trim().isEmpty()) {
                    result.addError("Schema missing db_id");
                    continue;
                }
                
                if (dbIds.contains(schema.getDbId())) {
                    result.addError("Duplicate db_id found: " + schema.getDbId());
                } else {
                    dbIds.add(schema.getDbId());
                }
                
                if (schema.getTableNames() == null || schema.getTableNames().isEmpty()) {
                    result.addError("Schema " + schema.getDbId() + " has no tables");
                }
                
                if (schema.getColumnNames() == null || schema.getColumnNames().isEmpty()) {
                    result.addError("Schema " + schema.getDbId() + " has no columns");
                }
            }
            
        } catch (IOException e) {
            result.addError("Failed to parse tables.json: " + e.getMessage());
        }
    }
    
    /**
     * 验证dev.json文件
     */
    private void validateDevJson(Path filePath, ValidationResult result) {
        if (!Files.exists(filePath)) {
            result.addError("dev.json file not found");
            return;
        }
        
        try {
            List<SpiderDataItem> items = objectMapper.readValue(
                filePath.toFile(), 
                new TypeReference<List<SpiderDataItem>>() {}
            );
            
            result.addInfo("Successfully parsed dev.json with " + items.size() + " items");
            
            // 验证每个item的完整性
            for (int i = 0; i < items.size(); i++) {
                SpiderDataItem item = items.get(i);
                
                if (item.getDbId() == null || item.getDbId().trim().isEmpty()) {
                    result.addError("Item " + i + " missing db_id");
                }
                
                if (item.getQuestion() == null || item.getQuestion().trim().isEmpty()) {
                    result.addError("Item " + i + " missing question");
                }
                
                if (item.getQuery() == null || item.getQuery().trim().isEmpty()) {
                    result.addError("Item " + i + " missing query");
                }
            }
            
        } catch (IOException e) {
            result.addError("Failed to parse dev.json: " + e.getMessage());
        }
    }
    
    /**
     * 验证train.json文件
     */
    private void validateTrainJson(Path filePath, ValidationResult result) {
        if (!Files.exists(filePath)) {
            result.addError("train_spider.json file not found");
            return;
        }
        
        try {
            List<SpiderDataItem> items = objectMapper.readValue(
                filePath.toFile(), 
                new TypeReference<List<SpiderDataItem>>() {}
            );
            
            result.addInfo("Successfully parsed train_spider.json with " + items.size() + " items");
            
        } catch (IOException e) {
            result.addError("Failed to parse train_spider.json: " + e.getMessage());
        }
    }
    
    /**
     * 验证SQL文件格式
     */
    private void validateSqlFiles(String dataDir, ValidationResult result) {
        validateSqlFile(Paths.get(dataDir, "dev_gold.sql"), result, "dev_gold.sql");
        validateSqlFile(Paths.get(dataDir, "train_gold.sql"), result, "train_gold.sql");
    }
    
    /**
     * 验证单个SQL文件
     */
    private void validateSqlFile(Path filePath, ValidationResult result, String fileName) {
        if (!Files.exists(filePath)) {
            result.addError(fileName + " file not found");
            return;
        }
        
        try {
            List<String> lines = Files.readAllLines(filePath);
            result.addInfo("Successfully read " + fileName + " with " + lines.size() + " lines");
            
            // 验证每行SQL的基本格式
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty()) continue;
                
                String[] parts = line.split("\t");
                if (parts.length < 2) {
                    result.addError(fileName + " line " + (i + 1) + " has incorrect format (should be SQL\\tDB_ID)");
                }
            }
            
        } catch (IOException e) {
            result.addError("Failed to read " + fileName + ": " + e.getMessage());
        }
    }
    
    /**
     * 验证数据库文件
     */
    private void validateDatabaseFiles(String dataDir, ValidationResult result) {
        Path databaseDir = Paths.get(dataDir, "database");
        if (!Files.exists(databaseDir)) {
            result.addError("Database directory not found");
            return;
        }
        
        try {
            List<Path> dbDirs = Files.list(databaseDir)
                .filter(Files::isDirectory)
                .collect(Collectors.toList());
            
            result.addInfo("Found " + dbDirs.size() + " database directories");
            
            for (Path dbDir : dbDirs) {
                String dbName = dbDir.getFileName().toString();
                Path sqliteFile = dbDir.resolve(dbName + ".sqlite");
                
                if (!Files.exists(sqliteFile)) {
                    result.addError("SQLite file missing for database: " + dbName);
                } else {
                    result.addInfo("Found SQLite file for database: " + dbName);
                }
            }
            
        } catch (IOException e) {
            result.addError("Failed to list database directories: " + e.getMessage());
        }
    }
    
    /**
     * 验证数据一致性
     */
    private void validateDataConsistency(String dataDir, ValidationResult result) {
        try {
            // 加载schemas
            List<DatabaseSchema> schemas = objectMapper.readValue(
                Paths.get(dataDir, "tables.json").toFile(), 
                new TypeReference<List<DatabaseSchema>>() {}
            );
            
            Set<String> schemaDbIds = schemas.stream()
                .map(DatabaseSchema::getDbId)
                .collect(Collectors.toSet());
            
            // 检查dev.json中的db_id是否都存在于schemas中
            List<SpiderDataItem> devItems = objectMapper.readValue(
                Paths.get(dataDir, "dev.json").toFile(), 
                new TypeReference<List<SpiderDataItem>>() {}
            );
            
            for (SpiderDataItem item : devItems) {
                if (!schemaDbIds.contains(item.getDbId())) {
                    result.addError("dev.json contains unknown db_id: " + item.getDbId());
                }
            }
            
            result.addInfo("Data consistency validation completed");
            
        } catch (IOException e) {
            result.addError("Failed to validate data consistency: " + e.getMessage());
        }
    }
    
    /**
     * 验证结果类
     */
    public static class ValidationResult {
        private final List<String> errors = new java.util.ArrayList<>();
        private final List<String> warnings = new java.util.ArrayList<>();
        private final List<String> infos = new java.util.ArrayList<>();
        
        public void addError(String error) {
            errors.add(error);
            logger.error("Validation Error: " + error);
        }
        
        public void addWarning(String warning) {
            warnings.add(warning);
            logger.warn("Validation Warning: " + warning);
        }
        
        public void addInfo(String info) {
            infos.add(info);
            logger.info("Validation Info: " + info);
        }
        
        public boolean isValid() {
            return errors.isEmpty();
        }
        
        public List<String> getErrors() { return errors; }
        public List<String> getWarnings() { return warnings; }
        public List<String> getInfos() { return infos; }
        
        public void printSummary() {
            System.out.println("=".repeat(60));
            System.out.println("VALIDATION SUMMARY");
            System.out.println("=".repeat(60));
            System.out.println("Status: " + (isValid() ? "PASSED" : "FAILED"));
            System.out.println("Errors: " + errors.size());
            System.out.println("Warnings: " + warnings.size());
            System.out.println("Infos: " + infos.size());
            
            if (!errors.isEmpty()) {
                System.out.println("\nErrors:");
                for (String error : errors) {
                    System.out.println("  - " + error);
                }
            }
            
            if (!warnings.isEmpty()) {
                System.out.println("\nWarnings:");
                for (String warning : warnings) {
                    System.out.println("  - " + warning);
                }
            }
            
            System.out.println("=".repeat(60));
        }
    }
} 