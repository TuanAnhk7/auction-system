package auction.common.model.user;

import auction.common.model.item.Item;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Seller extends User {
    private double rating;
    private final List<Item> products;

    public Seller(String username, String password, String fullName, double rating) {
        super(username, password, fullName);
        this.rating = rating;
        this.products = new ArrayList<>();
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
        touch();
    }

    public List<Item> getProducts() {
        return Collections.unmodifiableList(products);
    }

    public void addProduct(Item item) {
        products.add(item);
        touch();
    }

    public boolean updateProduct(String itemId, String newName, String newDescription,
                                 double newStartingPrice, Instant newStartTime, Instant newEndTime) {
        for (Item item : products) {
            if (item.getId().equals(itemId)) {
                item.setName(newName);
                item.setDescription(newDescription);
                item.setStartingPrice(newStartingPrice);
                item.setStartTime(newStartTime);
                item.setEndTime(newEndTime);
                touch();
                return true;
            }
        }
        return false;
    }

    public boolean removeProduct(String itemId) {
        boolean removed = products.removeIf(item -> item.getId().equals(itemId));
        if (removed) touch();
        return removed;
    }
}
