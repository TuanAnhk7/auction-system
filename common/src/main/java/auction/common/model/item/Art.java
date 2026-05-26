package auction.common.model.item;
import java.time.Instant;
public class Art extends Item {
    private String artist;
    private int yearCreated;

    public Art(String name,
               String description,
               double startingPrice,
               Instant startTime,
               Instant endTime,
               String sellerId,
               String artist,
               int yearCreated) {

        super(name, description, startingPrice,
                startTime, endTime, sellerId);

        this.artist = artist;
        this.yearCreated = yearCreated;
    }

    @Override
    public String getCategory() {
        return "Art";
    }

    @Override
    public String getDisplayCreator() {
        return artist;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
        touch();
    }

    public int getYearCreated() {
        return yearCreated;
    }

    public void setYearCreated(int yearCreated) {
        this.yearCreated = yearCreated;
        touch();
    }
}