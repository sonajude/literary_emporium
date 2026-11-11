package com.literaryemporium.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.literaryemporium.dao.PaymentDAO;
import com.literaryemporium.model.Payment;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@WebServlet(name = "PaymentServlet", urlPatterns = {"/api/payments", "/api/payments/*"})
public class PaymentServlet extends HttpServlet {

    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCorsAndJson(response);
        String paymentId = request.getParameter("paymentId");

        if (paymentId == null || paymentId.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"status\":\"error\",\"message\":\"Missing paymentId parameter\"}");
            return;
        }

        Payment payment = paymentDAO.getPaymentById(paymentId.trim());
        if (payment == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("{\"status\":\"error\",\"message\":\"Payment request not found\"}");
            return;
        }

        Map<String, Object> res = new HashMap<>();
        res.put("status", "success");
        res.put("paymentId", payment.getPaymentId());
        res.put("paymentStatus", payment.getPaymentStatus());
        res.put("amount", payment.getAmount());
        res.put("paymentMethod", payment.getPaymentMethod());
        res.put("providerReference", payment.getProviderReference());
        res.put("verifiedAt", payment.getVerifiedAt() != null ? payment.getVerifiedAt().toString() : null);

        response.getWriter().write(gson.toJson(res));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCorsAndJson(response);
        String pathInfo = request.getPathInfo();
        JsonObject body = parseJsonBody(request);
        String action = getString(body, "action", request.getParameter("action"));

        if ("/verify".equals(pathInfo) || "verify".equalsIgnoreCase(action)) {
            handlePaymentVerification(body, response);
            return;
        }

        // Default POST: Create QR Payment Request
        handleCreatePaymentRequest(body, response);
    }

    private void handleCreatePaymentRequest(JsonObject body, HttpServletResponse response) throws IOException {
        Map<String, Object> res = new HashMap<>();
        try {
            String amountStr = getString(body, "amount", "0");
            BigDecimal amount = new BigDecimal(amountStr);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                amount = BigDecimal.valueOf(100.00); // Safe minimum
            }

            int userId = 0;
            String userIdStr = getString(body, "userId", null);
            if (userIdStr != null && !userIdStr.isEmpty()) {
                try { userId = Integer.parseInt(userIdStr); } catch (Exception ignored) {}
            }

            String method = getString(body, "method", "GPay");
            String orderCode = getString(body, "orderCode", null);

            String paymentId = "PAY-" + System.currentTimeMillis() + "-" + (int) (Math.random() * 9000 + 1000);

            // NPCI UPI URI Specification
            String upiId = "literaryemporium@oksbi";
            if ("PhonePe".equalsIgnoreCase(method)) {
                upiId = "literaryemporium@ybl";
            }
            String customUpi = getString(body, "customUpiId", null);
            if (customUpi != null && customUpi.contains("@")) {
                upiId = customUpi.trim();
            }

            String formattedAmount = String.format(Locale.US, "%.2f", amount);
            String note = URLEncoder.encode("Literary Emporium Order " + paymentId, StandardCharsets.UTF_8);

            String upiString = "upi://pay?pa=" + upiId +
                               "&pn=Literary%20Emporium" +
                               "&am=" + formattedAmount +
                               "&cu=INR" +
                               "&tn=" + note +
                               "&tr=" + paymentId;

            String qrCodeUrl = "https://api.qrserver.com/v1/create-qr-code/?size=250x250&margin=8&data=" +
                               URLEncoder.encode(upiString, StandardCharsets.UTF_8);

            Payment p = new Payment();
            p.setPaymentId(paymentId);
            p.setOrderCode(orderCode);
            p.setUserId(userId > 0 ? userId : null);
            p.setAmount(amount);
            p.setCurrency("INR");
            p.setPaymentMethod(method);
            p.setPaymentStatus("PENDING");
            p.setProvider("UPI_QR_SANDBOX");
            p.setUpiString(upiString);
            p.setQrCodeUrl(qrCodeUrl);

            boolean saved = paymentDAO.createPaymentRequest(p);
            if (saved) {
                res.put("status", "success");
                res.put("paymentId", paymentId);
                res.put("amount", amount);
                res.put("formattedAmount", formattedAmount);
                res.put("upiId", upiId);
                res.put("payeeName", "Literary Emporium");
                res.put("upiString", upiString);
                res.put("qrCodeUrl", qrCodeUrl);
                res.put("paymentStatus", "PENDING");
                res.put("provider", "UPI_QR_SANDBOX");
                res.put("message", "Payment request created. Scan QR code to complete payment.");
            } else {
                res.put("status", "error");
                res.put("message", "Failed to initialize payment request in database.");
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            res.put("status", "error");
            res.put("message", e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
        response.getWriter().write(gson.toJson(res));
    }

    private void handlePaymentVerification(JsonObject body, HttpServletResponse response) throws IOException {
        Map<String, Object> res = new HashMap<>();
        try {
            String paymentId = getString(body, "paymentId", null);
            if (paymentId == null || paymentId.trim().isEmpty()) {
                res.put("status", "error");
                res.put("message", "Missing paymentId for verification.");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(res));
                return;
            }

            Payment payment = paymentDAO.getPaymentById(paymentId.trim());
            if (payment == null) {
                res.put("status", "error");
                res.put("message", "Payment transaction not found on server.");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write(gson.toJson(res));
                return;
            }

            // Already verified?
            if ("PAID".equalsIgnoreCase(payment.getPaymentStatus())) {
                res.put("status", "success");
                res.put("paymentStatus", "PAID");
                res.put("message", "Payment is already verified and confirmed.");
                res.put("providerReference", payment.getProviderReference());
                response.getWriter().write(gson.toJson(res));
                return;
            }

            String outcome = getString(body, "simulateOutcome", "SUCCESS");
            String ref = getString(body, "providerReference", getString(body, "utr", null));

            boolean isSuccess = !"FAIL".equalsIgnoreCase(outcome);
            if (ref != null && ref.toUpperCase().startsWith("FAIL")) {
                isSuccess = false;
            }

            if (ref == null || ref.trim().isEmpty()) {
                ref = "UTR-" + System.currentTimeMillis() + (int) (Math.random() * 900 + 100);
            }

            boolean updated = paymentDAO.verifyPayment(paymentId, ref, isSuccess);
            if (updated) {
                if (isSuccess) {
                    res.put("status", "success");
                    res.put("paymentStatus", "PAID");
                    res.put("providerReference", ref);
                    res.put("message", "Payment verified successfully by server.");
                } else {
                    res.put("status", "error");
                    res.put("paymentStatus", "FAILED");
                    res.put("providerReference", ref);
                    res.put("message", "Payment verification failed or transaction was declined.");
                }
            } else {
                res.put("status", "error");
                res.put("message", "Server database error during verification.");
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            res.put("status", "error");
            res.put("message", e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
        response.getWriter().write(gson.toJson(res));
    }

    private void setCorsAndJson(HttpServletResponse response) {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }

    private JsonObject parseJsonBody(HttpServletRequest request) {
        try {
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            if (sb.length() > 0) {
                return JsonParser.parseString(sb.toString()).getAsJsonObject();
            }
        } catch (Exception ignored) {
        }
        return new JsonObject();
    }

    private String getString(JsonObject obj, String key, String fallback) {
        if (obj != null && obj.has(key) && !obj.get(key).isJsonNull()) {
            return obj.get(key).getAsString();
        }
        return fallback;
    }
}
