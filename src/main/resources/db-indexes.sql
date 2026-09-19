-- Performance Optimization: Database Indexes for Fast Queries
-- Run this script on your database to create indexes for millions of records

-- Business Scenario Indexes
CREATE INDEX IF NOT EXISTS idx_business_scenario_company_code ON business_scenario(company_code);
CREATE INDEX IF NOT EXISTS idx_business_scenario_business_scenario ON business_scenario(business_scenario);
CREATE INDEX IF NOT EXISTS idx_business_scenario_company_business ON business_scenario(company_code, business_scenario);
CREATE INDEX IF NOT EXISTS idx_business_scenario_responsible ON business_scenario(responsible);
CREATE INDEX IF NOT EXISTS idx_business_scenario_work_stream ON business_scenario(work_stream);
CREATE INDEX IF NOT EXISTS idx_business_scenario_activity ON business_scenario(activity(100));

-- Scenario Activities Indexes
CREATE INDEX IF NOT EXISTS idx_scenario_activities_company_code ON scenario_activities(company_code);
CREATE INDEX IF NOT EXISTS idx_scenario_activities_business_scenario ON scenario_activities(business_scenario);
CREATE INDEX IF NOT EXISTS idx_scenario_activities_transaction_key ON scenario_activities(transaction_key);
CREATE INDEX IF NOT EXISTS idx_scenario_activities_company_business ON scenario_activities(company_code, business_scenario);
CREATE INDEX IF NOT EXISTS idx_scenario_activities_transaction_suffix ON scenario_activities(transaction_suffix);
CREATE INDEX IF NOT EXISTS idx_scenario_activities_activity ON scenario_activities(activity(100));

-- Test Case Header Indexes
CREATE INDEX IF NOT EXISTS idx_testcase_headers_company_code ON testcase_headers(company_code);
CREATE INDEX IF NOT EXISTS idx_testcase_headers_transaction_key ON testcase_headers(transaction_key);
CREATE INDEX IF NOT EXISTS idx_testcase_headers_combined_key ON testcase_headers(combined_key);
CREATE INDEX IF NOT EXISTS idx_testcase_headers_test_case_no ON testcase_headers(test_case_no);
CREATE INDEX IF NOT EXISTS idx_testcase_headers_company_transaction ON testcase_headers(company_code, transaction_key);
CREATE INDEX IF NOT EXISTS idx_testcase_headers_activity ON testcase_headers(activity(100));

-- Test Case Transaction Indexes
CREATE INDEX IF NOT EXISTS idx_testcase_transactions_company_code ON testcase_transactions(company_code);
CREATE INDEX IF NOT EXISTS idx_testcase_transactions_testcase_header_id ON testcase_transactions(testcase_header_id);
CREATE INDEX IF NOT EXISTS idx_testcase_transactions_main_key ON testcase_transactions(main_key);
CREATE INDEX IF NOT EXISTS idx_testcase_transactions_company_header ON testcase_transactions(company_code, testcase_header_id);

-- Composite Indexes for Common Query Patterns
CREATE INDEX IF NOT EXISTS idx_business_scenario_search ON business_scenario(company_code, business_scenario, activity(50), responsible);
CREATE INDEX IF NOT EXISTS idx_scenario_activities_search ON scenario_activities(company_code, transaction_key, activity(50));
CREATE INDEX IF NOT EXISTS idx_testcase_headers_search ON testcase_headers(company_code, transaction_key, combined_key, activity(50));

-- Full-text indexes for text search (MySQL 5.6+)
-- ALTER TABLE business_scenario ADD FULLTEXT INDEX ft_business_scenario_search (activity, scenario_description, responsible, work_stream);
-- ALTER TABLE scenario_activities ADD FULLTEXT INDEX ft_scenario_activities_search (activity, scenario_description);
-- ALTER TABLE testcase_headers ADD FULLTEXT INDEX ft_testcase_headers_search (activity, description);









