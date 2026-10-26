package com.rajesh.canteen.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// Totals built from the lines of sales.txt.
public class SalesReport {
    private final Set<Integer> tokens = new HashSet<>();              // unique orders
    private final Map<String, Integer> qtyByItem = new HashMap<>();    // item -> quantity sold
    private final Map<String, Double> amountByItem = new HashMap<>();  // item -> money earned
    private double revenue;
    private int lastToken;

    public void addLine(int token, String item, int qty, double amount) {
        tokens.add(token);
        qtyByItem.merge(item, qty, Integer::sum);
        amountByItem.merge(item, amount, Double::sum);
        revenue += amount;
        lastToken = Math.max(lastToken, token);
    }

    public int getTotalOrders() {
        return tokens.size();
    }

    public double getRevenue() {
        return revenue;
    }

    public int getLastToken() {
        return lastToken;
    }

    public Map<String, Integer> getQtyByItem() {
        return qtyByItem;
    }

    public Map<String, Double> getAmountByItem() {
        return amountByItem;
    }

    public String getBestSeller() {
        String best = null;
        int bestQty = 0;
        for (Map.Entry<String, Integer> e : qtyByItem.entrySet()) {
            if (e.getValue() > bestQty) {
                best = e.getKey();
                bestQty = e.getValue();
            }
        }
        return best;
    }

    public int getBestSellerQty() {
        String best = getBestSeller();
        return best == null ? 0 : qtyByItem.get(best);
    }
}
