package com.literaryemporium.controller;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.literaryemporium.dao.OrderDAO;
import com.literaryemporium.model.Order;
import com.literaryemporium.model.OrderItem;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "OrderServlet", urlPatterns = {"/api/orders", "/api/orders/*"})
public class OrderServlet extends HttpServlet {

    private final OrderDAO orderDAO = new OrderDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCorsAndJson(response);

        String idParam = request.getParameter("orderId");
        if (idParam != null && !idParam.isEmpty()) {
            try {
                int orderId = Integer.parseInt(idParam);
                Order order = orderDAO.getOrderById(orderId);
                if (order != null) {
                    response.getWriter().write(gson.toJson(order));
                    return;
                }
            } catch (NumberFormatException ignored) {}
        }

        String codeParam = request.getParameter("orderCode");
        if (codeParam != null && !codeParam.isEmpty()) {
            Order order = orderDAO.getOrderByCode(codeParam);
            if (order != null) {
                response.getWriter().write(gson.toJson(order));
                return;
            }
        }

        String userIdParam = request.getParameter("userId");
        if (userIdParam != null) {
            try {
                int userId = Integer.parseInt(userIdParam);
                List<Order> orders = orderDAO.getOrdersByUser(userId);
                response.getWriter().write(gson.toJson(orders));
                return;
            } catch (NumberFormatException ignored) {}
        }

        // Return all orders (for Admin / Sellers)
        List<Order> allOrders = orderDAO.getAllOrders();
        response.getWriter().write(gson.toJson(allOrders));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCorsAndJson(response);
        JsonObject body = parseJsonBody(request);
        Map<String, Object> res = new HashMap<>();

        try {
            int userId = 0;
            String userIdStr = getString(body, "userId", request.getParameter("userId"));
            if (userIdStr != null && !userIdStr.isEmpty()) {
                try {
                    userId = Integer.parseInt(userIdStr);
                } catch (Exception ignored) {}
            }

            Order order = new Order();
            order.setUserId(userId);

            String orderCode = getString(body, "orderCode", null);
            if (orderCode == null || orderCode.isEmpty()) {
                orderCode = "ORD-" + System.currentTimeMillis();
            }
            order.setOrderCode(orderCode);

            BigDecimal total = BigDecimal.ZERO;
            if (body.has("total") && !body.get("total").isJsonNull()) {
                total = new BigDecimal(body.get("total").getAsString());
            }
            order.setTotalAmount(total);

            String method = getString(body, "paymentMethod", "Pay Later");
            order.setPaymentMethod(method);

            String pStatus = getString(body, "paymentStatus", "PENDING");
            // Pay Later must always have PENDING payment status
            if ("Pay Later".equalsIgnoreCase(method)) {
                pStatus = "PENDING";
            }
            order.setPaymentStatus(pStatus);

            String oStatus = getString(body, "status", "Order Placed");
            order.setStatus(oStatus);

            order.setPaymentId(getString(body, "paymentId", null));
            order.setCustomerName(getString(body, "customerName", null));
            order.setCustomerEmail(getString(body, "customerEmail", null));
            order.setCustomerPhone(getString(body, "customerPhone", null));
            order.setDeliveryAddress(getString(body, "deliveryAddress", null));

            List<OrderItem> items = new ArrayList<>();
            com.literaryemporium.dao.BookDAO bookDAO = new com.literaryemporium.dao.BookDAO();
            if (body.has("items") && body.get("items").isJsonArray()) {
                for (JsonElement el : body.getAsJsonArray("items")) {
                    JsonObject obj = el.getAsJsonObject();
                    OrderItem item = new OrderItem();
                    int rawBookId = 0;
                    if (obj.has("bookId") && !obj.get("bookId").isJsonNull()) {
                        try { rawBookId = Integer.parseInt(obj.get("bookId").getAsString()); } catch (Exception ignored) {}
                    } else if (obj.has("id") && !obj.get("id").isJsonNull()) {
                        try { rawBookId = Integer.parseInt(obj.get("id").getAsString()); } catch (Exception ignored) {}
                    }
                    String itTitle = getString(obj, "title", null);
                    String itAuthor = getString(obj, "author", null);
                    String itCategory = getString(obj, "category", null);
                    String itImg = getString(obj, "cover", getString(obj, "image", null));
                    BigDecimal itPrice = BigDecimal.valueOf(450.00);
                    if (obj.has("price") && !obj.get("price").isJsonNull()) {
                        try {
                            itPrice = new BigDecimal(obj.get("price").getAsString().replaceAll("[^0-9.]", ""));
                        } catch (Exception ignored) {}
                    }
                    int bookId = bookDAO.findOrCreateBook(rawBookId, itTitle, itAuthor, itCategory, itPrice, itImg);
                    item.setBookId(bookId);
                    item.setQuantity(obj.has("quantity") ? obj.get("quantity").getAsInt() : 1);
                    item.setPrice(itPrice);
                    item.setTitle(itTitle);
                    item.setAuthor(itAuthor);
                    item.setImageUrl(itImg);
                    items.add(item);
                }
            }

            if (items.isEmpty() && userId > 0) {
                com.literaryemporium.dao.CartDAO cartDAO = new com.literaryemporium.dao.CartDAO();
                List<com.literaryemporium.model.CartItem> cItems = cartDAO.getCartByUser(userId);
                for (com.literaryemporium.model.CartItem ci : cItems) {
                    OrderItem oi = new OrderItem();
                    oi.setBookId(ci.getBookId());
                    oi.setQuantity(ci.getQuantity());
                    oi.setPrice(ci.getPrice());
                    oi.setTitle(ci.getTitle());
                    oi.setAuthor(ci.getAuthor());
                    oi.setImageUrl(ci.getImageUrl());
                    items.add(oi);
                }
            }
            order.setItems(items);

            int orderId = orderDAO.createOrder(order);

            if (orderId > 0) {
                res.put("status", "success");
                res.put("orderId", orderId);
                res.put("orderCode", orderCode);
                res.put("totalAmount", order.getTotalAmount());
                res.put("paymentMethod", order.getPaymentMethod());
                res.put("paymentStatus", order.getPaymentStatus());
                res.put("orderStatus", order.getStatus());
                res.put("orderDate", new java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a").format(new java.util.Date()));
                res.put("customerName", order.getCustomerName());
                res.put("customerEmail", order.getCustomerEmail());
                res.put("customerPhone", order.getCustomerPhone());
                res.put("deliveryAddress", order.getDeliveryAddress());
                res.put("items", items);
                res.put("message", "Order placed successfully.");
            } else {
                res.put("status", "error");
                res.put("message", "Failed to place order in database.");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (Exception e) {
            res.put("status", "error");
            res.put("message", e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        response.getWriter().write(gson.toJson(res));
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCorsAndJson(response);
        JsonObject body = parseJsonBody(request);
        Map<String, Object> res = new HashMap<>();

        try {
            int orderId = Integer.parseInt(getString(body, "orderId", request.getParameter("orderId")));
            String status = getString(body, "status", request.getParameter("status"));
            String paymentStatus = getString(body, "paymentStatus", request.getParameter("paymentStatus"));

            boolean success = false;
            if (status != null && !status.isEmpty()) {
                success = orderDAO.updateOrderStatus(orderId, status);
            }
            if (paymentStatus != null && !paymentStatus.isEmpty()) {
                success = orderDAO.updatePaymentStatus(orderId, paymentStatus);
            }

            res.put("status", success ? "success" : "error");
            res.put("message", success ? "Order updated successfully." : "Failed to update order.");
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
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
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
