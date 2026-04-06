package dao.impl;

import connectDB.DatabaseConnection;
import dao.HoaDonDAO;
import entity.HoaDon;
import enums.HinhThucThanhToan;
import enums.LoaiDon;
import enums.TrangThaiHoaDon;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HoaDonDAOImpl implements HoaDonDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    private HoaDon mapRow(ResultSet rs) throws SQLException {
        return new HoaDon(
            rs.getString("maHD"),
            rs.getTimestamp("thoiGianXuat") != null ? rs.getTimestamp("thoiGianXuat").toLocalDateTime() : null,
            rs.getTimestamp("thoiGianThanhToan") != null ? rs.getTimestamp("thoiGianThanhToan").toLocalDateTime() : null,
            rs.getDouble("tongTienPhaiTra"),
            TrangThaiHoaDon.valueOf(rs.getString("trangThai")),
            rs.getString("hinhThucThanhToan") != null ? HinhThucThanhToan.valueOf(rs.getString("hinhThucThanhToan")) : null,
            rs.getString("maBan"),
            rs.getString("maCa"),
            rs.getString("loaiDon") != null ? LoaiDon.valueOf(rs.getString("loaiDon")) : LoaiDon.TAI_BAN,
            rs.getString("ghiChu"),
            rs.getString("maNV")
        );
    }

    @Override
    public boolean insert(HoaDon hd) {
        String sql = "INSERT INTO HoaDon(maHD, thoiGianXuat, thoiGianThanhToan, tongTienPhaiTra, trangThai, hinhThucThanhToan, maBan, maCa, loaiDon, ghiChu, maNV) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, hd.getMaHD());
            ps.setTimestamp(2, hd.getThoiGianXuat() != null ? Timestamp.valueOf(hd.getThoiGianXuat()) : null);
            ps.setTimestamp(3, hd.getThoiGianThanhToan() != null ? Timestamp.valueOf(hd.getThoiGianThanhToan()) : null);
            ps.setDouble(4, hd.getTongTienPhaiTra());
            ps.setString(5, hd.getTrangThai().name());
            ps.setString(6, hd.getHinhThucThanhToan() != null ? hd.getHinhThucThanhToan().name() : null);
            ps.setString(7, hd.getMaBan());
            ps.setString(8, hd.getMaCa());
            ps.setString(9, hd.getLoaiDon() != null ? hd.getLoaiDon().name() : "TAI_BAN");
            ps.setString(10, hd.getGhiChu());
            ps.setString(11, hd.getMaNV());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("HoaDonDAOImpl.insert: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean update(HoaDon hd) {
        String sql = "UPDATE HoaDon SET thoiGianXuat=?, thoiGianThanhToan=?, tongTienPhaiTra=?, trangThai=?, hinhThucThanhToan=?, maBan=?, maCa=?, loaiDon=?, ghiChu=?, maNV=? WHERE maHD=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setTimestamp(1, hd.getThoiGianXuat() != null ? Timestamp.valueOf(hd.getThoiGianXuat()) : null);
            ps.setTimestamp(2, hd.getThoiGianThanhToan() != null ? Timestamp.valueOf(hd.getThoiGianThanhToan()) : null);
            ps.setDouble(3, hd.getTongTienPhaiTra());
            ps.setString(4, hd.getTrangThai().name());
            ps.setString(5, hd.getHinhThucThanhToan() != null ? hd.getHinhThucThanhToan().name() : null);
            ps.setString(6, hd.getMaBan());
            ps.setString(7, hd.getMaCa());
            ps.setString(8, hd.getLoaiDon() != null ? hd.getLoaiDon().name() : "TAI_BAN");
            ps.setString(9, hd.getGhiChu());
            ps.setString(10, hd.getMaNV());
            ps.setString(11, hd.getMaHD());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("HoaDonDAOImpl.update: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean delete(String maHD) {
        String sql = "DELETE FROM HoaDon WHERE maHD=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maHD);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("HoaDonDAOImpl.delete: " + e.getMessage());
        }
        return false;
    }

    @Override
    public HoaDon findById(String maHD) {
        String sql = "SELECT * FROM HoaDon WHERE maHD=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maHD);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("HoaDonDAOImpl.findById: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<HoaDon> findAll() {
        List<HoaDon> list = new ArrayList<>();
        String sql = "SELECT * FROM HoaDon ORDER BY thoiGianXuat DESC";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("HoaDonDAOImpl.findAll: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<HoaDon> findByCa(String maCa) {
        List<HoaDon> list = new ArrayList<>();
        String sql = "SELECT * FROM HoaDon WHERE maCa=? ORDER BY thoiGianXuat DESC";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maCa);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("HoaDonDAOImpl.findByCa: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<HoaDon> findByNgay(LocalDate ngay) {
        List<HoaDon> list = new ArrayList<>();
        String sql = "SELECT * FROM HoaDon WHERE CAST(thoiGianXuat AS DATE) = ? ORDER BY thoiGianXuat DESC";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(ngay));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("HoaDonDAOImpl.findByNgay: " + e.getMessage());
        }
        return list;
    }

    @Override
    public int countByCa(String maCa) {
        String sql = "SELECT COUNT(*) FROM HoaDon WHERE maCa=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maCa);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("HoaDonDAOImpl.countByCa: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public int countHoanThanhByCa(String maCa) {
        String sql = "SELECT COUNT(*) FROM HoaDon WHERE maCa=? AND trangThai='DA_THANH_TOAN'";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maCa);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("HoaDonDAOImpl.countHoanThanhByCa: " + e.getMessage());
        }
        return 0;
    }
}
