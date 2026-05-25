package dao.impl;

import connectDB.DatabaseConnection;
import dao.HoaDonDAO;
import entity.HoaDon;
import enums.HinhThucThanhToan;
import enums.LoaiDon;
import enums.TrangThaiHoaDon;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HoaDonDAOImpl implements HoaDonDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    private HoaDon mapRow(ResultSet rs) throws SQLException {
        HoaDon hd = new HoaDon(
            rs.getString("maHD"),
            rs.getTimestamp("thoiGianXuat") != null ? rs.getTimestamp("thoiGianXuat").toLocalDateTime() : null,
            rs.getTimestamp("thoiGianThanhToan") != null ? rs.getTimestamp("thoiGianThanhToan").toLocalDateTime() : null,
            rs.getDouble("tongTienPhaiTra"),
            TrangThaiHoaDon.valueOf(rs.getString("trangThai")),
            rs.getString("hinhThucThanhToan") != null ? HinhThucThanhToan.valueOf(rs.getString("hinhThucThanhToan")) : null,
            rs.getString("maBan"),
            rs.getString("maCa"),
            rs.getString("loaiDon") != null ? LoaiDon.valueOf(rs.getString("loaiDon")) : LoaiDon.TAI_BAN,
            rs.getString("ghiChu"),
            rs.getString("maNV")
        );
        try { hd.setSoBan(rs.getString("soBan")); } catch (SQLException ignored) {}
        try { hd.setTenNV(rs.getString("tenNV")); } catch (SQLException ignored) {}
        
        // Cập nhật Phase 2
        try { hd.setTienGiamGia(rs.getDouble("tienGiamGia")); } catch (SQLException ignored) {}
        try { hd.setTienThueVAT(rs.getDouble("tienThueVAT")); } catch (SQLException ignored) {}
        try { hd.setDiemSuDung(rs.getInt("diemSuDung")); } catch (SQLException ignored) {}
        
        try { 
            String sdt = rs.getString("soDienThoai");
            if (sdt != null) {
                entity.KhachHang kh = new entity.KhachHang();
                kh.setSoDienThoai(sdt);
                try { kh.setTenKhachHang(rs.getString("tenKhachHang")); } catch (SQLException ignored) {}
                hd.setKhachHang(kh);
            }
        } catch (SQLException ignored) {}

        try { 
            String makm = rs.getString("maKhuyenMai");
            if (makm != null) {
                entity.KhuyenMai km = new entity.KhuyenMai();
                km.setMaKhuyenMai(makm);
                try { km.setTenKhuyenMai(rs.getString("tenKhuyenMai")); } catch (SQLException ignored) {}
                hd.setKhuyenMai(km);
            }
        } catch (SQLException ignored) {}
        
        return hd;
    }

    @Override
    public boolean insert(HoaDon hd) {
        String sql = "INSERT INTO HoaDon(maHD, thoiGianXuat, thoiGianThanhToan, tongTienPhaiTra, trangThai, hinhThucThanhToan, maBan, maCa, loaiDon, ghiChu, maNV, tienGiamGia, tienThueVAT, soDienThoai, maKhuyenMai, diemSuDung) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, hd.getMaHD());
            ps.setTimestamp(2, hd.getThoiGianXuat() != null ? Timestamp.valueOf(hd.getThoiGianXuat()) : null);
            ps.setTimestamp(3, hd.getThoiGianThanhToan() != null ? Timestamp.valueOf(hd.getThoiGianThanhToan()) : null);
            ps.setDouble(4, hd.getTongTienPhaiTra());
            ps.setString(5, hd.getTrangThai().name());
            ps.setString(6, hd.getHinhThucThanhToan() != null ? hd.getHinhThucThanhToan().name() : null);
            ps.setString(7, hd.getMaBan());
            ps.setString(8, hd.getMaCa());
            ps.setString(9, hd.getLoaiDon() != null ? hd.getLoaiDon().name() : "TAI_BAN");
            ps.setString(10, hd.getGhiChu());
            ps.setString(11, hd.getMaNV());
            ps.setDouble(12, hd.getTienGiamGia());
            ps.setDouble(13, hd.getTienThueVAT());
            ps.setString(14, hd.getKhachHang() != null ? hd.getKhachHang().getSoDienThoai() : null);
            ps.setString(15, hd.getKhuyenMai() != null ? hd.getKhuyenMai().getMaKhuyenMai() : null);
            ps.setInt(16, hd.getDiemSuDung());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("HoaDonDAOImpl.insert: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean update(HoaDon hd) {
        String sql = "UPDATE HoaDon SET thoiGianXuat=?, thoiGianThanhToan=?, tongTienPhaiTra=?, trangThai=?, hinhThucThanhToan=?, maBan=?, maCa=?, loaiDon=?, ghiChu=?, maNV=?, tienGiamGia=?, tienThueVAT=?, soDienThoai=?, maKhuyenMai=?, diemSuDung=? WHERE maHD=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setTimestamp(1, hd.getThoiGianXuat() != null ? Timestamp.valueOf(hd.getThoiGianXuat()) : null);
            ps.setTimestamp(2, hd.getThoiGianThanhToan() != null ? Timestamp.valueOf(hd.getThoiGianThanhToan()) : null);
            ps.setDouble(3, hd.getTongTienPhaiTra());
            ps.setString(4, hd.getTrangThai().name());
            ps.setString(5, hd.getHinhThucThanhToan() != null ? hd.getHinhThucThanhToan().name() : null);
            ps.setString(6, hd.getMaBan());
            ps.setString(7, hd.getMaCa());
            ps.setString(8, hd.getLoaiDon() != null ? hd.getLoaiDon().name() : "TAI_BAN");
            ps.setString(9, hd.getGhiChu());
            ps.setString(10, hd.getMaNV());
            ps.setDouble(11, hd.getTienGiamGia());
            ps.setDouble(12, hd.getTienThueVAT());
            ps.setString(13, hd.getKhachHang() != null ? hd.getKhachHang().getSoDienThoai() : null);
            ps.setString(14, hd.getKhuyenMai() != null ? hd.getKhuyenMai().getMaKhuyenMai() : null);
            ps.setInt(15, hd.getDiemSuDung());
            ps.setString(16, hd.getMaHD());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("HoaDonDAOImpl.update: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean delete(String maHD) {
        // Không cho phép xóa hóa đơn
        System.err.println("HoaDonDAOImpl.delete: Không được phép xóa Hóa Đơn!");
        return false;
    }

    @Override
    public HoaDon findById(String maHD) {
        String sql = "SELECT hd.*, b.soBan, nv.tenNV, kh.tenKhachHang, km.tenKhuyenMai FROM HoaDon hd LEFT JOIN Ban b ON hd.maBan = b.maBan LEFT JOIN NhanVien nv ON hd.maNV = nv.maNV LEFT JOIN KhachHang kh ON hd.soDienThoai = kh.soDienThoai LEFT JOIN KhuyenMai km ON hd.maKhuyenMai = km.maKhuyenMai WHERE hd.maHD=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maHD);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("HoaDonDAOImpl.findById: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<HoaDon> findAll() {
        List<HoaDon> list = new ArrayList<>();
        String sql = "SELECT hd.*, b.soBan, nv.tenNV, kh.tenKhachHang, km.tenKhuyenMai FROM HoaDon hd LEFT JOIN Ban b ON hd.maBan = b.maBan LEFT JOIN NhanVien nv ON hd.maNV = nv.maNV LEFT JOIN KhachHang kh ON hd.soDienThoai = kh.soDienThoai LEFT JOIN KhuyenMai km ON hd.maKhuyenMai = km.maKhuyenMai ORDER BY hd.thoiGianXuat DESC";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("HoaDonDAOImpl.findAll: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<HoaDon> findByCa(String maCa) {
        List<HoaDon> list = new ArrayList<>();
        String sql = "SELECT hd.*, b.soBan, nv.tenNV, kh.tenKhachHang, km.tenKhuyenMai FROM HoaDon hd LEFT JOIN Ban b ON hd.maBan = b.maBan LEFT JOIN NhanVien nv ON hd.maNV = nv.maNV LEFT JOIN KhachHang kh ON hd.soDienThoai = kh.soDienThoai LEFT JOIN KhuyenMai km ON hd.maKhuyenMai = km.maKhuyenMai WHERE hd.maCa=? ORDER BY hd.thoiGianXuat DESC";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maCa);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("HoaDonDAOImpl.findByCa: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<HoaDon> findByNgay(LocalDate ngay) {
        List<HoaDon> list = new ArrayList<>();
        String sql = "SELECT hd.*, b.soBan, nv.tenNV, kh.tenKhachHang, km.tenKhuyenMai FROM HoaDon hd LEFT JOIN Ban b ON hd.maBan = b.maBan LEFT JOIN NhanVien nv ON hd.maNV = nv.maNV LEFT JOIN KhachHang kh ON hd.soDienThoai = kh.soDienThoai LEFT JOIN KhuyenMai km ON hd.maKhuyenMai = km.maKhuyenMai WHERE CAST(hd.thoiGianXuat AS DATE) = ? ORDER BY hd.thoiGianXuat DESC";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(ngay));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("HoaDonDAOImpl.findByNgay: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<HoaDon> findByFilter(LocalDate tuNgay, LocalDate denNgay,
                                     String hinhThuc, String maBan, String maNV) {
        List<HoaDon> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT hd.*, b.soBan, nv.tenNV, kh.tenKhachHang, km.tenKhuyenMai FROM HoaDon hd LEFT JOIN Ban b ON hd.maBan = b.maBan LEFT JOIN NhanVien nv ON hd.maNV = nv.maNV LEFT JOIN KhachHang kh ON hd.soDienThoai = kh.soDienThoai LEFT JOIN KhuyenMai km ON hd.maKhuyenMai = km.maKhuyenMai WHERE 1=1");
        if (tuNgay  != null) sql.append(" AND CAST(hd.thoiGianXuat AS DATE) >= ?");
        if (denNgay != null) sql.append(" AND CAST(hd.thoiGianXuat AS DATE) <= ?");
        if (hinhThuc != null && !hinhThuc.isEmpty()) sql.append(" AND hd.hinhThucThanhToan = ?");
        if (maBan    != null && !maBan.isEmpty())    sql.append(" AND b.soBan LIKE ?");
        if (maNV     != null && !maNV.isEmpty())     sql.append(" AND hd.maNV LIKE ?");
        sql.append(" ORDER BY hd.thoiGianXuat DESC");
        try (PreparedStatement ps = getConn().prepareStatement(sql.toString())) {
            int idx = 1;
            if (tuNgay  != null) ps.setDate(idx++, Date.valueOf(tuNgay));
            if (denNgay != null) ps.setDate(idx++, Date.valueOf(denNgay));
            if (hinhThuc != null && !hinhThuc.isEmpty()) ps.setString(idx++, hinhThuc);
            if (maBan    != null && !maBan.isEmpty())    ps.setString(idx++, "%" + maBan + "%");
            if (maNV     != null && !maNV.isEmpty())     ps.setString(idx++, "%" + maNV + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("HoaDonDAOImpl.findByFilter: " + e.getMessage());
        }
        return list;
    }

    @Override
    public int countByCa(String maCa) {
        String sql = "SELECT COUNT(*) FROM HoaDon WHERE maCa=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maCa);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("HoaDonDAOImpl.countByCa: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public int countHoanThanhByCa(String maCa) {
        String sql = "SELECT COUNT(*) FROM HoaDon WHERE maCa=? AND trangThai='DA_THANH_TOAN'";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maCa);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("HoaDonDAOImpl.countHoanThanhByCa: " + e.getMessage());
        }
        return 0;
    }
}
