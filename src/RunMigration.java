import controller.KhoController;

public class RunMigration {
    public static void main(String[] args) {
        System.out.println("Bắt đầu chạy Migration chuyển đổi TonKho sang Base Units...");
        KhoController khoController = new KhoController();
        boolean success = khoController.runMigrationToBaseUnits();
        if (success) {
            System.out.println("✅ Migration THÀNH CÔNG! Dữ liệu đã được chuyển sang Base Units.");
        } else {
            System.out.println("❌ Migration THẤT BẠI!");
        }
    }
}
