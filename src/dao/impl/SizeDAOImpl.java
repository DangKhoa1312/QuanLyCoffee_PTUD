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
            rs.getString("maMon"),
            rs.getBoolean("trangThai"),
            rs.getDouble("tileSize")  // Đọc tỉ lệ từ DB
        );
    }

    @Override
    public boolean insert(Size size) {
        String sql = "INSERT INTO Size(maSize, tenSize, maMon, trangThai, tileSize) VALUES(?,?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, size.getMaSize());
            ps.setString(2, size.getTenSize());
            ps.setString(3, size.getMaMon());
            ps.setBoolean(4, size.isTrangThai());
            ps.setDouble(5, size.getTileSize());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("SizeDAOImpl.insert: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean update(Size size) {
        String sql = "UPDATE Size SET tenSize=?, maMon=?, trangThai=?, tileSize=? WHERE maSize=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, size.getTenSize());
            ps.setString(2, size.getMaMon());
            ps.setBoolean(3, size.isTrangThai());
            ps.setDouble(4, size.getTileSize());
            ps.setString(5, size.getMaSize());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("SizeDAOImpl.update: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean delete(String maSize) {
        String sql = "UPDATE Size SET trangThai=0 WHERE maSize=?";
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
        String sql = "SELECT * FROM Size WHERE trangThai=1 ORDER BY maSize";
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
        String sql = "SELECT * FROM Size WHERE maMon=? AND trangThai=1 ORDER BY tenSize";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maMon);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("SizeDAOImpl.findByMon: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<Size> findAllByMon(String maMon) {
        List<Size> list = new ArrayList<>();
        String sql = "SELECT * FROM Size WHERE maMon=? ORDER BY tenSize";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maMon);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("SizeDAOImpl.findAllByMon: " + e.getMessage());
        }
        return list;
    }

    @Override
    public boolean isSizeUsedInHoaDon(String maSize) {
        String sql = "SELECT TOP 1 1 FROM ChiTietHoaDon WHERE maSize=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maSize);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return true;
        } catch (SQLException e) {
            // Hiệu đính Fallback nếu database không hỗ trợ TOP 1(mặc dù QLQC dùng SQLServer)
            if (e.getMessage().contains("TOP")) {
                sql = "SELECT 1 FROM ChiTietHoaDon WHERE maSize=? LIMIT 1";
                try (PreparedStatement ps2 = getConn().prepareStatement(sql)) {
                    ps2.setString(1, maSize);
                    ResultSet rs2 = ps2.executeQuery();
                    if (rs2.next()) return true;
                } catch (SQLException ex) { }
            }
            System.err.println("SizeDAOImpl.isSizeUsedInHoaDon: " + e.getMessage());
        }
        return false;
    }
}
