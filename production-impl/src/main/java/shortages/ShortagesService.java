package shortages;

import external.CurrentStock;
import external.JiraService;
import external.StockService;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

public class ShortagesService {

    private StockService stockService;

    private ShortageRepository shortageRepository;
    private ShortageFinderRepository finderRepository;

    private Notifications notification;
    private JiraService jiraService;
    private Clock clock;

    private int confShortagePredictionDaysAhead;
    private long confIncreaseQATaskPriorityInDays;

    public void processShortagesFromPlanning(List<String> changedRefNos) {
        LocalDate today = LocalDate.now(clock);

        for (String refNo : changedRefNos) {
            CurrentStock currentStock = stockService.getCurrentStock(refNo);
            Shortages shortages = finderRepository.get(refNo, today, currentStock)
                    .findShortages(today, confShortagePredictionDaysAhead);

            Shortages previous = shortageRepository.get(refNo);
            if (shortages.isDifferentThen(previous)) {
                notification.markOnPlan(shortages);
                if (currentStock.getLocked() > 0 &&
                        shortages.isFirstBefore(today.plusDays(confIncreaseQATaskPriorityInDays))) {
                    jiraService.increasePriorityFor(refNo);
                }
                shortageRepository.save(shortages);
            }
            if (shortages.isSolvedComparingTo(previous)) {
                shortageRepository.delete(refNo);
            }
        }
    }

    public void processShortagesFromLogistic(String productRefNo) {
        LocalDate today = LocalDate.now(clock);
        CurrentStock stock = stockService.getCurrentStock(productRefNo);
        Shortages shortages = finderRepository.get(productRefNo, today, stock)
                .findShortages(today, confShortagePredictionDaysAhead);

        Shortages previous = shortageRepository.get(productRefNo);
        if (shortages.isDifferentThen(previous)) {
            notification.alertPlanner(shortages);
            if (stock.getLocked() > 0 &&
                    shortages.isFirstBefore(today.plusDays(confIncreaseQATaskPriorityInDays))) {
                jiraService.increasePriorityFor(productRefNo);
            }
            shortageRepository.save(shortages);
        }
        if (shortages.isSolvedComparingTo(previous)) {
            shortageRepository.delete(productRefNo);
        }
    }

    public void processShortagesFromWarehouse(String productRefNo) {
        LocalDate today = LocalDate.now(clock);
        CurrentStock currentStock = stockService.getCurrentStock(productRefNo);
        Shortages shortages = finderRepository.get(productRefNo, today, currentStock)
                .findShortages(today, confShortagePredictionDaysAhead);

        Shortages previous = shortageRepository.get(productRefNo);
        if (shortages.isDifferentThen(previous)) {
            notification.alertPlanner(shortages);
            if (currentStock.getLocked() > 0 &&
                    shortages.isFirstBefore(today.plusDays(confIncreaseQATaskPriorityInDays))) {
                jiraService.increasePriorityFor(productRefNo);
            }
        }
        if (shortages.isSolvedComparingTo(previous)) {
            shortageRepository.delete(productRefNo);
        }
    }

    public void processShortagesFromQuality(String productRefNo) {
        LocalDate today = LocalDate.now(clock);
        CurrentStock currentStock = stockService.getCurrentStock(productRefNo);
        Shortages shortages = finderRepository.get(productRefNo, today, currentStock)
                .findShortages(today, confShortagePredictionDaysAhead);

        Shortages previous = shortageRepository.get(productRefNo);
        if (shortages.isDifferentThen(previous)) {
            notification.softNotifyPlanner(shortages);
            if (currentStock.getLocked() > 0 &&
                    shortages.isFirstBefore(today.plusDays(confIncreaseQATaskPriorityInDays))) {
                jiraService.increasePriorityFor(productRefNo);
            }
        }
        if (shortages.isSolvedComparingTo(previous)) {
            shortageRepository.delete(productRefNo);
        }
    }

}
