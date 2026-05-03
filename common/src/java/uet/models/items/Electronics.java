package src.java.uet.models.items;
public class Electronics extends Item {
    private String brand;
    private int warrantyMonths;

    public Electronics(String name, String description, double startingPrice, String brand, int warrantyMonths) {
        super(name, description, startingPrice);
        this.brand = brand;
        this.warrantyMonths = warrantyMonths;
    }

    @Override
    public void displayDetails() {
        System.out.println("[ELECTRONICS] " + getName() +
                " | Brand: " + brand +
                " | Warranty: " + warrantyMonths + " months" +
                " | Price: $" + getCurrentPrice());
    }
}
