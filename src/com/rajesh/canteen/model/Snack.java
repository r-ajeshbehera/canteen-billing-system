package com.rajesh.canteen.model;

public class Snack extends FoodItem {

    public Snack(int id, String name, double price) {
        super(id, name, price);
    }

    @Override
    public String getCategory() {
        return "Snack";
    }

    @Override
    public double getGstRate() {
        return 0.05; // 5% GST
    }
}
