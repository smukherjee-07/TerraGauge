import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ReportManager {
    private final DatabaseManager dbManager;

    public ReportManager() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public void printTotalImpact(int userId) {
        String sql = "SELECT COUNT(a.activity_id) AS total_count, COALESCE(SUM(i.co2e_kg), 0) AS total_co2e " +
                     "FROM activities a JOIN impact_records i ON a.activity_id = i.activity_id WHERE a.user_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Total Tracked Activities: " + rs.getInt("total_count"));
                    System.out.printf("Total Estimated Footprint: %.3f kg CO2e%n", rs.getDouble("total_co2e"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Report query failed: " + e.getMessage());
        }
    }

    public void printCategoryWiseImpact(int userId) {
        String sql = "SELECT a.category, COUNT(a.activity_id) AS entry_count, SUM(i.co2e_kg) AS category_co2e " +
                     "FROM activities a JOIN impact_records i ON a.activity_id = i.activity_id WHERE a.user_id = ? " +
                     "GROUP BY a.category ORDER BY category_co2e DESC";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                System.out.println("Category | Count | Total CO2e (kg)");
                while (rs.next()) {
                    System.out.printf("%-12s | %-5d | %-12.3f%n",
                            rs.getString("category"),
                            rs.getInt("entry_count"),
                            rs.getDouble("category_co2e"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Category summary query failed: " + e.getMessage());
        }
    }

    public void printTopEmittingActivities(int userId, int limit) {
        String sql = "SELECT a.activity_name, a.quantity, a.unit, a.activity_date, i.co2e_kg " +
                     "FROM activities a JOIN impact_records i ON a.activity_id = i.activity_id WHERE a.user_id = ? " +
                     "ORDER BY i.co2e_kg DESC LIMIT ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                System.out.println("Activity | Quantity | Date | Total CO2e (kg)");
                while (rs.next()) {
                    System.out.printf("%-18s | %-6.2f %-4s | %s | %-10.3f%n",
                            rs.getString("activity_name"),
                            rs.getDouble("quantity"),
                            rs.getString("unit"),
                            rs.getDate("activity_date"),
                            rs.getDouble("co2e_kg"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Ranked analysis query failed: " + e.getMessage());
        }
    }
}