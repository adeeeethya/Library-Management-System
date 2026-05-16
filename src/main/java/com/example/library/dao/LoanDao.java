package com.example.library.dao;

import com.example.library.db.Database;
import com.example.library.model.Loan;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LoanDao {
    public void createLoan(long bookId, long memberId, LocalDate dueDate) throws SQLException {
        String insertLoan = "INSERT INTO loans(book_id, member_id, loan_date, due_date) VALUES(?,?,CURDATE(),?)";
        String decAvail   = "UPDATE books SET copies_available = copies_available - 1 WHERE id=? AND copies_available > 0";
        try (Connection c = Database.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement psDec = c.prepareStatement(decAvail)) {
                psDec.setLong(1, bookId);
                int upd = psDec.executeUpdate();
                if (upd == 0) { c.rollback(); throw new SQLException("No available copies"); }
            }
            try (PreparedStatement ps = c.prepareStatement(insertLoan)) {
                ps.setLong(1, bookId);
                ps.setLong(2, memberId);
                ps.setDate(3, Date.valueOf(dueDate));
                ps.executeUpdate();
            }
            c.commit();
        }
    }

    public void returnLoan(long loanId) throws SQLException {
        String markReturned = "UPDATE loans SET return_date=CURDATE() WHERE id=? AND return_date IS NULL";
        String incAvail = "UPDATE books b JOIN loans l ON l.book_id=b.id SET b.copies_available=b.copies_available+1 WHERE l.id=? AND l.return_date IS NULL";
        try (Connection c = Database.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement ps1 = c.prepareStatement(markReturned)) {
                ps1.setLong(1, loanId);
                int upd1 = ps1.executeUpdate();
                if (upd1 == 0) { c.rollback(); return; }
            }
            try (PreparedStatement ps2 = c.prepareStatement(incAvail)) {
                ps2.setLong(1, loanId);
                ps2.executeUpdate();
            }
            c.commit();
        }
    }

    public List<Loan> listActive() throws SQLException {
        String sql = "SELECT id, book_id, member_id, loan_date, due_date, return_date FROM loans WHERE return_date IS NULL ORDER BY loan_date DESC";
        List<Loan> list = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Loan> listReturned() throws SQLException {
        String sql = "SELECT id, book_id, member_id, loan_date, due_date, return_date FROM loans WHERE return_date IS NOT NULL ORDER BY return_date DESC";
        List<Loan> list = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    private Loan map(ResultSet rs) throws SQLException {
        Loan l = new Loan();
        l.setId(rs.getLong("id"));
        l.setBookId(rs.getLong("book_id"));
        l.setMemberId(rs.getLong("member_id"));
        l.setLoanDate(rs.getDate("loan_date").toLocalDate());
        Date due = rs.getDate("due_date");
        l.setDueDate(due == null ? null : due.toLocalDate());
        Date r = rs.getDate("return_date");
        l.setReturnDate(r == null ? null : r.toLocalDate());
        return l;
    }
}


