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
                rs.getString("maBan"),
                rs.getString("maHD"),
                hienThi);
        
        String tt = rs.getString("trangThai");
        try { db.setTrangThai(TrangThaiDatBan.valueOf(tt)); }
        catch (Exception e) { db.setTrangThai(TrangThaiDatBan.CHO_XAC_NHAN); }
        
        // Gán soBan nếu có trong ResultSet (kết quả của JOIN)
        try {
            db.setSoBan(rs.getString("soBan"));
        } catch (SQLException ignored) {}
        
        return db;
    }

    // ══ BaseDAO: insert ═══════════════════════════════════════════════════════
    @Override
    public boolean insert(DatBan db) {
        String sql = "INSERT INTO DatBan(maDatBan,tenKhach,soDienThoai,soLuongNguoi," +
                     "trangThai,thoiGianDen,thoiGianDat,maBan,maHD,hienThi) " +
                     "VALUES(?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, db.getMaDatBan());
            ps.setString(2, db.getTenKhach());
            ps.setString(3, db.getSoDienThoai());
            ps.setInt   (4, db.getSoLuongNguoi());
            ps.setString(5, db.getTrangThai().name());
            ps.setTimestamp(6, db.getThoiGianDen() != null ? Timestamp.valueOf(db.getThoiGianDen()) : null);
            ps.setTimestamp(7, db.getThoiGianDat() != null ? Timestamp.valueOf(db.getThoiGianDat()) : null);
            ps.setString(8, db.getMaBan());
            ps.setString(9, db.getMaHD());
            ps.setBoolean(10, db.isHienThi());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("DatBanDAOImpl.insert: " + e.getMessage());
        }
        return false;
    }

    // ══ BaseDAO: update ═══════════════════════════════════════════════════════
    @Override
    public boolean update(DatBan db) {
        String sql = "UPDATE DatBan SET tenKhach=?,soDienThoai=?,soLuongNguoi=?," +
                     "trangThai=?,thoiGianDen=?,thoiGianDat=?,maBan=?,maHD=?,hienThi=? " +
                     "WHERE maDatBan=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, db.getTenKhach());
            ps.setString(2, db.getSoDienThoai());
            ps.setInt   (3, db.getSoLuongNguoi());
            ps.setString(4, db.getTrangThai().name());
            ps.setTimestamp(5, db.getThoiGianDen() != null ? Timestamp.valueOf(db.getThoiGianDen()) : null);
            ps.setTimestamp(6, db.getThoiGianDat() != null ? Timestamp.valueOf(db.getThoiGianDat()) : null);
            ps.setString(7, db.getMaBan());
            ps.setString(8, db.getMaHD());
            ps.setBoolean(9, db.isHienThi());
            ps.setString(10, db.getMaDatBan());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("DatBanDAOImpl.update: " + e.getMessage());
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
        String sql = "SELECT db.*, b.soBan FROM DatBan db LEFT JOIN Ban b ON db.maBan = b.maBan WHERE db.maDatBan=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maDatBan);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("DatBanDAOImpl.findById: " + e.getMessage());
        }
        return null;
    }

    // ══ BaseDAO: findAll (tất cả, kể cả ẩn) ═════════════════════════════════
    @Override
    public List<DatBan> findAll() {
        List<DatBan> list = new ArrayList<>();
        String sql = "SELECT db.*, b.soBan FROM DatBan db LEFT JOIN Ban b ON db.maBan = b.maBan ORDER BY db.thoiGianDen DESC";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("DatBanDAOImpl.findAll: " + e.getMessage());
        }
        return list;
    }

    // ══ findVisible: chỉ lấy hienThi=1, sắp theo giờ đến ASC ════════════════
    @Override
    public List<DatBan> findVisible() {
        List<DatBan> list = new ArrayList<>();
        String sql = "SELECT db.*, b.soBan FROM DatBan db LEFT JOIN Ban b ON db.maBan = b.maBan WHERE db.hienThi=1 ORDER BY db.maDatBan ASC";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("DatBanDAOImpl.findVisible: " + e.getMessage());
        }
        return list;
    }

    // ══ findHidden: chỉ lấy hienThi=0 ═══════════════════════════════════════
    @Override
    public List<DatBan> findHidden() {
        List<DatBan> list = new ArrayList<>();
        String sql = "SELECT db.*, b.soBan FROM DatBan db LEFT JOIN Ban b ON db.maBan = b.maBan WHERE db.hienThi=0 ORDER BY db.maDatBan ASC";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("DatBanDAOImpl.findHidden: " + e.getMessage());
        }
        return list;
    }

    // ══ findByBan: tất cả đặt bàn của 1 bàn (kể cả ẩn) ═════════════════════
    @Override
    public List<DatBan> findByBan(String maBan) {
        List<DatBan> list = new ArrayList<>();
        String sql = "SELECT db.*, b.soBan FROM DatBan db LEFT JOIN Ban b ON db.maBan = b.maBan WHERE db.maBan=? AND db.hienThi=1 ORDER BY db.thoiGianDen DESC";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maBan);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("DatBanDAOImpl.findByBan: " + e.getMessage());
        }
        return list;
    }

    // ══ findConHieuLuc: CHO_XAC_NHAN + DA_XAC_NHAN ══════════════════════════
    @Override
    public List<DatBan> findConHieuLuc() {
        List<DatBan> list = new ArrayList<>();
        String sql = "SELECT db.*, b.soBan FROM DatBan db LEFT JOIN Ban b ON db.maBan = b.maBan " +
                     "WHERE db.trangThai IN ('CHO_XAC_NHAN','DA_XAC_NHAN','DA_THANH_TOAN') AND db.hienThi=1 " +
                     "ORDER BY db.thoiGianDen ASC";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("DatBanDAOImpl.findConHieuLuc: " + e.getMessage());
        }
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
        String sql = "SELECT COUNT(*) FROM DatBan " +
                     "WHERE maBan=? " +
                     "  AND maDatBan<>? " +
                     "  AND trangThai IN ('CHO_XAC_NHAN','DA_XAC_NHAN') " +
                     "  AND ABS(DATEDIFF(MINUTE, thoiGianDen, ?)) < 60";
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
