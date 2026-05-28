package auction.common.service;

import auction.common.exception.AuctionClosedException;
import auction.common.exception.InvalidBidException;
import auction.common.model.auction.Auction;
import auction.common.model.auction.AuctionStatus;
import auction.common.model.item.Art;
import auction.common.model.user.Bidder;
import auction.common.model.user.Seller;
import auction.common.repository.AuctionRepository;
import auction.common.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuctionServiceTest {

    private AuctionRepository auctionRepository;
    private UserRepository userRepository;
    private NotificationService notificationService;
    private AuctionService auctionService;

    @BeforeEach
    void setUp() {
        auctionRepository = mock(AuctionRepository.class);
        userRepository = mock(UserRepository.class);
        notificationService = mock(NotificationService.class);
        auctionService = new AuctionService(auctionRepository, userRepository, notificationService);
    }

    @Test
    void createAuction_Success() {
        String sellerId = "seller1";
        Seller seller = new Seller(sellerId, "pwd", "Seller 1", 0.0);
        Art item = new Art("Art 1", "Desc", 100.0, null, null, sellerId, "Artist", 2020);
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = start.plusHours(2);

        when(userRepository.findById(sellerId)).thenReturn(Optional.of(seller));
        when(auctionRepository.save(any(Auction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<Auction> result = auctionService.createAuction(item, start, end, sellerId);

        assertTrue(result.isPresent());
        assertEquals(sellerId, result.get().getItem().getSellerId());
        verify(auctionRepository).save(any(Auction.class));
    }

    @Test
    void createAuction_InvalidTimes_ThrowsException() {
        String sellerId = "seller1";
        Art item = new Art("Art 1", "Desc", 100.0, null, null, sellerId, "Artist", 2020);
        LocalDateTime start = LocalDateTime.now().plusHours(2);
        LocalDateTime end = start.minusHours(1); // End before start

        assertThrows(IllegalArgumentException.class, () -> 
            auctionService.createAuction(item, start, end, sellerId)
        );
    }

    @Test
    void placeBid_Success() throws Exception {
        String auctionId = "auc1";
        String username = "bidder1";
        double bidAmount = 150.0;

        Bidder bidder = new Bidder(username, "pwd", "Bidder 1", 1000.0);
        String bidderId = bidder.getId();
        
        Art item = new Art("Art 1", "Desc", 100.0, null, null, "seller1", "Artist", 2020);
        Auction auction = new Auction(item, LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1), "seller1");
        auction.startAuction();

        when(auctionRepository.findById(auctionId)).thenReturn(Optional.of(auction));
        when(userRepository.findById(bidderId)).thenReturn(Optional.of(bidder));
        when(auctionRepository.save(any(Auction.class))).thenReturn(auction);

        auctionService.placeBid(auctionId, bidderId, bidAmount);

        assertEquals(bidAmount, auction.getCurrentHighestBid());
        assertEquals(bidderId, auction.getHighestBidder().getId());
        verify(auctionRepository).save(auction);
    }
}
