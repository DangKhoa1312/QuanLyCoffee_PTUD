import entity.Mon;
import entity.Size;
import enums.LoaiMon;
import controller.MenuController;
import dao.impl.MonDAOImpl;
import dao.impl.SizeDAOImpl;
import connectDB.DatabaseConnection;

import java.util.ArrayList;
import java.util.List;

public class TestMenuModule {
    static int pass = 0, fail = 0;
    static MenuController ctrl = new MenuController();
    static MonDAOImpl monDAO = new MonDAOImpl();
    static SizeDAOImpl sizeDAO = new SizeDAOImpl();

    public static void main(String[] args) {
        System.out.println("=== TEST SUITE: Module Thuc Don (Menu) ===\n");

        tc01_SizeEntityDefaultTrangThai();
        tc02_InsertMon();
        tc03_MonDAOfindByLoai();
        tc04_MonDAOfindDangBan();
        tc05_SizeDAOInsert();
        tc06_SizeDAOUpdate();
        tc07_SizeDAOSoftDelete();
        tc08_SizeDAOFindByMon();
        tc09_SizeDAOFindAllByMon();
        tc10_SizeDAOIsUsedInHoaDon();

        tc11_GenerateNextMaMon();
        tc12_SaveMonAndSizes_Insert();
        tc13_SaveMonAndSizes_UpdateSize();
        tc14_SaveMonAndSizes_SoftDeleteSize();
        tc15_IsTenMonDuplicate_TruongHopTrung();
        tc16_IsTenMonDuplicate_TruongHopKhacCase();
        tc17_IsTenMonDuplicate_IgnoreSelf();
        tc18_IsSizeUsedInBangGia_False();
        tc19_CheckTonKhoMoiMon();
        tc20_TeardownAndCleanUp();

        System.out.println("\n=== KET QUA: " + pass + " PASS | " + fail + " FAIL ===");
    }

    private static void assert_(String name, boolean condition) {
        if (condition) {
            System.out.println("[PASS] " + name);
            pass++;
        } else {
            System.out.println("[FAIL] " + name);
            fail++;
        }
    }

    private static final String FAKE_MON = "MON_TEST_99";
    private static final String FAKE_SIZE = "SZ_TEST_99";
    private static final String FAKE_MON_2 = "MON_TEST_88";

    static void tc01_SizeEntityDefaultTrangThai() {
        Size s = new Size("S1", "Test", "M1");
        assert_("TC01 - SizeEntityDefaultTrangThai: default = true", s.isTrangThai());
    }

    static void tc02_InsertMon() {
        Mon m = new Mon(FAKE_MON, "Mon Test Insert", "Mo ta", "", LoaiMon.COFFEE, true);
        boolean ok = monDAO.insert(m);
        assert_("TC02 - InsertMon vao DB thanh cong", ok);
    }

    static void tc03_MonDAOfindByLoai() {
        // Mon vua tao co LoaiMon.COFFEE va trangThai=true
        List<Mon> ds = monDAO.findByLoai(LoaiMon.COFFEE);
        boolean found = ds.stream().anyMatch(m -> m.getMaMon().equals(FAKE_MON) && m.isTrangThai());
        assert_("TC03 - MonDAOfindByLoai chi tra ve trangThai=1", found);
    }

    static void tc04_MonDAOfindDangBan() {
        List<Mon> ds = monDAO.findDangBan();
        boolean found = ds.stream().anyMatch(m -> m.getMaMon().equals(FAKE_MON));
        assert_("TC04 - MonDAOfindDangBan tim thay mon vua tao", found);
    }

    static void tc05_SizeDAOInsert() {
        Size s = new Size(FAKE_SIZE, "Size Test", FAKE_MON, true);
        boolean ok = sizeDAO.insert(s);
        assert_("TC05 - SizeDAOInsert thanh cong", ok);
    }

    static void tc06_SizeDAOUpdate() {
        Size s = new Size(FAKE_SIZE, "Size Changed", FAKE_MON, true);
        boolean ok = sizeDAO.update(s);
        Size s2 = sizeDAO.findById(FAKE_SIZE);
        assert_("TC06 - SizeDAOUpdate thanh cong", ok && s2 != null && "Size Changed".equals(s2.getTenSize()));
    }

    static void tc07_SizeDAOSoftDelete() {
        // Soft delete bằng update
        Size s = sizeDAO.findById(FAKE_SIZE);
        if (s != null) {
            s.setTrangThai(false);
            sizeDAO.update(s);
        }
        Size check = sizeDAO.findById(FAKE_SIZE);
        assert_("TC07 - SizeDAOSoftDelete: Record khong bi xoa khoi DB, nhung trangThai=false", check != null && !check.isTrangThai());
    }

    static void tc08_SizeDAOFindByMon() {
        List<Size> activeSizes = sizeDAO.findByMon(FAKE_MON);
        boolean found = activeSizes.stream().anyMatch(s -> s.getMaSize().equals(FAKE_SIZE));
        assert_("TC08 - SizeDAOFindByMon: KHONG tra ve size bi an", !found);
    }

    static void tc09_SizeDAOFindAllByMon() {
        List<Size> allSizes = sizeDAO.findAllByMon(FAKE_MON);
        boolean found = allSizes.stream().anyMatch(s -> s.getMaSize().equals(FAKE_SIZE));
        assert_("TC09 - SizeDAOFindAllByMon: CO tra ve size bi an", found);
    }

    static void tc10_SizeDAOIsUsedInHoaDon() {
        boolean used = sizeDAO.isSizeUsedInHoaDon(FAKE_SIZE);
        assert_("TC10 - SizeDAOIsUsedInHoaDon: Khong the ton tai (tra ve false)", !used);
    }

    static void tc11_GenerateNextMaMon() {
        String next = ctrl.generateNextMaMon();
        assert_("TC11 - GenerateNextMaMon hoat dong (" + next + ")", next != null && next.startsWith("MON"));
    }

    static void tc12_SaveMonAndSizes_Insert() {
        Mon m = new Mon(FAKE_MON_2, "Test Multi", "", "", LoaiMon.TRA_SUA, true);
        List<Size> sizes = new ArrayList<>();
        sizes.add(new Size("SZ_T_1", "Nhỏ", FAKE_MON_2, true));
        sizes.add(new Size("SZ_T_2", "Lớn", FAKE_MON_2, true));
        boolean ok = ctrl.saveMonAndSizes(m, sizes, false);
        assert_("TC12 - SaveMonAndSizes tao mon kèm 2 size", ok && sizeDAO.findByMon(FAKE_MON_2).size() == 2);
    }

    static void tc13_SaveMonAndSizes_UpdateSize() {
        Mon m = monDAO.findById(FAKE_MON_2);
        List<Size> sizes = sizeDAO.findByMon(FAKE_MON_2);
        for (Size s : sizes) {
            if (s.getMaSize().equals("SZ_T_1")) {
                s.setTenSize("Siêu Nhỏ");
            }
        }
        boolean ok = ctrl.saveMonAndSizes(m, sizes, true);
        Size check = sizeDAO.findById("SZ_T_1");
        assert_("TC13 - SaveMonAndSizes update ten size", ok && check != null && "Siêu Nhỏ".equals(check.getTenSize()));
    }

    static void tc14_SaveMonAndSizes_SoftDeleteSize() {
        Mon m = monDAO.findById(FAKE_MON_2);
        List<Size> sizes = sizeDAO.findByMon(FAKE_MON_2);
        sizes.removeIf(s -> s.getMaSize().equals("SZ_T_2")); // User UI xoa 1 dong
        
        boolean ok = ctrl.saveMonAndSizes(m, sizes, true);
        Size szRemoved = sizeDAO.findById("SZ_T_2"); // van the lay ngam
        assert_("TC14 - SaveMonAndSizes tự chuyển thành SoftDelete khi mất size", ok && !szRemoved.isTrangThai());
    }

    static void tc15_IsTenMonDuplicate_TruongHopTrung() {
        boolean dup = ctrl.isTenMonDuplicate("Test Multi", null);
        assert_("TC15 - IsTenMonDuplicate phát hiện trung chính xác", dup);
    }

    static void tc16_IsTenMonDuplicate_TruongHopKhacCase() {
        boolean dup = ctrl.isTenMonDuplicate("test MULTI", null);
        assert_("TC16 - IsTenMonDuplicate phat hien trung ignore case", dup);
    }

    static void tc17_IsTenMonDuplicate_IgnoreSelf() {
        // Đang sửa chính nó thì không được báo trùng
        boolean dup = ctrl.isTenMonDuplicate("Test Multi", FAKE_MON_2);
        assert_("TC17 - IsTenMonDuplicate bỏ qua chính nó (ignore self)", !dup);
    }

    static void tc18_IsSizeUsedInBangGia_False() {
        boolean used = ctrl.isSizeUsedInBangGia("SZ_T_1");
        assert_("TC18 - IsSizeUsedInBangGia khong co gia tri ao", !used);
    }

    static void tc19_CheckTonKhoMoiMon() {
        try {
            boolean hetHang = ctrl.isHetHang(FAKE_MON_2);
            assert_("TC19 - CheckTonKho khong crash voi mon ao", true);
        } catch (Exception e) {
            assert_("TC19 - CheckTonKho", false);
        }
    }

    static void tc20_TeardownAndCleanUp() {
        // Clean Size
        sizeDAO.delete(FAKE_SIZE);
        sizeDAO.delete("SZ_T_1");
        sizeDAO.delete("SZ_T_2");
        // Clean Mon
        monDAO.delete(FAKE_MON);
        boolean cleanMon2 = monDAO.delete(FAKE_MON_2);
        
        Size finalCheck = sizeDAO.findById(FAKE_SIZE);
        Mon monCheck = monDAO.findById(FAKE_MON_2);
        assert_("TC20 - Teardown dọn dẹp sạch test data DB (" + (monCheck==null) + ")", monCheck == null && finalCheck == null);
    }
}
