package com.auction.common.support;

public class InvalidBidException extends com.auction.common.support.AuctionException {
    public InvalidBidException(String message){
        super(message);
    }
}