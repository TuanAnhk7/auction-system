package auction.common.service;

import auction.common.model.auction.Auction;
import auction.common.model.user.Bidder;
import auction.common.model.user.Seller;
import auction.common.repository.UserRepository;

import java.util.Optional;

public class NotificationService {//gửi thông báo

    private final UserRepository userRepository;

    public NotificationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void notifyAuctionWinner(Auction auction, Bidder winner) {
        if (winner != null) {
            System.out.println("--- NOTIFICATION ---");
            System.out.println("To: " + winner.getFullName() + " (" + winner.getUsername() + ")");
            System.out.println("Subject: Congratulations! You won the auction for '" + auction.getItem().getName() + "'!");
            System.out.println("Details: You won with a bid of " + auction.getCurrentHighestBid() + ".");
            System.out.println("--------------------");
        } else {
            System.out.println("--- NOTIFICATION ---");
            System.out.println("Subject: Auction for '" + auction.getItem().getName() + "' ended without a winner.");
            System.out.println("--------------------");
        }
    }

    public void notifySellerAuctionEnded(Auction auction, Seller seller) {
        System.out.println("--- NOTIFICATION ---");
        System.out.println("To: " + seller.getFullName() + " (" + seller.getUsername() + ")");
        System.out.println("Subject: Your auction for '" + auction.getItem().getName() + "' has ended.");
        System.out.println("Details: Highest bid: " + auction.getCurrentHighestBid() + ". Winner: " + (auction.getHighestBidder() != null ? auction.getHighestBidder().getUsername() : "N/A"));
        System.out.println("--------------------");
    }
}