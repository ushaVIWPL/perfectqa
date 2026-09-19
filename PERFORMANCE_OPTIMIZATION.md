# Performance Optimization Guide

## Overview
This application has been optimized to handle millions of records with millisecond-level response times.

## Database Optimization

### 1. Run Database Indexes Script
Execute the SQL script to create indexes on frequently queried columns:

```bash
mysql -u philip -p perfect < src/main/resources/db-indexes.sql
```

Or run it directly in your MySQL client:
- Connect to your database
- Execute the contents of `src/main/resources/db-indexes.sql`

### 2. Indexes Created

#### Business Scenario Indexes
- `idx_business_scenario_company_code` - Fast filtering by company
- `idx_business_scenario_business_scenario` - Fast lookup by scenario number
- `idx_business_scenario_company_business` - Composite index for common queries
- `idx_business_scenario_responsible` - Fast filtering by responsible person
- `idx_business_scenario_work_stream` - Fast filtering by work stream
- `idx_business_scenario_activity` - Fast text search on activity

#### Scenario Activities Indexes
- `idx_scenario_activities_company_code` - Fast filtering by company
- `idx_scenario_activities_transaction_key` - Fast lookup by transaction key
- `idx_scenario_activities_company_business` - Composite index
- `idx_scenario_activities_activity` - Fast text search

#### Test Case Header Indexes
- `idx_testcase_headers_company_code` - Fast filtering by company
- `idx_testcase_headers_transaction_key` - Fast lookup by transaction key
- `idx_testcase_headers_combined_key` - Fast lookup by combined key
- `idx_testcase_headers_company_transaction` - Composite index

#### Composite Search Indexes
- Optimized indexes for common search patterns combining multiple columns

## Application Configuration

### Connection Pooling
The application uses HikariCP with optimized settings:
- **Maximum Pool Size**: 20 connections
- **Minimum Idle**: 5 connections
- **Connection Timeout**: 30 seconds
- **Idle Timeout**: 10 minutes
- **Max Lifetime**: 30 minutes

### JPA/Hibernate Optimization
- **Batch Size**: 50 (reduces database round trips)
- **Fetch Size**: 50 (optimizes result set retrieval)
- **Query Plan Cache**: 2048 entries (caches query execution plans)
- **Ordered Inserts/Updates**: Enabled (improves batch performance)

## Query Optimization

### Fetch Joins
All repository queries use `LEFT JOIN FETCH` to:
- Avoid N+1 query problems
- Load related entities in a single query
- Reduce database round trips

### Pagination
All list endpoints support:
- Server-side pagination (default: 20 items per page)
- Configurable page size (10, 20, 50, 100)
- Efficient LIMIT/OFFSET queries

### Search Optimization
- Database-level search using LIKE with indexes
- Debounced client-side search (500ms delay)
- Search resets to page 0 for fresh results

## Performance Metrics

### Expected Performance
With proper indexes and optimization:
- **Page Load**: < 100ms for first page (20 items)
- **Search**: < 200ms for filtered results
- **Pagination**: < 50ms for page navigation
- **Large Datasets**: Handles millions of records efficiently

### Monitoring
Check query performance:
1. Enable `spring.jpa.show-sql=true` temporarily to see queries
2. Use MySQL `EXPLAIN` to analyze query execution plans
3. Monitor connection pool usage in application logs

## Troubleshooting

### Slow Queries
1. **Verify Indexes**: Run `SHOW INDEXES FROM table_name;` to confirm indexes exist
2. **Check Query Plans**: Use `EXPLAIN SELECT ...` to see if indexes are used
3. **Connection Pool**: Check if pool is exhausted (increase `maximum-pool-size`)

### Memory Issues
1. **Reduce Page Size**: Use smaller page sizes (10 or 20)
2. **Enable Pagination**: Ensure all list endpoints use pagination
3. **Connection Pool**: Reduce `maximum-pool-size` if memory is limited

## Best Practices

1. **Always Use Pagination**: Never load all records at once
2. **Use Search Filters**: Filter data at database level, not in memory
3. **Monitor Indexes**: Regularly check index usage and add new ones as needed
4. **Connection Pool**: Adjust pool size based on concurrent users
5. **Query Optimization**: Use `EXPLAIN` to identify slow queries

## Next Steps

1. Run the database indexes script
2. Restart the application
3. Monitor performance metrics
4. Adjust connection pool size based on load
5. Consider enabling second-level cache for frequently accessed data









