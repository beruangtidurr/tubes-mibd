import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

public class SRusunCLI {
    static Scanner scanner = new Scanner(System.in);
    static List<User> users = new ArrayList<>();
    static List<Pemilik> pemilikList = new ArrayList<>();
    static List<Sarusun> sarusunList = new ArrayList<>();
    static List<IoTDevice> iotDevices = new ArrayList<>();
    static List<String> logSystem = new ArrayList<>();
    static List<WaterUsageLog> waterLogs = new ArrayList<>();
    static User currentUser;

    public static void main(String[] args) {
        seedData();
        System.out.println("Welcome to Unit Sarusun");
        login();
    }

    static void seedData() {
        users.add(new User("admin01", "adminpass", "081", "Admin", null));
        users.add(new User("pengelola01", "pengelolapass", "082", "Pengelola", null));
        users.add(new User("pemilik01", "pemilikpass", "083", "Pemilik", "1234567890"));

        pemilikList.add(new Pemilik("1234567890", "Budi", "Jl. Mawar No.1", "083"));
        pemilikList.add(new Pemilik("2345678901", "Santi", "Jl. Melati No.2", "084"));

        sarusunList.add(new Sarusun("A0101", "A", 1, "1234567890"));
        sarusunList.add(new Sarusun("B0201", "B", 2, "2345678901"));

        iotDevices.add(new IoTDevice("SN001", "A0101", 0.0));
        iotDevices.add(new IoTDevice("SN002", "B0201", 0.0));

        // Dummy pemakaian air
        waterLogs.add(new WaterUsageLog("SN001", LocalDate.now().minusDays(1), 10.5));
        waterLogs.add(new WaterUsageLog("SN001", LocalDate.now(), 5.0));
        waterLogs.add(new WaterUsageLog("SN002", LocalDate.now().minusWeeks(1), 15.75));
        waterLogs.add(new WaterUsageLog("SN002", LocalDate.now(), 3.0));

        log("Dummy data loaded.");
    }

    static void login() {
        System.out.print("Username: ");
        String uname = scanner.nextLine();
        System.out.print("No HP: ");
        String phone = scanner.nextLine();
        System.out.print("Password: ");
        String pwd = scanner.nextLine();

        User user = users.stream()
                .filter(u -> u.username.equals(uname) && u.hp.equals(phone) && u.password.equals(pwd))
                .findFirst()
                .orElse(null);

        if (user != null) {
            String otp = generateOTP();
            System.out.println("Kode OTP (simulasi): " + otp);
            System.out.print("Masukkan OTP: ");
            String inputOtp = scanner.nextLine();
            if (otp.equals(inputOtp)) {
                currentUser = user;
                log("Login sebagai " + user.role + " (" + user.username + ")");
                showMenu();
            } else {
                System.out.println("OTP salah.");
            }
        } else {
            System.out.println("User tidak ditemukan atau password salah.");
        }
    }

    static void showMenu() {
        while (true) {
            System.out.println("\n MENU (" + currentUser.role.toUpperCase() + ")");
            switch (currentUser.role) {
                case "Admin" -> adminMenu();
                case "Pengelola" -> pengelolaMenu();
                case "Pemilik" -> pemilikMenu();
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
        sarusunList.add(new Sarusun(id, tower, lantai, nik));
        log("Tambah sarusun " + id);
        System.out.println("Sarusun successfully added.");
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
        pemilikList.add(new Pemilik(nik, nama, alamat, hp));
        log("Tambah pemilik " + nik);
        System.out.println("Pemilik successfully added.\n");
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
        iotDevices.add(new IoTDevice(sn, id, 0.0));
        log("Tambah perangkat SN " + sn);
        System.out.println("Perangkat successfully added.");
    }

    static void kendalikanAirSemua() {
        System.out.print("SN Perangkat: ");
        String sn = scanner.nextLine();
        IoTDevice d = findDevice(sn);
        if (d != null) {
            System.out.print("Perintah (on/off): ");
            d.status = scanner.nextLine().equalsIgnoreCase("on");
            log("Pengelola set air " + (d.status ? "ON" : "OFF") + " untuk " + sn);
        }
    }

    static void kendalikanAirPemilik() {
        System.out.print("SN Perangkat Anda: ");
        String sn = scanner.nextLine();
        IoTDevice d = findDevice(sn);
        if (d != null && isOwnedByCurrentUser(d.idSarusun)) {
            System.out.print("Perintah (on/off): ");
            d.status = scanner.nextLine().equalsIgnoreCase("on");
            log("Pemilik set air " + (d.status ? "ON" : "OFF") + " untuk " + sn);
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
        int pilihan = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Masukkan tanggal acuan (yyyy-mm-dd): ");
        LocalDate tanggal = LocalDate.parse(scanner.nextLine());

        for (IoTDevice d : iotDevices) {
            if (!semuaUnit && !isOwnedByCurrentUser(d.idSarusun)) continue;

            List<WaterUsageLog> logs = getLogsForDevice(d.sn, pilihan, tanggal);
            double total = logs.stream().mapToDouble(l -> l.volume).sum();

            System.out.printf("%s | SN: %s | Total Air: %.2fL | Status: %s\n",
                    d.idSarusun, d.sn, total, d.status ? "ON" : "OFF");

            for (WaterUsageLog log : logs) {
                System.out.printf("  - [%s] %.2fL\n", log.date, log.volume);
            }
        }
    }

    static List<WaterUsageLog> getLogsForDevice(String sn, int mode, LocalDate tanggal) {
        return waterLogs.stream().filter(log -> {
            if (!log.sn.equals(sn)) return false;
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
        return currentUser.role.equals("Pemilik") &&
                sarusunList.stream().anyMatch(s -> s.id.equals(idSarusun) && s.nikPemilik.equals(currentUser.ownerNIK));
    }

    static IoTDevice findDevice(String sn) {
        return iotDevices.stream().filter(d -> d.sn.equals(sn)).findFirst().orElse(null);
    }

    static String generateOTP() {
        return String.format("%06d", new Random().nextInt(1000000));
    }

    static void log(String msg) {
        logSystem.add("[LOG] " + msg);
    }

    static void exit() {
        log("Logout " + currentUser.username);
        currentUser = null;
        System.out.println("Logout berhasil.\n");
        login();
    }

    // --------------------- Data Classes ---------------------
    static class Sarusun {
        String id, tower;
        i
