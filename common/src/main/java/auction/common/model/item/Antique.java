package auction.common.model.item;

public class Antique extends Item {
    private final String origin;
    private final int estimatedAge;

    public Antique(String name, String description, double startingPrice, String origin, int estimatedAge) {
        this(java.util.UUID.randomUUID().toString(), name, description, startingPrice, origin, estimatedAge);
    }

    public Antique(String itemId, String name, String description, double startingPrice, String origin, int estimatedAge) {
        super(itemId, name, description, startingPrice);
        this.origin = origin;
        this.estimatedAge = estimatedAge;
    }

    @Override
    public String getCategory() {
        return "Antique";
    }

    public String getOrigin() {
        return origin;
    }

    public int getEstimatedAge() {
        return estimatedAge;
    }
}
