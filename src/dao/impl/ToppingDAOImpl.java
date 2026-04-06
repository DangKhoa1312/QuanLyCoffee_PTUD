package dao.impl;

import connectDB.DatabaseConnection;
import dao.ToppingDAO;
import entity.Topping;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ToppingDAOImpl implements ToppingDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    private Topping mapRow(ResultSet rs) throws SQLException {
        return new Topping(
            rs.getString("maTopping"),
            rs.getString("tenTopping"),
            rs.getDouble("giaTopping"),
            rs.getBoolean("trangThai")
        );
    }

    @Override
    public boolean insert(Topping topping) {
        String sql = "INSERT INTO Topping(maTopping, tenTopping, giaTopping, trangThai) VALUES(?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, topping.getMaTopping());
            ps.setString(2, topping.getTenTopping());
            ps.setDouble(3, topping.getGiaTopping());
            ps.setBoolean(4, topping.isTrangThai());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("ToppingDAOImpl.insert: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean update(Topping topping) {
        String sql = "UPDATE Topping SET tenTopping=?, giaTopping=?, trangThai=? WHERE maTopping=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, topping.getTenTopping());
            ps.setDouble(2, topping.getGiaTopping());
            ps.setBoolean(3, topping.isTrangThai());
            ps.setString(4, topping.getMaTopping());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("ToppingDAOImpl.update: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean delete(String maTopping) {
        String sql = "DELETE FROM Topping WHERE maTopping=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maTopping);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("ToppingDAOImpl.delete: " + e.getMessage());
        }
        return false;
    }

    @Override
    public Topping findById(String maTopping) {
        String sql = "SELECT * FROM Topping WHERE maTopping=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maTopping);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("ToppingDAOImpl.findById: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Topping> findAll() {
        List<Topping> list = new ArrayList<>();
        String sql = "SELECT * FROM Topping ORDER BY tenTopping";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("ToppingDAOImpl.findAll: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<Topping> findDangCungCap() {
        List<Topping> list = new ArrayList<>();
        String sql = "SELECT * FROM Topping WHERE trangThai=1 ORDER BY tenTopping";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("ToppingDAOImpl.findDangCungCap: " + e.getMessage());
        }
        return list;
    }
}
