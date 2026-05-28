package auction.server.db.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "items")
public class ItemEntity {
    @Id
    private String id;
    private String title;
    private String description;
    private double currentPrice;
    private String startTime;
    private String endTime;
    private String sellerUsername;
    private String artist;
    private int year;

    // Constructor mặc định cho JPA
    public ItemEntity() {
    }

    // Constructor đầy đủ tham số
    public ItemEntity(String id, String title, String description, double currentPrice,
                      String startTime, String endTime, String sellerUsername, String artist, int year) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.currentPrice = currentPrice;
        this.startTime = startTime;
        this.endTime = endTime;
        this.sellerUsername = sellerUsername;
        this.artist = artist;
        this.year = year;
    }

    // --- Các hàm Getter và Setter thuần túy ---
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getSellerUsername() {
        return sellerUsername;
    }

    public void setSellerUsername(String sellerUsername) {
        this.sellerUsername = sellerUsername;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }
}