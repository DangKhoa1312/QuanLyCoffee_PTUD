package dao.impl;

import connectDB.DatabaseConnection;
import dao.SizeDAO;
import entity.Size;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SizeDAOImpl implements SizeDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    private Size mapRow(ResultSet rs) throws SQLException {
        return new Size(
            rs.getString("maSize"),
            rs.getString("tenSize"),
            rs.getString("maMon")
        );
    }

    @Override
    public boolean insert(Size size) {
        String sql = "INSERT INTO Size(maSize, tenSize, maMon) VALUES(?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, size.getMaSize());
            ps.setString(2, size.getTenSize());
            ps.setString(3, size.getMaMon());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("SizeDAOImpl.insert: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean update(Size size) {
        String sql = "UPDATE Size SET tenSize=?, maMon=? WHERE maSize=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, size.getTenSize());
            ps.setString(2, size.getMaMon());
            ps.setString(3, size.getMaSize());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("SizeDAOImpl.update: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean delete(String maSize) {
        String sql = "DELETE FROM Size WHERE maSize=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maSize);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("SizeDAOImpl.delete: " + e.getMessage());
        }
        return false;
    }

    @Override
    public Size findById(String maSize) {
        String sql = "SELECT * FROM Size WHERE maSize=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maSize);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("SizeDAOImpl.findById: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Size> findAll() {
        List<Size> list = new ArrayList<>();
        String sql = "SELECT * FROM Size ORDER BY maSize";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("SizeDAOImpl.findAll: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<Size> findByMon(String maMon) {
        List<Size> list = new ArrayList<>();
        String sql = "SELECT * FROM Size WHERE maMon=? ORDER BY tenSize";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maMon);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("SizeDAOImpl.findByMon: " + e.getMessage());
        }
        return list;
    }
}
