package com.literaryemporium.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.literaryemporium.dao.BookDAO;
import com.literaryemporium.model.Book;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "BookServlet", urlPatterns = {"/api/books/*"})
public class BookServlet extends HttpServlet {

    private final BookDAO bookDAO = new BookDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCorsAndJson(response);

        String idParam = request.getParameter("id");
        if (idParam != null) {
            try {
                int id = Integer.parseInt(idParam);
                Book book = bookDAO.getBookById(id);
                if (book != null) {
                    response.getWriter().write(gson.toJson(book));
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.getWriter().write("{\"status\":\"error\",\"message\":\"Book not found\"}");
                }
                return;
            } catch (NumberFormatException ignored) {
            }
        }

        String search = request.getParameter("search");
        if (search != null && !search.trim().isEmpty()) {
            List<Book> books = bookDAO.searchBooks(search);
            response.getWriter().write(gson.toJson(books));
            return;
        }

        String category = request.getParameter("category");
        if (category != null && !category.trim().isEmpty() && !category.equalsIgnoreCase("all")) {
            List<Book> books = bookDAO.getBooksByCategory(category);
            response.getWriter().write(gson.toJson(books));
            return;
        }

        String sellerIdParam = request.getParameter("sellerId");
        if (sellerIdParam != null) {
            try {
                int sellerId = Integer.parseInt(sellerIdParam);
                List<Book> books = bookDAO.getBooksBySeller(sellerId);
                response.getWriter().write(gson.toJson(books));
                return;
            } catch (NumberFormatException ignored) {
            }
        }

        // Default: return all books
        List<Book> books = bookDAO.getAllBooks();
        response.getWriter().write(gson.toJson(books));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCorsAndJson(response);
        JsonObject body = parseJsonBody(request);
        Map<String, Object> res = new HashMap<>();

        try {
            Book book = new Book();
            book.setTitle(getString(body, "title", request.getParameter("title")));
            book.setAuthor(getString(body, "author", request.getParameter("author")));
            book.setCategory(getString(body, "category", request.getParameter("category")));

            String priceStr = getString(body, "price", request.getParameter("price"));
            book.setPrice(priceStr != null ? new BigDecimal(priceStr) : BigDecimal.ZERO);

            book.setDescription(getString(body, "description", request.getParameter("description")));
            book.setImageUrl(getString(body, "imageUrl", request.getParameter("imageUrl")));

            String sellerIdStr = getString(body, "sellerId", request.getParameter("sellerId"));
            if (sellerIdStr != null && !sellerIdStr.isEmpty()) {
                book.setSellerId(Integer.parseInt(sellerIdStr));
            }

            String stockStr = getString(body, "stock", request.getParameter("stock"));
            book.setStock(stockStr != null && !stockStr.isEmpty() ? Integer.parseInt(stockStr) : 10);

            boolean success = bookDAO.addBook(book);
            if (success) {
                res.put("status", "success");
                res.put("message", "Book added successfully.");
                res.put("book", book);
            } else {
                res.put("status", "error");
                res.put("message", "Failed to add book.");
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
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCorsAndJson(response);
        JsonObject body = parseJsonBody(request);
        Map<String, Object> res = new HashMap<>();

        try {
            Book book = new Book();
            int id = Integer.parseInt(getString(body, "id", request.getParameter("id")));
            book.setId(id);
            book.setTitle(getString(body, "title", request.getParameter("title")));
            book.setAuthor(getString(body, "author", request.getParameter("author")));
            book.setCategory(getString(body, "category", request.getParameter("category")));

            String priceStr = getString(body, "price", request.getParameter("price"));
            book.setPrice(priceStr != null ? new BigDecimal(priceStr) : BigDecimal.ZERO);

            book.setDescription(getString(body, "description", request.getParameter("description")));
            book.setImageUrl(getString(body, "imageUrl", request.getParameter("imageUrl")));

            String stockStr = getString(body, "stock", request.getParameter("stock"));
            book.setStock(stockStr != null && !stockStr.isEmpty() ? Integer.parseInt(stockStr) : 10);

            boolean success = bookDAO.updateBook(book);
            res.put("status", success ? "success" : "error");
            res.put("message", success ? "Book updated successfully." : "Failed to update book.");
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

        String idParam = request.getParameter("id");
        if (idParam != null) {
            try {
                int id = Integer.parseInt(idParam);
                boolean deleted = bookDAO.deleteBook(id);
                res.put("status", deleted ? "success" : "error");
                res.put("message", deleted ? "Book deleted successfully." : "Failed to delete book.");
            } catch (NumberFormatException e) {
                res.put("status", "error");
                res.put("message", "Invalid book ID.");
            }
        } else {
            res.put("status", "error");
            res.put("message", "Missing book ID.");
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
