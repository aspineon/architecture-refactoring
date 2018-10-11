package shortages;

import java.time.LocalDate;
import java.util.Map;

public class ProductionOutputs {

    private final Map<LocalDate, Long> outputs;
    private final String productRefNo;

    public ProductionOutputs(String refNo, Map<LocalDate, Long> outputs) {
        this.outputs = outputs;
        this.productRefNo = refNo;
    }

    public long get(LocalDate day) {
        return outputs.getOrDefault(day, 0L);
    }

    public String getProductRefNo() {
        return productRefNo;
    }
}
