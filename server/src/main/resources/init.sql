-- Users table
CREATE TABLE IF NOT EXISTS users (
                                     username TEXT PRIMARY KEY,
                                     password TEXT NOT NULL,
                                     role TEXT NOT NULL,
                                     account_balance REAL NOT NULL DEFAULT 1000.0,
                                     created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Items table
CREATE TABLE IF NOT EXISTS items (
                                     id TEXT PRIMARY KEY,
                                     name TEXT NOT NULL,
                                     description TEXT,
                                     category TEXT NOT NULL,
                                     starting_price REAL NOT NULL,
                                     current_price REAL NOT NULL,
                                     seller_username TEXT NOT NULL,
                                     display_creator TEXT,
                                     item_type TEXT NOT NULL,
                                     specific_prop1 TEXT,
                                     specific_prop2 REAL,
                                     created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                     FOREIGN KEY (seller_username) REFERENCES users(username)
    );

-- Auctions table
CREATE TABLE IF NOT EXISTS auctions (
                                        id TEXT PRIMARY KEY,
                                        item_id TEXT NOT NULL,
                                        seller_username TEXT NOT NULL,
                                        start_time DATETIME NOT NULL,
                                        end_time DATETIME NOT NULL,
                                        current_highest_bid REAL NOT NULL,
                                        highest_bidder_username TEXT,
                                        status TEXT NOT NULL,
                                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                        last_modified DATETIME DEFAULT CURRENT_TIMESTAMP,
                                        FOREIGN KEY (item_id) REFERENCES items(id),
    FOREIGN KEY (seller_username) REFERENCES users(username),
    FOREIGN KEY (highest_bidder_username) REFERENCES users(username)
    );

-- Bid transactions table
CREATE TABLE IF NOT EXISTS bid_transactions (
                                                id TEXT PRIMARY KEY,
                                                auction_id TEXT NOT NULL,
                                                bidder_username TEXT NOT NULL,
                                                amount REAL NOT NULL,
                                                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                                FOREIGN KEY (auction_id) REFERENCES auctions(id),
    FOREIGN KEY (bidder_username) REFERENCES users(username)
    );

-- Insert default users
INSERT OR IGNORE INTO users (username, password, role, account_balance) VALUES
('admin', 'admin123', 'ADMIN', 0.0),
('seller', 'seller123', 'SELLER', 5000.0),
('bidder', 'bidder123', 'BIDDER', 10000.0);