package auction.common.model.network;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class AuctionView implements Serializable {
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter BID_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    private final String auctionId;
    private final String itemId;
    private final String itemName;
    private final String description;
    private final String creatorName;
    private final String sellerUsername;
    private final String category;
    private final double startingPrice;
    private final double currentPrice;
    private final String highestBidderUsername;
    private final LocalDateTime endTime;
    private final String status;
    private final List<String> bidHistory;

    public AuctionView(
            String auctionId,
            String itemId,
            String itemName,
            String description,
            String creatorName,
            String sellerUsername,
            String category,
            double startingPrice,
            double currentPrice,
            String highestBidderUsername,
            LocalDateTime endTime,
            String status,
            List<String> bidHistory
    ) {
        this.auctionId = auctionId;
        this.itemId = itemId;
        this.itemName = itemName;
        this.description = description;
        this.creatorName = creatorName;
        this.sellerUsername = sellerUsername;
        this.category = category;
        this.startingPrice = startingPrice;
        this.currentPrice = currentPrice;
        this.highestBidderUsername = highestBidderUsername;
        this.endTime = endTime;
        this.status = status;
        this.bidHistory = List.copyOf(bidHistory);
    }

    public String getAuctionId() {
        return auctionId;
    }

    public String getItemId() {
        return itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public String getDescription() {
        return description;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public String getSellerUsername() {
        return sellerUsername;
    }

    public String getCategory() {
        return category;
    }

    public double getStartingPrice() {
        return startingPrice;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public String getHighestBidderUsername() {
        return highestBidderUsername;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public String getStatus() {
        return status;
    }

    public List<String> getBidHistory() {
        return bidHistory;
    }

    // Các getter display gom logic format ngay trong DTO để TableView/FXML chỉ cần bind trực tiếp.
    public String getLeadingBidderDisplay() {
        return highestBidderUsername == null || highestBidderUsername.isBlank()
                ? "Chưa có"
                : highestBidderUsername;
    }

    public String getEndTimeDisplay() {
        return endTime == null ? "Không rõ" : endTime.format(DATE_TIME_FORMATTER);
    }

    public String getTimeRemainingDisplay() {
        if (endTime == null) {
            return "Không rõ";
        }
        long remainingSeconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), endTime);
        if (remainingSeconds <= 0) {
            return "Đã kết thúc";
        }
        long hours = remainingSeconds / 3600;
        long minutes = (remainingSeconds % 3600) / 60;
        long seconds = remainingSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    // Hiển thị trạng thái theo ngôn ngữ người dùng thay vì mã enum thô.
    public String getStatusDisplay() {
        return switch (status) {
            case "OPEN" -> "Chờ mở phiên";
            case "RUNNING" -> "Đang diễn ra";
            case "FINISHED" -> "Đã kết thúc";
            case "PAID" -> "Chờ thanh toán";
            case "CANCELED" -> "Đã hủy";
            default -> status;
        };
    }

    // Chuẩn hóa lịch sử bid để các màn hình detail/live hiển thị đồng nhất hơn.
    public List<String> getBidHistoryDisplay() {
        return bidHistory.stream()
                .map(this::formatBidHistoryLine)
                .toList();
    }

    private String formatBidHistoryLine(String rawLine) {
        int endBracket = rawLine.indexOf(']');
        if (!rawLine.startsWith("[") || endBracket <= 1) {
            return rawLine;
        }

        String timeText = rawLine.substring(1, endBracket);
        try {
            LocalDateTime timestamp = LocalDateTime.parse(timeText);
            return "[" + timestamp.format(BID_TIME_FORMATTER) + "]" + rawLine.substring(endBracket + 1);
        } catch (Exception e) {
            return rawLine;
        }
    }
}
