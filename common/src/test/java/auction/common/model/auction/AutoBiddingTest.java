package auction.common.model.auction;

import auction.common.exception.AuctionException;
import auction.common.model.item.Art;
import auction.common.model.user.Bidder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AutoBiddingTest {

    private Auction auction;
    private Bidder bidder1;
    private Bidder bidder2;

    @BeforeEach
    void setUp() throws AuctionException {
        Art item = new Art("Item 1", "Description", 100.0, null, null, "seller", "Artist", 2020);
        auction = new Auction(item, LocalDateTime.now().minusMinutes(1), LocalDateTime.now().plusMinutes(10), "seller");
        auction.startAuction();
        
        bidder1 = new Bidder("bidder1", "pwd", "Bidder One", 1000.0);
        bidder2 = new Bidder("bidder2", "pwd", "Bidder Two", 1000.0);
    }

    @Test
    void autoBidCounterBidsAutomatically() throws Exception {
        // Bidder 1 registers auto-bid: max 500, increment 10
        auction.registerAutoBid(bidder1, 500.0, 10.0);
        
        // Initial price is 100. Auto-bid should trigger and make it 110 (highest bidder: bidder1)
        assertEquals(110.0, auction.getCurrentHighestBid());
        assertEquals("bidder1", auction.getHighestBidder().getUsername());
        
        // Bidder 2 places a manual bid of 150
        auction.placeBid(bidder2, 150.0);
        
        // Auto-bid should counter-bid to 160
        assertEquals(160.0, auction.getCurrentHighestBid());
        assertEquals("bidder1", auction.getHighestBidder().getUsername());
    }

    @Test
    void twoAutoBidsCompeteUntilOneHitsMax() throws Exception {
        // Bidder 1: max 200, increment 10
        auction.registerAutoBid(bidder1, 200.0, 10.0);
        // Bidder 2: max 300, increment 20
        auction.registerAutoBid(bidder2, 300.0, 20.0);
        
        // They will compete. 
        // 1. B1 bids 110.
        // 2. B2 bids 130.
        // ... (competing)
        // B1 hits max. B2 wins at 220.
        
        assertEquals(220.0, auction.getCurrentHighestBid());
        assertEquals("bidder2", auction.getHighestBidder().getUsername());
    }
}
