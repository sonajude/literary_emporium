package com.literaryemporium.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class WishlistItem {
    private int id;
    private int userId;
    private int bookId;
    private Timestamp addedAt;

    // Joined fields from Book
    private String title;
    private String author;
    private String category;
    private BigDecimal price;
    private String imageUrl;

    public WishlistItem() {
    }

    public WishlistItem(int id, int userId, int bookId, Timestamp addedAt) {
        this.id = id;
        this.userId = userId;
        this.bookId = bookId;
        this.addedAt = addedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public Timestamp getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(Timestamp addedAt) {
        this.addedAt = addedAt;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
