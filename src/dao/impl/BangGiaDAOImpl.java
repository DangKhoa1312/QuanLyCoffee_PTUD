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
            rs.getBoolean("trangThai")
        );
    }

    @Override
    public boolean insert(BangGia bangGia) {
        String sql = "INSERT INTO BangGia(maBangGia, tenBangGia, ngayBatDau, ngayKetThuc, trangThai) VALUES(?,?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, bangGia.getMaBangGia());
            ps.setString(2, bangGia.getTenBangGia());
            ps.setDate(3, bangGia.getNgayBatDau() != null ? Date.valueOf(bangGia.getNgayBatDau()) : null);
            ps.setDate(4, bangGia.getNgayKetThuc() != null ? Date.valueOf(bangGia.getNgayKetThuc()) : null);
            ps.setBoolean(5, bangGia.isTrangThai());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("BangGiaDAOImpl.insert: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean update(BangGia bangGia) {
        String sql = "UPDATE BangGia SET tenBangGia=?, ngayBatDau=?, ngayKetThuc=?, trangThai=? WHERE maBangGia=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, bangGia.getTenBangGia());
            ps.setDate(2, bangGia.getNgayBatDau() != null ? Date.valueOf(bangGia.getNgayBatDau()) : null);
            ps.setDate(3, bangGia.getNgayKetThuc() != null ? Date.valueOf(bangGia.getNgayKetThuc()) : null);
            ps.setBoolean(4, bangGia.isTrangThai());
            ps.setString(5, bangGia.getMaBangGia());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("BangGiaDAOImpl.update: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean delete(String maBangGia) {
        String sql = "DELETE FROM BangGia WHERE maBangGia=?";
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
        String sql = "SELECT * FROM BangGia ORDER BY ngayBatDau DESC";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("BangGiaDAOImpl.findAll: " + e.getMessage());
        }
        return list;
    }

    @Override
    public BangGia findHienHanh(LocalDate ngay) {
        Date d = Date.valueOf(ngay);
        String sql = "SELECT TOP 1 * FROM BangGia WHERE trangThai=1 AND ngayBatDau <= ? AND (ngayKetThuc IS NULL OR ngayKetThuc >= ?) ORDER BY ngayBatDau DESC";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setDate(1, d);
            ps.setDate(2, d);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("BangGiaDAOImpl.findHienHanh: " + e.getMessage());
            // fallback cho SQL Server hoặc MySQL không dùng TOP 1
            if (e.getMessage().contains("TOP")) {
                sql = "SELECT * FROM BangGia WHERE trangThai=1 AND ngayBatDau <= ? AND (ngayKetThuc IS NULL OR ngayKetThuc >= ?) ORDER BY ngayBatDau DESC";
                try (PreparedStatement psFallback = getConn().prepareStatement(sql)) {
                    psFallback.setDate(1, d);
                    psFallback.setDate(2, d);
                    ResultSet rsFallback = psFallback.executeQuery();
                    if (rsFallback.next()) return mapRow(rsFallback);
                } catch (SQLException ex) {}
            }
        }
        return null;
    }

    @Override
    public List<BangGia> findTatCaHienHanh(LocalDate ngay) {
        List<BangGia> list = new ArrayList<>();
        Date d = Date.valueOf(ngay);
        String sql = "SELECT * FROM BangGia WHERE trangThai=1 AND ngayBatDau <= ? AND (ngayKetThuc IS NULL OR ngayKetThuc >= ?) ORDER BY ngayBatDau DESC";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setDate(1, d);
            ps.setDate(2, d);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("BangGiaDAOImpl.findTatCaHienHanh: " + e.getMessage());
        }
        return list;
    }
}
