package dao.impl;

import connectDB.DatabaseConnection;
import dao.KhachHangDAO;
import entity.KhachHang;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class KhachHangDAOImpl implements KhachHangDAO {

    @Override
    public List<KhachHang> findAll() {
        List<KhachHang> list = new ArrayList<>();
        String sql = "SELECT * FROM KhachHang WHERE hienThi = 1 ORDER BY ngayThamGia DESC";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public KhachHang findById(String soDienThoai) {
        String sql = "SELECT * FROM KhachHang WHERE soDienThoai = ? AND hienThi = 1";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, soDienThoai);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean insert(KhachHang khachHang) {
        String sql = "INSERT INTO KhachHang (soDienThoai, tenKhachHang, diemTichLuy, ngayThamGia, hienThi) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, khachHang.getSoDienThoai());
            ps.setString(2, khachHang.getTenKhachHang());
            ps.setInt(3, khachHang.getDiemTichLuy());
            ps.setTimestamp(4, Timestamp.valueOf(khachHang.getNgayThamGia() != null ? khachHang.getNgayThamGia() : LocalDateTime.now()));
            ps.setBoolean(5, khachHang.isHienThi());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(KhachHang khachHang) {
        String sql = "UPDATE KhachHang SET tenKhachHang = ?, diemTichLuy = ?, hienThi = ? WHERE soDienThoai = ?";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, khachHang.getTenKhachHang());
            ps.setInt(2, khachHang.getDiemTichLuy());
            ps.setBoolean(3, khachHang.isHienThi());
            ps.setString(4, khachHang.getSoDienThoai());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateDiem(String soDienThoai, int diemThayDoi) {
        String sql = "UPDATE KhachHang SET diemTichLuy = diemTichLuy + ? WHERE soDienThoai = ?";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, diemThayDoi);
            ps.setString(2, soDienThoai);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(String soDienThoai) {
        String sql = "UPDATE KhachHang SET hienThi = 0 WHERE soDienThoai = ?";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, soDienThoai);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private KhachHang mapRow(ResultSet rs) throws SQLException {
        KhachHang kh = new KhachHang();
        kh.setSoDienThoai(rs.getString("soDienThoai"));
        kh.setTenKhachHang(rs.getString("tenKhachHang"));
        kh.setDiemTichLuy(rs.getInt("diemTichLuy"));

        Timestamp ts = rs.getTimestamp("ngayThamGia");
        if (ts != null) {
            kh.setNgayThamGia(ts.toLocalDateTime());
        }

        kh.setHienThi(rs.getBoolean("hienThi"));
        return kh;
    }
}
