package com.literaryemporium.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.literaryemporium.dao.WishlistDAO;
import com.literaryemporium.model.WishlistItem;
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

@WebServlet(name = "WishlistServlet", urlPatterns = {"/api/wishlist", "/api/wishlist/*"})
public class WishlistServlet extends HttpServlet {

    private final WishlistDAO wishlistDAO = new WishlistDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCorsAndJson(response);

        String userIdParam = request.getParameter("userId");
        if (userIdParam != null) {
            try {
                int userId = Integer.parseInt(userIdParam);
                List<WishlistItem> list = wishlistDAO.getWishlistByUser(userId);
                response.getWriter().write(gson.toJson(list));
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

            int result = wishlistDAO.addToWishlist(userId, bookId);
            if (result == 1) {
                res.put("status", "success");
                res.put("bookId", bookId);
                res.put("message", "Book added to wishlist successfully!");
            } else if (result == 0) {
                res.put("status", "exists");
                res.put("bookId", bookId);
                res.put("message", "This book is already in your wishlist.");
            } else {
                res.put("status", "error");
                res.put("message", "Failed to add to wishlist.");
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
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

        String userIdParam = request.getParameter("userId");
        String bookIdParam = request.getParameter("bookId");

        if (userIdParam != null && bookIdParam != null) {
            try {
                int userId = Integer.parseInt(userIdParam);
                int bookId = Integer.parseInt(bookIdParam);

                boolean deleted = wishlistDAO.removeFromWishlist(userId, bookId);
                res.put("status", deleted ? "success" : "error");
                res.put("message", deleted ? "Removed from wishlist." : "Failed to remove.");
            } catch (NumberFormatException e) {
                res.put("status", "error");
                res.put("message", "Invalid IDs.");
            }
        } else {
            res.put("status", "error");
            res.put("message", "Missing userId or bookId.");
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
