package com.rajesh.canteen.web;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.rajesh.canteen.billing.Order;
import com.rajesh.canteen.exception.InvalidQuantityException;
import com.rajesh.canteen.model.FoodItem;
import com.rajesh.canteen.service.Canteen;
import com.rajesh.canteen.service.SalesReport;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/*
 * Web version of the Smart Canteen Billing System.
 * Backend: plain Java (JDK HttpServer) + the same OOP classes (FoodItem, Order, Canteen).
 * Frontend: HTML, CSS and JavaScript in the web/ folder.
 *
 * REST API
 *   GET  /api/menu      all items with category, price and GST rate
 *   POST /api/order     items=1:2,5:3   (item id : quantity) -> saves the order, returns the bill
 *   GET  /api/report    total orders, revenue, best seller, item-wise sales
 */
public class WebServer {

    private static final Canteen canteen = new Canteen();
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8090;
        Path webRoot = HttpUtil.webRoot();

        // if the port is busy (another app is using it), try the next ones
        HttpServer server = null;
        for (int tries = 0; tries < 10 && server == null; tries++) {
            try {
                server = HttpServer.create(new InetSocketAddress(port), 0);
            } catch (java.net.BindException e) {
                System.out.println("Port " + port + " is busy, trying " + (port + 1) + "...");
                port++;
            }
        }
        if (server == null) {
            System.out.println("Could not find a free port. Close other servers and try again.");
            return;
        }
        server.createContext("/api/", WebServer::handleApi);
        server.createContext("/", ex -> HttpUtil.sendStaticFile(ex, webRoot));
        server.start(); // default executor handles one request at a time, so tokens never clash

        System.out.println("Smart Canteen Billing is running.");
        System.out.println("Open http://localhost:" + port + " in your browser. Press Ctrl+C to stop.");
        openBrowser("http://localhost:" + port);
    }

    // opens the default browser on Windows/Mac/Linux; if it can't, the user opens the link
    private static void openBrowser(String url) {
        try {
            if (java.awt.Desktop.isDesktopSupported()
                    && java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.BROWSE)) {
                java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
            }
        } catch (Exception e) {
            // not important: the link is printed above
        }
    }

    private static void handleApi(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        String path = ex.getRequestURI().getPath();
        try {
            if (method.equals("GET") && path.equals("/api/menu")) {
                HttpUtil.sendJson(ex, 200, menuJson());
            } else if (method.equals("POST") && path.equals("/api/order")) {
                placeOrder(ex);
            } else if (method.equals("GET") && path.equals("/api/report")) {
                HttpUtil.sendJson(ex, 200, reportJson(canteen.getReport()));
            } else {
                HttpUtil.sendError(ex, 404, "Unknown API: " + method + " " + path);
            }
        } catch (IllegalArgumentException e) {
            HttpUtil.sendError(ex, 400, e.getMessage());
        }
    }

    private static String menuJson() {
        StringBuilder sb = new StringBuilder("[");
        for (FoodItem f : canteen.getMenu()) {
            if (sb.length() > 1) {
                sb.append(',');
            }
            sb.append("{\"id\":").append(f.getId())
              .append(",\"name\":").append(Json.str(f.getName()))
              .append(",\"category\":").append(Json.str(f.getCategory()))
              .append(",\"price\":").append(Json.money(f.getPrice()))
              .append(",\"gstRate\":").append(f.getGstRate())   // polymorphism: each type gives its own rate
              .append('}');
        }
        return sb.append(']').toString();
    }

    // body: items=1:2,5:3
    private static void placeOrder(HttpExchange ex) throws IOException {
        String itemsText = HttpUtil.body(ex).getOrDefault("items", "");
        if (itemsText.isEmpty()) {
            throw new IllegalArgumentException("The order is empty. Add at least one item.");
        }

        // read and check every line first, so a wrong order doesn't use up a token number
        Map<FoodItem, Integer> wanted = new LinkedHashMap<>();
        for (String part : itemsText.split(",")) {
            String[] p = part.split(":");
            if (p.length != 2) {
                throw new IllegalArgumentException("Wrong order format: " + part);
            }
            int id;
            int qty;
            try {
                id = Integer.parseInt(p[0].trim());
                qty = Integer.parseInt(p[1].trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Item id and quantity must be numbers.");
            }
            FoodItem item = canteen.findItem(id);
            if (item == null) {
                throw new IllegalArgumentException("No item with id " + id);
            }
            wanted.merge(item, qty, Integer::sum);
        }

        Order check = new Order(0);
        try {
            for (Map.Entry<FoodItem, Integer> e : wanted.entrySet()) {
                check.addItem(e.getKey(), e.getValue());
            }
        } catch (InvalidQuantityException e) {
            // our own checked exception becomes a clear message for the user
            throw new IllegalArgumentException(e.getMessage());
        }

        Order order = canteen.newOrder();
        try {
            for (Map.Entry<FoodItem, Integer> e : wanted.entrySet()) {
                order.addItem(e.getKey(), e.getValue());
            }
        } catch (InvalidQuantityException e) {
            throw new IllegalArgumentException(e.getMessage()); // already checked above
        }
        canteen.saveOrder(order);
        HttpUtil.sendJson(ex, 201, billJson(order));
    }

    private static String billJson(Order order) {
        StringBuilder lines = new StringBuilder("[");
        for (Map.Entry<FoodItem, Integer> e : order.getItems().entrySet()) {
            FoodItem f = e.getKey();
            int q = e.getValue();
            if (lines.length() > 1) {
                lines.append(',');
            }
            lines.append("{\"name\":").append(Json.str(f.getName()))
                 .append(",\"category\":").append(Json.str(f.getCategory()))
                 .append(",\"qty\":").append(q)
                 .append(",\"price\":").append(Json.money(f.getPrice()))
                 .append(",\"gstRate\":").append(f.getGstRate())
                 .append(",\"amount\":").append(Json.money(f.getPrice() * q))
                 .append('}');
        }
        lines.append(']');
        return "{\"token\":" + order.getTokenNumber()
                + ",\"time\":" + Json.str(LocalDateTime.now().format(TIME))
                + ",\"items\":" + lines
                + ",\"subtotal\":" + Json.money(order.getSubtotal())
                + ",\"gst\":" + Json.money(order.getTotal() - order.getSubtotal())
                + ",\"total\":" + Json.money(order.getTotal()) + "}";
    }

    private static String reportJson(SalesReport r) {
        if (r == null || r.getTotalOrders() == 0) {
            return "{\"totalOrders\":0,\"revenue\":0,\"bestSeller\":null,\"bestQty\":0,\"items\":[]}";
        }
        // item-wise list, most sold first
        List<Map.Entry<String, Integer>> rows = new ArrayList<>(r.getQtyByItem().entrySet());
        rows.sort((a, b) -> b.getValue() - a.getValue());
        StringBuilder items = new StringBuilder("[");
        for (Map.Entry<String, Integer> e : rows) {
            if (items.length() > 1) {
                items.append(',');
            }
            items.append("{\"name\":").append(Json.str(e.getKey()))
                 .append(",\"qty\":").append(e.getValue())
                 .append(",\"amount\":").append(Json.money(r.getAmountByItem().get(e.getKey())))
                 .append('}');
        }
        items.append(']');
        return "{\"totalOrders\":" + r.getTotalOrders()
                + ",\"revenue\":" + Json.money(r.getRevenue())
                + ",\"bestSeller\":" + Json.str(r.getBestSeller())
                + ",\"bestQty\":" + r.getBestSellerQty()
                + ",\"items\":" + items + "}";
    }
}
