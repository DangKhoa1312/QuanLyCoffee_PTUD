package dao.impl;

import connectDB.DatabaseConnection;
import dao.DatBanDAO;
import entity.Ban;
import entity.DatBan;
import enums.TrangThaiBan;
import enums.TrangThaiDatBan;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DatBanDAOImpl implements DatBanDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    // ══ Map ResultSet → DatBan ════════════════════════════════════════════════
    private DatBan mapRow(ResultSet rs) throws SQLException {
        boolean hienThi = true;
        try { hienThi = rs.getBoolean("hienThi"); } catch (SQLException ignored) {}
        DatBan db = new DatBan(
                rs.getString("maDatBan"),
                rs.getString("tenKhach"),
                rs.getString("soDienThoai"),
                rs.getInt("soLuongNguoi"),
                null,
                rs.getTimestamp("thoiGianDen") != null ? rs.getTimestamp("thoiGianDen").toLocalDateTime() : null,
                rs.getTimestamp("thoiGianDat") != null ? rs.getTimestamp("thoiGianDat").toLocalDateTime() : null,
                new ArrayList<>(),
                rs.getString("maHD"),
                hienThi);
        
        String tt = rs.getString("trangThai");
        try { db.setTrangThai(TrangThaiDatBan.valueOf(tt)); }
        catch (Exception e) { db.setTrangThai(TrangThaiDatBan.CHO_XAC_NHAN); }
        
        return db;
    }

    private void loadChiTiet(DatBan db) {
        String sql = "SELECT ct.maBan, b.soBan FROM ChiTietDatBan ct JOIN Ban b ON ct.maBan = b.maBan WHERE ct.maDatBan=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, db.getMaDatBan());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                db.getDsMaBan().add(rs.getString("maBan"));
                String cur = db.getDanhSachSoBan();
                String soBan = rs.getString("soBan");
                db.setDanhSachSoBan(cur == null ? soBan : cur + ", " + soBan);
            }
        } catch (SQLException e) {}
    }

    private void loadChiTietList(List<DatBan> list) {
        for (DatBan db : list) loadChiTiet(db);
    }

    // ══ BaseDAO: insert ═══════════════════════════════════════════════════════
    @Override
    public boolean insert(DatBan db) {
        String sql = "INSERT INTO DatBan(maDatBan,tenKhach,soDienThoai,soLuongNguoi," +
                     "trangThai,thoiGianDen,thoiGianDat,maHD,hienThi) " +
                     "VALUES(?,?,?,?,?,?,?,?,?)";
        try {
            getConn().setAutoCommit(false);
            try (PreparedStatement ps = getConn().prepareStatement(sql)) {
                ps.setString(1, db.getMaDatBan());
                ps.setString(2, db.getTenKhach());
                ps.setString(3, db.getSoDienThoai());
                ps.setInt   (4, db.getSoLuongNguoi());
                ps.setString(5, db.getTrangThai().name());
                ps.setTimestamp(6, db.getThoiGianDen() != null ? Timestamp.valueOf(db.getThoiGianDen()) : null);
                ps.setTimestamp(7, db.getThoiGianDat() != null ? Timestamp.valueOf(db.getThoiGianDat()) : null);
                ps.setString(8, db.getMaHD());
                ps.setBoolean(9, db.isHienThi());
                ps.executeUpdate();
            }
            
            if (db.getDsMaBan() != null) {
                String sqlCt = "INSERT INTO ChiTietDatBan(maDatBan, maBan) VALUES(?,?)";
                try (PreparedStatement psCt = getConn().prepareStatement(sqlCt)) {
                    for (String mb : db.getDsMaBan()) {
                        psCt.setString(1, db.getMaDatBan());
                        psCt.setString(2, mb);
                        psCt.addBatch();
                    }
                    psCt.executeBatch();
                }
            }
            getConn().commit();
            getConn().setAutoCommit(true);
            return true;
        } catch (SQLException e) {
            System.err.println("DatBanDAOImpl.insert: " + e.getMessage());
            try { getConn().rollback(); getConn().setAutoCommit(true); } catch (SQLException ex) {}
        }
        return false;
    }

    // ══ BaseDAO: update ═══════════════════════════════════════════════════════
    @Override
    public boolean update(DatBan db) {
        String sql = "UPDATE DatBan SET tenKhach=?,soDienThoai=?,soLuongNguoi=?," +
                     "trangThai=?,thoiGianDen=?,thoiGianDat=?,maHD=?,hienThi=? " +
                     "WHERE maDatBan=?";
        try {
            getConn().setAutoCommit(false);
            try (PreparedStatement ps = getConn().prepareStatement(sql)) {
                ps.setString(1, db.getTenKhach());
                ps.setString(2, db.getSoDienThoai());
                ps.setInt   (3, db.getSoLuongNguoi());
                ps.setString(4, db.getTrangThai().name());
                ps.setTimestamp(5, db.getThoiGianDen() != null ? Timestamp.valueOf(db.getThoiGianDen()) : null);
                ps.setTimestamp(6, db.getThoiGianDat() != null ? Timestamp.valueOf(db.getThoiGianDat()) : null);
                ps.setString(7, db.getMaHD());
                ps.setBoolean(8, db.isHienThi());
                ps.setString(9, db.getMaDatBan());
                ps.executeUpdate();
            }
            
            try (PreparedStatement psDel = getConn().prepareStatement("DELETE FROM ChiTietDatBan WHERE maDatBan=?")) {
                psDel.setString(1, db.getMaDatBan());
                psDel.executeUpdate();
            }

            if (db.getDsMaBan() != null) {
                String sqlCt = "INSERT INTO ChiTietDatBan(maDatBan, maBan) VALUES(?,?)";
                try (PreparedStatement psCt = getConn().prepareStatement(sqlCt)) {
                    for (String mb : db.getDsMaBan()) {
                        psCt.setString(1, db.getMaDatBan());
                        psCt.setString(2, mb);
                        psCt.addBatch();
                    }
                    psCt.executeBatch();
                }
            }
            
            getConn().commit();
            getConn().setAutoCommit(true);
            return true;
        } catch (SQLException e) {
            System.err.println("DatBanDAOImpl.update: " + e.getMessage());
            try { getConn().rollback(); getConn().setAutoCommit(true); } catch (SQLException ex) {}
        }
        return false;
    }

    // ══ BaseDAO: delete (xoá thật, chỉ dùng nội bộ) ═════════════════════════
    @Override
    public boolean delete(String maDatBan) {
        String sql = "DELETE FROM DatBan WHERE maDatBan=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maDatBan);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("DatBanDAOImpl.delete: " + e.getMessage());
        }
        return false;
    }

    // ══ BaseDAO: findById ════════════════════════════════════════════════════
    @Override
    public DatBan findById(String maDatBan) {
        String sql = "SELECT * FROM DatBan WHERE maDatBan=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maDatBan);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                DatBan db = mapRow(rs);
                loadChiTiet(db);
                return db;
            }
        } catch (SQLException e) {
            System.err.println("DatBanDAOImpl.findById: " + e.getMessage());
        }
        return null;
    }

    // ══ BaseDAO: findAll (tất cả, kể cả ẩn) ═════════════════════════════════
    @Override
    public List<DatBan> findAll() {
        List<DatBan> list = new ArrayList<>();
        String sql = "SELECT * FROM DatBan ORDER BY thoiGianDen DESC";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("DatBanDAOImpl.findAll: " + e.getMessage());
        }
        loadChiTietList(list);
        return list;
    }

    // ══ findVisible: chỉ lấy hienThi=1, sắp theo giờ đến ASC ════════════════
    @Override
    public List<DatBan> findVisible() {
        List<DatBan> list = new ArrayList<>();
        String sql = "SELECT * FROM DatBan WHERE hienThi=1 ORDER BY maDatBan ASC";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("DatBanDAOImpl.findVisible: " + e.getMessage());
        }
        loadChiTietList(list);
        return list;
    }

    // ══ findHidden: chỉ lấy hienThi=0 ═══════════════════════════════════════
    @Override
    public List<DatBan> findHidden() {
        List<DatBan> list = new ArrayList<>();
        String sql = "SELECT * FROM DatBan WHERE hienThi=0 ORDER BY maDatBan ASC";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("DatBanDAOImpl.findHidden: " + e.getMessage());
        }
        loadChiTietList(list);
        return list;
    }

    // ══ findByBan: tất cả đặt bàn của 1 bàn (kể cả ẩn) ═════════════════════
    @Override
    public List<DatBan> findByBan(String maBan) {
        List<DatBan> list = new ArrayList<>();
        String sql = "SELECT d.* FROM DatBan d JOIN ChiTietDatBan ct ON d.maDatBan = ct.maDatBan WHERE ct.maBan=? AND d.hienThi=1 ORDER BY d.thoiGianDen DESC";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maBan);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("DatBanDAOImpl.findByBan: " + e.getMessage());
        }
        loadChiTietList(list);
        return list;
    }

    // ══ findConHieuLuc: CHO_XAC_NHAN + DA_XAC_NHAN ══════════════════════════
    @Override
    public List<DatBan> findConHieuLuc() {
        List<DatBan> list = new ArrayList<>();
        String sql = "SELECT * FROM DatBan " +
                     "WHERE trangThai IN ('CHO_XAC_NHAN','DA_XAC_NHAN','DA_THANH_TOAN') AND hienThi=1 " +
                     "ORDER BY thoiGianDen ASC";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("DatBanDAOImpl.findConHieuLuc: " + e.getMessage());
        }
        loadChiTietList(list);
        return list;
    }

    // ══ Soft-delete: hide ════════════════════════════════════════════════════
    @Override
    public boolean hide(String maDatBan) {
        String sql = "UPDATE DatBan SET hienThi=0 WHERE maDatBan=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maDatBan);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("DatBanDAOImpl.hide: " + e.getMessage());
        }
        return false;
    }

    // ══ Soft-delete: show ════════════════════════════════════════════════════
    @Override
    public boolean show(String maDatBan) {
        String sql = "UPDATE DatBan SET hienThi=1 WHERE maDatBan=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maDatBan);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("DatBanDAOImpl.show: " + e.getMessage());
        }
        return false;
    }

    // ══ updateTrangThai ═══════════════════════════════════════════════════════
    @Override
    public boolean updateTrangThai(String maDatBan, TrangThaiDatBan tt) {
        String sql = "UPDATE DatBan SET trangThai=? WHERE maDatBan=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, tt.name());
            ps.setString(2, maDatBan);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("DatBanDAOImpl.updateTrangThai: " + e.getMessage());
        }
        return false;
    }

    // ══ updateMaHD: gán hoá đơn sau khi thanh toán ═══════════════════════════
    @Override
    public boolean updateMaHD(String maDatBan, String maHD) {
        String sql = "UPDATE DatBan SET maHD=?, trangThai='DA_THANH_TOAN' WHERE maDatBan=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maHD);
            ps.setString(2, maDatBan);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("DatBanDAOImpl.updateMaHD: " + e.getMessage());
        }
        return false;
    }

    // ══ isTrungGio: kiểm tra bàn có bị trùng giờ ±60 phút không ════════════
    @Override
    public boolean isTrungGio(String maBan, LocalDateTime thoiGianDen, String excludeMaDatBan) {
        int khoangCach = utils.AppConfig.getInstance().getInt("KHOANG_CACH_DAT_BAN", 60);
        String sql = "SELECT COUNT(*) FROM DatBan d JOIN ChiTietDatBan ct ON d.maDatBan = ct.maDatBan " +
                     "WHERE ct.maBan=? " +
                     "  AND d.maDatBan<>? " +
                     "  AND d.trangThai IN ('CHO_XAC_NHAN','DA_XAC_NHAN') " +
                     "  AND ABS(DATEDIFF(MINUTE, d.thoiGianDen, ?)) < " + khoangCach;
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maBan);
            ps.setString(2, excludeMaDatBan != null ? excludeMaDatBan : "");
            ps.setTimestamp(3, Timestamp.valueOf(thoiGianDen));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("DatBanDAOImpl.isTrungGio: " + e.getMessage());
        }
        return false;
    }

    // ══ findBanTrongByKhuVuc: tất cả bàn không TAM_NGUNG + không trùng giờ ═══
    // (cho phép đặt nhiều slot trong cùng bàn, kể cả bàn đang CO_KHACH/DA_DAT_TRUOC)
    @Override
    public List<Ban> findBanTrongByKhuVuc(String maKhuVuc, LocalDateTime thoiGianDen, String excludeMaDatBan) {
        List<Ban> list = new ArrayList<>();
        // Lấy tất cả bàn không bị tạm ngưng trong khu vực
        String sql = "SELECT b.* FROM Ban b " +
                     "WHERE b.maKhuVuc=? AND b.trangThai <> 'TAM_NGUNG' " +
                     "ORDER BY b.soBan";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maKhuVuc);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Ban ban = new Ban(
                    rs.getString("maBan"),
                    rs.getString("soBan"),
                    rs.getString("maKhuVuc"),
                    rs.getInt("sucChua"),
                    TrangThaiBan.valueOf(rs.getString("trangThai"))
                );
                // Kiểm tra trùng giờ cho từng bàn (cách nhau < 60 phút là trùng)
                if (thoiGianDen == null || !isTrungGio(ban.getMaBan(), thoiGianDen, excludeMaDatBan)) {
                    list.add(ban);
                }
            }
        } catch (SQLException e) {
            System.err.println("DatBanDAOImpl.findBanTrongByKhuVuc: " + e.getMessage());
        }
        return list;
    }

    // ══ generateNextMaDatBan: sinh mã tự động DB_001, DB_002... ══════════════
    @Override
    public String generateNextMaDatBan() {
        String sql = "SELECT MAX(CAST(SUBSTRING(maDatBan, 4, LEN(maDatBan)) AS INT)) " +
                     "FROM DatBan WHERE maDatBan LIKE 'DB_%'";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int max = rs.getInt(1);
                return String.format("DB_%03d", max + 1);
            }
        } catch (SQLException e) {
            System.err.println("DatBanDAOImpl.generateNextMaDatBan: " + e.getMessage());
        }
        return "DB_001";
    }
}
