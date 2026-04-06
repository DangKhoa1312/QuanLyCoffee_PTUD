package dao.impl;

import connectDB.DatabaseConnection;
import dao.PhieuNhapDAO;
import entity.PhieuNhap;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PhieuNhapDAOImpl implements PhieuNhapDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    private PhieuNhap mapRow(ResultSet rs) throws SQLException {
        return new PhieuNhap(
            rs.getString("maPN"),
            rs.getTimestamp("ngayNhap") != null ? rs.getTimestamp("ngayNhap").toLocalDateTime() : null,
            rs.getDouble("tongTien"),
            rs.getString("maNV"),
            rs.getString("maNCC"),
            rs.getString("maKho")
        );
    }

    @Override
    public boolean insert(PhieuNhap pn) {
        String sql = "INSERT INTO PhieuNhap(maPN, ngayNhap, tongTien, maNV, maNCC, maKho) VALUES(?,?,?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, pn.getMaPN());
            ps.setTimestamp(2, pn.getNgayNhap() != null ? Timestamp.valueOf(pn.getNgayNhap()) : null);
            ps.setDouble(3, pn.getTongTien());
            ps.setString(4, pn.getMaNV());
            ps.setString(5, pn.getMaNCC());
            ps.setString(6, pn.getMaKho());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("PhieuNhapDAOImpl.insert: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean update(PhieuNhap pn) {
        String sql = "UPDATE PhieuNhap SET ngayNhap=?, tongTien=?, maNV=?, maNCC=?, maKho=? WHERE maPN=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setTimestamp(1, pn.getNgayNhap() != null ? Timestamp.valueOf(pn.getNgayNhap()) : null);
            ps.setDouble(2, pn.getTongTien());
            ps.setString(3, pn.getMaNV());
            ps.setString(4, pn.getMaNCC());
            ps.setString(5, pn.getMaKho());
            ps.setString(6, pn.getMaPN());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("PhieuNhapDAOImpl.update: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean delete(String maPN) {
        String sql = "DELETE FROM PhieuNhap WHERE maPN=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maPN);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("PhieuNhapDAOImpl.delete: " + e.getMessage());
        }
        return false;
    }

    @Override
    public PhieuNhap findById(String maPN) {
        String sql = "SELECT * FROM PhieuNhap WHERE maPN=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maPN);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("PhieuNhapDAOImpl.findById: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<PhieuNhap> findAll() {
        List<PhieuNhap> list = new ArrayList<>();
        String sql = "SELECT * FROM PhieuNhap ORDER BY ngayNhap DESC";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("PhieuNhapDAOImpl.findAll: " + e.getMessage());
        }
        return list;
    }
}
