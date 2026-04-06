package dao.impl;

import connectDB.DatabaseConnection;
import dao.BanDAO;
import entity.Ban;
import enums.TrangThaiBan;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BanDAOImpl implements BanDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    private Ban mapRow(ResultSet rs) throws SQLException {
        return new Ban(
            rs.getString("maBan"),
            rs.getString("soBan"),
            rs.getString("maKhuVuc"),
            rs.getInt("sucChua"),
            TrangThaiBan.valueOf(rs.getString("trangThai"))
        );
    }

    @Override
    public boolean insert(Ban ban) {
        String sql = "INSERT INTO Ban(maBan,soBan,maKhuVuc,sucChua,trangThai) VALUES(?,?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, ban.getMaBan());
            ps.setString(2, ban.getSoBan());
            ps.setString(3, ban.getMaKhuVuc());
            ps.setInt(4, ban.getSucChua());
            ps.setString(5, ban.getTrangThai().name());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("BanDAO.insert: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean update(Ban ban) {
        String sql = "UPDATE Ban SET soBan=?,maKhuVuc=?,sucChua=?,trangThai=? WHERE maBan=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, ban.getSoBan());
            ps.setString(2, ban.getMaKhuVuc());
            ps.setInt(3, ban.getSucChua());
            ps.setString(4, ban.getTrangThai().name());
            ps.setString(5, ban.getMaBan());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("BanDAO.update: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(String maBan) {
        String sql = "DELETE FROM Ban WHERE maBan=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maBan);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("BanDAO.delete: " + e.getMessage());
            return false;
        }
    }

    @Override
    public Ban findById(String maBan) {
        String sql = "SELECT * FROM Ban WHERE maBan=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maBan);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("BanDAO.findById: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Ban> findAll() {
        List<Ban> list = new ArrayList<>();
        String sql = "SELECT * FROM Ban ORDER BY maKhuVuc, soBan";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("BanDAO.findAll: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<Ban> findByKhuVuc(String maKhuVuc) {
        List<Ban> list = new ArrayList<>();
        String sql = "SELECT * FROM Ban WHERE maKhuVuc=? ORDER BY soBan";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maKhuVuc);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("BanDAO.findByKhuVuc: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<Ban> findByTrangThai(TrangThaiBan trangThai) {
        List<Ban> list = new ArrayList<>();
        String sql = "SELECT * FROM Ban WHERE trangThai=? ORDER BY soBan";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, trangThai.name());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("BanDAO.findByTrangThai: " + e.getMessage());
        }
        return list;
    }

    @Override
    public boolean updateTrangThai(String maBan, TrangThaiBan trangThai) {
        String sql = "UPDATE Ban SET trangThai=? WHERE maBan=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, trangThai.name());
            ps.setString(2, maBan);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("BanDAO.updateTrangThai: " + e.getMessage());
            return false;
        }
    }
}
