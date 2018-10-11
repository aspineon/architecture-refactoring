package shortages;

public interface Notifications {
    void markOnPlan(Shortages shortages);

    void alertPlanner(Shortages shortages);

    void softNotifyPlanner(Shortages shortages);
}
