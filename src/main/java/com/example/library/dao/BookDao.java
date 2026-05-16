package com.example.library.dao;

import com.example.library.db.Database;
import com.example.library.model.Book;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookDao {
    public void create(Book book) throws SQLException {
        String sql = "INSERT INTO books(isbn, title, author, copies_total, copies_available) VALUES(?,?,?,?,?)";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, book.getIsbn());
            ps.setString(2, book.getTitle());
            ps.setString(3, book.getAuthor());
            ps.setInt(4, book.getCopiesTotal());
            ps.setInt(5, book.getCopiesAvailable());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    book.setId(rs.getLong(1));
                }
            }
        }
    }

    public List<Book> listAll() throws SQLException {
        String sql = "SELECT id, isbn, title, author, copies_total, copies_available FROM books ORDER BY id";
        List<Book> books = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Book b = map(rs);
                books.add(b);
            }
        }
        return books;
    }

    public Book findByIsbn(String isbn) throws SQLException {
        String sql = "SELECT id, isbn, title, author, copies_total, copies_available FROM books WHERE isbn=?";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, isbn);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
                return null;
            }
        }
    }

    public void updateAvailability(long bookId, int copiesAvailable) throws SQLException {
        String sql = "UPDATE books SET copies_available=? WHERE id=?";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, copiesAvailable);
            ps.setLong(2, bookId);
            ps.executeUpdate();
        }
    }

    private Book map(ResultSet rs) throws SQLException {
        Book b = new Book();
        b.setId(rs.getLong("id"));
        b.setIsbn(rs.getString("isbn"));
        b.setTitle(rs.getString("title"));
        b.setAuthor(rs.getString("author"));
        b.setCopiesTotal(rs.getInt("copies_total"));
        b.setCopiesAvailable(rs.getInt("copies_available"));
        return b;
    }
}


