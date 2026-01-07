# MSSQL Library Management System Database

This directory contains the Microsoft SQL Server (MSSQL) version of the Library Management System database schema and related SQL scripts.

## Files Overview

1. **01_schema_creation.sql** - Creates all tables, indexes, and triggers
2. **02_sample_data.sql** - Inserts sample data for testing
3. **03_common_queries.sql** - Common operations as stored procedures
4. **04_reports_analytics.sql** - Advanced reporting and analytics procedures
5. **test_setup.sql** - Test script to verify the setup

## Setup Instructions

### Prerequisites

- Microsoft SQL Server 2016 or later
- SQL Server Management Studio (SSMS) or any SQL client that supports MSSQL

### Installation Steps

1. **Create Database** (if needed):

   ```sql
   CREATE DATABASE LibraryManagementSystem;
   GO
   USE LibraryManagementSystem;
   GO
   ```

2. **Run Scripts in Order**:

   ```sql
   -- 1. Create schema
   :r 01_schema_creation.sql

   -- 2. Insert sample data
   :r 02_sample_data.sql

   -- 3. Create common procedures
   :r 03_common_queries.sql

   -- 4. Create reporting procedures
   :r 04_reports_analytics.sql
   ```

### Key MSSQL-Specific Features Used

- **IDENTITY(1,1)** for auto-incrementing primary keys
- **DATETIME2** for timestamp fields with better precision
- **GETDATE()** for current timestamp
- **STRING_AGG** for string aggregation
- **DATEPART/DATEFROMPARTS** for date manipulation
- **GO** statements for batch separation
- **RAISERROR** for custom error handling
- **SCOPE_IDENTITY()** for getting last inserted ID

### Sample Usage

```sql
-- Get user information with roles
EXEC GetUserWithRoles 'admin1';

-- Search for books
EXEC SearchBooks 'Programming', NULL;

-- Check out a book
EXEC CheckoutBook @MemberId = 3, @BookCopyId = 1, @DueDays = 14;

-- Return a book
EXEC ReturnBook @LoanId = 1, @Condition = 'Good';

-- Generate reports
EXEC GetMemberActivityReport '2023-01-01', '2023-12-31';
EXEC GetBookUsageReport '2023-01-01', '2023-12-31';
EXEC GetOverdueLoansReport;
```

### Testing

Run the `test_setup.sql` file to verify that all components work correctly:

```sql
:r test_setup.sql
```

### Notes

- All triggers are properly separated with GO statements to avoid batch compilation errors
- Stored procedures include proper error handling and transaction management
- The schema maintains referential integrity with foreign key constraints
- Indexes are created for optimal query performance

### Troubleshooting

**Common Issues:**

1. **"CREATE TRIGGER must be the first statement in a query batch"**

   - Fixed: All triggers are now properly separated with GO statements

2. **Invalid column names in GROUP BY**

   - Fixed: All columns in SELECT are included in GROUP BY clauses

3. **String concatenation issues**

   - Fixed: Using CONCAT() function for string operations

4. **Date function compatibility**
   - Fixed: Using MSSQL-specific date functions like GETDATE(), DATEPART(), etc.

For any issues, ensure you're running the scripts in the correct order and that your SQL Server version supports all the features used (SQL Server 2016+).
