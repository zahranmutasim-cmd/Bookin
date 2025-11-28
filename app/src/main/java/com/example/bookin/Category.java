package com.example.bookin;

public class Category {
    private String name;
    private int iconResource;

    public Category(String name, int iconResource) {
        this.name = name;
        this.iconResource = iconResource;
    }

    public String getName() {
        return name;
    }

    public int getIconResource() {
        return iconResource;
    }
}
