CREATE DATABASE IF NOT EXISTS library_db
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;
USE library_db;

CREATE TABLE IF NOT EXISTS `books` (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  isbn VARCHAR(32) NOT NULL UNIQUE,
  title VARCHAR(255) NOT NULL,
  author VARCHAR(255) NOT NULL,
  copies_total INT NOT NULL,
  copies_available INT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `members` (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL,
  phone VARCHAR(64)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `loans` (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  book_id BIGINT NOT NULL,
  member_id BIGINT NOT NULL,
  loan_date DATE NOT NULL,
  due_date DATE,
  return_date DATE,
  CONSTRAINT fk_loans_book FOREIGN KEY (book_id) REFERENCES `books`(id) ON DELETE CASCADE,
  CONSTRAINT fk_loans_member FOREIGN KEY (member_id) REFERENCES `members`(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Optional test data
-- INSERT INTO books(isbn,title,author,copies_total,copies_available) VALUES('9780000000001','Sample Book','Author One',3,3);

