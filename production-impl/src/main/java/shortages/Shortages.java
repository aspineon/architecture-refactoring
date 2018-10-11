package shortages;

import java.time.LocalDate;
import java.util.SortedMap;
import java.util.TreeMap;

public class Shortages {
    private final String refNo;
    private final LocalDate found;
    private final SortedMap<LocalDate, Long> shortagesPerDay;

    public Shortages(String refNo, LocalDate found, SortedMap<LocalDate, Long> shortagesPerDay) {
        this.refNo = refNo;
        this.found = found;
        this.shortagesPerDay = shortagesPerDay;
    }

    public static Builder builder(String refNo, LocalDate found) {
        return new Builder(refNo, found);
    }

    public boolean isFirstBefore(LocalDate date) {
        return shortagesPerDay.firstKey()
                .isBefore(date);

    }

    public boolean isDifferentThen(Shortages previous) {
        return false;
    }

    public boolean isSolvedComparingTo(Shortages previous) {
        return false;
    }

    public static class Builder {
        private final String refNo;
        private final LocalDate found;
        private final SortedMap<LocalDate, Long> shortagesPerDay;

        public Builder(String refNo, LocalDate found) {
            this.refNo = refNo;
            this.found = found;
            shortagesPerDay = new TreeMap<>();
        }

        public void add(LocalDate day, long levelOnDelivery) {
            shortagesPerDay.put(day, levelOnDelivery);
        }

        public Shortages build() {
            return new Shortages(refNo, found, shortagesPerDay);
        }
    }
}
