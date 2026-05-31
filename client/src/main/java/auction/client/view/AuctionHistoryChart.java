package auction.client.view;

import auction.common.model.user.Bidder.Bid;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Comparator;

import javafx.application.Platform;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;

public class AuctionHistoryChart extends LineChart<Number, Number> {
    private final XYChart.Series<Number, Number> series;

    public AuctionHistoryChart() {
        super(new NumberAxis(), new NumberAxis());
        series = new XYChart.Series<>();
        this.getData().add(series);
        this.setTitle("Lịch sử đấu giá");
        ((NumberAxis) this.getXAxis()).setLabel("Thời gian (phút)");
        ((NumberAxis) this.getYAxis()).setLabel("Giá đấu");
    }

    public void updateChart(List<Bid> bids) {
        Platform.runLater(() -> {
            series.getData().clear();
            if (bids.isEmpty()) {
                return;
            }

            long startTime = bids.get(0)
                    .getBidTime()
                    .atZone(ZoneId.systemDefault())
                    .toEpochSecond();

            for (Bid bid : bids) {

                long x =
                        bid.getBidTime()
                                .atZone(ZoneId.systemDefault())
                                .toEpochSecond()
                                - startTime;

                XYChart.Data<Number, Number> data =
                        new XYChart.Data<>(x, bid.getBidAmount());

                Circle circle = new Circle(5);

                Tooltip.install(
                        circle,
                        new Tooltip(
                                bid.getBidderName()
                                        + "\n"
                                        + bid.getBidAmount()
                        )
                );

                data.setNode(circle);

                series.getData().add(data);
            }
        });
    }
}