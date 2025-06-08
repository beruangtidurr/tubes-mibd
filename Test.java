import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Test {
    public static void main(String[] args) {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=Sarusun;encrypt=false";
        String user = "sa"; // Change this if your SQL Server has different user
        String pass = "Hello122"; // Change this to match your Docker SQL Server password

        try {
            // Load the SQL Server JDBC driver
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            // Try to connect
            Connection conn = DriverManager.getConnection(url, user, pass);
            System.out.println("✅ Connected to SQL Server successfully!");

            // Close connection
            conn.close();
        } catch (ClassNotFoundException e) {
            System.err.println("❌ JDBC Driver not found. Did you include the .jar?");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("❌ Failed to connect to SQL Server:");
            e.printStackTrace();
        }
    }
}

// javac -cp ".:mssql-jdbc-12.10.0.jre11.jar" Test.java
// java -cp ".:mssql-jdbc-12.10.0.jre11.jar" Test
