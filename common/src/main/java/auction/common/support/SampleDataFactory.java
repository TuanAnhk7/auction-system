package auction.common.support;

import auction.common.model.item.Art;

import java.util.List;

public final class SampleDataFactory {
    public static final String MONA_LISA_ID = "art-001";
    public static final String STARRY_NIGHT_ID = "art-002";

    private SampleDataFactory() {
    }

    public static List<Art> createSampleArts() {
        return List.of(
                new Art(MONA_LISA_ID, "Mona Lisa", "Portrait by Leonardo da Vinci", 1000.0, "Leonardo da Vinci", 1503),
                new Art(STARRY_NIGHT_ID, "The Starry Night", "Painting by Vincent van Gogh", 1200.0, "Vincent van Gogh", 1889)
        );
    }
}
