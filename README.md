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
Java 17 only (no database, no external libraries)

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
│   └── service/Canteen.java              menu, tokens, file save, sales report
└── run.bat                               compile + run on Windows
```

## How to run
Double-click `run.bat`, or in the terminal:
```
javac -d bin src\com\rajesh\canteen\*.java src\com\rajesh\canteen\model\*.java src\com\rajesh\canteen\billing\*.java src\com\rajesh\canteen\exception\*.java src\com\rajesh\canteen\service\*.java
java -cp bin com.rajesh.canteen.Main
```

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
- Web interface
