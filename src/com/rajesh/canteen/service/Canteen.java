package com.rajesh.canteen.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import com.rajesh.canteen.billing.Order;
import com.rajesh.canteen.model.Beverage;
import com.rajesh.canteen.model.FoodItem;
import com.rajesh.canteen.model.Meal;
import com.rajesh.canteen.model.Snack;

public final class Canteen {
    private static final String SALES_FILE = "sales.txt";

    // TreeMap keeps the menu sorted by item id
    private final Map<Integer, FoodItem> menu = new TreeMap<>();
    private int nextToken = 101;

    public Canteen() {
        addToMenu(new Meal(1, "Veg Thali", 60));
        addToMenu(new Meal(2, "Egg Curry Rice", 70));
        addToMenu(new Snack(3, "Samosa", 15));
        addToMenu(new Snack(4, "Veg Sandwich", 30));
        addToMenu(new Beverage(5, "Tea", 10));
        addToMenu(new Beverage(6, "Cold Coffee", 40));
        addToMenu(new Meal(7, "Masala Dosa", 50));
        addToMenu(new Meal(8, "Chicken Biryani", 110));
        addToMenu(new Snack(9, "Veg Puff", 20));
        addToMenu(new Snack(10, "Aloo Chop", 15));
        addToMenu(new Beverage(11, "Lassi", 30));
        addToMenu(new Beverage(12, "Lemon Soda", 25));

        // continue token numbers after the last saved order,
        // so tokens don't repeat when the program is restarted
        SalesReport old = getReport();
        if (old != null && old.getLastToken() >= nextToken) {
            nextToken = old.getLastToken() + 1;
        }
    }

    private void addToMenu(FoodItem item) {
        menu.put(item.getId(), item);
    }

    public FoodItem findItem(int id) {
        return menu.get(id);
    }

    public Collection<FoodItem> getMenu() {
        return menu.values();
    }

    public void showMenu() {
        System.out.println("ID  Item               Category   Price");
        for (FoodItem item : menu.values()) {
            System.out.println(item);
        }
    }

    public Order newOrder() {
        return new Order(nextToken++);
    }

    // one line per item: token,item,qty,amount
    public void saveOrder(Order order) {
        try (FileWriter fw = new FileWriter(SALES_FILE, true)) {
            for (Map.Entry<FoodItem, Integer> e : order.getItems().entrySet()) {
                double amount = e.getKey().getPriceWithGst() * e.getValue();
                fw.write(order.getTokenNumber() + "," + e.getKey().getName() + ","
                        + e.getValue() + "," + String.format("%.2f", amount) + "\n");
            }
        } catch (IOException ex) {
            System.out.println("Could not save the bill: " + ex.getMessage());
        }
    }

    // reads sales.txt; returns null if there are no sales yet
    public SalesReport getReport() {
        SalesReport report = new SalesReport();
        try (BufferedReader br = new BufferedReader(new FileReader(SALES_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length != 4) {
                    continue; // skip a broken line instead of crashing
                }
                try {
                    report.addLine(Integer.parseInt(p[0].trim()), p[1].trim(),
                            Integer.parseInt(p[2].trim()), Double.parseDouble(p[3].trim()));
                } catch (NumberFormatException ex) {
                    // skip a line with bad numbers
                }
            }
        } catch (IOException ex) {
            return null;
        }
        return report;
    }

    public void showSalesReport() {
        SalesReport r = getReport();
        if (r == null || r.getTotalOrders() == 0) {
            System.out.println("No sales yet.");
            return;
        }
        System.out.println("------- SALES REPORT -------");
        System.out.println("Total orders : " + r.getTotalOrders());
        System.out.printf("Total revenue: Rs %.2f%n", r.getRevenue());
        System.out.println("Best seller  : " + r.getBestSeller() + " (" + r.getBestSellerQty() + " sold)");
    }
}
