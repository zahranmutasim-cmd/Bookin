package com.example.bookin.models;

public class Review {
    private String id;
    private String reviewerId;
    private String reviewerName;
    private String reviewerImage;
    private String sellerId;
    private int rating;
    private String text;
    private long createdAt;

    public Review() {
        // Default constructor for Firebase
    }

    public Review(String reviewerId, String reviewerName, String reviewerImage,
            String sellerId, int rating, String text) {
        this.reviewerId = reviewerId;
        this.reviewerName = reviewerName;
        this.reviewerImage = reviewerImage;
        this.sellerId = sellerId;
        this.rating = rating;
        this.text = text;
        this.createdAt = System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(String reviewerId) {
        this.reviewerId = reviewerId;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }

    public String getReviewerImage() {
        return reviewerImage;
    }

    public void setReviewerImage(String reviewerImage) {
        this.reviewerImage = reviewerImage;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
