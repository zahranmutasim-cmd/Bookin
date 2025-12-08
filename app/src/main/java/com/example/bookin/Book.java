package com.example.bookin;

public class Book {
    private String title;
    private String description;
    private String price;
    private String location;
    private int imageResource;
    private boolean isFavorited;

    public Book(String title, String description, String price, String location, int imageResource) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.location = location;
        this.imageResource = imageResource;
        this.isFavorited = false;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getPrice() {
        return price;
    }

    public String getLocation() {
        return location;
    }

    public int getImageResource() {
        return imageResource;
    }

    public boolean isFavorited() {
        return isFavorited;
    }

    public void setFavorited(boolean favorited) {
        isFavorited = favorited;
    }
}
