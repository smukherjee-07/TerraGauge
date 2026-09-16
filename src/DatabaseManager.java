import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private static final String DEFAULT_URL = System.getenv().getOrDefault(
            "TERRAGAUGE_DB_URL",
            "jdbc:mysql://localhost:3306/terragauge_db?useSSL=false&allowPublicKeyRetrieval=true");
    private String url = DEFAULT_URL;
    private String user = System.getenv().getOrDefault("TERRAGAUGE_DB_USER", "root");
    private String password = System.getenv().getOrDefault("TERRAGAUGE_DB_PASSWORD", "");

    private static DatabaseManager instance;

    private DatabaseManager() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL Driver not found. Add connector JAR to classpath: " + e.getMessage());
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public void configureCredentials(String user, String password) {
        this.user = user;
        this.password = password;
    }

    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}