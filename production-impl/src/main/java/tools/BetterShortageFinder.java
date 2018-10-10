package tools;

import entities.DemandEntity;
import entities.ProductionEntity;
import entities.ShortageEntity;
import enums.DeliverySchema;
import external.CurrentStock;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class BetterShortageFinder {

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
    public static List<ShortageEntity> findShortages(LocalDate today, int daysAhead, CurrentStock stock,
                                                     List<ProductionEntity> productions, List<DemandEntity> demands) {
        List<LocalDate> dates = Stream.iterate(today, date -> date.plusDays(1))
                .limit(daysAhead)
                .collect(toList());

        ProductionOutputs outputs = new ProductionOutputs(productions);
        Demands demandsPerDay = new Demands(demands);
        // TODO ASK including locked or only proper parts
        // TODO ASK current stock or on day start? what if we are in the middle of production a day?
        long level = stock.getLevel();

        List<ShortageEntity> gap = new LinkedList<>();
        for (LocalDate day : dates) {
            long produced = outputs.get(day);

            DailyDemand demand = demandsPerDay.getDemand(day);
            long levelOnDelivery = demand.calculateLevelOnDelivery(level, produced);
            if (levelOnDelivery < 0) {
                ShortageEntity entity = new ShortageEntity();
                entity.setRefNo(outputs.getProductRefNo());
                entity.setFound(LocalDate.now());
                entity.setAtDay(day);
                gap.add(entity);
            }
            long endOfDayLevel = level + produced - demand.getLevel();
            // TODO: ASK accumulated shortages or reset when under zero?
            level = endOfDayLevel >= 0 ? endOfDayLevel : 0;
        }
        return gap;
    }

    interface Calculation {
        Calculation AT_DAY_START = (long level, long produced, long demand)
                -> level - demand;
        Calculation TILL_DAY_END = (long level, long produced, long demand)
                -> level - demand + produced;
        Calculation CALCULATION_NOT_IMPLEMENTED = (long level, long produced, long demand) -> {
            throw new NotImplementedException();
        };

        long calculateLevelOnDelivery(long level, long produced, long demand);
    }

    private BetterShortageFinder() {
    }

    private static class ProductionOutputs {

        private final Map<LocalDate, ProductionEntity> outputs;
        private final String productRefNo;

        public ProductionOutputs(List<ProductionEntity> productions) {
            outputs = new HashMap<>();
            String productRefNo = null;
            for (ProductionEntity production : productions) {
                outputs.put(production.getStart().toLocalDate(), production);
                productRefNo = production.getForm().getRefNo();
            }
            this.productRefNo = productRefNo;
        }

        public long get(LocalDate day) {
            ProductionEntity production = outputs.get(day);
            if (production != null) {
                return production.getOutput();
            }
            return 0L;
        }

        public String getProductRefNo() {
            return productRefNo;
        }
    }

    private static class Demands {

        private final Map<LocalDate, DemandEntity> demandsPerDay;

        public Demands(List<DemandEntity> demands) {
            demandsPerDay = new HashMap<>();
            for (DemandEntity demand1 : demands) {
                demandsPerDay.put(demand1.getDay(), demand1);
            }
        }

        public DailyDemand getDemand(LocalDate day) {
            DemandEntity demand = demandsPerDay.get(day);
            if (demand == null) {
                return DailyDemand.nullObject();
            }
            return new DailyDemand(
                    Util.getLevel(demand),
                    pickVariant(Util.getDeliverySchema(demand))
            );
        }

        private Calculation pickVariant(DeliverySchema deliverySchema) {
//            Map<DeliverySchema, Calculation> mapping;
//            mapping.getOrDefault(deliverySchema, Calculation.CALCULATION_NOT_IMPLEMENTED);

            if (deliverySchema == DeliverySchema.atDayStart) {
                return Calculation.AT_DAY_START;
            } else if (deliverySchema == DeliverySchema.tillEndOfDay) {
                return Calculation.TILL_DAY_END;
            } else {
                return Calculation.CALCULATION_NOT_IMPLEMENTED;
            }
        }
    }

    private static class DailyDemand {

        private final long level;
        private final Calculation calculation;

        public DailyDemand(long level, Calculation calculation) {
            this.level = level;
            this.calculation = calculation;
        }

        public static DailyDemand nullObject() {
            return new DailyDemand(0L, Calculation.TILL_DAY_END);
        }

        public long getLevel() {
            return level;
        }

        public long calculateLevelOnDelivery(long level, long produced) {
            return calculation.calculateLevelOnDelivery(level, produced, this.level);
        }
    }
}
