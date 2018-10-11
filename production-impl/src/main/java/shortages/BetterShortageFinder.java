package shortages;

import external.CurrentStock;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class BetterShortageFinder {

    private final CurrentStock stock;
    private final ProductionOutputs outputs;
    private final Demands demands;

    public BetterShortageFinder(CurrentStock stock, ProductionOutputs outputs, Demands demands) {
        this.stock = stock;
        this.outputs = outputs;
        this.demands = demands;
    }

    /**
     * Production at day of expected delivery is quite complex:
     * We are able to produce and deliver just in time at same day
     * but depending on delivery time or scheme of multiple deliveries,
     * we need to plan properly to have right amount of parts ready before delivery time.
     * <p/>
     * Typical schemas are:
     * <li>Delivery at prod day start</li>
     * <li>Delivery till prod day end</li>
     * <li>Delivery during specified shift</li>
     * <li>Multiple deliveries at specified times</li>
     * Schema changes the way how we calculate shortages.
     * Pick of schema depends on customer demand on daily basis and for each product differently.
     * Some customers includes that information in callof document,
     * other stick to single schema per product. By manual adjustments of demand,
     * customer always specifies desired delivery schema
     * (increase amount in scheduled transport or organize extra transport at given time)
     */
    public Shortages findShortages(LocalDate today, int daysAhead) {
        List<LocalDate> dates = Stream.iterate(today, date -> date.plusDays(1))
                .limit(daysAhead)
                .collect(toList());

        // TODO ASK including locked or only proper parts
        // TODO ASK current stock or on day start? what if we are in the middle of production a day?
        long level = stock.getLevel();

        Shortages.Builder gap = Shortages.builder(outputs.getProductRefNo(), LocalDate.now());
        for (LocalDate day : dates) {
            long produced = outputs.get(day);
            Demands.DailyDemand demand = demands.getDemand(day);

            long levelOnDelivery = demand.calculateLevelOnDelivery(level, produced);
            if (levelOnDelivery < 0) {
                gap.add(day, levelOnDelivery);
            }
            long endOfDayLevel = level + produced - demand.getLevel();
            // TODO: ASK accumulated shortages or reset when under zero?
            level = endOfDayLevel >= 0 ? endOfDayLevel : 0;
        }
        return gap.build();
    }

}
