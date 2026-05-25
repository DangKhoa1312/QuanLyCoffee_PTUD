package dao.impl;

import connectDB.DatabaseConnection;
import dao.CauHinhDAO;
import entity.CauHinh;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CauHinhDAOImpl implements CauHinhDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    private CauHinh mapRow(ResultSet rs) throws SQLException {
        return new CauHinh(
                rs.getString("maCauHinh"),
                rs.getString("tenCauHinh"),
                rs.getString("giaTri"),
                rs.getString("kieuDuLieu"),
                rs.getString("moTa")
        );
    }

    @Override
    public boolean insert(CauHinh cauHinh) {
        String sql = "INSERT INTO CauHinh (maCauHinh, tenCauHinh, giaTri, kieuDuLieu, moTa) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, cauHinh.getMaCauHinh());
            ps.setString(2, cauHinh.getTenCauHinh());
            ps.setString(3, cauHinh.getGiaTri());
            ps.setString(4, cauHinh.getKieuDuLieu());
            ps.setString(5, cauHinh.getMoTa());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("CauHinhDAOImpl.insert: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean update(CauHinh cauHinh) {
        String sql = "UPDATE CauHinh SET tenCauHinh = ?, giaTri = ?, kieuDuLieu = ?, moTa = ? WHERE maCauHinh = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, cauHinh.getTenCauHinh());
            ps.setString(2, cauHinh.getGiaTri());
            ps.setString(3, cauHinh.getKieuDuLieu());
            ps.setString(4, cauHinh.getMoTa());
            ps.setString(5, cauHinh.getMaCauHinh());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("CauHinhDAOImpl.update: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(String maCauHinh) {
        String sql = "DELETE FROM CauHinh WHERE maCauHinh = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maCauHinh);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("CauHinhDAOImpl.delete: " + e.getMessage());
            return false;
        }
    }

    @Override
    public CauHinh findById(String maCauHinh) {
        String sql = "SELECT * FROM CauHinh WHERE maCauHinh = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maCauHinh);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("CauHinhDAOImpl.findById: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<CauHinh> findAll() {
        List<CauHinh> list = new ArrayList<>();
        String sql = "SELECT * FROM CauHinh ORDER BY tenCauHinh";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("CauHinhDAOImpl.findAll: " + e.getMessage());
        }
        return list;
    }
}
