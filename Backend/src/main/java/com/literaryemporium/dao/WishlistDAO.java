package com.literaryemporium.dao;

import com.literaryemporium.model.WishlistItem;
import com.literaryemporium.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class WishlistDAO {

    public boolean addToWishlist(int userId, int bookId) {
        String checkSql = "SELECT id FROM wishlist WHERE user_id = ? AND book_id = ?";
        String insertSql = "INSERT INTO wishlist (user_id, book_id) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
                checkPs.setInt(1, userId);
                checkPs.setInt(2, bookId);
                try (ResultSet rs = checkPs.executeQuery()) {
                    if (rs.next()) {
                        return true; // Already in wishlist
                    }
                }
            }

            try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                insertPs.setInt(1, userId);
                insertPs.setInt(2, bookId);
                return insertPs.executeUpdate() > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<WishlistItem> getWishlistByUser(int userId) {
        List<WishlistItem> list = new ArrayList<>();
        String sql = "SELECT w.id, w.user_id, w.book_id, w.added_at, " +
                "b.title, b.author, b.category, b.price, b.image_url " +
                "FROM wishlist w " +
                "JOIN books b ON w.book_id = b.id " +
                "WHERE w.user_id = ? ORDER BY w.id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    WishlistItem item = new WishlistItem();
                    item.setId(rs.getInt("id"));
                    item.setUserId(rs.getInt("user_id"));
                    item.setBookId(rs.getInt("book_id"));
                    item.setAddedAt(rs.getTimestamp("added_at"));
                    item.setTitle(rs.getString("title"));
                    item.setAuthor(rs.getString("author"));
                    item.setCategory(rs.getString("category"));
                    item.setPrice(rs.getBigDecimal("price"));
                    item.setImageUrl(rs.getString("image_url"));
                    list.add(item);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean removeFromWishlist(int userId, int bookId) {
        String sql = "DELETE FROM wishlist WHERE user_id = ? AND book_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, bookId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
