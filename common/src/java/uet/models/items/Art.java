package src.java.uet.models.items;
public class Art extends Item {
    private String artist;
    private int yearCreated;

    public Art(String name, String description, double startingPrice, String artist, int yearCreated) {
        super(name, description, startingPrice);
        this.artist = artist;
        this.yearCreated = yearCreated;
    }

    @Override
    public void displayDetails() {
        System.out.println("[ART] " + getName() +
                " | Artist: " + artist +
                " | Year: " + yearCreated +
                " | Price: $" + getCurrentPrice());
    }
}
