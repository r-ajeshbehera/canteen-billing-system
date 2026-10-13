package com.rajesh.canteen.model;

// Common parent for every item on the menu.
// Each type decides its own category and GST rate.
public abstract class FoodItem {
    private int id;
    private String name;
    private double price;

    protected FoodItem(int id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public abstract String getCategory();

    public abstract double getGstRate();

    public double getPriceWithGst() {
        return price + price * getGstRate();
    }

    @Override
    public String toString() {
        return String.format("%-3d %-18s %-10s Rs %6.2f", id, name, getCategory(), price);
    }
}
