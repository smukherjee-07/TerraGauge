import java.time.LocalDate;

public class Activity {
    private int activityId;
    private int userId;
    private ActivityCategory category;
    private String activityName;
    private double quantity;
    private String unit;
    private LocalDate activityDate;

    public Activity(int activityId, int userId, ActivityCategory category, String activityName, double quantity, String unit, LocalDate activityDate) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
        this.activityId = activityId;
        this.userId = userId;
        this.category = category;
        this.activityName = activityName;
        this.quantity = quantity;
        this.unit = unit;
        this.activityDate = activityDate;
    }

    public Activity(int userId, ActivityCategory category, String activityName, double quantity, String unit, LocalDate activityDate) {
        this(0, userId, category, activityName, quantity, unit, activityDate);
    }

    public int getActivityId() {
        return activityId;
    }

    public void setActivityId(int activityId) {
        this.activityId = activityId;
    }

    public int getUserId() {
        return userId;
    }

    public ActivityCategory getCategory() {
        return category;
    }

    public String getActivityName() {
        return activityName;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public LocalDate getActivityDate() {
        return activityDate;
    }

    public double calculateEstimatedCo2e(double factor) {
        if (factor < 0) {
            throw new IllegalArgumentException("Emission factor cannot be negative.");
        }
        return this.quantity * factor;
    }

    @Override
    public String toString() {
        return String.format("Activity ID %d | %s | %s | %.2f %s | %s",
                activityId, category, activityName, quantity, unit, activityDate);
    }
}