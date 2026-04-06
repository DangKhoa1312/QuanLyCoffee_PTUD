package utils;

import connectDB.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Tạo mã tự động theo format: PREFIX + số thứ tự 3 chữ số
 * Ví dụ: NV001, HD001, DH001
 * Đọc max hiện có trong DB rồi tăng lên 1.
 */
public class IDGenerator {

    /**
     * @param prefix     Tiền tố mã, ví dụ "NV", "HD", "DH"
     * @param tableName  Tên bảng SQL, ví dụ "NhanVien"
     * @param idColumn   Tên cột khóa chính, ví dụ "maNV"
     * @param digits     Số chữ số phần số, ví dụ 3 → "001"
     * @return mã mới chưa tồn tại trong DB
     */
    public static String generate(String prefix, String tableName,
                                   String idColumn, int digits) {
        String sql = "SELECT MAX(" + idColumn + ") FROM " + tableName
                   + " WHERE " + idColumn + " LIKE ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, prefix + "%");
            ResultSet rs = ps.executeQuery();

            int nextNum = 1;
            if (rs.next() && rs.getString(1) != null) {
                String maxId    = rs.getString(1);          // e.g. "NV007"
                String numPart  = maxId.substring(prefix.length()); // "007"
                nextNum = Integer.parseInt(numPart) + 1;
            }

            return prefix + String.format("%0" + digits + "d", nextNum);

        } catch (Exception e) {
            System.err.println("IDGenerator error: " + e.getMessage());
            // fallback: timestamp-based
            return prefix + System.currentTimeMillis();
        }
    }

    // ── Shortcut methods cho từng loại mã ─────────────────────────────────

    public static String newMaNhanVien()          { return generate("NV",    "NhanVien",           "maNV",       3); }
    public static String newMaKhuVuc()            { return generate("KV",    "KhuVuc",             "maKhuVuc",   3); }
    public static String newMaCaLamViec()         { return generate("CA",    "CaLamViec",          "maCa",       3); }
    public static String newMaBan()               { return generate("BAN",   "Ban",                "maBan",      3); }
    public static String newMaMon()               { return generate("MON",   "Mon",                "maMon",      3); }
    public static String newMaSize()              { return generate("SZ",    "Size",               "maSize",     3); }
    public static String newMaTopping()           { return generate("TOP",   "Topping",            "maTopping",  3); }
    public static String newMaBangGia()           { return generate("BG",    "BangGia",            "maBangGia",  3); }
    public static String newMaBangGiaChiTiet()    { return generate("BGCT",  "BangGiaChiTiet",     "maBGCT",     3); }
    public static String newMaNguyenLieu()        { return generate("NL",    "NguyenLieu",         "maNL",       3); }
    public static String newMaDinhMuc()           { return generate("DM",    "DinhMucNguyenLieu",  "maDinhMuc",  3); }
    public static String newMaNhaCungCap()        { return generate("NCC",   "NhaCungCap",         "maNCC",      3); }
    public static String newMaKho()               { return generate("KHO",   "Kho",                "maKho",      3); }
    public static String newMaPhieuNhap()         { return generate("PN",    "PhieuNhap",          "maPN",       3); }
    public static String newMaChiTietPhieuNhap()  { return generate("CTPN",  "ChiTietPhieuNhap",   "maCTPN",     3); }
    public static String newMaTonKho()            { return generate("TK",    "TonKho",             "maTonKho",   3); }
    public static String newMaDatBan()            { return generate("DB",    "DatBan",             "maDatBan",   3); }
    public static String newMaHoaDon()            { return generate("HD",    "HoaDon",             "maHD",       3); }
    public static String newMaChiTietHoaDon()     { return generate("CTHD",  "ChiTietHoaDon",      "maCTHD",     3); }
    public static String newMaCTHDTopping()       { return generate("CTHDT", "ChiTietHoaDonTopping", "maID",      3); }
}
