package com.rajesh.canteen.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.rajesh.canteen.billing.Order;
import com.rajesh.canteen.model.Beverage;
import com.rajesh.canteen.model.FoodItem;
import com.rajesh.canteen.model.Meal;
import com.rajesh.canteen.model.Snack;

public class Canteen {
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
    }

    private void addToMenu(FoodItem item) {
        menu.put(item.getId(), item);
    }

    public FoodItem findItem(int id) {
        return menu.get(id);
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

    public void showSalesReport() {
        Map<String, Integer> qtyByItem = new HashMap<>();
        Set<String> tokens = new HashSet<>();
        double revenue = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(SALES_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length != 4) {
                    continue; // skip a broken line instead of crashing
                }
                tokens.add(p[0]);
                qtyByItem.merge(p[1], Integer.parseInt(p[2]), Integer::sum);
                revenue += Double.parseDouble(p[3]);
            }
        } catch (IOException ex) {
            System.out.println("No sales yet.");
            return;
        }

        String best = null;
        int bestQty = 0;
        for (Map.Entry<String, Integer> e : qtyByItem.entrySet()) {
            if (e.getValue() > bestQty) {
                best = e.getKey();
                bestQty = e.getValue();
            }
        }

        System.out.println("------- SALES REPORT -------");
        System.out.println("Total orders : " + tokens.size());
        System.out.printf("Total revenue: Rs %.2f%n", revenue);
        System.out.println("Best seller  : " + best + " (" + bestQty + " sold)");
    }
}
