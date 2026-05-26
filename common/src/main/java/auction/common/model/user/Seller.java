package auction.common.model.user;

import auction.common.model.item.Item;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Seller extends User {
    private double rating;
    private final Map<String, Item> products;

    public Seller(String username, String hashedPassword, String fullName, double rating) {
        super(username, hashedPassword, fullName, Role.SELLER);
        this.rating = rating;
        this.products = new HashMap<>();
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
        touch();
    }

    public Collection<Item> getProducts() {
        return Collections.unmodifiableCollection(products.values());
    }

    public boolean addProduct(Item item) {
        if (item == null || item.getId() == null || item.getId().trim().isEmpty()) {
            return false;
        }
        if (products.containsKey(item.getId())) {
            return false;
        }
        products.put(item.getId(), item);
        touch();
        return true;
    }

    public boolean updateProduct(String itemId, String newName, String newDescription,
                                 double newStartingPrice, Instant newStartTime, Instant newEndTime) {
        Item item = products.get(itemId);
        if (newName == null || newName.trim().isEmpty()) {
            return false;
        }
        if (newStartingPrice <= 0) {
            return false;
        }
        if (newStartTime == null || newEndTime == null || newStartTime.isAfter(newEndTime)) {
            return false;
        }

        if (item != null) {
                item.setName(newName);
                item.setDescription(newDescription);
                item.setStartingPrice(newStartingPrice);
                item.setStartTime(newStartTime);
                item.setEndTime(newEndTime);
                touch();
                return true;
        }
        return false;
    }

    public boolean removeProduct(String itemId) {
        Item removedItem = products.remove(itemId);
        boolean removed = (removedItem != null);
        if (removed) { touch(); }
        return removed;
    }
}
