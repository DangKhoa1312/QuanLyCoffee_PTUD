package dao.impl;

import connectDB.DatabaseConnection;
import dao.DatBanDAO;
import entity.DatBan;
import enums.TrangThaiDatBan;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatBanDAOImpl implements DatBanDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    private DatBan mapRow(ResultSet rs) throws SQLException {
        return new DatBan(
            rs.getString("maDatBan"),
            rs.getString("tenKhach"),
            rs.getString("soDienThoai"),
            rs.getInt("soLuongNguoi"),
            TrangThaiDatBan.valueOf(rs.getString("trangThai")),
            rs.getTimestamp("thoiGianDen") != null ? rs.getTimestamp("thoiGianDen").toLocalDateTime() : null,
            rs.getTimestamp("thoiGianDat") != null ? rs.getTimestamp("thoiGianDat").toLocalDateTime() : null,
            rs.getString("maBan"),
            rs.getString("maHD")
        );
    }

    @Override
    public boolean insert(DatBan datBan) {
        String sql = "INSERT INTO DatBan(maDatBan, tenKhach, soDienThoai, soLuongNguoi, trangThai, thoiGianDen, thoiGianDat, maBan, maHD) VALUES(?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, datBan.getMaDatBan());
            ps.setString(2, datBan.getTenKhach());
            ps.setString(3, datBan.getSoDienThoai());
            ps.setInt(4, datBan.getSoLuongNguoi());
            ps.setString(5, datBan.getTrangThai().name());
            ps.setTimestamp(6, datBan.getThoiGianDen() != null ? Timestamp.valueOf(datBan.getThoiGianDen()) : null);
            ps.setTimestamp(7, datBan.getThoiGianDat() != null ? Timestamp.valueOf(datBan.getThoiGianDat()) : null);
            ps.setString(8, datBan.getMaBan());
            ps.setString(9, datBan.getMaHD());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("DatBanDAOImpl.insert: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean update(DatBan datBan) {
        String sql = "UPDATE DatBan SET tenKhach=?, soDienThoai=?, soLuongNguoi=?, trangThai=?, thoiGianDen=?, thoiGianDat=?, maBan=?, maHD=? WHERE maDatBan=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, datBan.getTenKhach());
            ps.setString(2, datBan.getSoDienThoai());
            ps.setInt(3, datBan.getSoLuongNguoi());
            ps.setString(4, datBan.getTrangThai().name());
            ps.setTimestamp(5, datBan.getThoiGianDen() != null ? Timestamp.valueOf(datBan.getThoiGianDen()) : null);
            ps.setTimestamp(6, datBan.getThoiGianDat() != null ? Timestamp.valueOf(datBan.getThoiGianDat()) : null);
            ps.setString(7, datBan.getMaBan());
            ps.setString(8, datBan.getMaHD());
            ps.setString(9, datBan.getMaDatBan());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("DatBanDAOImpl.update: " + e.getMessage());
        }
        return false;
    }

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

    @Override
    public DatBan findById(String maDatBan) {
        String sql = "SELECT * FROM DatBan WHERE maDatBan=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maDatBan);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("DatBanDAOImpl.findById: " + e.getMessage());
        }
        return null;
    }

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
        return list;
    }

    @Override
    public List<DatBan> findByBan(String maBan) {
        List<DatBan> list = new ArrayList<>();
        String sql = "SELECT * FROM DatBan WHERE maBan=? ORDER BY thoiGianDen DESC";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maBan);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("DatBanDAOImpl.findByBan: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<DatBan> findConHieuLuc() {
        List<DatBan> list = new ArrayList<>();
        String sql = "SELECT * FROM DatBan WHERE trangThai IN ('CHO_XAC_NHAN', 'DA_XAC_NHAN') ORDER BY thoiGianDen ASC";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("DatBanDAOImpl.findConHieuLuc: " + e.getMessage());
        }
        return list;
    }
}
