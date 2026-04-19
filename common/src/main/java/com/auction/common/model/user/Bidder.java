package com.auction.common.model.user;

public class Bidder extends User {
    private double accountBalance;

    public double getBalance() { return accountBalance; }
    public void setBalance(double balance) { this.accountBalance = balance; }
}