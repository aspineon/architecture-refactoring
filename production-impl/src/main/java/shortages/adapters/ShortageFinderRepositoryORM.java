package shortages.adapters;

import dao.DemandDao;
import dao.ProductionDao;
import entities.DemandEntity;
import entities.ProductionEntity;
import enums.DeliverySchema;
import external.CurrentStock;
import external.StockService;
import shortages.BetterShortageFinder;
import shortages.Calculation;
import shortages.Demands;
import shortages.ProductionOutputs;
import tools.Util;

import java.time.Clock;
import java.time.LocalDate;
import java.util.stream.Collectors;

public class ShortageFinderRepositoryORM implements shortages.ShortageFinderRepository {
    private Clock clock;
    private DemandDao demandDao;
    private StockService stockService;
    private ProductionDao productionDao;

    public BetterShortageFinder get(String productRefNo, LocalDate today, CurrentStock stock) {
        return new BetterShortageFinder(
                stock,
                new ProductionOutputs(productRefNo,
                        productionDao.findFromTime(productRefNo, today.atStartOfDay())
                                .stream()
                                .collect(Collectors.toMap(
                                        production -> production.getStart().toLocalDate(),
                                        ProductionEntity::getOutput
                                ))),
                new Demands(demandDao.findFrom(today.atStartOfDay(), productRefNo).stream().collect(Collectors.toMap(
                        DemandEntity::getDay,
                        demand1 -> new Demands.DailyDemand(
                                Util.getLevel(demand1),
                                pickVariant(Util.getDeliverySchema(demand1))
                        )
                )))
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
