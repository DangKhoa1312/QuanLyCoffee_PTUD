package dao.impl;

import connectDB.DatabaseConnection;
import dao.NguyenLieuDAO;
import entity.NguyenLieu;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NguyenLieuDAOImpl implements NguyenLieuDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    private NguyenLieu mapRow(ResultSet rs) throws SQLException {
        return new NguyenLieu(
            rs.getString("maNL"),
            rs.getString("tenNL"),
            rs.getString("donViTinh"),
            rs.getDouble("donGiaNhap"),
            rs.getDate("ngayHetHan") != null ? rs.getDate("ngayHetHan").toLocalDate() : null
        );
    }

    @Override
    public boolean insert(NguyenLieu nl) {
        String sql = "INSERT INTO NguyenLieu(maNL, tenNL, donViTinh, donGiaNhap, ngayHetHan) VALUES(?,?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, nl.getMaNL());
            ps.setString(2, nl.getTenNL());
            ps.setString(3, nl.getDonViTinh());
            ps.setDouble(4, nl.getDonGiaNhap());
            ps.setDate(5, nl.getNgayHetHan() != null ? Date.valueOf(nl.getNgayHetHan()) : null);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("NguyenLieuDAOImpl.insert: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean update(NguyenLieu nl) {
        String sql = "UPDATE NguyenLieu SET tenNL=?, donViTinh=?, donGiaNhap=?, ngayHetHan=? WHERE maNL=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, nl.getTenNL());
            ps.setString(2, nl.getDonViTinh());
            ps.setDouble(3, nl.getDonGiaNhap());
            ps.setDate(4, nl.getNgayHetHan() != null ? Date.valueOf(nl.getNgayHetHan()) : null);
            ps.setString(5, nl.getMaNL());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("NguyenLieuDAOImpl.update: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean delete(String maNL) {
        String sql = "DELETE FROM NguyenLieu WHERE maNL=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maNL);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("NguyenLieuDAOImpl.delete: " + e.getMessage());
        }
        return false;
    }

    @Override
    public NguyenLieu findById(String maNL) {
        String sql = "SELECT * FROM NguyenLieu WHERE maNL=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maNL);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("NguyenLieuDAOImpl.findById: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<NguyenLieu> findAll() {
        List<NguyenLieu> list = new ArrayList<>();
        String sql = "SELECT * FROM NguyenLieu ORDER BY tenNL";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("NguyenLieuDAOImpl.findAll: " + e.getMessage());
        }
        return list;
    }
}
