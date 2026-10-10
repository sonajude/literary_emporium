package com.literaryemporium.dao;

import com.literaryemporium.model.CartItem;
import com.literaryemporium.model.Order;
import com.literaryemporium.model.OrderItem;
import com.literaryemporium.util.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    public int checkout(int userId) {
        CartDAO cartDAO = new CartDAO();
        List<CartItem> cartItems = cartDAO.getCartByUser(userId);
        if (cartItems.isEmpty()) {
            return -1; // Empty cart
        }

        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : cartItems) {
            BigDecimal itemTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(itemTotal);
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // 1. Insert Order
            int orderId = -1;
            String orderSql = "INSERT INTO orders (user_id, total_amount, status) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, userId);
                ps.setBigDecimal(2, total);
                ps.setString(3, "Pending");
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        orderId = rs.getInt(1);
                    }
                }
            }

            if (orderId <= 0) {
                conn.rollback();
                return -1;
            }

            // 2. Insert Order Items & Update Stock
            String itemSql = "INSERT INTO order_items (order_id, book_id, quantity, price) VALUES (?, ?, ?, ?)";
            String stockSql = "UPDATE books SET stock = GREATEST(0, stock - ?) WHERE id = ?";

            try (PreparedStatement itemPs = conn.prepareStatement(itemSql);
                 PreparedStatement stockPs = conn.prepareStatement(stockSql)) {

                for (CartItem item : cartItems) {
                    itemPs.setInt(1, orderId);
                    itemPs.setInt(2, item.getBookId());
                    itemPs.setInt(3, item.getQuantity());
                    itemPs.setBigDecimal(4, item.getPrice());
                    itemPs.addBatch();

                    stockPs.setInt(1, item.getQuantity());
                    stockPs.setInt(2, item.getBookId());
                    stockPs.addBatch();
                }

                itemPs.executeBatch();
                stockPs.executeBatch();
            }

            // 3. Clear Cart
            String clearCartSql = "DELETE FROM cart WHERE user_id = ?";
            try (PreparedStatement clearPs = conn.prepareStatement(clearCartSql)) {
                clearPs.setInt(1, userId);
                clearPs.executeUpdate();
            }

            conn.commit();
            return orderId;

        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (Exception ignored) {
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (Exception ignored) {
                }
            }
        }
        return -1;
    }

    public List<Order> getOrdersByUser(int userId) {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE user_id = ? ORDER BY id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order order = new Order();
                    order.setId(rs.getInt("id"));
                    order.setUserId(rs.getInt("user_id"));
                    order.setTotalAmount(rs.getBigDecimal("total_amount"));
                    order.setStatus(rs.getString("status"));
                    order.setOrderDate(rs.getTimestamp("order_date"));
                    order.setItems(getOrderItems(order.getId(), conn));
                    list.add(order);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Order> getAllOrders() {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT o.*, u.name as user_name, u.email as user_email " +
                "FROM orders o " +
                "LEFT JOIN users u ON o.user_id = u.id " +
                "ORDER BY o.id DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Order order = new Order();
                order.setId(rs.getInt("id"));
                order.setUserId(rs.getInt("user_id"));
                order.setTotalAmount(rs.getBigDecimal("total_amount"));
                order.setStatus(rs.getString("status"));
                order.setOrderDate(rs.getTimestamp("order_date"));
                order.setUserName(rs.getString("user_name"));
                order.setUserEmail(rs.getString("user_email"));
                order.setItems(getOrderItems(order.getId(), conn));
                list.add(order);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean updateOrderStatus(int orderId, String status) {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, orderId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private List<OrderItem> getOrderItems(int orderId, Connection conn) {
        List<OrderItem> items = new ArrayList<>();
        String sql = "SELECT oi.*, b.title, b.author, b.image_url " +
                "FROM order_items oi " +
                "LEFT JOIN books b ON oi.book_id = b.id " +
                "WHERE oi.order_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setId(rs.getInt("id"));
                    item.setOrderId(rs.getInt("order_id"));
                    item.setBookId(rs.getInt("book_id"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setPrice(rs.getBigDecimal("price"));
                    item.setTitle(rs.getString("title"));
                    item.setAuthor(rs.getString("author"));
                    item.setImageUrl(rs.getString("image_url"));
                    items.add(item);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }
}
