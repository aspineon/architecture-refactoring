package shortages;

import java.time.LocalDate;
import java.util.Map;

public class Demands {

    private final Map<LocalDate, DailyDemand> demandsPerDay;

    public Demands(Map<LocalDate, DailyDemand> demands) {
        demandsPerDay = demands;
    }

    public DailyDemand getDemand(LocalDate day) {
        return demandsPerDay.getOrDefault(day, DailyDemand.nullObject());
    }

    public static class DailyDemand {

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
