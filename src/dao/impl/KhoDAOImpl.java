package dao.impl;

import connectDB.DatabaseConnection;
import dao.KhoDAO;
import entity.Kho;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class KhoDAOImpl implements KhoDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    private Kho mapRow(ResultSet rs) throws SQLException {
        return new Kho(
            rs.getString("maKho"),
            rs.getString("tenKho"),
            rs.getString("diaChi"),
            rs.getString("maNV")
        );
    }

    @Override
    public boolean insert(Kho kho) {
        String sql = "INSERT INTO Kho(maKho, tenKho, diaChi, maNV) VALUES(?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, kho.getMaKho());
            ps.setString(2, kho.getTenKho());
            ps.setString(3, kho.getDiaChi());
            ps.setString(4, kho.getMaNV());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("KhoDAOImpl.insert: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean update(Kho kho) {
        String sql = "UPDATE Kho SET tenKho=?, diaChi=?, maNV=? WHERE maKho=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, kho.getTenKho());
            ps.setString(2, kho.getDiaChi());
            ps.setString(3, kho.getMaNV());
            ps.setString(4, kho.getMaKho());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("KhoDAOImpl.update: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean delete(String maKho) {
        String sql = "DELETE FROM Kho WHERE maKho=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maKho);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("KhoDAOImpl.delete: " + e.getMessage());
        }
        return false;
    }

    @Override
    public Kho findById(String maKho) {
        String sql = "SELECT * FROM Kho WHERE maKho=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maKho);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("KhoDAOImpl.findById: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Kho> findAll() {
        List<Kho> list = new ArrayList<>();
        String sql = "SELECT * FROM Kho ORDER BY tenKho";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("KhoDAOImpl.findAll: " + e.getMessage());
        }
        return list;
    }
}
