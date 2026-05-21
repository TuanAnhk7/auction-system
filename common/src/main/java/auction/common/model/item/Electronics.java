package auction.common.model.item;

public class Electronics extends Item {
    private final String brand;
    private final int warrantyMonths;

    public Electronics(String name, String description, double startingPrice, String brand, int warrantyMonths) {
        this(java.util.UUID.randomUUID().toString(), name, description, startingPrice, brand, warrantyMonths);
    }

    public Electronics(String itemId, String name, String description, double startingPrice, String brand, int warrantyMonths) {
        super(itemId, name, description, startingPrice);
        this.brand = brand;
        this.warrantyMonths = warrantyMonths;
    }

    @Override
    public String getCategory() {
        return "Electronics";
    }

    public String getBrand() {
        return brand;
    }

    public int getWarrantyMonths() {
        return warrantyMonths;
    }
}
