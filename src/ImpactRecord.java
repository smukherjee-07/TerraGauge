import java.time.LocalDateTime;

public class ImpactRecord {
    private int impactId;
    private final int activityId;
    private final double emissionFactor;
    private final double co2eKg;
    private final LocalDateTime calculatedAt;

    public ImpactRecord(int impactId, int activityId, double emissionFactor, double co2eKg, LocalDateTime calculatedAt) {
        this.impactId = impactId;
        this.activityId = activityId;
        this.emissionFactor = emissionFactor;
        this.co2eKg = co2eKg;
        this.calculatedAt = calculatedAt;
    }

    public ImpactRecord(int activityId, double emissionFactor, double co2eKg) {
        this(0, activityId, emissionFactor, co2eKg, LocalDateTime.now());
    }

    public int getImpactId() {
        return impactId;
    }

    public void setImpactId(int impactId) {
        this.impactId = impactId;
    }

    public int getActivityId() {
        return activityId;
    }

    public double getEmissionFactor() {
        return emissionFactor;
    }

    public double getCo2eKg() {
        return co2eKg;
    }

    public LocalDateTime getCalculatedAt() {
        return calculatedAt;
    }

    @Override
    public String toString() {
        return String.format("ImpactRecord [ID: %d, Activity ID: %d, Factor: %.4f, Total CO2e: %.3f kg]",
                impactId, activityId, emissionFactor, co2eKg);
    }
}