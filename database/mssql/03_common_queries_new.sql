-- Common queries for library operations

-- User Management Queries

-- Get user details with roles
CREATE OR ALTER PROCEDURE GetUserWithRoles
    @Username VARCHAR(100)
AS
BEGIN
    SELECT 
        u.id, u.username, u.password_hash, u.full_name, u.email, 
        u.phone_number, u.active, u.status, u.last_login, u.created_at, u.updated_at,
        STRING_AGG(r.name, ', ') WITHIN GROUP (ORDER BY r.name) as roles
    FROM users u
    LEFT JOIN user_roles ur ON u.id = ur.user_id
    LEFT JOIN roles r ON ur.role_id = r.id
    WHERE u.username = @Username
    GROUP BY u.id, u.username, u.password_hash, u.full_name, u.email, 
             u.phone_number, u.active, u.status, u.last_login, u.created_at, u.updated_at;
END
GO

-- Book Management Queries

-- Search books by various criteria
CREATE OR ALTER PROCEDURE SearchBooks
    @SearchTerm VARCHAR(255),
    @CategoryId BIGINT = NULL
AS
BEGIN
    SELECT DISTINCT
        b.id, b.title, b.isbn, b.publication_year,
        b.cover_image, b.pages, b.publisher_id, b.category_id, 
        b.created_at, b.updated_at,
        p.name as publisher_name,
        c.name as category_name,
        STRING_AGG(a.name, ', ') WITHIN GROUP (ORDER BY a.name) as authors,
        (SELECT COUNT(*) FROM book_copies bc WHERE bc.book_id = b.id AND bc.status = 'AVAILABLE') as available_copies
    FROM books b
    LEFT JOIN publishers p ON b.publisher_id = p.id
    LEFT JOIN categories c ON b.category_id = c.id
    LEFT JOIN book_authors ba ON b.id = ba.book_id
    LEFT JOIN authors a ON ba.author_id = a.id
    WHERE (@SearchTerm IS NULL OR 
           b.title LIKE '%' + @SearchTerm + '%' OR 
           b.isbn LIKE '%' + @SearchTerm + '%' OR
           a.name LIKE '%' + @SearchTerm + '%')
        AND (@CategoryId IS NULL OR b.category_id = @CategoryId)
    GROUP BY b.id, b.title, b.isbn, b.publication_year, 
             b.cover_image, b.pages, b.publisher_id, b.category_id, 
             b.created_at, b.updated_at, p.name, c.name;
END
GO

-- Loan Management Queries

-- Check out a book
CREATE OR ALTER PROCEDURE CheckoutBook
    @MemberId BIGINT,
    @BookCopyId BIGINT,
    @DueDays INT = 14
AS
BEGIN
    BEGIN TRANSACTION;
    
    DECLARE @ErrorMsg NVARCHAR(4000);
    
    -- Check if member exists and is active
    IF NOT EXISTS (
        SELECT 1 FROM members m 
        JOIN users u ON m.user_id = u.id 
        WHERE m.user_id = @MemberId AND u.status = 'ACTIVE'
    )
    BEGIN
        SET @ErrorMsg = 'Invalid or inactive member';
        GOTO ERROR;
    END;
    
    -- Check if book copy exists and is available
    IF NOT EXISTS (
        SELECT 1 FROM book_copies 
        WHERE id = @BookCopyId AND status = 'AVAILABLE'
    )
    BEGIN
        SET @ErrorMsg = 'Book copy not available';
        GOTO ERROR;
    END;
    
    -- Create loan record
    INSERT INTO loans (member_id, book_copy_id, checkout_date, due_date, status)
    VALUES (@MemberId, @BookCopyId, GETDATE(), DATEADD(day, @DueDays, GETDATE()), 'ACTIVE');
    
    -- Update book copy status
    UPDATE book_copies 
    SET status = 'CHECKED_OUT' 
    WHERE id = @BookCopyId;
    
    -- Add to loan history
    INSERT INTO loan_history (member_id, loan_id, book_id, action)
    SELECT @MemberId, SCOPE_IDENTITY(), bc.book_id, 'CHECKOUT'
    FROM book_copies bc
    WHERE bc.id = @BookCopyId;
    
    COMMIT;
    RETURN 0;
    
    ERROR:
    ROLLBACK;
    RAISERROR(@ErrorMsg, 16, 1);
    RETURN 1;
END
GO

-- Return a book
CREATE OR ALTER PROCEDURE ReturnBook
    @LoanId BIGINT,
    @Condition VARCHAR(50)
AS
BEGIN
    BEGIN TRANSACTION;
    
    DECLARE @ErrorMsg NVARCHAR(4000);
    DECLARE @BookCopyId BIGINT;
    DECLARE @MemberId BIGINT;
    DECLARE @DueDate DATE;
    DECLARE @BookId BIGINT;
    
    -- Get loan details
    SELECT @BookCopyId = book_copy_id, @MemberId = member_id, @DueDate = due_date
    FROM loans
    WHERE id = @LoanId AND status = 'ACTIVE';
    
    IF @BookCopyId IS NULL
    BEGIN
        SET @ErrorMsg = 'Invalid or inactive loan';
        GOTO ERROR;
    END;
    
    -- Get book ID
    SELECT @BookId = book_id
    FROM book_copies
    WHERE id = @BookCopyId;
    
    -- Update loan status
    UPDATE loans
    SET status = 'RETURNED',
        return_date = GETDATE()
    WHERE id = @LoanId;
    
    -- Update book copy status and condition
    UPDATE book_copies
    SET status = 'AVAILABLE',
        condition = @Condition
    WHERE id = @BookCopyId;
    
    -- Add to loan history
    INSERT INTO loan_history (member_id, loan_id, book_id, action)
    VALUES (@MemberId, @LoanId, @BookId, 'RETURN');
    
    -- Check for overdue and create fine if necessary
    IF @DueDate < GETDATE()
    BEGIN
        DECLARE @DaysOverdue INT = DATEDIFF(day, @DueDate, GETDATE());
        DECLARE @FineAmount DECIMAL(10,2) = @DaysOverdue * 0.50; -- $0.50 per day
        
        INSERT INTO fines (member_id, loan_id, amount, reason, issue_date, due_date, status)
        VALUES (@MemberId, @LoanId, @FineAmount, 'OVERDUE', GETDATE(), DATEADD(day, 30, GETDATE()), 'PENDING');
        
        -- Update member's fine balance
        UPDATE members
        SET fine_balance = fine_balance + @FineAmount
        WHERE user_id = @MemberId;
    END;
    
    COMMIT;
    RETURN 0;
    
    ERROR:
    ROLLBACK;
    RAISERROR(@ErrorMsg, 16, 1);
    RETURN 1;
END
GO

-- Financial Management Queries

-- Process fine payment
CREATE OR ALTER PROCEDURE ProcessFinePayment
    @FineId BIGINT,
    @Amount DECIMAL(10,2),
    @PaymentMethod VARCHAR(50)
AS
BEGIN
    BEGIN TRANSACTION;
    
    DECLARE @ErrorMsg NVARCHAR(4000);
    DECLARE @MemberId BIGINT;
    DECLARE @RemainingAmount DECIMAL(10,2);
    
    -- Get fine details
    SELECT @MemberId = member_id, @RemainingAmount = amount
    FROM fines
    WHERE id = @FineId AND status = 'PENDING';
    
    IF @MemberId IS NULL
    BEGIN
        SET @ErrorMsg = 'Invalid or already paid fine';
        GOTO ERROR;
    END;
    
    IF @Amount > @RemainingAmount
    BEGIN
        SET @ErrorMsg = 'Payment amount exceeds fine amount';
        GOTO ERROR;
    END;
    
    -- Create transaction record
    INSERT INTO transactions (member_id, fine_id, amount, type, payment_method)
    VALUES (@MemberId, @FineId, @Amount, 'FINE_PAYMENT', @PaymentMethod);
    
    -- Create receipt
    INSERT INTO receipts (transaction_id, items, total)
    VALUES (SCOPE_IDENTITY(), 
            CONCAT('{"description": "Fine payment for loan ", "amount": ', @Amount, '}'),
            @Amount);
    
    -- Update fine status if fully paid
    IF @Amount = @RemainingAmount
    BEGIN
        UPDATE fines
        SET status = 'PAID'
        WHERE id = @FineId;
    END;
    
    -- Update member's fine balance
    UPDATE members
    SET fine_balance = fine_balance - @Amount
    WHERE user_id = @MemberId;
    
    COMMIT;
    RETURN 0;
    
    ERROR:
    ROLLBACK;
    RAISERROR(@ErrorMsg, 16, 1);
    RETURN 1;
END
GO

-- Reporting Queries

-- Get overdue loans report
CREATE OR ALTER PROCEDURE GetOverdueLoansReport
AS
BEGIN
    SELECT 
        l.id as loan_id,
        u.full_name as member_name,
        b.title as book_title,
        l.checkout_date,
        l.due_date,
        DATEDIFF(day, l.due_date, GETDATE()) as days_overdue,
        CAST(DATEDIFF(day, l.due_date, GETDATE()) * 0.50 AS DECIMAL(10,2)) as estimated_fine
    FROM loans l
    JOIN members m ON l.member_id = m.user_id
    JOIN users u ON m.user_id = u.id
    JOIN book_copies bc ON l.book_copy_id = bc.id
    JOIN books b ON bc.book_id = b.id
    WHERE l.status = 'ACTIVE'
        AND l.due_date < GETDATE()
    ORDER BY l.due_date ASC;
END
GO

-- Get popular books report
CREATE OR ALTER PROCEDURE GetPopularBooksReport
    @StartDate DATE,
    @EndDate DATE
AS
BEGIN
    SELECT 
        b.title,
        b.isbn,
        COUNT(DISTINCT l.id) as total_checkouts,
        AVG(CAST(
            CASE 
                WHEN l.return_date IS NOT NULL 
                THEN DATEDIFF(day, l.checkout_date, l.return_date)
                ELSE DATEDIFF(day, l.checkout_date, GETDATE())
            END AS FLOAT)
        ) as avg_loan_days,
        STRING_AGG(a.name, ', ') WITHIN GROUP (ORDER BY a.name) as authors
    FROM books b
    JOIN book_copies bc ON b.id = bc.book_id
    JOIN loans l ON bc.id = l.book_copy_id
    JOIN book_authors ba ON b.id = ba.book_id
    JOIN authors a ON ba.author_id = a.id
    WHERE l.checkout_date BETWEEN @StartDate AND @EndDate
    GROUP BY b.title, b.isbn
    ORDER BY COUNT(DISTINCT l.id) DESC
END
GO 