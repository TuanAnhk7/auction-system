package auction.common.model.item;

public class Art extends Item {
    private final String artist;
    private final int yearCreated;

    public Art(String name, String description, double startingPrice, String artist, int yearCreated) {
        super(name, description, startingPrice);
        this.artist = artist;
        this.yearCreated = yearCreated;
    }

    @Override
    public String getCategory() {
        return "Art";
    }

    public String getArtist() {
        return artist;
    }

    public int getYearCreated() {
        return yearCreated;
    }
}
