-- Common queries for the library management system

-- User Management Queries

-- Find user by username with roles
SELECT u.*, array_agg(r.name) as roles
FROM users u
LEFT JOIN user_roles ur ON u.id = ur.user_id
LEFT JOIN roles r ON ur.role_id = r.id
WHERE u.username = :username
GROUP BY u.id;

-- Get all active members with their fine balance
SELECT u.full_name, u.email, m.membership_end_date, m.fine_balance
FROM users u
JOIN members m ON u.id = m.user_id
WHERE u.status = 'ACTIVE'
ORDER BY u.full_name;

-- Find overdue members (membership expired)
SELECT u.full_name, u.email, m.membership_end_date
FROM users u
JOIN members m ON u.id = m.user_id
WHERE m.membership_end_date < CURRENT_DATE
AND u.status = 'ACTIVE';

-- Book Management Queries

-- Search books by title, author, or ISBN
SELECT DISTINCT b.*, a.name as author_name, p.name as publisher_name, c.name as category_name
FROM books b
LEFT JOIN book_authors ba ON b.id = ba.book_id
LEFT JOIN authors a ON ba.author_id = a.id
LEFT JOIN publishers p ON b.publisher_id = p.id
LEFT JOIN categories c ON b.category_id = c.id
WHERE 
    b.title ILIKE :search_term OR
    a.name ILIKE :search_term OR
    b.isbn = :search_term;

-- Get available copies of a book
SELECT bc.*, b.title
FROM book_copies bc
JOIN books b ON bc.book_id = b.id
WHERE bc.book_id = :book_id
AND bc.status = 'AVAILABLE';

-- Get popular books (most borrowed)
SELECT b.title, COUNT(l.id) as borrow_count
FROM books b
JOIN book_copies bc ON b.id = bc.book_id
JOIN loans l ON bc.id = l.book_copy_id
WHERE l.checkout_date >= CURRENT_DATE - INTERVAL '30 days'
GROUP BY b.id, b.title
ORDER BY borrow_count DESC
LIMIT 10;

-- Loan Management Queries

-- Get active loans for a member
SELECT l.*, b.title, bc.condition
FROM loans l
JOIN book_copies bc ON l.book_copy_id = bc.id
JOIN books b ON bc.book_id = b.id
WHERE l.member_id = :member_id
AND l.status = 'ACTIVE';

-- Find overdue loans
SELECT l.*, u.full_name as member_name, b.title
FROM loans l
JOIN members m ON l.member_id = m.user_id
JOIN users u ON m.user_id = u.id
JOIN book_copies bc ON l.book_copy_id = bc.id
JOIN books b ON bc.book_id = b.id
WHERE l.due_date < CURRENT_DATE
AND l.status = 'ACTIVE';

-- Get loan history for a book
SELECT l.*, u.full_name as member_name
FROM loans l
JOIN members m ON l.member_id = m.user_id
JOIN users u ON m.user_id = u.id
JOIN book_copies bc ON l.book_copy_id = bc.id
WHERE bc.book_id = :book_id
ORDER BY l.checkout_date DESC;

-- Reservation Management Queries

-- Get active reservations for a book
SELECT r.*, u.full_name as member_name
FROM reservations r
JOIN members m ON r.member_id = m.user_id
JOIN users u ON m.user_id = u.id
WHERE r.book_id = :book_id
AND r.status = 'PENDING'
ORDER BY r.priority_number;

-- Get member's reservations
SELECT r.*, b.title
FROM reservations r
JOIN books b ON r.book_id = b.id
WHERE r.member_id = :member_id
AND r.status IN ('PENDING', 'READY_FOR_PICKUP');

-- Financial Management Queries

-- Get unpaid fines for a member
SELECT f.*, l.checkout_date, b.title
FROM fines f
JOIN loans l ON f.loan_id = l.id
JOIN book_copies bc ON l.book_copy_id = bc.id
JOIN books b ON bc.book_id = b.id
WHERE f.member_id = :member_id
AND f.status = 'PENDING';

-- Get payment history for a member
SELECT t.*, f.reason as fine_reason
FROM transactions t
LEFT JOIN fines f ON t.fine_id = f.id
WHERE t.member_id = :member_id
ORDER BY t.date DESC;

-- Statistical Queries

-- Monthly loan statistics
SELECT 
    DATE_TRUNC('month', checkout_date) as month,
    COUNT(*) as total_loans,
    COUNT(CASE WHEN status = 'OVERDUE' THEN 1 END) as overdue_loans,
    COUNT(CASE WHEN status = 'RETURNED' THEN 1 END) as returned_loans
FROM loans
WHERE checkout_date >= CURRENT_DATE - INTERVAL '12 months'
GROUP BY DATE_TRUNC('month', checkout_date)
ORDER BY month DESC;

-- Category popularity
SELECT 
    c.name as category_name,
    COUNT(l.id) as loan_count
FROM categories c
JOIN books b ON c.id = b.category_id
JOIN book_copies bc ON b.id = bc.book_id
JOIN loans l ON bc.id = l.book_copy_id
WHERE l.checkout_date >= CURRENT_DATE - INTERVAL '6 months'
GROUP BY c.id, c.name
ORDER BY loan_count DESC;

-- Fine collection statistics
SELECT 
    DATE_TRUNC('month', date) as month,
    SUM(amount) as total_collected,
    COUNT(*) as transaction_count
FROM transactions
WHERE type = 'FINE_PAYMENT'
AND date >= CURRENT_DATE - INTERVAL '12 months'
GROUP BY DATE_TRUNC('month', date)
ORDER BY month DESC; 