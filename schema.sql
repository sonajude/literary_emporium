-- ========================================================
-- LITERARY EMPORIUM - DATABASE SCHEMA & SEED DATA
-- Database: literary_emporium
-- ========================================================

CREATE DATABASE IF NOT EXISTS literary_emporium;
USE literary_emporium;

-- 1. USERS TABLE
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL -- 'buyer', 'seller', 'admin'
);

-- 2. BOOKS TABLE
CREATE TABLE IF NOT EXISTS books (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    author VARCHAR(150) NOT NULL,
    category VARCHAR(100) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    description TEXT,
    image_url VARCHAR(500),
    seller_id INT,
    stock INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE SET NULL
);

-- 3. CART TABLE
CREATE TABLE IF NOT EXISTS cart (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    book_id INT NOT NULL,
    quantity INT DEFAULT 1,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE
);

-- 4. ORDERS TABLE
CREATE TABLE IF NOT EXISTS orders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(50) DEFAULT 'Pending',
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 5. ORDER_ITEMS TABLE
CREATE TABLE IF NOT EXISTS order_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    book_id INT NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE
);

-- 6. WISHLIST TABLE
CREATE TABLE IF NOT EXISTS wishlist (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    book_id INT NOT NULL,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE
);

-- 7. REVIEWS TABLE
CREATE TABLE IF NOT EXISTS reviews (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    book_id INT NOT NULL,
    rating INT NOT NULL,
    comment TEXT,
    review_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE
);

-- ========================================================
-- SEED DATA (Default Accounts & Books)
-- Passwords are hashed with SHA-256
-- admin123, seller123, buyer123
-- ========================================================

INSERT IGNORE INTO users (id, name, email, password, role) VALUES 
(1, 'System Administrator', 'admin@literaryemporium.com', SHA2('admin123', 256), 'admin'),
(2, 'Heritage Booksellers', 'seller@literaryemporium.com', SHA2('seller123', 256), 'seller'),
(3, 'Kavitha R', 'buyer@literaryemporium.com', SHA2('buyer123', 256), 'buyer');

INSERT INTO books (id, title, author, category, price, description, image_url, seller_id, stock) VALUES
(1, 'The Silent Library', 'A. Martin', 'Fiction', 450.00, 'A captivating mystery set within the corridors of a forgotten historical archive.', 'images/the-silent-library.jpg', 2, 15),
(2, 'Computer Science Essentials', 'Dr. K. Raman', 'Academic', 650.00, 'Comprehensive guide to core algorithms, data structures, and computer architecture.', 'images/computer-science-essentials.jpg', 2, 20),
(3, 'Ideas That Changed Society', 'Prof. Sundaram', 'Non-Fiction', 499.00, 'A historical exploration of progressive philosophical movements and sociological shifts.', 'images/ideas-that-changed-society.jpg', 2, 12),
(4, 'Lives That Inspired', 'M. Balaji', 'Biography', 520.00, 'Inspiring biographical accounts of global pioneers, leaders, and scientific visionaries.', 'images/lives-that-inspired.jpg', 2, 18),
(5, 'Foundations of Science', 'Dr. S. Priya', 'Academic', 580.00, 'Fundamental principles across classical physics, chemistry, and empirical enquiry.', 'images/foundations-of-science.jpg', 2, 25),
(6, 'The Last Journey', 'V. Raghavan', 'Fiction', 399.00, 'An emotional adventure novel detailing the final odyssey across forgotten territories.', 'images/the-last-journey.jpg', 2, 10),
(7, 'A Life of Purpose', 'Anand Sharma', 'Biography', 480.00, 'Lessons and philosophical reflections on self-discipline, resilience, and personal triumph.', 'images/a-life-of-purpose.jpg', 2, 14),
(8, 'Understanding the World', 'T. Narayanan', 'Non-Fiction', 540.00, 'A macro analysis of global economies, environmental cycles, and contemporary politics.', 'images/understanding-the-world.jpg', 2, 16)
ON DUPLICATE KEY UPDATE title=VALUES(title), price=VALUES(price), stock=VALUES(stock);
