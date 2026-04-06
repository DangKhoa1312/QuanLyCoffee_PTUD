package dao.impl;

import connectDB.DatabaseConnection;
import dao.MonDAO;
import entity.Mon;
import enums.LoaiMon;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MonDAOImpl implements MonDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    private Mon mapRow(ResultSet rs) throws SQLException {
        return new Mon(
            rs.getString("maMon"),
            rs.getString("tenMon"),
            rs.getString("moTa"),
            rs.getString("hinhAnh"),
            LoaiMon.valueOf(rs.getString("loaiMon")),
            rs.getBoolean("trangThai")
        );
    }

    @Override
    public boolean insert(Mon mon) {
        String sql = "INSERT INTO Mon(maMon, tenMon, moTa, hinhAnh, loaiMon, trangThai) VALUES(?,?,?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, mon.getMaMon());
            ps.setString(2, mon.getTenMon());
            ps.setString(3, mon.getMoTa());
            ps.setString(4, mon.getHinhAnh());
            ps.setString(5, mon.getLoaiMon().name());
            ps.setBoolean(6, mon.isTrangThai());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("MonDAOImpl.insert: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean update(Mon mon) {
        String sql = "UPDATE Mon SET tenMon=?, moTa=?, hinhAnh=?, loaiMon=?, trangThai=? WHERE maMon=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, mon.getTenMon());
            ps.setString(2, mon.getMoTa());
            ps.setString(3, mon.getHinhAnh());
            ps.setString(4, mon.getLoaiMon().name());
            ps.setBoolean(5, mon.isTrangThai());
            ps.setString(6, mon.getMaMon());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("MonDAOImpl.update: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean delete(String maMon) {
        String sql = "DELETE FROM Mon WHERE maMon=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maMon);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("MonDAOImpl.delete: " + e.getMessage());
        }
        return false;
    }

    @Override
    public Mon findById(String maMon) {
        String sql = "SELECT * FROM Mon WHERE maMon=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maMon);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("MonDAOImpl.findById: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Mon> findAll() {
        List<Mon> list = new ArrayList<>();
        String sql = "SELECT * FROM Mon ORDER BY maMon";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("MonDAOImpl.findAll: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<Mon> findByLoai(LoaiMon loaiMon) {
        List<Mon> list = new ArrayList<>();
        String sql = "SELECT * FROM Mon WHERE loaiMon=? ORDER BY maMon";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, loaiMon.name());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("MonDAOImpl.findByLoai: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<Mon> findDangBan() {
        List<Mon> list = new ArrayList<>();
        String sql = "SELECT * FROM Mon WHERE trangThai=1 ORDER BY loaiMon, tenMon";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("MonDAOImpl.findDangBan: " + e.getMessage());
        }
        return list;
    }
}
