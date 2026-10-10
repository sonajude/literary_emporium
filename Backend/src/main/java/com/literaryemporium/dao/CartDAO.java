package com.literaryemporium.dao;

import com.literaryemporium.model.CartItem;
import com.literaryemporium.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CartDAO {

    public boolean addToCart(int userId, int bookId, int quantity) {
        String checkSql = "SELECT id, quantity FROM cart WHERE user_id = ? AND book_id = ?";
        String updateSql = "UPDATE cart SET quantity = quantity + ? WHERE id = ?";
        String insertSql = "INSERT INTO cart (user_id, book_id, quantity) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
                checkPs.setInt(1, userId);
                checkPs.setInt(2, bookId);
                try (ResultSet rs = checkPs.executeQuery()) {
                    if (rs.next()) {
                        int cartId = rs.getInt("id");
                        try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
                            updatePs.setInt(1, quantity);
                            updatePs.setInt(2, cartId);
                            return updatePs.executeUpdate() > 0;
                        }
                    }
                }
            }

            try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                insertPs.setInt(1, userId);
                insertPs.setInt(2, bookId);
                insertPs.setInt(3, quantity > 0 ? quantity : 1);
                return insertPs.executeUpdate() > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<CartItem> getCartByUser(int userId) {
        List<CartItem> list = new ArrayList<>();
        String sql = "SELECT c.id, c.user_id, c.book_id, c.quantity, c.added_at, " +
                "b.title, b.author, b.price, b.image_url " +
                "FROM cart c " +
                "JOIN books b ON c.book_id = b.id " +
                "WHERE c.user_id = ? ORDER BY c.id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CartItem item = new CartItem();
                    item.setId(rs.getInt("id"));
                    item.setUserId(rs.getInt("user_id"));
                    item.setBookId(rs.getInt("book_id"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setAddedAt(rs.getTimestamp("added_at"));
                    item.setTitle(rs.getString("title"));
                    item.setAuthor(rs.getString("author"));
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

    public boolean updateQuantity(int cartId, int quantity) {
        if (quantity <= 0) {
            return removeFromCart(cartId);
        }
        String sql = "UPDATE cart SET quantity = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, quantity);
            ps.setInt(2, cartId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean removeFromCart(int cartId) {
        String sql = "DELETE FROM cart WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, cartId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean clearCart(int userId) {
        String sql = "DELETE FROM cart WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            return ps.executeUpdate() >= 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
