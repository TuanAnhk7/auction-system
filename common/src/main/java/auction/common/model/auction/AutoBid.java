package auction.common.model.auction;

import auction.common.model.user.Bidder;
import java.util.Objects;

public class AutoBid implements Comparable<AutoBid> {
    private final Bidder bidder;
    private final double maxBid;
    private final double increment;

    public AutoBid(Bidder bidder, double maxBid, double increment) {
        if (bidder == null) throw new IllegalArgumentException("Bidder cannot be null.");
        if (maxBid <= 0) throw new IllegalArgumentException("Max bid must be positive.");
        if (increment <= 0) throw new IllegalArgumentException("Increment must be positive.");
        this.bidder = bidder;
        this.maxBid = maxBid;
        this.increment = increment;
    }

    public Bidder getBidder() {
        return bidder;
    }

    public double getMaxBid() {
        return maxBid;
    }

    public double getIncrement() {
        return increment;
    }

    @Override
    public int compareTo(AutoBid other) {
        // Higher maxBid has priority
        int res = Double.compare(other.maxBid, this.maxBid);
        if (res == 0) {
            // If maxBid is same, maybe FIFO or some other logic. 
            // Here we use bidder username for consistency.
            return this.bidder.getUsername().compareTo(other.bidder.getUsername());
        }
        return res;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AutoBid autoBid = (AutoBid) o;
        return Objects.equals(bidder.getId(), autoBid.bidder.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(bidder.getId());
    }
}
