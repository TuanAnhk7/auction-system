package auction.common.model.item;

import java.time.Instant;

public class Electronics extends Item {
    private String brand;
    private int warrantyMonths;

    public Electronics(String name,
                       String description,
                       double startingPrice,
                       Instant startTime,
                       Instant endTime,
                       String sellerId,
                       String brand,
                       int warrantyMonths) {

        super(name, description, startingPrice,
                startTime, endTime, sellerId);

        this.brand = brand;
        this.warrantyMonths = warrantyMonths;
    }

    @Override
    public String getCategory() {
        return "Electronics";
    }

    @Override
    public String getDisplayCreator() {
        return brand != null && !brand.isBlank() ? brand : getSellerUsername();
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
        touch();
    }

    public int getWarrantyMonths() {
        return warrantyMonths;
    }

    public void setWarrantyMonths(int warrantyMonths) {
        this.warrantyMonths = warrantyMonths;
        touch();
    }
}