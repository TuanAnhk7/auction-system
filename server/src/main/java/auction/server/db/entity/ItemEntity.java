package auction.server.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "items")
public class ItemEntity {
    private static final DateTimeFormatter DB_TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "name", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "starting_price", nullable = false)
    private double startingPrice;

    @Column(name = "current_price", nullable = false)
    private double currentPrice;

    @Column(name = "seller_username", nullable = false)
    private String sellerUsername;

    @Column(name = "display_creator")
    private String displayCreator;

    @Column(name = "item_type", nullable = false)
    private String itemType;

    @Column(name = "specific_prop1")
    private String specificProp1;

    @Column(name = "specific_prop2")
    private Double specificProp2;

    @Column(name = "created_at")
    private String createdAt;

    public ItemEntity() {
    }

    public ItemEntity(
            String id,
            String title,
            String description,
            String category,
            double startingPrice,
            double currentPrice,
            String sellerUsername,
            String displayCreator,
            String itemType,
            String specificProp1,
            Double specificProp2,
            String createdAt
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.startingPrice = startingPrice;
        this.currentPrice = currentPrice;
        this.sellerUsername = sellerUsername;
        this.displayCreator = displayCreator;
        this.itemType = itemType;
        this.specificProp1 = specificProp1;
        this.specificProp2 = specificProp2;
        this.createdAt = createdAt;
    }

    public ItemEntity(
            String id,
            String title,
            String description,
            double currentPrice,
            String startTime,
            String endTime,
            String sellerUsername,
            String artist,
            int year
    ) {
        this(id, title, description, "Art", currentPrice, currentPrice, sellerUsername, artist, "Art", artist, (double) year, null);
    }

    @PrePersist
    private void prePersist() {
        if (createdAt == null || createdAt.isBlank()) {
            createdAt = LocalDateTime.now().format(DB_TIMESTAMP_FORMAT);
        }
    }

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

    public String getName() {
        return title;
    }

    public void setName(String name) {
        this.title = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getStartingPrice() {
        return startingPrice;
    }

    public void setStartingPrice(double startingPrice) {
        this.startingPrice = startingPrice;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public String getSellerUsername() {
        return sellerUsername;
    }

    public void setSellerUsername(String sellerUsername) {
        this.sellerUsername = sellerUsername;
    }

    public String getDisplayCreator() {
        return displayCreator;
    }

    public void setDisplayCreator(String displayCreator) {
        this.displayCreator = displayCreator;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public String getSpecificProp1() {
        return specificProp1;
    }

    public void setSpecificProp1(String specificProp1) {
        this.specificProp1 = specificProp1;
    }

    public Double getSpecificProp2() {
        return specificProp2;
    }

    public void setSpecificProp2(Double specificProp2) {
        this.specificProp2 = specificProp2;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getArtist() {
        return displayCreator != null ? displayCreator : specificProp1;
    }

    public void setArtist(String artist) {
        this.displayCreator = artist;
        this.specificProp1 = artist;
    }

    public int getYear() {
        return specificProp2 == null ? 0 : specificProp2.intValue();
    }

    public void setYear(int year) {
        this.specificProp2 = (double) year;
    }
}
