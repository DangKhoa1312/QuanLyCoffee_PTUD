package dao.impl;

import connectDB.DatabaseConnection;
import dao.ChiTietHoaDonToppingDAO;
import entity.ChiTietHoaDonTopping;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChiTietHoaDonToppingDAOImpl implements ChiTietHoaDonToppingDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    private ChiTietHoaDonTopping mapRow(ResultSet rs) throws SQLException {
        return new ChiTietHoaDonTopping(
            rs.getString("maID"),
            rs.getInt("soLuong"),
            rs.getDouble("giaTopping"),
            rs.getString("maCTHD"),
            rs.getString("maTopping")
        );
    }

    @Override
    public boolean insert(ChiTietHoaDonTopping ct) {
        String sql = "INSERT INTO ChiTietHoaDonTopping(maID, soLuong, giaTopping, maCTHD, maTopping) VALUES(?,?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, ct.getMaID());
            ps.setInt(2, ct.getSoLuong());
            ps.setDouble(3, ct.getGiaTopping());
            ps.setString(4, ct.getMaCTHD());
            ps.setString(5, ct.getMaTopping());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("ChiTietHoaDonToppingDAOImpl.insert: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean update(ChiTietHoaDonTopping ct) {
        String sql = "UPDATE ChiTietHoaDonTopping SET soLuong=?, giaTopping=?, maCTHD=?, maTopping=? WHERE maID=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, ct.getSoLuong());
            ps.setDouble(2, ct.getGiaTopping());
            ps.setString(3, ct.getMaCTHD());
            ps.setString(4, ct.getMaTopping());
            ps.setString(5, ct.getMaID());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("ChiTietHoaDonToppingDAOImpl.update: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean delete(String maID) {
        String sql = "DELETE FROM ChiTietHoaDonTopping WHERE maID=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("ChiTietHoaDonToppingDAOImpl.delete: " + e.getMessage());
        }
        return false;
    }

    @Override
    public ChiTietHoaDonTopping findById(String maID) {
        String sql = "SELECT * FROM ChiTietHoaDonTopping WHERE maID=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("ChiTietHoaDonToppingDAOImpl.findById: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<ChiTietHoaDonTopping> findAll() {
        List<ChiTietHoaDonTopping> list = new ArrayList<>();
        String sql = "SELECT * FROM ChiTietHoaDonTopping ORDER BY maCTHD, maID";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("ChiTietHoaDonToppingDAOImpl.findAll: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<ChiTietHoaDonTopping> findByCTHD(String maCTHD) {
        List<ChiTietHoaDonTopping> list = new ArrayList<>();
        String sql = "SELECT * FROM ChiTietHoaDonTopping WHERE maCTHD=? ORDER BY maID";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maCTHD);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("ChiTietHoaDonToppingDAOImpl.findByCTHD: " + e.getMessage());
        }
        return list;
    }
}
