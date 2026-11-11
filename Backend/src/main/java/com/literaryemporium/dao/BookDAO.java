package com.literaryemporium.dao;

import com.literaryemporium.model.Book;
import com.literaryemporium.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {

    public List<Book> getAllBooks() {
        List<Book> list = new ArrayList<>();
        String sql = "SELECT * FROM books ORDER BY id DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResultSetToBook(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public Book getBookById(int id) {
        String sql = "SELECT * FROM books WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBook(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Book> getBooksByCategory(String category) {
        if (category == null || category.equalsIgnoreCase("all")) {
            return getAllBooks();
        }
        List<Book> list = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE LOWER(category) = ? ORDER BY id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, category.toLowerCase().trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToBook(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Book> searchBooks(String query) {
        List<Book> list = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE LOWER(title) LIKE ? OR LOWER(author) LIKE ? OR LOWER(category) LIKE ? ORDER BY id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String pattern = "%" + (query != null ? query.toLowerCase().trim() : "") + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToBook(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Book> getBooksBySeller(int sellerId) {
        List<Book> list = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE seller_id = ? ORDER BY id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sellerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToBook(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean addBook(Book book) {
        String sql = "INSERT INTO books (title, author, category, price, description, image_url, seller_id, stock) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, book.getTitle());
            ps.setString(2, book.getAuthor());
            ps.setString(3, book.getCategory());
            ps.setBigDecimal(4, book.getPrice());
            ps.setString(5, book.getDescription());
            ps.setString(6, book.getImageUrl());
            if (book.getSellerId() != null) {
                ps.setInt(7, book.getSellerId());
            } else {
                ps.setNull(7, java.sql.Types.INTEGER);
            }
            ps.setInt(8, book.getStock());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        book.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateBook(Book book) {
        String sql = "UPDATE books SET title = ?, author = ?, category = ?, price = ?, description = ?, image_url = ?, stock = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, book.getTitle());
            ps.setString(2, book.getAuthor());
            ps.setString(3, book.getCategory());
            ps.setBigDecimal(4, book.getPrice());
            ps.setString(5, book.getDescription());
            ps.setString(6, book.getImageUrl());
            ps.setInt(7, book.getStock());
            ps.setInt(8, book.getId());

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteBook(int id) {
        String sql = "DELETE FROM books WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public int findOrCreateBook(int bookId, String title, String author, String category, java.math.BigDecimal price, String imageUrl) {
        if (bookId > 0) {
            Book existing = getBookById(bookId);
            if (existing != null) {
                return existing.getId();
            }
        }
        if (title != null && !title.trim().isEmpty()) {
            String checkSql = "SELECT id FROM books WHERE LOWER(title) = LOWER(?) LIMIT 1";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setString(1, title.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("id");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Create book
            Book b = new Book();
            b.setTitle(title.trim());
            b.setAuthor(author != null && !author.trim().isEmpty() ? author.trim() : "Various Authors");
            b.setCategory(category != null && !category.trim().isEmpty() ? category.trim() : "Fiction");
            b.setPrice(price != null && price.compareTo(java.math.BigDecimal.ZERO) > 0 ? price : java.math.BigDecimal.valueOf(399.00));
            b.setDescription("Catalogue title at Literary Emporium.");
            b.setImageUrl(imageUrl != null ? imageUrl : "");
            b.setSellerId(2); // default seller
            b.setStock(50);
            boolean added = addBook(b);
            if (added && b.getId() > 0) {
                return b.getId();
            }
        }
        return 1; // default seed book
    }

    private Book mapResultSetToBook(ResultSet rs) throws Exception {
        Book b = new Book();
        b.setId(rs.getInt("id"));
        b.setTitle(rs.getString("title"));
        b.setAuthor(rs.getString("author"));
        b.setCategory(rs.getString("category"));
        b.setPrice(rs.getBigDecimal("price"));
        b.setDescription(rs.getString("description"));
        b.setImageUrl(rs.getString("image_url"));
        int sellerId = rs.getInt("seller_id");
        if (!rs.wasNull()) {
            b.setSellerId(sellerId);
        }
        b.setStock(rs.getInt("stock"));
        b.setCreatedAt(rs.getTimestamp("created_at"));
        return b;
    }
}

