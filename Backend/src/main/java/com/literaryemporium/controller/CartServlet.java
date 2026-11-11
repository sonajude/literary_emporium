package com.literaryemporium.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.literaryemporium.dao.CartDAO;
import com.literaryemporium.model.CartItem;
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

@WebServlet(name = "CartServlet", urlPatterns = {"/api/cart", "/api/cart/*"})
public class CartServlet extends HttpServlet {

    private final CartDAO cartDAO = new CartDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCorsAndJson(response);

        String userIdParam = request.getParameter("userId");
        if (userIdParam != null) {
            try {
                int userId = Integer.parseInt(userIdParam);
                List<CartItem> cartItems = cartDAO.getCartByUser(userId);
                response.getWriter().write(gson.toJson(cartItems));
                return;
            } catch (NumberFormatException ignored) {
            }
        }
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write("{\"status\":\"error\",\"message\":\"Missing or invalid userId\"}");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCorsAndJson(response);
        JsonObject body = parseJsonBody(request);
        Map<String, Object> res = new HashMap<>();

        try {
            int userId = Integer.parseInt(getString(body, "userId", request.getParameter("userId")));
            String bookIdStr = getString(body, "bookId", request.getParameter("bookId"));
            int rawBookId = 0;
            if (bookIdStr != null && !bookIdStr.isEmpty()) {
                try { rawBookId = Integer.parseInt(bookIdStr); } catch (Exception ignored) {}
            }
            String title = getString(body, "title", null);
            String author = getString(body, "author", null);
            String category = getString(body, "category", null);
            String priceStr = getString(body, "price", null);
            java.math.BigDecimal price = null;
            if (priceStr != null && !priceStr.isEmpty()) {
                try { price = new java.math.BigDecimal(priceStr.replaceAll("[^0-9.]", "")); } catch (Exception ignored) {}
            }
            String imageUrl = getString(body, "imageUrl", getString(body, "image", null));
            int bookId = new com.literaryemporium.dao.BookDAO().findOrCreateBook(rawBookId, title, author, category, price, imageUrl);

            String qtyStr = getString(body, "quantity", request.getParameter("quantity"));
            int quantity = (qtyStr != null && !qtyStr.isEmpty()) ? Integer.parseInt(qtyStr) : 1;

            boolean success = cartDAO.addToCart(userId, bookId, quantity);
            res.put("status", success ? "success" : "error");
            res.put("bookId", bookId);
            res.put("message", success ? "Book added to cart successfully!" : "Failed to add to cart.");
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
            int cartId = Integer.parseInt(getString(body, "cartId", request.getParameter("cartId")));
            int quantity = Integer.parseInt(getString(body, "quantity", request.getParameter("quantity")));

            boolean success = cartDAO.updateQuantity(cartId, quantity);
            res.put("status", success ? "success" : "error");
            res.put("message", success ? "Cart updated." : "Failed to update cart.");
        } catch (Exception e) {
            res.put("status", "error");
            res.put("message", e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        response.getWriter().write(gson.toJson(res));
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCorsAndJson(response);
        Map<String, Object> res = new HashMap<>();

        String clearParam = request.getParameter("clear");
        String userIdParam = request.getParameter("userId");
        if ("true".equalsIgnoreCase(clearParam) && userIdParam != null) {
            try {
                int userId = Integer.parseInt(userIdParam);
                boolean success = cartDAO.clearCart(userId);
                res.put("status", success ? "success" : "error");
                res.put("message", "Cart cleared.");
                response.getWriter().write(gson.toJson(res));
                return;
            } catch (NumberFormatException ignored) {
            }
        }

        String cartIdParam = request.getParameter("cartId");
        if (cartIdParam != null) {
            try {
                int cartId = Integer.parseInt(cartIdParam);
                boolean deleted = cartDAO.removeFromCart(cartId);
                res.put("status", deleted ? "success" : "error");
                res.put("message", deleted ? "Item removed from cart." : "Failed to remove item.");
            } catch (NumberFormatException e) {
                res.put("status", "error");
                res.put("message", "Invalid cart ID.");
            }
        } else {
            res.put("status", "error");
            res.put("message", "Missing cart ID.");
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
