package dao.impl;

import connectDB.DatabaseConnection;
import dao.ChiTietHoaDonDAO;
import entity.ChiTietHoaDon;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChiTietHoaDonDAOImpl implements ChiTietHoaDonDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    private ChiTietHoaDon mapRow(ResultSet rs) throws SQLException {
        return new ChiTietHoaDon(
            rs.getString("maCTHD"),
            rs.getInt("soLuong"),
            rs.getDouble("donGia"),
            rs.getDouble("thanhTien"),
            rs.getString("ghiChu"),
            rs.getString("maHD"),
            rs.getString("maMon"),
            rs.getString("maSize")
        );
    }

    @Override
    public boolean insert(ChiTietHoaDon ct) {
        String sql = "INSERT INTO ChiTietHoaDon(maCTHD, soLuong, donGia, thanhTien, ghiChu, maHD, maMon, maSize) VALUES(?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, ct.getMaCTHD());
            ps.setInt(2, ct.getSoLuong());
            ps.setDouble(3, ct.getDonGia());
            ps.setDouble(4, ct.getThanhTien());
            ps.setString(5, ct.getGhiChu());
            ps.setString(6, ct.getMaHD());
            ps.setString(7, ct.getMaMon());
            ps.setString(8, ct.getMaSize());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("ChiTietHoaDonDAOImpl.insert: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean update(ChiTietHoaDon ct) {
        String sql = "UPDATE ChiTietHoaDon SET soLuong=?, donGia=?, thanhTien=?, ghiChu=?, maHD=?, maMon=?, maSize=? WHERE maCTHD=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, ct.getSoLuong());
            ps.setDouble(2, ct.getDonGia());
            ps.setDouble(3, ct.getThanhTien());
            ps.setString(4, ct.getGhiChu());
            ps.setString(5, ct.getMaHD());
            ps.setString(6, ct.getMaMon());
            ps.setString(7, ct.getMaSize());
            ps.setString(8, ct.getMaCTHD());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("ChiTietHoaDonDAOImpl.update: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean delete(String maCTHD) {
        String sql = "DELETE FROM ChiTietHoaDon WHERE maCTHD=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maCTHD);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("ChiTietHoaDonDAOImpl.delete: " + e.getMessage());
        }
        return false;
    }

    @Override
    public ChiTietHoaDon findById(String maCTHD) {
        String sql = "SELECT * FROM ChiTietHoaDon WHERE maCTHD=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maCTHD);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("ChiTietHoaDonDAOImpl.findById: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<ChiTietHoaDon> findAll() {
        List<ChiTietHoaDon> list = new ArrayList<>();
        String sql = "SELECT * FROM ChiTietHoaDon ORDER BY maHD, maCTHD";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("ChiTietHoaDonDAOImpl.findAll: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<ChiTietHoaDon> findByHoaDon(String maHD) {
        List<ChiTietHoaDon> list = new ArrayList<>();
        String sql = "SELECT * FROM ChiTietHoaDon WHERE maHD=? ORDER BY maCTHD";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maHD);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("ChiTietHoaDonDAOImpl.findByHoaDon: " + e.getMessage());
        }
        return list;
    }

    @Override
    public boolean deleteByHoaDon(String maHD) {
        String sql = "DELETE FROM ChiTietHoaDon WHERE maHD=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maHD);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("ChiTietHoaDonDAOImpl.deleteByHoaDon: " + e.getMessage());
        }
        return false;
    }
}
