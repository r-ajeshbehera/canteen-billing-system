# Smart Canteen Billing System

In our college canteen, bills were written by hand and the queue was always long. This Java application takes an order, adds the correct GST for each item, prints the bill with a token number and keeps a record of every sale.

## Features
- Menu of meals, snacks and beverages
- New order: add items, remove an item (enter `-id`), confirm before billing
- Bill with subtotal, GST and total, plus a token number
- Item-wise GST: food 5%, beverages 12%
- Every confirmed order is saved to `sales.txt`
- Sales report: total orders, total revenue and best-selling item
- Max 20 of one item per order; wrong input doesn't crash the program

## Tech
Java 17 only (no database, no external libraries). Web version: plain Java (JDK built-in `HttpServer`, no framework) + HTML, CSS and JavaScript.

## Two ways to use it
| Version | Start with | What you get |
|---|---|---|
| Console | `run.bat` | Menu in the terminal (the original version) |
| Web | `run-web.bat` | Ordering screen in the browser at http://localhost:8090 |

Both use the same OOP classes and the same `sales.txt`. `run-web.bat` copies 25 dummy orders from `sample-data/sales.txt` the first time, so the report has data.

## Project structure
```
canteen-billing-system/
├── src/com/rajesh/canteen/
│   ├── Main.java                         menu and order flow
│   ├── model/FoodItem.java               abstract parent class
│   ├── model/Meal.java, Snack.java, Beverage.java
│   ├── billing/Billable.java             interface
│   ├── billing/Order.java                one customer's order and bill
│   ├── exception/InvalidQuantityException.java
│   ├── service/Canteen.java              menu, tokens, file save
│   ├── service/SalesReport.java          totals read from sales.txt
│   └── web/
│       ├── WebServer.java                REST API + serves the web pages
│       ├── HttpUtil.java                 read requests, send JSON / files
│       └── Json.java                     JSON helpers
├── web/                                  frontend: index.html, style.css, app.js
├── sample-data/sales.txt                 25 dummy orders
├── run.bat                               console version
└── run-web.bat                           web version
```

## How to run
Web version: double-click `run-web.bat`. The browser opens http://localhost:8090.

Console version: double-click `run.bat`, or in the terminal:
```
javac -d bin src\com\rajesh\canteen\*.java src\com\rajesh\canteen\model\*.java src\com\rajesh\canteen\billing\*.java src\com\rajesh\canteen\exception\*.java src\com\rajesh\canteen\service\*.java
java -cp bin com.rajesh.canteen.Main
```

## Web version
- **New order:** menu cards filtered by category (with GST rate), + / − buttons, live subtotal, GST and total
- **Place order:** confirm, then the server builds the `Order`, saves it to `sales.txt` and returns the bill with a token number
- **Sales report:** total orders, revenue, best seller and item-wise sales bars

REST API (all JSON):
| Method | Path | Does |
|---|---|---|
| GET | `/api/menu` | all items with category, price and GST rate |
| POST | `/api/order` | `items=1:2,5:3` (item id : quantity) → saves the order, returns the bill |
| GET | `/api/report` | total orders, revenue, best seller, item-wise sales |

The server checks the order again: unknown items and quantities over 20 are rejected using `InvalidQuantityException`, and a wrong order doesn't use up a token number. Token numbers continue after the last saved order, even after a restart.

## OOP used
| Concept | Where |
|---|---|
| Abstraction | `FoodItem` is abstract with `getCategory()` and `getGstRate()` |
| Inheritance | `Meal`, `Snack`, `Beverage` extend `FoodItem` |
| Polymorphism | each type overrides `getGstRate()`; `Order.getTotal()` works for all of them |
| Encapsulation | private fields with getters |
| Interface | `Order` implements `Billable` |

## Ideas for next version
- Daily stock limit for each item
- Student discount
- Date-wise reports
