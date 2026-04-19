package dao.impl;

import connectDB.DatabaseConnection;
import dao.ChiTietPhieuXuatDAO;
import entity.ChiTietPhieuXuat;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChiTietPhieuXuatDAOImpl implements ChiTietPhieuXuatDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    private ChiTietPhieuXuat mapRow(ResultSet rs) throws SQLException {
        return new ChiTietPhieuXuat(
            rs.getString("maCTPX"),
            rs.getDouble("soLuong"),
            rs.getString("maPX"),
            rs.getString("maNL")
        );
    }

    @Override
    public boolean insert(ChiTietPhieuXuat ct) {
        String sql = "INSERT INTO ChiTietPhieuXuat(maCTPX, soLuong, maPX, maNL) VALUES(?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, ct.getMaCTPX());
            ps.setDouble(2, ct.getSoLuong());
            ps.setString(3, ct.getMaPX());
            ps.setString(4, ct.getMaNL());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("ChiTietPhieuXuatDAOImpl.insert: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean update(ChiTietPhieuXuat ct) {
        String sql = "UPDATE ChiTietPhieuXuat SET soLuong=?, maPX=?, maNL=? WHERE maCTPX=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setDouble(1, ct.getSoLuong());
            ps.setString(2, ct.getMaPX());
            ps.setString(3, ct.getMaNL());
            ps.setString(4, ct.getMaCTPX());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("ChiTietPhieuXuatDAOImpl.update: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean delete(String maCTPX) {
        String sql = "DELETE FROM ChiTietPhieuXuat WHERE maCTPX=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maCTPX);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("ChiTietPhieuXuatDAOImpl.delete: " + e.getMessage());
        }
        return false;
    }

    @Override
    public ChiTietPhieuXuat findById(String maCTPX) {
        String sql = "SELECT * FROM ChiTietPhieuXuat WHERE maCTPX=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maCTPX);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("ChiTietPhieuXuatDAOImpl.findById: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<ChiTietPhieuXuat> findAll() {
        List<ChiTietPhieuXuat> list = new ArrayList<>();
        String sql = "SELECT * FROM ChiTietPhieuXuat ORDER BY maPX, maCTPX";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("ChiTietPhieuXuatDAOImpl.findAll: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<ChiTietPhieuXuat> findByPhieuXuat(String maPX) {
        List<ChiTietPhieuXuat> list = new ArrayList<>();
        String sql = "SELECT * FROM ChiTietPhieuXuat WHERE maPX=? ORDER BY maCTPX";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maPX);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("ChiTietPhieuXuatDAOImpl.findByPhieuXuat: " + e.getMessage());
        }
        return list;
    }
}
