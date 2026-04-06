package dao.impl;

import connectDB.DatabaseConnection;
import dao.TonKhoDAO;
import entity.TonKho;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TonKhoDAOImpl implements TonKhoDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    private TonKho mapRow(ResultSet rs) throws SQLException {
        return new TonKho(
            rs.getString("maTonKho"),
            rs.getDouble("soLuongTon"),
            rs.getDouble("mucToiThieu"),
            rs.getTimestamp("ngayCapNhat") != null ? rs.getTimestamp("ngayCapNhat").toLocalDateTime() : null,
            rs.getString("maKho"),
            rs.getString("maNL")
        );
    }

    @Override
    public boolean insert(TonKho tonKho) {
        String sql = "INSERT INTO TonKho(maTonKho, soLuongTon, mucToiThieu, ngayCapNhat, maKho, maNL) VALUES(?,?,?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, tonKho.getMaTonKho());
            ps.setDouble(2, tonKho.getSoLuongTon());
            ps.setDouble(3, tonKho.getMucToiThieu());
            ps.setTimestamp(4, tonKho.getNgayCapNhat() != null ? Timestamp.valueOf(tonKho.getNgayCapNhat()) : null);
            ps.setString(5, tonKho.getMaKho());
            ps.setString(6, tonKho.getMaNL());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("TonKhoDAOImpl.insert: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean update(TonKho tonKho) {
        String sql = "UPDATE TonKho SET soLuongTon=?, mucToiThieu=?, ngayCapNhat=?, maKho=?, maNL=? WHERE maTonKho=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setDouble(1, tonKho.getSoLuongTon());
            ps.setDouble(2, tonKho.getMucToiThieu());
            ps.setTimestamp(3, tonKho.getNgayCapNhat() != null ? Timestamp.valueOf(tonKho.getNgayCapNhat()) : null);
            ps.setString(4, tonKho.getMaKho());
            ps.setString(5, tonKho.getMaNL());
            ps.setString(6, tonKho.getMaTonKho());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("TonKhoDAOImpl.update: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean delete(String maTonKho) {
        String sql = "DELETE FROM TonKho WHERE maTonKho=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maTonKho);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("TonKhoDAOImpl.delete: " + e.getMessage());
        }
        return false;
    }

    @Override
    public TonKho findById(String maTonKho) {
        String sql = "SELECT * FROM TonKho WHERE maTonKho=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maTonKho);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("TonKhoDAOImpl.findById: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<TonKho> findAll() {
        List<TonKho> list = new ArrayList<>();
        String sql = "SELECT * FROM TonKho ORDER BY maKho, maNL";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("TonKhoDAOImpl.findAll: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<TonKho> findSapHet(String maKho) {
        List<TonKho> list = new ArrayList<>();
        String sql = "SELECT * FROM TonKho WHERE maKho=? AND soLuongTon <= mucToiThieu ORDER BY soLuongTon ASC";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maKho);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("TonKhoDAOImpl.findSapHet: " + e.getMessage());
        }
        return list;
    }

    @Override
    public boolean updateSoLuong(String maTonKho, double delta) {
        String sql = "UPDATE TonKho SET soLuongTon = soLuongTon + ?, ngayCapNhat=GETDATE() WHERE maTonKho=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setDouble(1, delta);
            ps.setString(2, maTonKho);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("TonKhoDAOImpl.updateSoLuong: " + e.getMessage());
        }
        return false;
    }
}
