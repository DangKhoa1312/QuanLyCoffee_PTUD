package dao.impl;

import connectDB.DatabaseConnection;
import dao.KhuyenMaiDAO;
import entity.KhuyenMai;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class KhuyenMaiDAOImpl implements KhuyenMaiDAO {

    @Override
    public List<KhuyenMai> findAll() {
        List<KhuyenMai> list = new ArrayList<>();
        String sql = "SELECT * FROM KhuyenMai ORDER BY ngayBatDau DESC";
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
    public KhuyenMai findById(String maKhuyenMai) {
        String sql = "SELECT * FROM KhuyenMai WHERE maKhuyenMai = ?";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maKhuyenMai);
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
    public boolean insert(KhuyenMai km) {
        String sql = "INSERT INTO KhuyenMai (maKhuyenMai, tenKhuyenMai, loaiKhuyenMai, giaTri, donHangToiThieu, giamToiDa, ngayBatDau, ngayKetThuc, trangThai) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, km.getMaKhuyenMai());
            ps.setString(2, km.getTenKhuyenMai());
            ps.setString(3, km.getLoaiKhuyenMai());
            ps.setDouble(4, km.getGiaTri());
            ps.setDouble(5, km.getDonHangToiThieu());
            ps.setDouble(6, km.getGiamToiDa());

            ps.setTimestamp(7, km.getNgayBatDau() != null ? Timestamp.valueOf(km.getNgayBatDau()) : null);
            ps.setTimestamp(8, km.getNgayKetThuc() != null ? Timestamp.valueOf(km.getNgayKetThuc()) : null);
            ps.setString(9, km.getTrangThai());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(KhuyenMai km) {
        String sql = "UPDATE KhuyenMai SET tenKhuyenMai = ?, loaiKhuyenMai = ?, giaTri = ?, donHangToiThieu = ?, giamToiDa = ?, ngayBatDau = ?, ngayKetThuc = ?, trangThai = ? WHERE maKhuyenMai = ?";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, km.getTenKhuyenMai());
            ps.setString(2, km.getLoaiKhuyenMai());
            ps.setDouble(3, km.getGiaTri());
            ps.setDouble(4, km.getDonHangToiThieu());
            ps.setDouble(5, km.getGiamToiDa());

            ps.setTimestamp(6, km.getNgayBatDau() != null ? Timestamp.valueOf(km.getNgayBatDau()) : null);
            ps.setTimestamp(7, km.getNgayKetThuc() != null ? Timestamp.valueOf(km.getNgayKetThuc()) : null);
            ps.setString(8, km.getTrangThai());
            ps.setString(9, km.getMaKhuyenMai());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(String maKhuyenMai) {
        // Chỉ đổi trạng thái sang TAM_DUNG để giữ lịch sử
        String sql = "UPDATE KhuyenMai SET trangThai = 'TAM_DUNG' WHERE maKhuyenMai = ?";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maKhuyenMai);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<KhuyenMai> findValidPromotions(double tongTien) {
        List<KhuyenMai> list = new ArrayList<>();
        String sql = "SELECT * FROM KhuyenMai WHERE trangThai = 'DANG_HOAT_DONG' " +
                     "AND donHangToiThieu <= ? " +
                     "AND (ngayBatDau IS NULL OR ngayBatDau <= GETDATE()) " +
                     "AND (ngayKetThuc IS NULL OR ngayKetThuc >= GETDATE())";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDouble(1, tongTien);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private KhuyenMai mapRow(ResultSet rs) throws SQLException {
        KhuyenMai km = new KhuyenMai();
        km.setMaKhuyenMai(rs.getString("maKhuyenMai"));
        km.setTenKhuyenMai(rs.getString("tenKhuyenMai"));
        km.setLoaiKhuyenMai(rs.getString("loaiKhuyenMai"));
        km.setGiaTri(rs.getDouble("giaTri"));
        km.setDonHangToiThieu(rs.getDouble("donHangToiThieu"));
        km.setGiamToiDa(rs.getDouble("giamToiDa"));

        Timestamp tsStart = rs.getTimestamp("ngayBatDau");
        if (tsStart != null) km.setNgayBatDau(tsStart.toLocalDateTime());

        Timestamp tsEnd = rs.getTimestamp("ngayKetThuc");
        if (tsEnd != null) km.setNgayKetThuc(tsEnd.toLocalDateTime());

        km.setTrangThai(rs.getString("trangThai"));
        return km;
    }
}
