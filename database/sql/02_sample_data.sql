-- Sample data for testing and development

-- Roles
INSERT INTO roles (name, description) VALUES
('ROLE_ADMIN', 'System administrator with full access'),
('ROLE_LIBRARIAN', 'Library staff member'),
('ROLE_MEMBER', 'Regular library member');

-- Users (passwords are hashed versions of 'password123')
INSERT INTO users (username, password_hash, full_name, email, phone_number, status) VALUES
('admin1', '$2a$10$xPJ5sYqK8YxX9f9N1zE5o.Rz1y3kX9k4j5J6J5J6J5J6J5J6J5', 'Admin User', 'admin@library.com', '1234567890', 'ACTIVE'),
('librarian1', '$2a$10$xPJ5sYqK8YxX9f9N1zE5o.Rz1y3kX9k4j5J6J5J6J5J6J5J6J5', 'John Librarian', 'john@library.com', '1234567891', 'ACTIVE'),
('member1', '$2a$10$xPJ5sYqK8YxX9f9N1zE5o.Rz1y3kX9k4j5J6J5J6J5J6J5J6J5', 'Alice Member', 'alice@email.com', '1234567892', 'ACTIVE'),
('member2', '$2a$10$xPJ5sYqK8YxX9f9N1zE5o.Rz1y3kX9k4j5J6J5J6J5J6J5J6J5', 'Bob Member', 'bob@email.com', '1234567893', 'ACTIVE');

-- User roles
INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1), -- admin1 -> ROLE_ADMIN
(2, 2), -- librarian1 -> ROLE_LIBRARIAN
(3, 3), -- member1 -> ROLE_MEMBER
(4, 3); -- member2 -> ROLE_MEMBER

-- Extended user information
INSERT INTO admins (user_id, admin_level, department) VALUES
(1, 1, 'System Administration');

INSERT INTO librarians (user_id, employment_date, employee_id) VALUES
(2, '2023-01-01', 'LIB001');

INSERT INTO members (user_id, membership_start_date, membership_end_date, address) VALUES
(3, '2023-01-01', '2024-01-01', '123 Main St, City'),
(4, '2023-01-01', '2024-01-01', '456 Oak St, City');

-- Categories
INSERT INTO categories (name, description) VALUES
('Fiction', 'Fictional literature and novels'),
('Non-Fiction', 'Educational and factual books'),
('Science', 'Scientific publications and research'),
('Technology', 'Books about computers and technology'),
('History', 'Historical books and documentaries');

-- Publishers
INSERT INTO publishers (name, address, contact) VALUES
('Tech Books Inc', '789 Publisher Ave, City', 'contact@techbooks.com'),
('History Press', '101 History Rd, City', 'info@historypress.com'),
('Science Publications', '202 Science Blvd, City', 'contact@sciencepub.com');

-- Authors
INSERT INTO authors (name, biography, birth_date) VALUES
('John Smith', 'Renowned technology author', '1980-01-01'),
('Jane Doe', 'Historical fiction writer', '1975-06-15'),
('Robert Johnson', 'Science researcher and author', '1982-03-22');

-- Books
INSERT INTO books (title, isbn, publication_year, description, pages, publisher_id, category_id) VALUES
('Advanced Programming', '1234567890123', 2023, 'Guide to advanced programming concepts', 400, 1, 4),
('World War II: A History', '2345678901234', 2022, 'Comprehensive history of WWII', 500, 2, 5),
('Modern Physics', '3456789012345', 2023, 'Introduction to modern physics', 300, 3, 3);

-- Book Authors
INSERT INTO book_authors (book_id, author_id) VALUES
(1, 1), -- Advanced Programming -> John Smith
(2, 2), -- World War II -> Jane Doe
(3, 3); -- Modern Physics -> Robert Johnson

-- Book Copies
INSERT INTO book_copies (book_id, acquisition_date, condition, status, location) VALUES
(1, '2023-01-01', 'New', 'AVAILABLE', 'Shelf A1'),
(1, '2023-01-01', 'New', 'AVAILABLE', 'Shelf A2'),
(2, '2023-01-01', 'New', 'AVAILABLE', 'Shelf B1'),
(3, '2023-01-01', 'New', 'CHECKED_OUT', 'Shelf C1');

-- Loans
INSERT INTO loans (member_id, book_copy_id, checkout_date, due_date, status) VALUES
(3, 1, '2023-06-01', '2023-06-15', 'RETURNED'),
(4, 4, CURRENT_DATE - INTERVAL '5 days', CURRENT_DATE + INTERVAL '9 days', 'ACTIVE');

-- Loan History
INSERT INTO loan_history (member_id, loan_id, book_id, action, action_date) VALUES
(3, 1, 1, 'CHECKOUT', '2023-06-01'),
(3, 1, 1, 'RETURN', '2023-06-15'),
(4, 2, 3, 'CHECKOUT', CURRENT_DATE - INTERVAL '5 days');

-- Reservations
INSERT INTO reservations (member_id, book_id, status, priority_number) VALUES
(3, 2, 'PENDING', 1);

-- Fines
INSERT INTO fines (member_id, loan_id, amount, reason, issue_date, due_date, status) VALUES
(3, 1, 5.00, 'OVERDUE', '2023-06-16', '2023-06-30', 'PAID');

-- Transactions
INSERT INTO transactions (member_id, fine_id, amount, type, payment_method) VALUES
(3, 1, 5.00, 'FINE_PAYMENT', 'CREDIT_CARD');

-- Receipts
INSERT INTO receipts (transaction_id, items, total) VALUES
(1, '[{"description": "Late return fine", "amount": 5.00}]', 5.00);

-- Reports
INSERT INTO reports (title, start_date, end_date, content, generated_by) VALUES
('Monthly Loan Report', '2023-06-01', '2023-06-30', 
 '{"total_loans": 2, "total_returns": 1, "total_fines": 5.00}', 
 'System'); 