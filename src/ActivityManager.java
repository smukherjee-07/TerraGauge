import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ActivityManager {
    private final DatabaseManager dbManager;

    public ActivityManager() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public int addActivity(Activity activity) throws SQLException {
        String sql = "INSERT INTO activities (user_id, category, activity_name, quantity, unit, activity_date) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, activity.getUserId());
            stmt.setString(2, activity.getCategory().name());
            stmt.setString(3, activity.getActivityName());
            stmt.setDouble(4, activity.getQuantity());
            stmt.setString(5, activity.getUnit());
            stmt.setDate(6, Date.valueOf(activity.getActivityDate()));

            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int generatedId = rs.getInt(1);
                    activity.setActivityId(generatedId);
                    return generatedId;
                }
            }
        }
        return 0;
    }

    public List<Activity> getActivitiesByUser(int userId) throws SQLException {
        List<Activity> list = new ArrayList<>();
        String sql = "SELECT * FROM activities WHERE user_id = ? ORDER BY activity_date DESC";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToActivity(rs));
                }
            }
        }
        return list;
    }

    public List<Activity> getUncalculatedActivities(int userId) throws SQLException {
        List<Activity> list = new ArrayList<>();
        String sql = "SELECT a.* FROM activities a LEFT JOIN impact_records i ON a.activity_id = i.activity_id WHERE a.user_id = ? AND i.impact_id IS NULL";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToActivity(rs));
                }
            }
        }
        return list;
    }

    public boolean updateActivityQuantity(int activityId, double newQuantity) throws SQLException {
        if (newQuantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive.");
        }
        String sql = "UPDATE activities SET quantity = ? WHERE activity_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, newQuantity);
            stmt.setInt(2, activityId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deleteActivity(int activityId) throws SQLException {
        String sql = "DELETE FROM activities WHERE activity_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, activityId);
            return stmt.executeUpdate() > 0;
        }
    }

    public void saveImpactBatch(List<ImpactRecord> records) throws SQLException {
        String sql = "INSERT INTO impact_records (activity_id, emission_factor, co2e_kg) VALUES (?, ?, ?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            for (ImpactRecord ir : records) {
                stmt.setInt(1, ir.getActivityId());
                stmt.setDouble(2, ir.getEmissionFactor());
                stmt.setDouble(3, ir.getCo2eKg());
                stmt.addBatch();
            }
            stmt.executeBatch();
            conn.commit();
            conn.setAutoCommit(true);
        }
    }

    private Activity mapRowToActivity(ResultSet rs) throws SQLException {
        return new Activity(
                rs.getInt("activity_id"),
                rs.getInt("user_id"),
                ActivityCategory.fromString(rs.getString("category")),
                rs.getString("activity_name"),
                rs.getDouble("quantity"),
                rs.getString("unit"),
                rs.getDate("activity_date").toLocalDate()
        );
    }
}