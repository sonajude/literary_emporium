package com.literaryemporium.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.literaryemporium.dao.OrderDAO;
import com.literaryemporium.model.Order;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "OrderServlet", urlPatterns = {"/api/orders/*"})
public class OrderServlet extends HttpServlet {

    private final OrderDAO orderDAO = new OrderDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCorsAndJson(response);

        String userIdParam = request.getParameter("userId");
        if (userIdParam != null) {
            try {
                int userId = Integer.parseInt(userIdParam);
                List<Order> orders = orderDAO.getOrdersByUser(userId);
                response.getWriter().write(gson.toJson(orders));
                return;
            } catch (NumberFormatException ignored) {
            }
        }

        // Return all orders (for Admin)
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
            int userId = Integer.parseInt(getString(body, "userId", request.getParameter("userId")));
            int orderId = orderDAO.checkout(userId);

            if (orderId > 0) {
                res.put("status", "success");
                res.put("orderId", orderId);
                res.put("message", "Order placed successfully.");
            } else {
                res.put("status", "error");
                res.put("message", "Checkout failed. Your cart might be empty.");
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

            boolean success = orderDAO.updateOrderStatus(orderId, status);
            res.put("status", success ? "success" : "error");
            res.put("message", success ? "Order status updated." : "Failed to update order status.");
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
