package dao.impl;

import connectDB.DatabaseConnection;
import dao.KhuVucDAO;
import entity.KhuVuc;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class KhuVucDAOImpl implements KhuVucDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    private KhuVuc mapRow(ResultSet rs) throws SQLException {
        return new KhuVuc(
            rs.getString("maKhuVuc"),
            rs.getString("tenKhuVuc"),
            rs.getString("moTa"),
            rs.getBoolean("trangThai")
        );
    }

    @Override
    public boolean insert(KhuVuc kv) {
        String sql = "INSERT INTO KhuVuc(maKhuVuc,tenKhuVuc,moTa,trangThai) VALUES(?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, kv.getMaKhuVuc());
            ps.setString(2, kv.getTenKhuVuc());
            ps.setString(3, kv.getMoTa());
            ps.setBoolean(4, kv.isTrangThai());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("KhuVucDAO.insert: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean update(KhuVuc kv) {
        String sql = "UPDATE KhuVuc SET tenKhuVuc=?,moTa=?,trangThai=? WHERE maKhuVuc=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, kv.getTenKhuVuc());
            ps.setString(2, kv.getMoTa());
            ps.setBoolean(3, kv.isTrangThai());
            ps.setString(4, kv.getMaKhuVuc());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("KhuVucDAO.update: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(String maKhuVuc) {
        String sql = "DELETE FROM KhuVuc WHERE maKhuVuc=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maKhuVuc);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("KhuVucDAO.delete: " + e.getMessage());
            return false;
        }
    }

    @Override
    public KhuVuc findById(String maKhuVuc) {
        String sql = "SELECT * FROM KhuVuc WHERE maKhuVuc=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maKhuVuc);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("KhuVucDAO.findById: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<KhuVuc> findAll() {
        List<KhuVuc> list = new ArrayList<>();
        String sql = "SELECT * FROM KhuVuc ORDER BY maKhuVuc";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("KhuVucDAO.findAll: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<KhuVuc> findActive() {
        List<KhuVuc> list = new ArrayList<>();
        String sql = "SELECT * FROM KhuVuc WHERE trangThai=1 ORDER BY maKhuVuc";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("KhuVucDAO.findActive: " + e.getMessage());
        }
        return list;
    }
}
