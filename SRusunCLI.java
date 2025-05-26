import java.util.*;

public class SRusunCLI {
    static Scanner scanner = new Scanner(System.in);
    static List<User> users = new ArrayList<>();
    static List<Pemilik> pemilikList = new ArrayList<>();
    static List<Sarusun> sarusunList = new ArrayList<>();
    static List<IoTDevice> iotDevices = new ArrayList<>();
    static List<String> logSystem = new ArrayList<>();
    static User currentUser;

    public static void main(String[] args) {
        seedData();
        System.out.println("=== Sistem sRusun ===");
        login();
    }

    //  Data Dummy 
    static void seedData() {
    users.add(new User("admin01", "adminpass", "081", "Admin", null));
    users.add(new User("pengelola01", "pengelolapass", "082", "Pengelola", null));
    users.add(new User("pemilik01", "pemilikpass", "083", "Pemilik", "1234567890"));

    pemilikList.add(new Pemilik("1234567890", "Budi", "Jl. Mawar No.1", "083"));
    pemilikList.add(new Pemilik("2345678901", "Santi", "Jl. Melati No.2", "084"));

    sarusunList.add(new Sarusun("A0101", "A", 1, "1234567890"));
    sarusunList.add(new Sarusun("B0201", "B", 2, "2345678901"));

    iotDevices.add(new IoTDevice("SN001", "A0101", 120.5));
    iotDevices.add(new IoTDevice("SN002", "B0201", 98.75));

    log("Dummy data loaded.");
}


    //  Login & Role Menu 
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
            System.out.println("\n=== MENU (" + currentUser.role.toUpperCase() + ") ===");
            switch (currentUser.role) {
                case "Admin" -> adminMenu();
                case "Pengelola" -> pengelolaMenu();
                case "Pemilik" -> pemilikMenu();
            }
        }
    }

    // --------------------- Menu Per Role ---------------------
    static void adminMenu() {
        System.out.println("1. Kelola Sarusun");
        System.out.println("2. Kelola Pemilik");
        System.out.println("3. Kelola Perangkat IoT");
        System.out.println("0. Logout");
        int ch = scanner.nextInt(); scanner.nextLine();
        switch (ch) {
            case 1 : 
             kelolaSarusun();
             break;
            case 2 : 
            kelolaPemilik();
            break;
            case 3 :
            kelolaPerangkat();
            break;
            case 0 : 
            exit();
            break;
        }
    }

    static void pengelolaMenu() {
        System.out.println("1. Laporan Pemakaian Air Semua Unit");
        System.out.println("2. Kendalikan Perangkat Air");
        System.out.println("0. Logout");
        int ch = scanner.nextInt(); scanner.nextLine();
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
        int ch = scanner.nextInt(); scanner.nextLine();
        switch (ch) {
            case 1 -> laporanPemakaianPemilik();
            case 2 -> kendalikanAirPemilik();
            case 0 -> exit();
        }
    }

    // --------------------- Admin Functions ---------------------
    static void kelolaSarusun() {
        System.out.print("ID Sarusun: ");
        String id = scanner.nextLine();
        System.out.print("Tower: ");
        String tower = scanner.nextLine();
        System.out.print("Lantai: ");
        int lantai = scanner.nextInt(); scanner.nextLine();
        System.out.print("NIK Pemilik: ");
        String nik = scanner.nextLine();
        sarusunList.add(new Sarusun(id, tower, lantai, nik));
        log("Tambah sarusun " + id);
        System.out.println("Sarusun berhasil ditambahkan.");
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
        System.out.println("Pemilik berhasil ditambahkan.");
    }

    static void kelolaPerangkat() {
        System.out.print("SN Perangkat: ");
        String sn = scanner.nextLine();
        System.out.print("ID Sarusun: ");
        String id = scanner.nextLine();
        iotDevices.add(new IoTDevice(sn, id, 0.0));
        log("Tambah perangkat SN " + sn);
        System.out.println("Perangkat berhasil ditambahkan.");
    }

    // --------------------- Shared Functions ---------------------
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
            System.out.println("Anda tidak berhak.");
        }
    }

    static void laporanPemakaian() {
        for (IoTDevice d : iotDevices) {
            System.out.printf("Sarusun %s | SN: %s | Air: %.2fL | Status: %s\n",
                    d.idSarusun, d.sn, d.totalAir, d.status ? "ON" : "OFF");
        }
    }

    static void laporanPemakaianPemilik() {
        for (IoTDevice d : iotDevices) {
            if (isOwnedByCurrentUser(d.idSarusun)) {
                System.out.printf("Unit Anda %s | SN: %s | Air: %.2fL | Status: %s\n",
                        d.idSarusun, d.sn, d.totalAir, d.status ? "ON" : "OFF");
            }
        }
    }

    // --------------------- Utility Functions ---------------------
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
    String username, password, hp, role, ownerNIK;
    User(String username, String password, String hp, String role, String ownerNIK) {
        this.username = username;
        this.password = password;
        this.hp = hp;
        this.role = role;
        this.ownerNIK = ownerNIK;
    }
}
}
