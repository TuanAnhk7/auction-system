package uet.models.items;

import uet.Entity;

public abstract class Item extends Entity {
    private String name;
    private String description;
    private double startingPrice;
    private double currentPrice;

    public Item(String name, String description, double startingPrice) {
        super(); // Gọi Constructor của uet.Entity để tạo ID/Time
        this.name = name;
        this.description = description;
        this.startingPrice = startingPrice;
        this.currentPrice = startingPrice;
    }

    // Phương thức trừu tượng (Tính Đa hình - Polymorphism)
    // Mỗi loại hàng hóa sẽ có cách hiển thị chi tiết khác nhau
    public abstract void displayDetails();

    // Getters
    public String getName() { return name; }
    public double getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice; }


    public double getStartingPrice() {
        return startingPrice;
    }
}

