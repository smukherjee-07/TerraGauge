public class EmissionFactor {
    private final ActivityCategory category;
    private final String activityName;
    private final double factor;
    private final String unit;
    private final String source;

    public EmissionFactor(ActivityCategory category, String activityName, double factor, String unit, String source) {
        if (factor < 0) {
            throw new IllegalArgumentException("Emission factor cannot be negative.");
        }
        this.category = category;
        this.activityName = activityName;
        this.factor = factor;
        this.unit = unit;
        this.source = source;
    }

    public ActivityCategory getCategory() {
        return category;
    }

    public String getActivityName() {
        return activityName;
    }

    public double getFactor() {
        return factor;
    }

    public String getUnit() {
        return unit;
    }

    public String getSource() {
        return source;
    }

    @Override
    public String toString() {
        return String.format("%s (%s): %.4f %s [%s]", activityName, category, factor, unit, source);
    }
}