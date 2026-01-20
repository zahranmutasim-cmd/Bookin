package com.example.bookin.models;

import com.google.firebase.database.PropertyName;

public class Report {
    private String reportId;
    private String bookId;
    private String bookTitle;
    private String bookImage;
    private String sellerId;
    private String sellerName;
    private String reason;
    private long timestamp;
    private String status; // "pending", "reviewed", "resolved"

    public Report() {
        // Required for Firebase
    }

    public Report(String bookId, String bookTitle, String bookImage, String sellerId, String sellerName,
            String reason) {
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.bookImage = bookImage;
        this.sellerId = sellerId;
        this.sellerName = sellerName;
        this.reason = reason;
        this.timestamp = System.currentTimeMillis();
        this.status = "pending";
    }

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getBookImage() {
        return bookImage;
    }

    public void setBookImage(String bookImage) {
        this.bookImage = bookImage;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
