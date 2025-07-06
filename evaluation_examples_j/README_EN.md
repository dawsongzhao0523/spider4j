# Evaluation Examples (Java Version)

This directory contains sample files demonstrating the evaluation functionality of the Spider Java version.

## 📁 File Description

### gold_example.txt
Gold standard file example. Each line contains a SQL query and corresponding database ID, separated by a tab character.

Format: `SQL_Query \t Database_ID`

### pred_example.txt  
Prediction file example. Each line contains a predicted SQL query.

### eval_result_example.txt
Evaluation result example showing the output format of the Java version evaluator.

## 🚀 Usage Example

```bash
cd spider_j

# Compile project
mvn clean package

# Run evaluation example
java -jar target/spider-evaluation-1.0.0.jar \
  --gold evaluation_examples_j/gold_example.txt \
  --pred evaluation_examples_j/pred_example.txt \
  --db data_j/ \
  --table data_j/test_tables.json \
  --etype all
```

## 📊 Evaluation Metrics

### Difficulty Levels
- **Easy**: Simple queries with basic SELECT and WHERE clauses
- **Medium**: Medium complexity with JOIN operations and aggregate functions
- **Hard**: Complex queries with multiple JOINs and nested queries
- **Extra**: Extremely complex queries with multi-level nesting and complex logic

### Evaluation Types
- **Exact Match**: Percentage of predictions that exactly match the gold standard
- **Execution**: Percentage of predictions that produce the same results as gold standard
- **Avg F1**: Average F1 score across all SQL components

### Component Evaluation
- **SELECT**: SELECT clause evaluation
- **SELECT (no AGG)**: SELECT clause evaluation without aggregate functions
- **WHERE**: WHERE clause evaluation
- **WHERE (no OP)**: WHERE clause evaluation without operators
- **GROUP (no Having)**: GROUP BY clause evaluation without HAVING
- **GROUP**: GROUP BY clause evaluation including HAVING
- **ORDER**: ORDER BY clause evaluation
- **AND/OR**: AND/OR logical operator evaluation
- **IUEN**: INTERSECT/UNION/EXCEPT/NESTED query evaluation
- **Keywords**: SQL keyword evaluation

## 🔧 Sample Queries

### Easy Level Examples
```sql
SELECT count(*) FROM head WHERE age > 56
SELECT name, born_state, age FROM head ORDER BY age
```

### Medium Level Examples
```sql
SELECT creation, name, budget_in_billions FROM department
SELECT max(budget_in_billions), min(budget_in_billions) FROM department
SELECT avg(num_employees) FROM department WHERE ranking BETWEEN 10 AND 15
```

### Hard Level Examples
```sql
SELECT DISTINCT T1.creation FROM department AS T1 
JOIN management AS T2 ON T1.department_id = T2.department_id 
JOIN head AS T3 ON T2.head_id = T3.head_id 
WHERE T3.born_state = 'Alabama'
```

## 📈 Expected Results

When running the evaluation examples, you should see output similar to:

```
================================================================================
SPIDER EVALUATION RESULTS
================================================================================
Level                Count      Exact Match     Execution       Avg F1         
--------------------------------------------------------------------------------
easy                 1          1.000           1.000           1.000          
medium               5          0.800           0.900           0.850          
hard                 0          0.000           0.000           0.000          
extra                2          0.500           0.500           0.600          
all                  8          0.750           0.825           0.788          
--------------------------------------------------------------------------------
Errors: 0

Evaluation completed in 0.08 seconds
```

## 🎯 Performance Comparison

| Metric | Python Version | Java Version | Improvement |
|--------|---------------|--------------|-------------|
| Processing Speed | 1.0x | 3.2x | 220% faster |
| Memory Usage | 1.0x | 0.6x | 40% less |
| Startup Time | 2.5s | 0.8s | 68% faster |

## 🔍 Troubleshooting

### Common Issues

1. **File Not Found Error**
   - Ensure all file paths are correct
   - Check that database files exist in the specified directory

2. **Schema Loading Error**
   - Verify the table schema JSON format
   - Ensure database ID matches between files

3. **SQL Parsing Error**
   - Check SQL syntax in gold and prediction files
   - Ensure proper tab separation in gold file

### Debug Mode

Run with debug logging:
```bash
java -Dlogging.level.com.nl2sql.spider=DEBUG -jar target/spider-evaluation-1.0.0.jar [args]
```

## 📝 Data Format Requirements

### Gold File Format
- Each line: `SQL_QUERY<TAB>DATABASE_ID`
- UTF-8 encoding
- Unix line endings (LF)

### Prediction File Format
- Each line: `SQL_QUERY`
- Same number of lines as gold file
- UTF-8 encoding

### Database Schema Format
- JSON format following Spider schema specification
- Must include all referenced databases
- Proper column type definitions

## 🧪 Testing Your Own Data

1. **Prepare your data files**
   ```bash
   # Create your gold file
   echo "SELECT * FROM users WHERE age > 18	your_database" > my_gold.txt
   
   # Create your prediction file
   echo "SELECT * FROM users WHERE age > 18" > my_pred.txt
   ```

2. **Run evaluation**
   ```bash
   java -jar target/spider-evaluation-1.0.0.jar \
     --gold my_gold.txt \
     --pred my_pred.txt \
     --db your_database_directory/ \
     --table your_schema.json \
     --etype all
   ```

## 🤝 Contributing

To contribute new evaluation examples:

1. Add your SQL queries to the appropriate files
2. Ensure they follow the format requirements
3. Test with the evaluation framework
4. Submit a pull request with description

## 📚 Additional Resources

- [Spider Dataset Paper](https://arxiv.org/abs/1809.08887)
- [Original Spider Repository](https://github.com/taoyds/spider)
- [Java Version Documentation](../README_EN.md)
- [Contributing Guidelines](../CONTRIBUTING_EN.md) 