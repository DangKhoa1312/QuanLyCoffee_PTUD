package dao.impl;

import connectDB.DatabaseConnection;
import dao.BangGiaDAO;
import entity.BangGia;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BangGiaDAOImpl implements BangGiaDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    private BangGia mapRow(ResultSet rs) throws SQLException {
        return new BangGia(
            rs.getString("maBangGia"),
            rs.getString("tenBangGia"),
            rs.getDate("ngayBatDau") != null ? rs.getDate("ngayBatDau").toLocalDate() : null,
            rs.getDate("ngayKetThuc") != null ? rs.getDate("ngayKetThuc").toLocalDate() : null,
            rs.getBoolean("trangThai"),
            rs.getBoolean("hoatDong")
        );
    }

    @Override
    public boolean insert(BangGia bangGia) {
        String sql = "INSERT INTO BangGia(maBangGia, tenBangGia, ngayBatDau, ngayKetThuc, trangThai, hoatDong) VALUES(?,?,?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, bangGia.getMaBangGia());
            ps.setString(2, bangGia.getTenBangGia());
            ps.setDate(3, bangGia.getNgayBatDau() != null ? Date.valueOf(bangGia.getNgayBatDau()) : null);
            ps.setDate(4, bangGia.getNgayKetThuc() != null ? Date.valueOf(bangGia.getNgayKetThuc()) : null);
            ps.setBoolean(5, bangGia.isTrangThai());
            ps.setBoolean(6, bangGia.isHoatDong());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("BangGiaDAOImpl.insert: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean update(BangGia bangGia) {
        String sql = "UPDATE BangGia SET tenBangGia=?, ngayBatDau=?, ngayKetThuc=?, trangThai=?, hoatDong=? WHERE maBangGia=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, bangGia.getTenBangGia());
            ps.setDate(2, bangGia.getNgayBatDau() != null ? Date.valueOf(bangGia.getNgayBatDau()) : null);
            ps.setDate(3, bangGia.getNgayKetThuc() != null ? Date.valueOf(bangGia.getNgayKetThuc()) : null);
            ps.setBoolean(4, bangGia.isTrangThai());
            ps.setBoolean(5, bangGia.isHoatDong());
            ps.setString(6, bangGia.getMaBangGia());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("BangGiaDAOImpl.update: " + e.getMessage());
        }
        return false;
    }

    /**
     * Soft Delete: đặt hoatDong=0 thay vì xóa thật.
     * Bảo toàn toàn bộ lịch sử giá.
     */
    @Override
    public boolean delete(String maBangGia) {
        String sql = "UPDATE BangGia SET hoatDong=0 WHERE maBangGia=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maBangGia);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("BangGiaDAOImpl.delete: " + e.getMessage());
        }
        return false;
    }

    @Override
    public BangGia findById(String maBangGia) {
        String sql = "SELECT * FROM BangGia WHERE maBangGia=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maBangGia);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("BangGiaDAOImpl.findById: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<BangGia> findAll() {
        List<BangGia> list = new ArrayList<>();
        String sql = "SELECT * FROM BangGia WHERE hoatDong=1 ORDER BY ngayBatDau DESC";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("BangGiaDAOImpl.findAll: " + e.getMessage());
        }
        return list;
    }

    /**
     * Tìm bảng giá hiện hành: hoatDong=1, trangThai=1, và ngày nằm trong [ngayBatDau, ngayKetThuc].
     * Ưu tiên bảng giá có ngayBatDau mới nhất.
     */
    @Override
    public BangGia findHienHanh(LocalDate ngay) {
        Date d = Date.valueOf(ngay);
        String sql = "SELECT TOP 1 * FROM BangGia WHERE hoatDong=1 AND trangThai=1 AND ngayBatDau <= ? AND (ngayKetThuc IS NULL OR ngayKetThuc >= ?) ORDER BY ngayBatDau DESC";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setDate(1, d);
            ps.setDate(2, d);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("BangGiaDAOImpl.findHienHanh: " + e.getMessage());
            // Fallback cho DB không hỗ trợ TOP 1
            if (e.getMessage() != null && e.getMessage().contains("TOP")) {
                sql = "SELECT * FROM BangGia WHERE hoatDong=1 AND trangThai=1 AND ngayBatDau <= ? AND (ngayKetThuc IS NULL OR ngayKetThuc >= ?) ORDER BY ngayBatDau DESC";
                try (PreparedStatement psFb = getConn().prepareStatement(sql)) {
                    psFb.setDate(1, d);
                    psFb.setDate(2, d);
                    ResultSet rsFb = psFb.executeQuery();
                    if (rsFb.next()) return mapRow(rsFb);
                } catch (SQLException ex) {
                    System.err.println("BangGiaDAOImpl.findHienHanh fallback: " + ex.getMessage());
                }
            }
        }
        return null;
    }

    /**
     * Tìm tất cả bảng giá hiện hành (overlap ngày với ngay).
     */
    @Override
    public List<BangGia> findTatCaHienHanh(LocalDate ngay) {
        List<BangGia> list = new ArrayList<>();
        Date d = Date.valueOf(ngay);
        String sql = "SELECT * FROM BangGia WHERE hoatDong=1 AND trangThai=1 AND ngayBatDau <= ? AND (ngayKetThuc IS NULL OR ngayKetThuc >= ?) ORDER BY ngayBatDau DESC";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setDate(1, d);
            ps.setDate(2, d);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("BangGiaDAOImpl.findTatCaHienHanh: " + e.getMessage());
        }
        return list;
    }
}
