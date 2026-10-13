package com.rajesh.canteen.model;

public class Beverage extends FoodItem {

    public Beverage(int id, String name, double price) {
        super(id, name, price);
    }

    @Override
    public String getCategory() {
        return "Beverage";
    }

    @Override
    public double getGstRate() {
        return 0.12; // 12% GST on cold drinks
    }
}
