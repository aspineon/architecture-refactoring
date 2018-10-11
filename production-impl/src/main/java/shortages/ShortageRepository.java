package shortages;

public interface ShortageRepository {
    Shortages get(String refNo);

    void save(Shortages shortages);

    void delete(String refNo);
}
