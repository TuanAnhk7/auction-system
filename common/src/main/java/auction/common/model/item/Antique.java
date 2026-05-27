package auction.common.model.item;
import java.time.Instant;
public class Antique extends Item {
    private String origin;
    private int estimatedAge;

    public Antique(String name,
                   String description,
                   double startingPrice,
                   Instant startTime,
                   Instant endTime,
                   String sellerId,
                   String origin,
                   int estimatedAge) {

        super(name, description, startingPrice,
                startTime, endTime, sellerId);

        this.origin = origin;
        this.estimatedAge = estimatedAge;
    }

    // Constructor cho phép truyền ID (dùng khi cập nhật/giữ nguyên ID cũ)
    public Antique(String id,
                   String name,
                   String description,
                   double startingPrice,
                   Instant startTime,
                   Instant endTime,
                   String sellerId,
                   String origin,
                   int estimatedAge) {
        super(id, name, description, startingPrice, startTime, endTime, sellerId);
        this.origin = origin;
        this.estimatedAge = estimatedAge;
    }

    @Override
    public String getCategory() {
        return "Antique";
    }

    @Override
    public String getDisplayCreator() {
        return origin != null && !origin.isBlank() ? origin : "Không rõ";
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
        touch();
    }

    public int getEstimatedAge() {
        return estimatedAge;
    }

    public void setEstimatedAge(int estimatedAge) {
        this.estimatedAge = estimatedAge;
        touch();
    }
}