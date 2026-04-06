package dao.impl;

import connectDB.DatabaseConnection;
import dao.CaLamViecDAO;
import entity.CaLamViec;
import enums.TrangThaiCa;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class CaLamViecDAOImpl implements CaLamViecDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    private CaLamViec mapRow(ResultSet rs) throws SQLException {
        CaLamViec ca = new CaLamViec();
        ca.setMaCa(rs.getString("maCa"));
        ca.setNgayLam(rs.getDate("ngayLam").toLocalDate());
        ca.setGioBatDau(rs.getTime("gioBatDau").toLocalTime());
        Time gioKT = rs.getTime("gioKetThuc");
        ca.setGioKetThuc(gioKT != null ? gioKT.toLocalTime() : null);
        ca.setTongDoanhThu(rs.getDouble("tongDoanhThu"));
        ca.setTrangThai(TrangThaiCa.valueOf(rs.getString("trangThai")));
        ca.setMaNV(rs.getString("maNV"));
        ca.setMaKhuVuc(rs.getString("maKhuVuc"));
        return ca;
    }

    @Override
    public boolean insert(CaLamViec ca) {
        String sql = "INSERT INTO CaLamViec(maCa,ngayLam,gioBatDau,gioKetThuc," +
                     "tongDoanhThu,trangThai,maNV,maKhuVuc) VALUES(?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, ca.getMaCa());
            ps.setDate(2, Date.valueOf(ca.getNgayLam()));
            ps.setTime(3, Time.valueOf(ca.getGioBatDau()));
            ps.setTime(4, ca.getGioKetThuc() != null ? Time.valueOf(ca.getGioKetThuc()) : null);
            ps.setDouble(5, ca.getTongDoanhThu());
            ps.setString(6, ca.getTrangThai().name());
            ps.setString(7, ca.getMaNV());
            ps.setString(8, ca.getMaKhuVuc());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("CaLamViecDAO.insert: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean update(CaLamViec ca) {
        String sql = "UPDATE CaLamViec SET ngayLam=?,gioBatDau=?,gioKetThuc=?," +
                     "tongDoanhThu=?,trangThai=?,maNV=?,maKhuVuc=? WHERE maCa=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(ca.getNgayLam()));
            ps.setTime(2, Time.valueOf(ca.getGioBatDau()));
            ps.setTime(3, ca.getGioKetThuc() != null ? Time.valueOf(ca.getGioKetThuc()) : null);
            ps.setDouble(4, ca.getTongDoanhThu());
            ps.setString(5, ca.getTrangThai().name());
            ps.setString(6, ca.getMaNV());
            ps.setString(7, ca.getMaKhuVuc());
            ps.setString(8, ca.getMaCa());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("CaLamViecDAO.update: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(String maCa) {
        String sql = "DELETE FROM CaLamViec WHERE maCa=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maCa);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("CaLamViecDAO.delete: " + e.getMessage());
            return false;
        }
    }

    @Override
    public CaLamViec findById(String maCa) {
        String sql = "SELECT * FROM CaLamViec WHERE maCa=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maCa);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("CaLamViecDAO.findById: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<CaLamViec> findAll() {
        List<CaLamViec> list = new ArrayList<>();
        String sql = "SELECT * FROM CaLamViec ORDER BY ngayLam DESC, gioBatDau DESC";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("CaLamViecDAO.findAll: " + e.getMessage());
        }
        return list;
    }

    @Override
    public CaLamViec findCaHienTai(String maNV) {
        String sql = "SELECT TOP 1 * FROM CaLamViec WHERE maNV=? AND trangThai='DANG_LAM'";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maNV);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("CaLamViecDAO.findCaHienTai: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<CaLamViec> findByNgay(LocalDate ngay) {
        List<CaLamViec> list = new ArrayList<>();
        String sql = "SELECT * FROM CaLamViec WHERE ngayLam=? ORDER BY gioBatDau";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(ngay));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("CaLamViecDAO.findByNgay: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<CaLamViec> findByNhanVien(String maNV) {
        List<CaLamViec> list = new ArrayList<>();
        String sql = "SELECT * FROM CaLamViec WHERE maNV=? ORDER BY ngayLam DESC";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maNV);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("CaLamViecDAO.findByNhanVien: " + e.getMessage());
        }
        return list;
    }

    @Override
    public boolean updateTongDoanhThu(String maCa, double tongDoanhThu) {
        String sql = "UPDATE CaLamViec SET tongDoanhThu=? WHERE maCa=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setDouble(1, tongDoanhThu);
            ps.setString(2, maCa);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("CaLamViecDAO.updateTongDoanhThu: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean dongCa(String maCa) {
        String sql = "UPDATE CaLamViec SET trangThai='DA_DONG', gioKetThuc=? WHERE maCa=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setTime(1, Time.valueOf(LocalTime.now()));
            ps.setString(2, maCa);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("CaLamViecDAO.dongCa: " + e.getMessage());
            return false;
        }
    }
}
