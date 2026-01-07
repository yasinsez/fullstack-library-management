-- Advanced reporting and analytics queries

-- Member Activity Analysis
CREATE OR ALTER PROCEDURE GetMemberActivityReport
    @StartDate DATE,
    @EndDate DATE
AS
BEGIN
    SET NOCOUNT ON;
    
    WITH MemberStats AS (
        SELECT 
            m.user_id,
            u.full_name,
            COUNT(DISTINCT l.id) as total_loans,
            SUM(CASE WHEN l.status = 'OVERDUE' THEN 1 ELSE 0 END) as overdue_loans,
            SUM(CASE WHEN l.status = 'RETURNED' AND l.return_date <= l.due_date THEN 1 ELSE 0 END) as on_time_returns,
            SUM(CASE WHEN l.status = 'RETURNED' AND l.return_date > l.due_date THEN 1 ELSE 0 END) as late_returns,
            COUNT(DISTINCT r.id) as total_reservations,
            SUM(f.amount) as total_fines,
            SUM(CASE WHEN f.status = 'PAID' THEN f.amount ELSE 0 END) as paid_fines
        FROM members m
        JOIN users u ON m.user_id = u.id
        LEFT JOIN loans l ON m.user_id = l.member_id AND l.checkout_date BETWEEN @StartDate AND @EndDate
        LEFT JOIN reservations r ON m.user_id = r.member_id AND r.reservation_time BETWEEN @StartDate AND @EndDate
        LEFT JOIN fines f ON m.user_id = f.member_id AND f.issue_date BETWEEN @StartDate AND @EndDate
        GROUP BY m.user_id, u.full_name
    )
    SELECT 
        ms.*,
        CASE 
            WHEN total_loans > 0 
            THEN CAST((CAST(on_time_returns AS FLOAT) / total_loans) * 100 AS DECIMAL(5,2))
            ELSE 0 
        END as on_time_return_percentage,
        CASE 
            WHEN total_fines > 0 
            THEN CAST((CAST(paid_fines AS FLOAT) / total_fines) * 100 AS DECIMAL(5,2))
            ELSE 100 
        END as fine_payment_percentage
    FROM MemberStats ms
    ORDER BY total_loans DESC
END
GO

-- Book Usage Analysis
CREATE OR ALTER PROCEDURE GetBookUsageReport
    @StartDate DATE,
    @EndDate DATE
AS
BEGIN
    SET NOCOUNT ON;
    
    WITH LoanData AS (
        SELECT
            b.id,
            COUNT(DISTINCT l.id) AS loans,
            SUM(CASE WHEN l.status = 'OVERDUE' THEN 1 ELSE 0 END) AS overdue_count,
            AVG(CAST(
                CASE
                    WHEN l.return_date IS NOT NULL
                    THEN DATEDIFF(DAY, l.checkout_date, l.return_date)
                    ELSE DATEDIFF(DAY, l.checkout_date, GETDATE())
                END AS FLOAT
            )) AS avg_loan_duration
        FROM books b
        LEFT JOIN book_copies bc ON b.id = bc.book_id
        LEFT JOIN loans l ON bc.id = l.book_copy_id
            AND l.checkout_date BETWEEN @StartDate AND @EndDate
        GROUP BY b.id
    ),
    CopyCounts AS (
        SELECT
            book_id,
            COUNT(*) AS total_copies
        FROM book_copies
        GROUP BY book_id
    ),
    ReservationCounts AS (
        SELECT
            book_id,
            COUNT(*) AS reservation_count
        FROM reservations
        WHERE reservation_time BETWEEN @StartDate AND @EndDate
        GROUP BY book_id
    )
    SELECT
        b.id,
        b.title,
        b.isbn,
        c.name AS category,
        lc.total_copies,
        ld.loans AS total_loans,
        ld.overdue_count,
        rc.reservation_count,
        ld.avg_loan_duration,
        CAST(
            ld.loans * 1.0 / NULLIF(lc.total_copies, 0) AS DECIMAL(10,2)
        ) AS loans_per_copy,
        CAST(
            ld.overdue_count * 100.0 / NULLIF(ld.loans, 0) AS DECIMAL(5,2)
        ) AS overdue_percentage
    FROM books b
    JOIN categories c ON b.category_id = c.id
    LEFT JOIN CopyCounts lc ON b.id = lc.book_id
    LEFT JOIN LoanData ld ON b.id = ld.id
    LEFT JOIN ReservationCounts rc ON b.id = rc.book_id
    ORDER BY ld.loans DESC
END
GO

-- Category Performance Analysis
CREATE OR ALTER PROCEDURE GetCategoryPerformanceReport
    @StartDate DATE,
    @EndDate DATE
AS
BEGIN
    SET NOCOUNT ON;
    
    SELECT 
        c.name as category_name,
        COUNT(DISTINCT b.id) as total_books,
        COUNT(DISTINCT bc.id) as total_copies,
        COUNT(DISTINCT l.id) as total_loans,
        CAST(COUNT(DISTINCT l.id) AS FLOAT) / NULLIF(COUNT(DISTINCT bc.id), 0) as loans_per_copy,
        COUNT(DISTINCT r.id) as total_reservations,
        AVG(CAST(
            CASE 
                WHEN l.return_date IS NOT NULL 
                THEN DATEDIFF(day, l.checkout_date, l.return_date)
                ELSE DATEDIFF(day, l.checkout_date, GETDATE())
            END AS FLOAT)) as avg_loan_duration
    FROM categories c
    LEFT JOIN books b ON c.id = b.category_id
    LEFT JOIN book_copies bc ON b.id = bc.book_id
    LEFT JOIN loans l ON bc.id = l.book_copy_id AND l.checkout_date BETWEEN @StartDate AND @EndDate
    LEFT JOIN reservations r ON b.id = r.book_id AND r.reservation_time BETWEEN @StartDate AND @EndDate
    GROUP BY c.id, c.name
    ORDER BY total_loans DESC
END
GO

-- Financial Analysis
CREATE OR ALTER PROCEDURE GetFinancialReport
    @StartDate DATE,
    @EndDate DATE
AS
BEGIN
    SET NOCOUNT ON;
    
    WITH MonthlyStats AS (
        SELECT 
            DATEFROMPARTS(YEAR(t.date), MONTH(t.date), 1) as month_start,
            t.type,
            COUNT(*) as transaction_count,
            SUM(t.amount) as total_amount
        FROM transactions t
        WHERE t.date BETWEEN @StartDate AND @EndDate
        GROUP BY DATEFROMPARTS(YEAR(t.date), MONTH(t.date), 1), t.type
    )
    SELECT 
        month_start,
        type,
        transaction_count,
        total_amount,
        SUM(total_amount) OVER (PARTITION BY type ORDER BY month_start) as running_total,
        CAST(
            total_amount * 100.0 / NULLIF(SUM(total_amount) OVER (PARTITION BY month_start), 0)
            AS DECIMAL(5,2)
        ) as percentage_of_month
    FROM MonthlyStats
    ORDER BY month_start, type
END
GO

-- System Usage Patterns
CREATE OR ALTER PROCEDURE GetSystemUsagePatterns
    @StartDate DATE,
    @EndDate DATE
AS
BEGIN
    SET NOCOUNT ON;
    
    WITH ActivityCounts AS (
        SELECT 
            DATEPART(dw, l.checkout_date) as day_of_week,
            DATEPART(hour, l.checkout_date) as hour_of_day,
            'LOAN' as activity_type,
            COUNT(*) as activity_count
        FROM loans l WHERE l.checkout_date BETWEEN @StartDate AND @EndDate
        GROUP BY DATEPART(dw, l.checkout_date), DATEPART(hour, l.checkout_date)
        UNION ALL
        SELECT 
            DATEPART(dw, r.reservation_time),
            DATEPART(hour, r.reservation_time),
            'RESERVATION',
            COUNT(*)
        FROM reservations r WHERE r.reservation_time BETWEEN @StartDate AND @EndDate
        GROUP BY DATEPART(dw, r.reservation_time), DATEPART(hour, r.reservation_time)
        UNION ALL
        SELECT 
            DATEPART(dw, t.date),
            DATEPART(hour, t.date),
            'TRANSACTION',
            COUNT(*)
        FROM transactions t WHERE t.date BETWEEN @StartDate AND @EndDate
        GROUP BY DATEPART(dw, t.date), DATEPART(hour, t.date)
    ),
    TypeTotals AS (
        SELECT activity_type, SUM(activity_count) as total_for_type
        FROM ActivityCounts
        GROUP BY activity_type
    )
    SELECT 
        CASE ac.day_of_week
            WHEN 1 THEN 'Sunday' WHEN 2 THEN 'Monday' WHEN 3 THEN 'Tuesday'
            WHEN 4 THEN 'Wednesday' WHEN 5 THEN 'Thursday' WHEN 6 THEN 'Friday'
            WHEN 7 THEN 'Saturday'
        END as day_name,
        ac.hour_of_day,
        ac.activity_type,
        ac.activity_count,
        CAST(ac.activity_count * 100.0 / NULLIF(tt.total_for_type, 0) AS DECIMAL(5,2)) as percentage_of_total
    FROM ActivityCounts ac
    JOIN TypeTotals tt ON ac.activity_type = tt.activity_type
    ORDER BY ac.day_of_week, ac.hour_of_day, ac.activity_type
END
GO

-- Inventory Health Report
CREATE OR ALTER PROCEDURE GetInventoryHealthReport
AS
BEGIN
    SET NOCOUNT ON;
    
    SELECT 
        b.title,
        b.isbn,
        COUNT(bc.id) as total_copies,
        SUM(CASE WHEN bc.status = 'AVAILABLE' THEN 1 ELSE 0 END) as available_copies,
        SUM(CASE WHEN bc.status = 'CHECKED_OUT' THEN 1 ELSE 0 END) as checked_out_copies,
        SUM(CASE WHEN bc.status = 'IN_REPAIR' THEN 1 ELSE 0 END) as repair_copies,
        SUM(CASE WHEN bc.status = 'LOST' THEN 1 ELSE 0 END) as lost_copies,
        COUNT(DISTINCT r.id) as active_reservations,
        CAST(
            SUM(CASE WHEN bc.status = 'AVAILABLE' THEN 1 ELSE 0 END) * 100.0 / NULLIF(COUNT(bc.id), 0)
            AS DECIMAL(5,2)
        ) as availability_percentage,
        CAST(
            SUM(CASE WHEN bc.condition = 'Poor' OR bc.condition = 'Damaged' THEN 1 ELSE 0 END) * 100.0 / NULLIF(COUNT(bc.id), 0)
            AS DECIMAL(5,2)
        ) as poor_condition_percentage
    FROM books b
    LEFT JOIN book_copies bc ON b.id = bc.book_id
    LEFT JOIN reservations r ON b.id = r.book_id AND r.status = 'PENDING'
    GROUP BY b.id, b.title, b.isbn
    HAVING COUNT(bc.id) > 0
    ORDER BY availability_percentage DESC
END
GO 