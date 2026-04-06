package dao.impl;

import connectDB.DatabaseConnection;
import dao.NhaCungCapDAO;
import entity.NhaCungCap;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NhaCungCapDAOImpl implements NhaCungCapDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    private NhaCungCap mapRow(ResultSet rs) throws SQLException {
        return new NhaCungCap(
            rs.getString("maNCC"),
            rs.getString("tenNCC"),
            rs.getString("diaChi"),
            rs.getString("soDienThoai"),
            rs.getString("email")
        );
    }

    @Override
    public boolean insert(NhaCungCap ncc) {
        String sql = "INSERT INTO NhaCungCap(maNCC, tenNCC, diaChi, soDienThoai, email) VALUES(?,?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, ncc.getMaNCC());
            ps.setString(2, ncc.getTenNCC());
            ps.setString(3, ncc.getDiaChi());
            ps.setString(4, ncc.getSoDienThoai());
            ps.setString(5, ncc.getEmail());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("NhaCungCapDAOImpl.insert: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean update(NhaCungCap ncc) {
        String sql = "UPDATE NhaCungCap SET tenNCC=?, diaChi=?, soDienThoai=?, email=? WHERE maNCC=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, ncc.getTenNCC());
            ps.setString(2, ncc.getDiaChi());
            ps.setString(3, ncc.getSoDienThoai());
            ps.setString(4, ncc.getEmail());
            ps.setString(5, ncc.getMaNCC());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("NhaCungCapDAOImpl.update: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean delete(String maNCC) {
        String sql = "DELETE FROM NhaCungCap WHERE maNCC=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maNCC);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("NhaCungCapDAOImpl.delete: " + e.getMessage());
        }
        return false;
    }

    @Override
    public NhaCungCap findById(String maNCC) {
        String sql = "SELECT * FROM NhaCungCap WHERE maNCC=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maNCC);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("NhaCungCapDAOImpl.findById: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<NhaCungCap> findAll() {
        List<NhaCungCap> list = new ArrayList<>();
        String sql = "SELECT * FROM NhaCungCap ORDER BY tenNCC";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("NhaCungCapDAOImpl.findAll: " + e.getMessage());
        }
        return list;
    }
}
