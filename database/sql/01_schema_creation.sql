-- Create custom types for enums
DO $$ BEGIN
    CREATE TYPE user_status AS ENUM ('ACTIVE', 'INACTIVE', 'SUSPENDED');
    CREATE TYPE loan_action AS ENUM ('CHECKOUT', 'RETURN', 'RENEWAL', 'OVERDUE_NOTICE');
    CREATE TYPE loan_status AS ENUM ('ACTIVE', 'OVERDUE', 'RETURNED', 'LOST');
    CREATE TYPE copy_status AS ENUM ('AVAILABLE', 'CHECKED_OUT', 'IN_REPAIR', 'LOST', 'RESERVED');
    CREATE TYPE reservation_status AS ENUM ('PENDING', 'READY_FOR_PICKUP', 'COMPLETED', 'CANCELLED');
    CREATE TYPE fine_status AS ENUM ('PENDING', 'PAID', 'WAIVED', 'DISPUTED');
    CREATE TYPE fine_reason AS ENUM ('OVERDUE', 'DAMAGED_ITEM', 'LOST_ITEM', 'ADMINISTRATIVE');
    CREATE TYPE transaction_type AS ENUM ('FINE_PAYMENT', 'MEMBERSHIP_FEE', 'LOST_ITEM_FEE', 'RESERVATION_FEE', 'REFUND');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- Create tables
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone_number VARCHAR(20),
    active BOOLEAN DEFAULT true,
    status user_status DEFAULT 'ACTIVE',
    last_login DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT REFERENCES users(id),
    role_id BIGINT REFERENCES roles(id),
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE IF NOT EXISTS members (
    user_id BIGINT PRIMARY KEY REFERENCES users(id),
    membership_start_date DATE DEFAULT CURRENT_DATE,
    membership_end_date DATE,
    address TEXT,
    fine_balance DECIMAL(10,2) DEFAULT 0
);

CREATE TABLE IF NOT EXISTS librarians (
    user_id BIGINT PRIMARY KEY REFERENCES users(id),
    employment_date DATE,
    employee_id VARCHAR(20) UNIQUE
);

CREATE TABLE IF NOT EXISTS admins (
    user_id BIGINT PRIMARY KEY REFERENCES users(id),
    admin_level INTEGER,
    department VARCHAR(100),
    permissions TEXT,
    last_activity DATE
);

CREATE TABLE IF NOT EXISTS authors (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    biography TEXT,
    birth_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS publishers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address TEXT,
    contact VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS books (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    isbn VARCHAR(13) UNIQUE NOT NULL,
    publication_year INTEGER,
    description TEXT,
    cover_image VARCHAR(255),
    pages INTEGER,
    publisher_id BIGINT REFERENCES publishers(id),
    category_id BIGINT REFERENCES categories(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS book_authors (
    book_id BIGINT REFERENCES books(id),
    author_id BIGINT REFERENCES authors(id),
    PRIMARY KEY (book_id, author_id)
);

CREATE TABLE IF NOT EXISTS book_copies (
    id BIGSERIAL PRIMARY KEY,
    book_id BIGINT NOT NULL REFERENCES books(id),
    acquisition_date DATE,
    condition VARCHAR(50),
    status copy_status DEFAULT 'AVAILABLE',
    location VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS loans (
    id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL REFERENCES members(user_id),
    book_copy_id BIGINT NOT NULL REFERENCES book_copies(id),
    checkout_date DATE NOT NULL DEFAULT CURRENT_DATE,
    due_date DATE NOT NULL,
    return_date DATE,
    status loan_status DEFAULT 'ACTIVE',
    renewal_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS reservations (
    id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL REFERENCES members(user_id),
    book_id BIGINT NOT NULL REFERENCES books(id),
    reservation_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status reservation_status DEFAULT 'PENDING',
    priority_number INTEGER
);

CREATE TABLE IF NOT EXISTS fines (
    id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL REFERENCES members(user_id),
    loan_id BIGINT REFERENCES loans(id),
    amount DECIMAL(10,2) NOT NULL,
    reason fine_reason NOT NULL,
    issue_date DATE DEFAULT CURRENT_DATE,
    due_date DATE,
    status fine_status DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS transactions (
    id BIGSERIAL PRIMARY KEY,
    member_id BIGINT REFERENCES members(user_id),
    fine_id BIGINT REFERENCES fines(id),
    date DATE DEFAULT CURRENT_DATE,
    amount DECIMAL(10,2) NOT NULL,
    type transaction_type NOT NULL,
    description TEXT,
    payment_method VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS receipts (
    id BIGSERIAL PRIMARY KEY,
    transaction_id BIGINT NOT NULL REFERENCES transactions(id),
    issue_date DATE DEFAULT CURRENT_DATE,
    items TEXT,
    total DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS loan_history (
    id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL REFERENCES members(user_id),
    loan_id BIGINT NOT NULL REFERENCES loans(id),
    book_id BIGINT NOT NULL REFERENCES books(id),
    action loan_action NOT NULL,
    action_date DATE DEFAULT CURRENT_DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS reports (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    start_date DATE,
    end_date DATE,
    content TEXT,
    generated_by VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for frequently accessed columns
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_books_isbn ON books(isbn);
CREATE INDEX IF NOT EXISTS idx_books_title ON books(title);
CREATE INDEX IF NOT EXISTS idx_book_copies_status ON book_copies(status);
CREATE INDEX IF NOT EXISTS idx_loans_status ON loans(status);
CREATE INDEX IF NOT EXISTS idx_loans_due_date ON loans(due_date);
CREATE INDEX IF NOT EXISTS idx_fines_status ON fines(status);
CREATE INDEX IF NOT EXISTS idx_reservations_status ON reservations(status);

-- Create triggers for updating timestamps
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

DO $$ 
DECLARE
    t text;
BEGIN
    FOR t IN 
        SELECT table_name 
        FROM information_schema.columns 
        WHERE column_name = 'updated_at'
        AND table_schema = 'public'
    LOOP
        EXECUTE format('
            DROP TRIGGER IF EXISTS update_updated_at_trigger ON %I;
            CREATE TRIGGER update_updated_at_trigger
                BEFORE UPDATE ON %I
                FOR EACH ROW
                EXECUTE FUNCTION update_updated_at_column();
        ', t, t);
    END LOOP;
END $$; 