-- Advanced reporting and analytics queries

-- 1. Library Usage Analytics

-- Daily check-out trends
CREATE OR REPLACE VIEW daily_checkouts AS
SELECT 
    DATE_TRUNC('day', checkout_date) as day,
    COUNT(*) as total_checkouts,
    COUNT(DISTINCT member_id) as unique_members
FROM loans
GROUP BY DATE_TRUNC('day', checkout_date)
ORDER BY day;

-- Book utilization rate
CREATE OR REPLACE VIEW book_utilization AS
SELECT 
    b.title,
    b.isbn,
    COUNT(DISTINCT bc.id) as total_copies,
    COUNT(DISTINCT CASE WHEN bc.status = 'CHECKED_OUT' THEN bc.id END) as checked_out_copies,
    ROUND(COUNT(DISTINCT CASE WHEN bc.status = 'CHECKED_OUT' THEN bc.id END)::DECIMAL / 
          COUNT(DISTINCT bc.id) * 100, 2) as utilization_rate
FROM books b
JOIN book_copies bc ON b.id = bc.book_id
GROUP BY b.id, b.title, b.isbn
ORDER BY utilization_rate DESC;

-- 2. Member Activity Analysis

-- Member engagement score
CREATE OR REPLACE VIEW member_engagement AS
WITH loan_counts AS (
    SELECT 
        m.user_id,
        COUNT(l.id) as total_loans,
        COUNT(CASE WHEN l.status = 'OVERDUE' THEN 1 END) as overdue_loans,
        COUNT(CASE WHEN l.return_date <= l.due_date THEN 1 END) as on_time_returns
    FROM members m
    LEFT JOIN loans l ON m.user_id = l.member_id
    WHERE l.checkout_date >= CURRENT_DATE - INTERVAL '6 months'
    GROUP BY m.user_id
)
SELECT 
    u.full_name,
    lc.total_loans,
    lc.overdue_loans,
    lc.on_time_returns,
    ROUND((lc.on_time_returns::DECIMAL / NULLIF(lc.total_loans, 0) * 100), 2) as return_rate,
    CASE 
        WHEN lc.total_loans = 0 THEN 'Inactive'
        WHEN lc.overdue_loans > lc.total_loans * 0.3 THEN 'At Risk'
        WHEN lc.on_time_returns > lc.total_loans * 0.8 THEN 'Excellent'
        ELSE 'Good'
    END as member_status
FROM users u
JOIN members m ON u.id = m.user_id
LEFT JOIN loan_counts lc ON m.user_id = lc.user_id;

-- 3. Collection Performance Metrics

-- Category performance analysis
CREATE OR REPLACE VIEW category_performance AS
WITH category_stats AS (
    SELECT 
        c.id,
        c.name,
        COUNT(DISTINCT b.id) as total_books,
        COUNT(DISTINCT bc.id) as total_copies,
        COUNT(DISTINCT l.id) as total_loans
    FROM categories c
    LEFT JOIN books b ON c.id = b.category_id
    LEFT JOIN book_copies bc ON b.id = bc.book_id
    LEFT JOIN loans l ON bc.id = l.book_copy_id
    AND l.checkout_date >= CURRENT_DATE - INTERVAL '6 months'
    GROUP BY c.id, c.name
)
SELECT 
    name as category_name,
    total_books,
    total_copies,
    total_loans,
    ROUND(total_loans::DECIMAL / NULLIF(total_copies, 0), 2) as loans_per_copy,
    CASE 
        WHEN total_loans = 0 THEN 'Underutilized'
        WHEN total_loans > total_copies * 2 THEN 'High Demand'
        ELSE 'Normal'
    END as category_status
FROM category_stats
ORDER BY loans_per_copy DESC;

-- 4. Financial Analytics

-- Revenue breakdown
CREATE OR REPLACE VIEW revenue_analysis AS
WITH monthly_revenue AS (
    SELECT 
        DATE_TRUNC('month', date) as month,
        type,
        SUM(amount) as total_amount,
        COUNT(*) as transaction_count
    FROM transactions
    WHERE date >= CURRENT_DATE - INTERVAL '12 months'
    GROUP BY DATE_TRUNC('month', date), type
)
SELECT 
    month,
    type,
    total_amount,
    transaction_count,
    ROUND(total_amount::DECIMAL / transaction_count, 2) as avg_transaction_amount,
    ROUND(total_amount::DECIMAL / SUM(total_amount) OVER (PARTITION BY month) * 100, 2) as percentage_of_monthly_revenue
FROM monthly_revenue
ORDER BY month DESC, total_amount DESC;

-- 5. Operational Efficiency Metrics

-- Staff performance metrics
CREATE OR REPLACE VIEW staff_performance AS
SELECT 
    u.full_name as librarian_name,
    COUNT(l.id) as loans_processed,
    COUNT(DISTINCT m.user_id) as members_served,
    COUNT(DISTINCT bc.book_id) as unique_books_handled,
    ROUND(COUNT(l.id)::DECIMAL / 
          EXTRACT(DAYS FROM (CURRENT_DATE - MIN(l.checkout_date))), 2) as avg_daily_loans
FROM users u
JOIN librarians lib ON u.id = lib.user_id
JOIN loans l ON l.checkout_date >= CURRENT_DATE - INTERVAL '30 days'
JOIN members m ON l.member_id = m.user_id
JOIN book_copies bc ON l.book_copy_id = bc.id
GROUP BY u.id, u.full_name;

-- 6. Inventory Health Check

-- Collection age analysis
CREATE OR REPLACE VIEW collection_age AS
SELECT 
    c.name as category_name,
    COUNT(DISTINCT b.id) as total_books,
    ROUND(AVG(EXTRACT(YEAR FROM CURRENT_DATE) - b.publication_year)) as avg_age,
    COUNT(CASE WHEN EXTRACT(YEAR FROM CURRENT_DATE) - b.publication_year > 10 THEN 1 END) as books_over_10_years,
    ROUND(COUNT(CASE WHEN EXTRACT(YEAR FROM CURRENT_DATE) - b.publication_year > 10 THEN 1 END)::DECIMAL / 
          COUNT(DISTINCT b.id) * 100, 2) as percentage_over_10_years
FROM categories c
JOIN books b ON c.id = b.category_id
GROUP BY c.id, c.name
ORDER BY avg_age DESC;

-- Book condition summary
CREATE OR REPLACE VIEW book_condition_summary AS
SELECT 
    b.title,
    COUNT(bc.id) as total_copies,
    STRING_AGG(DISTINCT bc.condition, ', ') as conditions,
    COUNT(CASE WHEN bc.condition ILIKE '%poor%' OR bc.condition ILIKE '%damaged%' THEN 1 END) as copies_needing_attention
FROM books b
JOIN book_copies bc ON b.id = bc.book_id
GROUP BY b.id, b.title
HAVING COUNT(CASE WHEN bc.condition ILIKE '%poor%' OR bc.condition ILIKE '%damaged%' THEN 1 END) > 0
ORDER BY copies_needing_attention DESC;

-- 7. Reservation System Analysis

-- Reservation fulfillment metrics
CREATE OR REPLACE VIEW reservation_metrics AS
WITH reservation_stats AS (
    SELECT 
        DATE_TRUNC('month', reservation_time) as month,
        COUNT(*) as total_reservations,
        COUNT(CASE WHEN status = 'COMPLETED' THEN 1 END) as fulfilled_reservations,
        AVG(CASE 
            WHEN status = 'COMPLETED' 
            THEN EXTRACT(EPOCH FROM (updated_at - reservation_time))/86400.0 
        END) as avg_fulfillment_days
    FROM reservations
    WHERE reservation_time >= CURRENT_DATE - INTERVAL '6 months'
    GROUP BY DATE_TRUNC('month', reservation_time)
)
SELECT 
    month,
    total_reservations,
    fulfilled_reservations,
    ROUND(fulfilled_reservations::DECIMAL / total_reservations * 100, 2) as fulfillment_rate,
    ROUND(avg_fulfillment_days, 1) as avg_fulfillment_days
FROM reservation_stats
ORDER BY month DESC;

-- 8. Generate Summary Report

CREATE OR REPLACE FUNCTION generate_monthly_summary(report_month DATE)
RETURNS TABLE (
    metric_name TEXT,
    metric_value TEXT
) AS $$
BEGIN
    RETURN QUERY
    
    -- Total active members
    SELECT 'Active Members', COUNT(*)::TEXT
    FROM users u
    JOIN members m ON u.id = m.user_id
    WHERE u.status = 'ACTIVE'
    
    UNION ALL
    
    -- Total loans
    SELECT 'Monthly Loans', COUNT(*)::TEXT
    FROM loans
    WHERE DATE_TRUNC('month', checkout_date) = DATE_TRUNC('month', report_month)
    
    UNION ALL
    
    -- Average daily checkouts
    SELECT 'Avg Daily Checkouts', 
           ROUND(COUNT(*)::DECIMAL / EXTRACT(DAYS FROM DATE_TRUNC('month', report_month) + INTERVAL '1 month - 1 day'), 2)::TEXT
    FROM loans
    WHERE DATE_TRUNC('month', checkout_date) = DATE_TRUNC('month', report_month)
    
    UNION ALL
    
    -- Total revenue
    SELECT 'Monthly Revenue', CONCAT('$', SUM(amount)::TEXT)
    FROM transactions
    WHERE DATE_TRUNC('month', date) = DATE_TRUNC('month', report_month)
    
    UNION ALL
    
    -- Most popular category
    SELECT 'Most Popular Category', c.name
    FROM categories c
    JOIN books b ON c.id = b.category_id
    JOIN book_copies bc ON b.id = bc.book_id
    JOIN loans l ON bc.id = l.book_copy_id
    WHERE DATE_TRUNC('month', l.checkout_date) = DATE_TRUNC('month', report_month)
    GROUP BY c.id, c.name
    ORDER BY COUNT(*) DESC
    LIMIT 1
    
    UNION ALL
    
    -- Overdue rate
    SELECT 'Overdue Rate', 
           ROUND(COUNT(CASE WHEN status = 'OVERDUE' THEN 1 END)::DECIMAL / COUNT(*) * 100, 2) || '%'
    FROM loans
    WHERE DATE_TRUNC('month', checkout_date) = DATE_TRUNC('month', report_month);
    
END;
$$ LANGUAGE plpgsql; 