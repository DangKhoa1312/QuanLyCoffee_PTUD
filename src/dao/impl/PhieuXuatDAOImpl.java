package dao.impl;

import connectDB.DatabaseConnection;
import dao.PhieuXuatDAO;
import entity.PhieuXuat;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PhieuXuatDAOImpl implements PhieuXuatDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    private PhieuXuat mapRow(ResultSet rs) throws SQLException {
        return new PhieuXuat(
            rs.getString("maPX"),
            rs.getTimestamp("ngayXuat") != null ? rs.getTimestamp("ngayXuat").toLocalDateTime() : null,
            rs.getString("lyDoXuat"),
            rs.getString("maNV"),
            rs.getString("maKho")
        );
    }

    @Override
    public boolean insert(PhieuXuat px) {
        return insert(getConn(), px);
    }

    @Override
    public boolean insert(Connection conn, PhieuXuat px) {
        String sql = "INSERT INTO PhieuXuat(maPX, ngayXuat, lyDoXuat, maNV, maKho) VALUES(?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, px.getMaPX());
            ps.setTimestamp(2, px.getNgayXuat() != null ? Timestamp.valueOf(px.getNgayXuat()) : null);
            ps.setString(3, px.getLyDoXuat());
            ps.setString(4, px.getMaNV());
            ps.setString(5, px.getMaKho());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("PhieuXuatDAOImpl.insert: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean update(PhieuXuat px) {
        String sql = "UPDATE PhieuXuat SET ngayXuat=?, lyDoXuat=?, maNV=?, maKho=? WHERE maPX=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setTimestamp(1, px.getNgayXuat() != null ? Timestamp.valueOf(px.getNgayXuat()) : null);
            ps.setString(2, px.getLyDoXuat());
            ps.setString(3, px.getMaNV());
            ps.setString(4, px.getMaKho());
            ps.setString(5, px.getMaPX());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("PhieuXuatDAOImpl.update: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean delete(String maPX) {
        String sql = "DELETE FROM PhieuXuat WHERE maPX=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maPX);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("PhieuXuatDAOImpl.delete: " + e.getMessage());
        }
        return false;
    }

    @Override
    public PhieuXuat findById(String maPX) {
        String sql = "SELECT * FROM PhieuXuat WHERE maPX=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maPX);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("PhieuXuatDAOImpl.findById: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<PhieuXuat> findAll() {
        List<PhieuXuat> list = new ArrayList<>();
        String sql = "SELECT * FROM PhieuXuat ORDER BY ngayXuat DESC";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("PhieuXuatDAOImpl.findAll: " + e.getMessage());
        }
        return list;
    }
}
