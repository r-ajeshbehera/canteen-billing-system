package com.rajesh.canteen.billing;

import java.util.LinkedHashMap;
import java.util.Map;

import com.rajesh.canteen.exception.InvalidQuantityException;
import com.rajesh.canteen.model.FoodItem;

public class Order implements Billable {
    private static final int MAX_QTY = 20;

    private final int tokenNumber;
    // keeps items in the order they were added, so the bill prints the same way
    private final Map<FoodItem, Integer> items = new LinkedHashMap<>();

    public Order(int tokenNumber) {
        this.tokenNumber = tokenNumber;
    }

    public int getTokenNumber() {
        return tokenNumber;
    }

    public Map<FoodItem, Integer> getItems() {
        return items;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public void addItem(FoodItem item, int qty) throws InvalidQuantityException {
        int newQty = items.getOrDefault(item, 0) + qty;
        if (qty <= 0 || newQty > MAX_QTY) {
            throw new InvalidQuantityException("Quantity must be between 1 and " + MAX_QTY + " per item");
        }
        items.put(item, newQty);
    }

    public boolean removeItem(FoodItem item) {
        return items.remove(item) != null;
    }

    @Override
    public double getSubtotal() {
        double sum = 0;
        for (Map.Entry<FoodItem, Integer> e : items.entrySet()) {
            sum += e.getKey().getPrice() * e.getValue();
        }
        return sum;
    }

    @Override
    public double getTotal() {
        double sum = 0;
        for (Map.Entry<FoodItem, Integer> e : items.entrySet()) {
            sum += e.getKey().getPriceWithGst() * e.getValue();
        }
        return sum;
    }

    public String getBillText() {
        StringBuilder sb = new StringBuilder();
        sb.append("------------- BILL -------------\n");
        sb.append("Token No: ").append(tokenNumber).append("\n");
        for (Map.Entry<FoodItem, Integer> e : items.entrySet()) {
            FoodItem f = e.getKey();
            int q = e.getValue();
            sb.append(String.format("%-18s x%-3d Rs %7.2f%n", f.getName(), q, f.getPrice() * q));
        }
        sb.append(String.format("Subtotal             Rs %7.2f%n", getSubtotal()));
        sb.append(String.format("GST                  Rs %7.2f%n", getTotal() - getSubtotal()));
        sb.append(String.format("TOTAL                Rs %7.2f%n", getTotal()));
        sb.append("--------------------------------\n");
        return sb.toString();
    }
}
