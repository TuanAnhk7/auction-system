package auction.client.view;

import auction.common.model.user.Bidder.Bid;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import javafx.application.Platform;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import javafx.scene.shape.Circle;

public class AuctionHistoryChart extends LineChart<Number, Number> {
    private final XYChart.Series<Number, Number> series;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    public AuctionHistoryChart() {
        super(new NumberAxis(), new NumberAxis());
        series = new XYChart.Series<>();
        this.getData().add(series);
        this.setTitle("Lịch sử đấu giá");
        this.setAnimated(false);
        this.setLegendVisible(false);
        this.setCreateSymbols(true);
        ((NumberAxis) this.getXAxis()).setLabel("Lượt bid");
        ((NumberAxis) this.getYAxis()).setLabel("Giá đấu");
    }

    public void updateChart(List<Bid> bids) {
        Platform.runLater(() -> {
            series.getData().clear();
            if (bids.isEmpty()) {
                return;
            }

            int bidIndex = 1;
            for (Bid bid : bids) {
                double x = bidIndex++;

                XYChart.Data<Number, Number> data =
                        new XYChart.Data<>(x, bid.getBidAmount());

                Circle circle = new Circle(5);
                circle.getStyleClass().add("auction-history-point");

                Tooltip.install(
                        circle,
                        new Tooltip(
                                bid.getBidderName()
                                        + "\n"
                                        + String.format(Locale.US, "%.2f", bid.getBidAmount())
                                        + " USD"
                                        + "\n"
                                        + bid.getBidTime().format(TIME_FORMATTER)
                        )
                );

                data.setNode(circle);

                series.getData().add(data);
            }
        });
    }
}
