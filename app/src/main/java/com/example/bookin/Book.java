package com.example.bookin;

public class Book {
    private String title;
    private String description;
    private String price;
    private String location;
    private int imageResource;

    public Book(String title, String description, String price, String location, int imageResource) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.location = location;
        this.imageResource = imageResource;
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
}
