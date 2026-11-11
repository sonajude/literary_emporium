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
            return -1;
        }

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<>();
        for (CartItem item : cartItems) {
            BigDecimal itemTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(itemTotal);

            OrderItem oi = new OrderItem();
            oi.setBookId(item.getBookId());
            oi.setQuantity(item.getQuantity());
            oi.setPrice(item.getPrice());
            items.add(oi);
        }

        Order order = new Order();
        order.setUserId(userId);
        order.setOrderCode("ORD-" + System.currentTimeMillis());
        order.setTotalAmount(total);
        order.setStatus("Order Placed");
        order.setPaymentMethod("Pay Later");
        order.setPaymentStatus("PENDING");
        order.setItems(items);

        return createOrder(order);
    }

    public int createOrder(Order order) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            if (order.getOrderCode() == null || order.getOrderCode().trim().isEmpty()) {
                order.setOrderCode("ORD-" + System.currentTimeMillis());
            }

            int orderId = -1;
            String orderSql = "INSERT INTO orders (order_code, user_id, total_amount, status, " +
                              "payment_method, payment_status, payment_id, " +
                              "customer_name, customer_email, customer_phone, delivery_address) " +
                              "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement ps = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, order.getOrderCode());
                ps.setInt(2, order.getUserId());
                ps.setBigDecimal(3, order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO);
                ps.setString(4, order.getStatus() != null ? order.getStatus() : "Order Placed");
                ps.setString(5, order.getPaymentMethod() != null ? order.getPaymentMethod() : "Pay Later");
                ps.setString(6, order.getPaymentStatus() != null ? order.getPaymentStatus() : "PENDING");
                ps.setString(7, order.getPaymentId());
                ps.setString(8, order.getCustomerName());
                ps.setString(9, order.getCustomerEmail());
                ps.setString(10, order.getCustomerPhone());
                ps.setString(11, order.getDeliveryAddress());

                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        orderId = rs.getInt(1);
                        order.setId(orderId);
                    }
                }
            }

            if (orderId <= 0) {
                conn.rollback();
                return -1;
            }

            // Insert order items & deduct stock
            if (order.getItems() != null && !order.getItems().isEmpty()) {
                String itemSql = "INSERT INTO order_items (order_id, book_id, quantity, price) VALUES (?, ?, ?, ?)";
                String stockSql = "UPDATE books SET stock = GREATEST(0, stock - ?) WHERE id = ?";

                try (PreparedStatement itemPs = conn.prepareStatement(itemSql);
                     PreparedStatement stockPs = conn.prepareStatement(stockSql)) {

                    for (OrderItem item : order.getItems()) {
                        int bId = item.getBookId() > 0 ? item.getBookId() : 1;
                        int qty = item.getQuantity() > 0 ? item.getQuantity() : 1;
                        BigDecimal prc = item.getPrice() != null ? item.getPrice() : BigDecimal.valueOf(450);

                        itemPs.setInt(1, orderId);
                        itemPs.setInt(2, bId);
                        itemPs.setInt(3, qty);
                        itemPs.setBigDecimal(4, prc);
                        itemPs.addBatch();

                        stockPs.setInt(1, qty);
                        stockPs.setInt(2, bId);
                        stockPs.addBatch();
                    }

                    itemPs.executeBatch();
                    stockPs.executeBatch();
                }
            }

            // Clear buyer's cart
            if (order.getUserId() > 0) {
                String clearCartSql = "DELETE FROM cart WHERE user_id = ?";
                try (PreparedStatement clearPs = conn.prepareStatement(clearCartSql)) {
                    clearPs.setInt(1, order.getUserId());
                    clearPs.executeUpdate();
                }
            }

            // If paymentId is present, link payment record
            if (order.getPaymentId() != null && !order.getPaymentId().trim().isEmpty()) {
                String linkSql = "UPDATE payments SET order_id = ?, order_code = ? WHERE payment_id = ?";
                try (PreparedStatement linkPs = conn.prepareStatement(linkSql)) {
                    linkPs.setInt(1, orderId);
                    linkPs.setString(2, order.getOrderCode());
                    linkPs.setString(3, order.getPaymentId());
                    linkPs.executeUpdate();
                }
            }

            conn.commit();
            return orderId;

        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (Exception ignored) {}
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (Exception ignored) {}
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
                    Order order = mapOrderRow(rs);
                    order.setItems(getOrderItems(order.getId(), conn));
                    list.add(order);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public Order getOrderById(int orderId) {
        String sql = "SELECT o.*, u.name as user_name, u.email as user_email " +
                     "FROM orders o LEFT JOIN users u ON o.user_id = u.id WHERE o.id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Order order = mapOrderRow(rs);
                    order.setUserName(rs.getString("user_name"));
                    order.setUserEmail(rs.getString("user_email"));
                    order.setItems(getOrderItems(order.getId(), conn));
                    return order;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Order getOrderByCode(String orderCode) {
        String sql = "SELECT o.*, u.name as user_name, u.email as user_email " +
                     "FROM orders o LEFT JOIN users u ON o.user_id = u.id WHERE o.order_code = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orderCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Order order = mapOrderRow(rs);
                    order.setUserName(rs.getString("user_name"));
                    order.setUserEmail(rs.getString("user_email"));
                    order.setItems(getOrderItems(order.getId(), conn));
                    return order;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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
                Order order = mapOrderRow(rs);
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

    public boolean updatePaymentStatus(int orderId, String paymentStatus) {
        String sql = "UPDATE orders SET payment_status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, paymentStatus);
            ps.setInt(2, orderId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private Order mapOrderRow(ResultSet rs) throws Exception {
        Order order = new Order();
        order.setId(rs.getInt("id"));
        try { order.setOrderCode(rs.getString("order_code")); } catch (Exception ignored) {}
        order.setUserId(rs.getInt("user_id"));
        order.setTotalAmount(rs.getBigDecimal("total_amount"));
        order.setStatus(rs.getString("status"));
        try { order.setPaymentMethod(rs.getString("payment_method")); } catch (Exception ignored) {}
        try { order.setPaymentStatus(rs.getString("payment_status")); } catch (Exception ignored) {}
        try { order.setPaymentId(rs.getString("payment_id")); } catch (Exception ignored) {}
        try { order.setCustomerName(rs.getString("customer_name")); } catch (Exception ignored) {}
        try { order.setCustomerEmail(rs.getString("customer_email")); } catch (Exception ignored) {}
        try { order.setCustomerPhone(rs.getString("customer_phone")); } catch (Exception ignored) {}
        try { order.setDeliveryAddress(rs.getString("delivery_address")); } catch (Exception ignored) {}
        order.setOrderDate(rs.getTimestamp("order_date"));
        return order;
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
