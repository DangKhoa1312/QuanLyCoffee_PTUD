package dao.impl;

import connectDB.DatabaseConnection;
import dao.ChiTietPhieuNhapDAO;
import entity.ChiTietPhieuNhap;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChiTietPhieuNhapDAOImpl implements ChiTietPhieuNhapDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    private ChiTietPhieuNhap mapRow(ResultSet rs) throws SQLException {
        return new ChiTietPhieuNhap(
            rs.getString("maCTPN"),
            rs.getDouble("soLuong"),
            rs.getDouble("donGia"),
            rs.getDouble("thanhTien"),
            rs.getString("maPN"),
            rs.getString("maNL")
        );
    }

    @Override
    public boolean insert(ChiTietPhieuNhap ctpn) {
        String sql = "INSERT INTO ChiTietPhieuNhap(maCTPN, soLuong, donGia, thanhTien, maPN, maNL) VALUES(?,?,?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, ctpn.getMaCTPN());
            ps.setDouble(2, ctpn.getSoLuong());
            ps.setDouble(3, ctpn.getDonGia());
            ps.setDouble(4, ctpn.getThanhTien());
            ps.setString(5, ctpn.getMaPN());
            ps.setString(6, ctpn.getMaNL());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("ChiTietPhieuNhapDAOImpl.insert: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean update(ChiTietPhieuNhap ctpn) {
        String sql = "UPDATE ChiTietPhieuNhap SET soLuong=?, donGia=?, thanhTien=?, maPN=?, maNL=? WHERE maCTPN=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setDouble(1, ctpn.getSoLuong());
            ps.setDouble(2, ctpn.getDonGia());
            ps.setDouble(3, ctpn.getThanhTien());
            ps.setString(4, ctpn.getMaPN());
            ps.setString(5, ctpn.getMaNL());
            ps.setString(6, ctpn.getMaCTPN());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("ChiTietPhieuNhapDAOImpl.update: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean delete(String maCTPN) {
        String sql = "DELETE FROM ChiTietPhieuNhap WHERE maCTPN=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maCTPN);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("ChiTietPhieuNhapDAOImpl.delete: " + e.getMessage());
        }
        return false;
    }

    @Override
    public ChiTietPhieuNhap findById(String maCTPN) {
        String sql = "SELECT * FROM ChiTietPhieuNhap WHERE maCTPN=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maCTPN);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("ChiTietPhieuNhapDAOImpl.findById: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<ChiTietPhieuNhap> findAll() {
        List<ChiTietPhieuNhap> list = new ArrayList<>();
        String sql = "SELECT * FROM ChiTietPhieuNhap ORDER BY maPN, maCTPN";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("ChiTietPhieuNhapDAOImpl.findAll: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<ChiTietPhieuNhap> findByPhieuNhap(String maPN) {
        List<ChiTietPhieuNhap> list = new ArrayList<>();
        String sql = "SELECT * FROM ChiTietPhieuNhap WHERE maPN=? ORDER BY maCTPN";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maPN);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("ChiTietPhieuNhapDAOImpl.findByPhieuNhap: " + e.getMessage());
        }
        return list;
    }
}
