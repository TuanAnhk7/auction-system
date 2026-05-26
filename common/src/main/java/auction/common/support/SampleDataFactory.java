package auction.common.support;

import auction.common.model.item.Art;

import java.time.Instant;
import java.util.List;

public final class SampleDataFactory {
    public static final String MONA_LISA_ID = "art-001";
    public static final String STARRY_NIGHT_ID = "art-002";

    private SampleDataFactory() {
    }

    public static List<Art> createSampleArts() {
        return List.of(
                new Art("Mona Lisa", "Portrait by Leonardo da Vinci", 1000.0,
                        Instant.parse("2026-05-27T00:00:00Z"),
                        Instant.parse("2026-06-03T00:00:00Z"),
                        "sampleSeller",
                        "Leonardo da Vinci",
                        1503),

                new Art("The Starry Night", "Painting by Vincent van Gogh", 1200.0,
                        Instant.parse("2026-05-27T00:00:00Z"),
                        Instant.parse("2026-06-03T00:00:00Z"),
                        "sampleSeller",
                        "Vincent van Gogh",
                        1889)
        );
    }
}