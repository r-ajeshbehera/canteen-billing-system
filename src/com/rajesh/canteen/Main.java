package com.rajesh.canteen;

import java.util.Scanner;

import com.rajesh.canteen.billing.Order;
import com.rajesh.canteen.exception.InvalidQuantityException;
import com.rajesh.canteen.model.FoodItem;
import com.rajesh.canteen.service.Canteen;

public class Main {

    private static final Scanner sc = new Scanner(System.in);
    private static final Canteen canteen = new Canteen();

    public static void main(String[] args) {
        while (true) {
            System.out.println();
            System.out.println("===== SMART CANTEEN BILLING =====");
            System.out.println("1. Show menu");
            System.out.println("2. New order");
            System.out.println("3. Sales report");
            System.out.println("0. Exit");
            try {
                int choice = readInt("Enter choice: ");
                switch (choice) {
                    case 1 -> canteen.showMenu();
                    case 2 -> takeOrder();
                    case 3 -> canteen.showSalesReport();
                    case 0 -> {
                        System.out.println("Bye!");
                        return;
                    }
                    default -> System.out.println("Invalid choice.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private static void takeOrder() {
        Order order = canteen.newOrder();
        canteen.showMenu();
        System.out.println("Enter item id to add, -id to remove (e.g. -3), 0 to finish.");

        while (true) {
            try {
                int id = readInt("Item id: ");
                if (id == 0) {
                    break;
                }
                FoodItem item = canteen.findItem(Math.abs(id));
                if (item == null) {
                    System.out.println("No item with id " + Math.abs(id));
                } else if (id < 0) {
                    System.out.println(order.removeItem(item) ? "Removed " + item.getName() : item.getName() + " is not in the order");
                } else {
                    int qty = readInt("Quantity: ");
                    order.addItem(item, qty);
                    System.out.println("Added " + qty + " x " + item.getName());
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            } catch (InvalidQuantityException e) {
                System.out.println(e.getMessage());
            }
        }

        if (order.isEmpty()) {
            System.out.println("Order cancelled (no items).");
            return;
        }
        System.out.print(order.getBillText());
        System.out.print("Confirm order? (y/n): ");
        if (!sc.nextLine().trim().equalsIgnoreCase("y")) {
            System.out.println("Order cancelled.");
            return;
        }
        canteen.saveOrder(order);
        System.out.println("Order placed. Please collect your food with token " + order.getTokenNumber());
    }

    private static int readInt(String prompt) {
        System.out.print(prompt);
        return Integer.parseInt(sc.nextLine().trim());
    }
}
