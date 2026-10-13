package com.rajesh.canteen.model;

public class Meal extends FoodItem {

    public Meal(int id, String name, double price) {
        super(id, name, price);
    }

    @Override
    public String getCategory() {
        return "Meal";
    }

    @Override
    public double getGstRate() {
        return 0.05; // 5% GST
    }
}
