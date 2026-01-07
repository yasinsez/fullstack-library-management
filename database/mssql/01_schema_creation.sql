-- Library Management System Database Schema for MS SQL Server

-- Create tables
CREATE TABLE users (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone_number VARCHAR(20),
    active BIT DEFAULT 1,
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED')),
    last_login DATE,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE()
);

CREATE TABLE roles (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE()
);

CREATE TABLE user_roles (
    user_id BIGINT REFERENCES users(id),
    role_id BIGINT REFERENCES roles(id),
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE members (
    user_id BIGINT PRIMARY KEY REFERENCES users(id),
    membership_start_date DATE DEFAULT GETDATE(),
    membership_end_date DATE,
    address TEXT,
    fine_balance DECIMAL(10,2) DEFAULT 0
);

CREATE TABLE librarians (
    user_id BIGINT PRIMARY KEY REFERENCES users(id),
    employment_date DATE,
    employee_id VARCHAR(20) UNIQUE
);

CREATE TABLE admins (
    user_id BIGINT PRIMARY KEY REFERENCES users(id),
    admin_level INT,
    department VARCHAR(100),
    permissions TEXT,
    last_activity DATE
);

CREATE TABLE authors (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    biography TEXT,
    birth_date DATE,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE()
);

CREATE TABLE publishers (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address TEXT,
    contact VARCHAR(100),
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE()
);

CREATE TABLE categories (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE()
);

CREATE TABLE books (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    isbn VARCHAR(13) UNIQUE NOT NULL,
    publication_year INT,
    description TEXT,
    cover_image VARCHAR(255),
    pages INT,
    publisher_id BIGINT REFERENCES publishers(id),
    category_id BIGINT REFERENCES categories(id),
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE()
);

CREATE TABLE book_authors (
    book_id BIGINT REFERENCES books(id),
    author_id BIGINT REFERENCES authors(id),
    PRIMARY KEY (book_id, author_id)
);

CREATE TABLE book_copies (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    book_id BIGINT NOT NULL REFERENCES books(id),
    acquisition_date DATE,
    condition VARCHAR(50),
    status VARCHAR(20) DEFAULT 'AVAILABLE' CHECK (status IN ('AVAILABLE', 'CHECKED_OUT', 'IN_REPAIR', 'LOST', 'RESERVED')),
    location VARCHAR(100),
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE()
);

CREATE TABLE loans (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    member_id BIGINT NOT NULL REFERENCES members(user_id),
    book_copy_id BIGINT NOT NULL REFERENCES book_copies(id),
    checkout_date DATE NOT NULL DEFAULT GETDATE(),
    due_date DATE NOT NULL,
    return_date DATE,
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'OVERDUE', 'RETURNED', 'LOST')),
    renewal_count INT DEFAULT 0,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE()
);

CREATE TABLE reservations (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    member_id BIGINT NOT NULL REFERENCES members(user_id),
    book_id BIGINT NOT NULL REFERENCES books(id),
    reservation_time DATETIME2 DEFAULT GETDATE(),
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'READY_FOR_PICKUP', 'COMPLETED', 'CANCELLED')),
    priority_number INT
);

CREATE TABLE fines (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    member_id BIGINT NOT NULL REFERENCES members(user_id),
    loan_id BIGINT REFERENCES loans(id),
    amount DECIMAL(10,2) NOT NULL,
    reason VARCHAR(20) NOT NULL CHECK (reason IN ('OVERDUE', 'DAMAGED_ITEM', 'LOST_ITEM', 'ADMINISTRATIVE')),
    issue_date DATE DEFAULT GETDATE(),
    due_date DATE,
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'PAID', 'WAIVED', 'DISPUTED')),
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE()
);

CREATE TABLE transactions (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    member_id BIGINT REFERENCES members(user_id),
    fine_id BIGINT REFERENCES fines(id),
    date DATE DEFAULT GETDATE(),
    amount DECIMAL(10,2) NOT NULL,
    type VARCHAR(20) NOT NULL CHECK (type IN ('FINE_PAYMENT', 'MEMBERSHIP_FEE', 'LOST_ITEM_FEE', 'RESERVATION_FEE', 'REFUND')),
    description TEXT,
    payment_method VARCHAR(50),
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE()
);

CREATE TABLE receipts (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    transaction_id BIGINT NOT NULL REFERENCES transactions(id),
    issue_date DATE DEFAULT GETDATE(),
    items TEXT,
    total DECIMAL(10,2) NOT NULL,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE()
);

CREATE TABLE loan_history (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    member_id BIGINT NOT NULL REFERENCES members(user_id),
    loan_id BIGINT NOT NULL REFERENCES loans(id),
    book_id BIGINT NOT NULL REFERENCES books(id),
    action VARCHAR(20) NOT NULL CHECK (action IN ('CHECKOUT', 'RETURN', 'RENEWAL', 'OVERDUE_NOTICE')),
    action_date DATE DEFAULT GETDATE(),
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE()
);

CREATE TABLE reports (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    start_date DATE,
    end_date DATE,
    content TEXT,
    generated_by VARCHAR(255),
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE()
);

-- Create indexes
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_books_isbn ON books(isbn);
CREATE INDEX idx_books_title ON books(title);
CREATE INDEX idx_book_copies_status ON book_copies(status);
CREATE INDEX idx_loans_status ON loans(status);
CREATE INDEX idx_loans_due_date ON loans(due_date);
CREATE INDEX idx_fines_status ON fines(status);
CREATE INDEX idx_reservations_status ON reservations(status);

-- Note: Triggers for automatic timestamp updates are not included in this version
-- due to batch separator requirements. If you need automatic timestamp updates,
-- you can create them separately using a tool that supports GO statements like SSMS.
-- 
-- Example trigger (run separately):
-- CREATE TRIGGER update_users_timestamp ON users AFTER UPDATE AS
-- BEGIN
--     UPDATE users SET updated_at = GETDATE()
--     FROM users u INNER JOIN inserted i ON u.id = i.id;
-- END; 