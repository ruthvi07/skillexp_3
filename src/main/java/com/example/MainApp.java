package com.example;

import java.util.List;
import java.util.Scanner;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

public class MainApp {

    static SessionFactory factory =
            new Configuration().configure().buildSessionFactory();
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {

        int choice;

        do {
            System.out.println("\n===== PRODUCT MANAGEMENT (HQL LAB) =====");
            System.out.println("1. Insert Product");
            System.out.println("2. Insert Sample Products");
            System.out.println("3. View Product By ID");
            System.out.println("4. Sort Products By Price (ASC)");
            System.out.println("5. Sort Products By Price (DESC)");
            System.out.println("6. Sort Products By Quantity (High → Low)");
            System.out.println("7. Pagination (First 3 / Next 3)");
            System.out.println("8. Aggregate Functions");
            System.out.println("9. Filter By Price Range");
            System.out.println("10. LIKE Queries");
            System.out.println("11. Exit");
            System.out.print("Enter choice: ");

            choice = sc.nextInt();

            switch (choice) {
            case 1:
                insertProduct();
                break;

            case 2:
                insertSampleProducts();
                break;

            case 3:
                viewProduct();
                break;

            case 4:
                sortByPriceAsc();
                break;

            case 5:
                sortByPriceDesc();
                break;

            case 6:
                sortByQuantity();
                break;

            case 7:
                paginationDemo();
                break;

            case 8:
                aggregateOperations();
                break;

            case 9:
                filterByPriceRange();
                break;

            case 10:
                likeQueries();
                break;

            case 11:
                factory.close();
                System.out.println("Exiting Application...");
                break;

            default:
                System.out.println("Invalid Choice!");
        }


        } while (choice != 11);
    }

    // 1. INSERT PRODUCT
    static void insertProduct() {
        Session session = factory.openSession();
        Transaction tx = session.beginTransaction();

        sc.nextLine();
        System.out.print("Enter Name: ");
        String name = sc.nextLine();

        System.out.print("Enter Description: ");
        String desc = sc.nextLine();

        System.out.print("Enter Price: ");
        double price = sc.nextDouble();

        System.out.print("Enter Quantity: ");
        int qty = sc.nextInt();

        Product p = new Product(name, desc, price, qty);
        session.save(p);

        tx.commit();
        session.close();
        System.out.println("Product Inserted Successfully!");
    }

    // 2. INSERT SAMPLE PRODUCTS (5–8 RECORDS)
    static void insertSampleProducts() {
        Session session = factory.openSession();
        Transaction tx = session.beginTransaction();

        session.save(new Product("Laptop", "Electronics", 55000, 10));
        session.save(new Product("Mouse", "Electronics", 500, 50));
        session.save(new Product("Keyboard", "Electronics", 1200, 30));
        session.save(new Product("Chair", "Furniture", 3500, 15));
        session.save(new Product("Table", "Furniture", 8000, 5));
        session.save(new Product("Bottle", "Accessories", 300, 0));

        tx.commit();
        session.close();
        System.out.println("Sample Products Inserted!");
    }

    // 3. VIEW PRODUCT BY ID
    static void viewProduct() {
        Session session = factory.openSession();

        System.out.print("Enter Product ID: ");
        int id = sc.nextInt();

        Product p = session.get(Product.class, id);

        if (p != null) {
            System.out.println(p.getId() + " | " +
                    p.getName() + " | " +
                    p.getDescription() + " | " +
                    p.getPrice() + " | " +
                    p.getQuantity());
        } else {
            System.out.println("Product Not Found!");
        }

        session.close();
    }

    // 4. SORT BY PRICE ASC
    static void sortByPriceAsc() {
        Session session = factory.openSession();

        Query<Product> q =
                session.createQuery("FROM Product p ORDER BY p.price ASC",
                        Product.class);

        q.list().forEach(MainApp::printProduct);

        session.close();
    }

    // 5. SORT BY PRICE DESC
    static void sortByPriceDesc() {
        Session session = factory.openSession();

        Query<Product> q =
                session.createQuery("FROM Product p ORDER BY p.price DESC",
                        Product.class);

        q.list().forEach(MainApp::printProduct);

        session.close();
    }

    // 6. SORT BY QUANTITY (HIGH → LOW)
    static void sortByQuantity() {
        Session session = factory.openSession();

        Query<Product> q =
                session.createQuery("FROM Product p ORDER BY p.quantity DESC",
                        Product.class);

        q.list().forEach(MainApp::printProduct);

        session.close();
    }

    // 7. PAGINATION
    static void paginationDemo() {
        Session session = factory.openSession();

        System.out.println("\n--- First 3 Products ---");
        Query<Product> q1 =
                session.createQuery("FROM Product", Product.class);
        q1.setFirstResult(0);
        q1.setMaxResults(3);
        q1.list().forEach(MainApp::printProduct);

        System.out.println("\n--- Next 3 Products ---");
        Query<Product> q2 =
                session.createQuery("FROM Product", Product.class);
        q2.setFirstResult(3);
        q2.setMaxResults(3);
        q2.list().forEach(MainApp::printProduct);

        session.close();
    }

    // 8. AGGREGATE FUNCTIONS
    static void aggregateOperations() {
        Session session = factory.openSession();

        Long total =
                session.createQuery("SELECT COUNT(p) FROM Product p",
                        Long.class).getSingleResult();
        System.out.println("Total Products: " + total);

        Long available =
                session.createQuery(
                        "SELECT COUNT(p) FROM Product p WHERE p.quantity > 0",
                        Long.class).getSingleResult();
        System.out.println("Products with Quantity > 0: " + available);

        Object[] minMax =
                (Object[]) session.createQuery(
                        "SELECT MIN(p.price), MAX(p.price) FROM Product p")
                        .getSingleResult();

        System.out.println("Min Price: " + minMax[0]);
        System.out.println("Max Price: " + minMax[1]);

        System.out.println("\nProducts Grouped By Description:");
        List<Object[]> list =
                session.createQuery(
                        "SELECT p.description, COUNT(p) FROM Product p GROUP BY p.description")
                        .getResultList();

        for (Object[] row : list) {
            System.out.println(row[0] + " → " + row[1]);
        }

        session.close();
    }

    // 9. FILTER BY PRICE RANGE
    static void filterByPriceRange() {
        Session session = factory.openSession();

        Query<Product> q =
                session.createQuery(
                        "FROM Product p WHERE p.price BETWEEN :min AND :max",
                        Product.class);

        q.setParameter("min", 1000.0);
        q.setParameter("max", 10000.0);

        q.list().forEach(MainApp::printProduct);

        session.close();
    }

    // 10. LIKE QUERIES
    static void likeQueries() {
        Session session = factory.openSession();

        System.out.println("\nNames starting with 'L':");
        session.createQuery(
                "FROM Product p WHERE p.name LIKE 'L%'", Product.class)
                .list().forEach(MainApp::printProduct);

        System.out.println("\nNames ending with 'r':");
        session.createQuery(
                "FROM Product p WHERE p.name LIKE '%r'", Product.class)
                .list().forEach(MainApp::printProduct);

        System.out.println("\nNames containing 'top':");
        session.createQuery(
                "FROM Product p WHERE p.name LIKE '%top%'", Product.class)
                .list().forEach(MainApp::printProduct);

        System.out.println("\nNames with length = 5:");
        session.createQuery(
                "FROM Product p WHERE LENGTH(p.name) = 5", Product.class)
                .list().forEach(MainApp::printProduct);

        session.close();
    }

    // COMMON PRINT METHOD
    static void printProduct(Product p) {
        System.out.println(p.getId() + " | " +
                p.getName() + " | " +
                p.getDescription() + " | " +
                p.getPrice() + " | " +
                p.getQuantity());
    }
}
