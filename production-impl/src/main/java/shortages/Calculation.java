package shortages;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public interface Calculation {
    Calculation AT_DAY_START = (level, produced, demand)
            -> level - demand;
    Calculation TILL_DAY_END = (level, produced, demand)
            -> level - demand + produced;
    Calculation CALCULATION_NOT_IMPLEMENTED = (level, produced, demand) -> {
        throw new NotImplementedException();
    };

    long calculateLevelOnDelivery(long level, long produced, long demand);
}
