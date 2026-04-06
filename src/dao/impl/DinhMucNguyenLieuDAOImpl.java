package dao.impl;

import connectDB.DatabaseConnection;
import dao.DinhMucNguyenLieuDAO;
import entity.DinhMucNguyenLieu;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DinhMucNguyenLieuDAOImpl implements DinhMucNguyenLieuDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    private DinhMucNguyenLieu mapRow(ResultSet rs) throws SQLException {
        return new DinhMucNguyenLieu(
            rs.getString("maDinhMuc"),
            rs.getDouble("soLuong"),
            rs.getString("maMon"),
            rs.getString("maNL")
        );
    }

    @Override
    public boolean insert(DinhMucNguyenLieu dm) {
        String sql = "INSERT INTO DinhMucNguyenLieu(maDinhMuc, soLuong, maMon, maNL) VALUES(?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, dm.getMaDinhMuc());
            ps.setDouble(2, dm.getSoLuong());
            ps.setString(3, dm.getMaMon());
            ps.setString(4, dm.getMaNL());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("DinhMucNguyenLieuDAOImpl.insert: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean update(DinhMucNguyenLieu dm) {
        String sql = "UPDATE DinhMucNguyenLieu SET soLuong=?, maMon=?, maNL=? WHERE maDinhMuc=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setDouble(1, dm.getSoLuong());
            ps.setString(2, dm.getMaMon());
            ps.setString(3, dm.getMaNL());
            ps.setString(4, dm.getMaDinhMuc());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("DinhMucNguyenLieuDAOImpl.update: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean delete(String maDinhMuc) {
        String sql = "DELETE FROM DinhMucNguyenLieu WHERE maDinhMuc=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maDinhMuc);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("DinhMucNguyenLieuDAOImpl.delete: " + e.getMessage());
        }
        return false;
    }

    @Override
    public DinhMucNguyenLieu findById(String maDinhMuc) {
        String sql = "SELECT * FROM DinhMucNguyenLieu WHERE maDinhMuc=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maDinhMuc);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("DinhMucNguyenLieuDAOImpl.findById: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<DinhMucNguyenLieu> findAll() {
        List<DinhMucNguyenLieu> list = new ArrayList<>();
        String sql = "SELECT * FROM DinhMucNguyenLieu ORDER BY maMon";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("DinhMucNguyenLieuDAOImpl.findAll: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<DinhMucNguyenLieu> findByMon(String maMon) {
        List<DinhMucNguyenLieu> list = new ArrayList<>();
        String sql = "SELECT * FROM DinhMucNguyenLieu WHERE maMon=? ORDER BY maNL";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maMon);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("DinhMucNguyenLieuDAOImpl.findByMon: " + e.getMessage());
        }
        return list;
    }
}
