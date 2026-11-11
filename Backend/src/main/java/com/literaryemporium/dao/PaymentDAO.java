package com.literaryemporium.dao;

import com.literaryemporium.model.Payment;
import com.literaryemporium.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

public class PaymentDAO {

    public boolean createPaymentRequest(Payment p) {
        String sql = "INSERT INTO payments (payment_id, order_id, order_code, user_id, amount, currency, " +
                     "payment_method, payment_status, provider, upi_string, qr_code_url) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getPaymentId());
            if (p.getOrderId() != null && p.getOrderId() > 0) {
                ps.setInt(2, p.getOrderId());
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }
            ps.setString(3, p.getOrderCode());
            if (p.getUserId() != null && p.getUserId() > 0) {
                ps.setInt(4, p.getUserId());
            } else {
                ps.setNull(4, java.sql.Types.INTEGER);
            }
            ps.setBigDecimal(5, p.getAmount());
            ps.setString(6, p.getCurrency() != null ? p.getCurrency() : "INR");
            ps.setString(7, p.getPaymentMethod());
            ps.setString(8, p.getPaymentStatus() != null ? p.getPaymentStatus() : "PENDING");
            ps.setString(9, p.getProvider() != null ? p.getProvider() : "UPI_QR_SANDBOX");
            ps.setString(10, p.getUpiString());
            ps.setString(11, p.getQrCodeUrl());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public Payment getPaymentById(String paymentId) {
        String sql = "SELECT * FROM payments WHERE payment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, paymentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Payment p = new Payment();
                    p.setId(rs.getInt("id"));
                    p.setPaymentId(rs.getString("payment_id"));
                    p.setOrderId((Integer) rs.getObject("order_id"));
                    p.setOrderCode(rs.getString("order_code"));
                    p.setUserId((Integer) rs.getObject("user_id"));
                    p.setAmount(rs.getBigDecimal("amount"));
                    p.setCurrency(rs.getString("currency"));
                    p.setPaymentMethod(rs.getString("payment_method"));
                    p.setPaymentStatus(rs.getString("payment_status"));
                    p.setProvider(rs.getString("provider"));
                    p.setUpiString(rs.getString("upi_string"));
                    p.setQrCodeUrl(rs.getString("qr_code_url"));
                    p.setProviderReference(rs.getString("provider_reference"));
                    p.setVerifiedAt(rs.getTimestamp("verified_at"));
                    p.setCreatedAt(rs.getTimestamp("created_at"));
                    p.setUpdatedAt(rs.getTimestamp("updated_at"));
                    return p;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean verifyPayment(String paymentId, String providerRef, boolean isSuccess) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            String status = isSuccess ? "PAID" : "FAILED";
            String updatePaymentSql = "UPDATE payments SET payment_status = ?, provider_reference = ?, " +
                                      "verified_at = ? WHERE payment_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updatePaymentSql)) {
                ps.setString(1, status);
                ps.setString(2, providerRef);
                ps.setTimestamp(3, isSuccess ? new Timestamp(System.currentTimeMillis()) : null);
                ps.setString(4, paymentId);
                int updated = ps.executeUpdate();
                if (updated <= 0) {
                    conn.rollback();
                    return false;
                }
            }

            // Sync with orders if linked
            String updateOrderSql = "UPDATE orders SET payment_status = ?, " +
                                    "status = CASE WHEN ? = 'PAID' THEN 'Processing' ELSE status END " +
                                    "WHERE payment_id = ?";
            try (PreparedStatement psOrder = conn.prepareStatement(updateOrderSql)) {
                psOrder.setString(1, status);
                psOrder.setString(2, status);
                psOrder.setString(3, paymentId);
                psOrder.executeUpdate();
            }

            conn.commit();
            return true;
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
        return false;
    }

    public boolean linkPaymentToOrder(String paymentId, int orderId, String orderCode) {
        String sql = "UPDATE payments SET order_id = ?, order_code = ? WHERE payment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setString(2, orderCode);
            ps.setString(3, paymentId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
