package com.example.library.dao;

import com.example.library.db.Database;
import com.example.library.model.Member;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MemberDao {
    public void create(Member m) throws SQLException {
        String sql = "INSERT INTO members(name, email, phone) VALUES(?,?,?)";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, m.getName());
            ps.setString(2, m.getEmail());
            ps.setString(3, m.getPhone());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) m.setId(rs.getLong(1));
            }
        }
    }

    public List<Member> listAll() throws SQLException {
        String sql = "SELECT id, name, email, phone FROM members ORDER BY id";
        List<Member> list = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public Member findById(long id) throws SQLException {
        String sql = "SELECT id, name, email, phone FROM members WHERE id=?";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
                return null;
            }
        }
    }

    private Member map(ResultSet rs) throws SQLException {
        Member m = new Member();
        m.setId(rs.getLong("id"));
        m.setName(rs.getString("name"));
        m.setEmail(rs.getString("email"));
        m.setPhone(rs.getString("phone"));
        return m;
    }
}


