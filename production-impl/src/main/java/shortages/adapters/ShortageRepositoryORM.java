package shortages.adapters;

import dao.ShortageDao;
import entities.ShortageEntity;
import shortages.Shortages;

import java.util.List;

public class ShortageRepositoryORM implements shortages.ShortageRepository {
    ShortageDao dao;

    public Shortages get(String refNo) {
        List<ShortageEntity> entities = dao.getForProduct(refNo);
        return null;
    }

    public void save(Shortages shortages) {
        // for each shortages
        // dao.save(entity);
    }

    public void delete(String refNo) {
        dao.delete(refNo);
    }
}
