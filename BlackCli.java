import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

// javac -cp ".:mssql-jdbc-12.10.0.jre11.jar" Test1.java
// java -cp ".:mssql-jdbc-12.10.0.jre11.jar" Test1

// Dummy Data
// nik : 123456, admin123
// nik : 12345, pengelola123
// nik : 123, wombat123

public class BlackCli {
    static Scanner scanner = new Scanner(System.in);
    static User currentUser;
    static Connection conn = null;

    public static void main(String[] args) {
        setupDatabase();
        System.out.println("Welcome to Unit Sarusun");
        showMainMenu();
    }

    static void showMainMenu() {
        while (true) {
            System.out.println("\nMAIN MENU");
            System.out.println("1. Login");
            // System.out.println("2. Register");
            System.out.println("0. Exit");
            System.out.print("Choose option: ");
            
            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline
            
            switch (choice) {
                case 1 -> login();
                // case 2 -> register();
                case 0 -> {
                    System.out.println("Thank you for using Unit Sarusun!");
                    System.exit(0);
                }
                default -> System.out.println("Invalid option!");
            }
        }
    }

    static void register() {
        System.out.println("\n=== REGISTER NEW USER ===");
        System.out.print("NIK: ");
        String nik = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        System.out.print("No HP: ");
        String hp = scanner.nextLine();
        System.out.print("Role (Admin/Pengelola/Pemilik): ");
        String role = scanner.nextLine();
        
        String ownerNIK = null;
        if (role.equalsIgnoreCase("Pemilik")) {
            System.out.print("NIK Pemilik: ");
            ownerNIK = scanner.nextLine();
            System.out.print("Nama: ");
            String nama = scanner.nextLine();
            System.out.print("Alamat: ");
            String alamat = scanner.nextLine();
            
            try {
                // First insert into Pemilik table
                PreparedStatement pemilikStmt = conn.prepareStatement(
                    "INSERT INTO Pemilik (nik, nama, alamat, hp) VALUES (?, ?, ?, ?)"
                );
                pemilikStmt.setString(1, ownerNIK);
                pemilikStmt.setString(2, nama);
                pemilikStmt.setString(3, alamat);
                pemilikStmt.setString(4, hp);
                pemilikStmt.executeUpdate();
                
                log("New pemilik registered: " + ownerNIK);
            } catch (SQLException e) {
                if (e.getMessage().contains("PRIMARY KEY")) {
                    System.out.println("NIK already exists in database.");
                } else {
                    System.err.println("Error registering pemilik:");
                    e.printStackTrace();
                    return;
                }
            }
        }

        try {
            // Check if NIK already exists
            PreparedStatement checkStmt = conn.prepareStatement(
                "SELECT COUNT(*) as count FROM Users WHERE nik = ?"
            );
            checkStmt.setString(1, nik);
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next() && rs.getInt("count") > 0) {
                System.out.println("NIK already exists. Please choose another NIK.");
                return;
            }

            // Insert new user
            PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO Users (NIK, password, hp, role, ownerNIK) VALUES (?, ?, ?, ?, ?)"
            );
            stmt.setString(1, nik);
            stmt.setString(2, password);
            stmt.setString(3, hp);
            stmt.setString(4, role);
            stmt.setString(5, ownerNIK);
            
            stmt.executeUpdate();
            System.out.println("Registration successful! Please login.");
            log("New user registered: " + nik);
            
        } catch (SQLException e) {
            System.err.println("Error during registration:");
            e.printStackTrace();
        }
    }

    static void setupDatabase() {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=Sarusun;encrypt=false";
        String user = "sa";
        String pass = "Hello122";

        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            conn = DriverManager.getConnection(url, user, pass);
            System.out.println("✅ Connected to database successfully!");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ JDBC Driver not found. Did you include the .jar?");
            e.printStackTrace();
            System.exit(1);
        } catch (SQLException e) {
            System.err.println("❌ Failed to connect to database:");
            e.printStackTrace();
            System.exit(1);
        }
    }

    static void seedData() {
        try {
            // Insert Users
            PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO Users (NIK, password, hp, role, ownerNIK) VALUES (?, ?, ?, ?, ?)"
            );
            
            // Admin
            stmt.setString(1, "admin01");
            stmt.setString(2, "adminpass");
            stmt.setString(3, "081");
            stmt.setString(4, "Admin");
            stmt.setString(5, null);
            stmt.executeUpdate();

            // Pengelola
            stmt.setString(1, "pengelola01");
            stmt.setString(2, "pengelolapass");
            stmt.setString(3, "082");
            stmt.setString(4, "Pengelola");
            stmt.setString(5, null);
            stmt.executeUpdate();

            // Pemilik
            stmt.setString(1, "pemilik01");
            stmt.setString(2, "pemilikpass");
            stmt.setString(3, "083");
            stmt.setString(4, "Pemilik");
            stmt.setString(5, "1234567890");
            stmt.executeUpdate();

            // Insert Pemilik
            stmt = conn.prepareStatement(
                "INSERT INTO Pemilik (nik, nama, alamat, hp) VALUES (?, ?, ?, ?)"
            );
            
            stmt.setString(1, "1234567890");
            stmt.setString(2, "Budi");
            stmt.setString(3, "Jl. Mawar No.1");
            stmt.setString(4, "083");
            stmt.executeUpdate();

            stmt.setString(1, "2345678901");
            stmt.setString(2, "Santi");
            stmt.setString(3, "Jl. Melati No.2");
            stmt.setString(4, "084");
            stmt.executeUpdate();

            // Insert Sarusun
            stmt = conn.prepareStatement(
                "INSERT INTO Sarusun (id, tower, lantai, nikPemilik) VALUES (?, ?, ?, ?)"
            );
            
            stmt.setString(1, "A0101");
            stmt.setString(2, "A");
            stmt.setInt(3, 1);
            stmt.setString(4, "1234567890");
            stmt.executeUpdate();

            stmt.setString(1, "B0201");
            stmt.setString(2, "B");
            stmt.setInt(3, 2);
            stmt.setString(4, "2345678901");
            stmt.executeUpdate();

            // Insert IoTDevice
            stmt = conn.prepareStatement(
                "INSERT INTO IoTDevice (sn, idSarusun, status, totalAir) VALUES (?, ?, ?, ?)"
            );
            
            stmt.setString(1, "SN001");
            stmt.setString(2, "A0101");
            stmt.setBoolean(3, true);
            stmt.setDouble(4, 0.0);
            stmt.executeUpdate();

            stmt.setString(1, "SN002");
            stmt.setString(2, "B0201");
            stmt.setBoolean(3, true);
            stmt.setDouble(4, 0.0);
            stmt.executeUpdate();

            // Insert WaterUsageLog
            stmt = conn.prepareStatement(
                "INSERT INTO WaterUsageLog (sn, date, volume) VALUES (?, ?, ?)"
            );
            
            stmt.setString(1, "SN001");
            stmt.setDate(2, java.sql.Date.valueOf(LocalDate.now().minusDays(1)));
            stmt.setDouble(3, 10.5);
            stmt.executeUpdate();

            stmt.setString(1, "SN001");
            stmt.setDate(2, java.sql.Date.valueOf(LocalDate.now()));
            stmt.setDouble(3, 5.0);
            stmt.executeUpdate();

            stmt.setString(1, "SN002");
            stmt.setDate(2, java.sql.Date.valueOf(LocalDate.now().minusWeeks(1)));
            stmt.setDouble(3, 15.75);
            stmt.executeUpdate();

            stmt.setString(1, "SN002");
            stmt.setDate(2, java.sql.Date.valueOf(LocalDate.now()));
            stmt.setDouble(3, 3.0);
            stmt.executeUpdate();

            log("Dummy data loaded.");
        } catch (SQLException e) {
            System.err.println("Error seeding data:");
            e.printStackTrace();
        }
    }

    static void login() {
        System.out.print("NIK: ");
        String uNik = scanner.nextLine();
        System.out.print("No HP: ");
        String phone = scanner.nextLine();
        System.out.print("Password: ");
        String pwd = scanner.nextLine();

        try {
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM Users WHERE NIK = ? AND hp = ? AND password = ?"
            );
            stmt.setString(1, uNik);
            stmt.setString(2, phone);
            stmt.setString(3, pwd);
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String otp = generateOTP();
                System.out.println("Kode OTP (simulasi): " + otp);
                System.out.print("Masukkan OTP: ");
                String inputOtp = scanner.nextLine();
                if (otp.equals(inputOtp)) {
                    currentUser = new User(
                        rs.getString("NIK"),
                        rs.getString("password"),
                        rs.getString("hp"),
                        rs.getString("role"),
                        rs.getString("ownerNIK")
                    );
                    log("Login sebagai " + currentUser.role + " (" + currentUser.NIK + ")");
                    showMenu();
                } else {
                    System.out.println("OTP salah.");
                }
            } else {
                System.out.println("User tidak ditemukan atau password salah.");
            }
        } catch (SQLException e) {
            System.err.println("Error during login:");
            e.printStackTrace();
        }
    }

    static void showMenu() {
        while (true) {
            System.out.println("\n MENU (" + currentUser.role.toUpperCase() + ")");
            switch (currentUser.role) {
                case "Admin" -> adminMenu();
                case "admin" -> adminMenu();
                case "Pengelola" -> pengelolaMenu();
                case "pengelola" -> pengelolaMenu();
                case "Pemilik" -> pemilikMenu();
                case "pemilik" -> pemilikMenu();
            }
        }
    }

    static void adminMenu() {
        System.out.println("1. Kelola Sarusun");
        System.out.println("2. Kelola Pemilik");
        System.out.println("3. Kelola Perangkat IoT");
        System.out.println("0. Logout");
        int ch = scanner.nextInt();
        scanner.nextLine();
        switch (ch) {
            case 1 -> kelolaSarusun();
            case 2 -> kelolaPemilik();
            case 3 -> kelolaPerangkat();
            case 0 -> exit();
        }
    }

    static void pengelolaMenu() {
        System.out.println("1. Laporan Pemakaian Air Semua Unit");
        System.out.println("2. Kendalikan Perangkat Air");
        System.out.println("0. Logout");
        int ch = scanner.nextInt();
        scanner.nextLine();
        switch (ch) {
            case 1 -> laporanPemakaian();
            case 2 -> kendalikanAirSemua();
            case 0 -> exit();
        }
    }

    static void pemilikMenu() {
        System.out.println("1. Laporan Pemakaian Air Unit Saya");
        System.out.println("2. Kendalikan Air Unit Saya");
        System.out.println("0. Logout");
        int ch = scanner.nextInt();
        scanner.nextLine();
        switch (ch) {
            case 1 -> laporanPemakaianPemilik();
            case 2 -> kendalikanAirPemilik();
            case 0 -> exit();
        }
    }

    static void kelolaSarusun() {
        System.out.print("ID Sarusun: ");
        String id = scanner.nextLine();
        System.out.print("Tower: ");
        String tower = scanner.nextLine();
        System.out.print("Lantai: ");
        int lantai = scanner.nextInt();
        scanner.nextLine();
        System.out.print("NIK Pemilik: ");
        String nik = scanner.nextLine();

        try {
            PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO Sarusun (id, tower, lantai, nikPemilik) VALUES (?, ?, ?, ?)"
            );
            stmt.setString(1, id);
            stmt.setString(2, tower);
            stmt.setInt(3, lantai);
            stmt.setString(4, nik);
            stmt.executeUpdate();
            
            log("Tambah sarusun " + id);
            System.out.println("Sarusun successfully added.");
        } catch (SQLException e) {
            System.err.println("Error adding Sarusun:");
            e.printStackTrace();
        }
    }

    static void kelolaPemilik() {
        System.out.print("NIK: ");
        String nik = scanner.nextLine();
        System.out.print("Nama: ");
        String nama = scanner.nextLine();
        System.out.print("Alamat: ");
        String alamat = scanner.nextLine();
        System.out.print("No HP: ");
        String hp = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        String role = "Pemilik";

        try {
            PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO Pemilik (nik, nama, alamat, hp) VALUES (?, ?, ?, ?)"
            );
            stmt.setString(1, nik);
            stmt.setString(2, nama);
            stmt.setString(3, alamat);
            stmt.setString(4, hp);
            stmt.executeUpdate();
            
            log("Tambah pemilik " + nik);
            System.out.println("Pemilik successfully added.\n");
        } catch (SQLException e) {
            System.err.println("Error adding Pemilik:");
            e.printStackTrace();
        }

        try {
            // Insert new user
            PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO Users (NIK, password, hp, role, ownerNIK) VALUES (?, ?, ?, ?, ?)"
            );
            stmt.setString(1, nik);
            stmt.setString(2, password);
            stmt.setString(3, hp);
            stmt.setString(4, role);
            stmt.setString(5, nik);
            
            stmt.executeUpdate();
            System.out.println("Registration successful! Please login.");
            log("New user registered: " + nik);
        } catch (SQLException e) {
            System.err.println("Error adding User:");
            e.printStackTrace();
        }
    }

    static void kelolaPerangkat() {
        System.out.print("SN Perangkat: ");
        String sn = scanner.nextLine();

        if (findDevice(sn) != null) {
            System.out.println("SN sudah terdaftar. Gunakan SN yang unik.");
            return;
        }

        System.out.print("ID Sarusun: ");
        String id = scanner.nextLine();

        try {
            PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO IoTDevice (sn, idSarusun, status, totalAir) VALUES (?, ?, ?, ?)"
            );
            stmt.setString(1, sn);
            stmt.setString(2, id);
            stmt.setBoolean(3, true);
            stmt.setDouble(4, 0.0);
            stmt.executeUpdate();
            
            log("Tambah perangkat SN " + sn);
            System.out.println("Perangkat successfully added.");
        } catch (SQLException e) {
            System.err.println("Error adding IoT Device:");
            e.printStackTrace();
        }
    }

    static void kendalikanAirSemua() {
        System.out.print("SN Perangkat: ");
        String sn = scanner.nextLine();
        IoTDevice d = findDevice(sn);
        if (d != null) {
            System.out.print("Perintah (on/off): ");
            boolean newStatus = scanner.nextLine().equalsIgnoreCase("on");
            try {
                PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE IoTDevice SET status = ? WHERE sn = ?"
                );
                stmt.setBoolean(1, newStatus);
                stmt.setString(2, sn);
                stmt.executeUpdate();
                
                log("Pengelola set air " + (newStatus ? "ON" : "OFF") + " untuk " + sn);
            } catch (SQLException e) {
                System.err.println("Error updating device status:");
                e.printStackTrace();
            }
        }
    }

    static void kendalikanAirPemilik() {
        System.out.print("SN Perangkat Anda: ");
        String sn = scanner.nextLine();
        IoTDevice d = findDevice(sn);
        if (d != null && isOwnedByCurrentUser(d.idSarusun)) {
            System.out.print("Perintah (on/off): ");
            boolean newStatus = scanner.nextLine().equalsIgnoreCase("on");
            try {
                PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE IoTDevice SET status = ? WHERE sn = ?"
                );
                stmt.setBoolean(1, newStatus);
                stmt.setString(2, sn);
                stmt.executeUpdate();
                
                log("Pemilik set air " + (newStatus ? "ON" : "OFF") + " untuk " + sn);
            } catch (SQLException e) {
                System.err.println("Error updating device status:");
                e.printStackTrace();
            }
        } else {
            System.out.println("Anda tidak berhak mengubah perangkat unit lain.");
        }
    }

    static void laporanPemakaian() {
        laporan(true);
    }

    static void laporanPemakaianPemilik() {
        laporan(false);
    }

    static void laporan(boolean semuaUnit) {
        System.out.println("Pilih jenis laporan: ");
        System.out.println("1. Harian");
        System.out.println("2. Mingguan");
        System.out.println("3. Bulanan");
        System.out.println("0. Semua (tanpa filter tanggal)");
        int pilihan = scanner.nextInt();
        scanner.nextLine();

        LocalDate tanggal = null;
        if (pilihan != 0) {
            System.out.print("Masukkan tanggal acuan (yyyy-mm-dd): ");
            String tanggalInput = scanner.nextLine();
            if (!tanggalInput.isBlank()) {
                try {
                    tanggal = LocalDate.parse(tanggalInput);
                } catch (Exception e) {
                    System.out.println("Format tanggal tidak valid, menampilkan semua data.");
                    pilihan = 0;
                }
            } else {
                pilihan = 0;
            }
        }

        try {
            String query = "SELECT i.*, w.date, w.volume FROM IoTDevice i " +
                          "LEFT JOIN WaterUsageLog w ON i.sn = w.sn ";
            
            if (!semuaUnit) {
                query += "JOIN Sarusun s ON i.idSarusun = s.id " +
                        "WHERE s.nikPemilik = ?";
            }

            PreparedStatement stmt = conn.prepareStatement(query);
            if (!semuaUnit) {
                stmt.setString(1, currentUser.ownerNIK);
            }

            ResultSet rs = stmt.executeQuery();
            Map<String, List<WaterUsageLog>> deviceLogs = new HashMap<>();
            Map<String, IoTDevice> devices = new HashMap<>();

            while (rs.next()) {
                String sn = rs.getString("sn");
                if (!devices.containsKey(sn)) {
                    IoTDevice device = new IoTDevice(
                        sn,
                        rs.getString("idSarusun"),
                        rs.getDouble("totalAir")
                    );
                    device.status = rs.getBoolean("status");
                    devices.put(sn, device);
                    deviceLogs.put(sn, new ArrayList<>());
                }

                java.sql.Date date = rs.getDate("date");
                if (date != null) {
                    deviceLogs.get(sn).add(new WaterUsageLog(
                        sn,
                        date.toLocalDate(),
                        rs.getDouble("volume")
                    ));
                }
            }

            for (IoTDevice device : devices.values()) {
                List<WaterUsageLog> logs = deviceLogs.get(device.sn);
                List<WaterUsageLog> filteredLogs;
                if (pilihan == 0) {
                    filteredLogs = logs;
                } else {
                    filteredLogs = filterLogs(logs, pilihan, tanggal);
                }
                double total = filteredLogs.stream().mapToDouble(l -> l.volume).sum();

                System.out.printf("%s | SN: %s | Total Air: %.2fL | Status: %s\n",
                        device.idSarusun, device.sn, total, device.status ? "ON" : "OFF");

                for (WaterUsageLog log : filteredLogs) {
                    System.out.printf("  - [%s] %.2fL\n", log.date, log.volume);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error generating report:");
            e.printStackTrace();
        }
    }

    static List<WaterUsageLog> filterLogs(List<WaterUsageLog> logs, int mode, LocalDate tanggal) {
        return logs.stream().filter(log -> {
            switch (mode) {
                case 1: return log.date.equals(tanggal);
                case 2: return isSameWeek(log.date, tanggal);
                case 3: return log.date.getMonth() == tanggal.getMonth() &&
                        log.date.getYear() == tanggal.getYear();
                default: return false;
            }
        }).collect(Collectors.toList());
    }

    static boolean isSameWeek(LocalDate d1, LocalDate d2) {
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        return d1.get(weekFields.weekOfWeekBasedYear()) == d2.get(weekFields.weekOfWeekBasedYear()) &&
                d1.getYear() == d2.getYear();
    }

    static boolean isOwnedByCurrentUser(String idSarusun) {
        try {
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT COUNT(*) as count FROM Sarusun WHERE id = ? AND nikPemilik = ?"
            );
            stmt.setString(1, idSarusun);
            stmt.setString(2, currentUser.ownerNIK);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking ownership:");
            e.printStackTrace();
        }
        return false;
    }

    static IoTDevice findDevice(String sn) {
        try {
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM IoTDevice WHERE sn = ?"
            );
            stmt.setString(1, sn);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                IoTDevice device = new IoTDevice(
                    rs.getString("sn"),
                    rs.getString("idSarusun"),
                    rs.getDouble("totalAir")
                );
                device.status = rs.getBoolean("status");
                return device;
            }
        } catch (SQLException e) {
            System.err.println("Error finding device:");
            e.printStackTrace();
        }
        return null;
    }

    static String generateOTP() {
        return String.format("%06d", new Random().nextInt(1000000));
    }

    static void log(String msg) {
        System.out.println("[LOG] " + msg);
    }

    static void exit() {
        log("Logout " + currentUser.NIK);
        currentUser = null;
        System.out.println("Logout berhasil.\n");
        showMainMenu();
    }

    // --------------------- Data Classes ---------------------
    static class Sarusun {
        String id, tower;
        int lantai;
        String nikPemilik;
        Sarusun(String id, String tower, int lantai, String nikPemilik) {
            this.id = id; this.tower = tower; this.lantai = lantai; this.nikPemilik = nikPemilik;
        }
    }

    static class Pemilik {
        String nik, nama, alamat, hp;
        Pemilik(String nik, String nama, String alamat, String hp) {
            this.nik = nik; this.nama = nama; this.alamat = alamat; this.hp = hp;
        }
    }

    static class IoTDevice {
        String sn, idSarusun;
        boolean status = true;
        double totalAir = 0.0;
        IoTDevice(String sn, String idSarusun, double initial) {
            this.sn = sn; this.idSarusun = idSarusun; this.totalAir = initial;
        }
    }

    static class User {
        String NIK, password, hp, role, ownerNIK;
        User(String NIK, String password, String hp, String role, String ownerNIK) {
            this.NIK = NIK;
            this.password = password;
            this.hp = hp;
            this.role = role;
            this.ownerNIK = ownerNIK;
        }
    }

    static class WaterUsageLog {
        String sn;
        LocalDate date;
        double volume;
        WaterUsageLog(String sn, LocalDate date, double volume) {
            this.sn = sn;
            this.date = date;
            this.volume = volume;
        }
    }
}
