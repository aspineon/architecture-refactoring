package shortages;

import external.CurrentStock;

import java.time.LocalDate;

public interface ShortageFinderRepository {

    BetterShortageFinder get(String productRefNo, LocalDate today, CurrentStock stock);
}
