package dao.impl;

import connectDB.DatabaseConnection;
import dao.BangGiaChiTietDAO;
import entity.BangGiaChiTiet;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BangGiaChiTietDAOImpl implements BangGiaChiTietDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    private BangGiaChiTiet mapRow(ResultSet rs) throws SQLException {
        return new BangGiaChiTiet(
            rs.getString("maBGCT"),
            rs.getDouble("giaBan"),
            rs.getString("maSize"),
            rs.getString("maBangGia")
        );
    }

    @Override
    public boolean insert(BangGiaChiTiet bgct) {
        String sql = "INSERT INTO BangGiaChiTiet(maBGCT, giaBan, maSize, maBangGia) VALUES(?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, bgct.getMaBGCT());
            ps.setDouble(2, bgct.getGiaBan());
            ps.setString(3, bgct.getMaSize());
            ps.setString(4, bgct.getMaBangGia());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("BangGiaChiTietDAOImpl.insert: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean update(BangGiaChiTiet bgct) {
        String sql = "UPDATE BangGiaChiTiet SET giaBan=?, maSize=?, maBangGia=? WHERE maBGCT=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setDouble(1, bgct.getGiaBan());
            ps.setString(2, bgct.getMaSize());
            ps.setString(3, bgct.getMaBangGia());
            ps.setString(4, bgct.getMaBGCT());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("BangGiaChiTietDAOImpl.update: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean delete(String maBGCT) {
        String sql = "DELETE FROM BangGiaChiTiet WHERE maBGCT=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maBGCT);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("BangGiaChiTietDAOImpl.delete: " + e.getMessage());
        }
        return false;
    }

    @Override
    public BangGiaChiTiet findById(String maBGCT) {
        String sql = "SELECT * FROM BangGiaChiTiet WHERE maBGCT=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maBGCT);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("BangGiaChiTietDAOImpl.findById: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<BangGiaChiTiet> findAll() {
        List<BangGiaChiTiet> list = new ArrayList<>();
        String sql = "SELECT * FROM BangGiaChiTiet ORDER BY maBangGia, maSize";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("BangGiaChiTietDAOImpl.findAll: " + e.getMessage());
        }
        return list;
    }

    @Override
    public BangGiaChiTiet findGia(String maSize, String maBangGia) {
        String sql = "SELECT * FROM BangGiaChiTiet WHERE maSize=? AND maBangGia=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maSize);
            ps.setString(2, maBangGia);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("BangGiaChiTietDAOImpl.findGia: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<BangGiaChiTiet> findByBangGia(String maBangGia) {
        List<BangGiaChiTiet> list = new ArrayList<>();
        String sql = "SELECT * FROM BangGiaChiTiet WHERE maBangGia=? ORDER BY maSize";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maBangGia);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("BangGiaChiTietDAOImpl.findByBangGia: " + e.getMessage());
        }
        return list;
    }
}
