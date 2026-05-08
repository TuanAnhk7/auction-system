package uet.models.user;

import java.util.List;

public class Seller extends User {
    private double rating;
    private List<Object> products; // Object ở đây nên là lớp Item trong Item Domain

    public double getRating() { return rating; }
    public List<Object> getProducts() { return products; }
}