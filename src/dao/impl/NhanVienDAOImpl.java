package dao.impl;

import connectDB.DatabaseConnection;
import dao.NhanVienDAO;
import entity.NhanVien;
import enums.TrangThaiNhanVien;
import enums.VaiTro;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NhanVienDAOImpl implements NhanVienDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    private NhanVien mapRow(ResultSet rs) throws SQLException {
        NhanVien nv = new NhanVien();
        nv.setMaNV(rs.getString("maNV"));
        nv.setTenNV(rs.getString("tenNV"));
        Date ngaySinh = rs.getDate("ngaySinh");
        nv.setNgaySinh(ngaySinh != null ? ngaySinh.toLocalDate() : null);
        nv.setSoDienThoai(rs.getString("soDienThoai"));
        nv.setDiaChi(rs.getString("diaChi"));
        nv.setUsername(rs.getString("username"));
        nv.setPasswordHash(rs.getString("passwordHash"));
        nv.setTrangThai(TrangThaiNhanVien.valueOf(rs.getString("trangThai")));
        nv.setVaiTro(VaiTro.valueOf(rs.getString("vaiTro")));
        return nv;
    }

    @Override
    public boolean insert(NhanVien nv) {
        String sql = "INSERT INTO NhanVien(maNV,tenNV,ngaySinh,soDienThoai,diaChi," +
                     "username,passwordHash,trangThai,vaiTro) VALUES(?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, nv.getMaNV());
            ps.setString(2, nv.getTenNV());
            ps.setDate(3, nv.getNgaySinh() != null ? Date.valueOf(nv.getNgaySinh()) : null);
            ps.setString(4, nv.getSoDienThoai());
            ps.setString(5, nv.getDiaChi());
            ps.setString(6, nv.getUsername());
            ps.setString(7, nv.getPasswordHash());
            ps.setString(8, nv.getTrangThai().name());
            ps.setString(9, nv.getVaiTro().name());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("NhanVienDAO.insert: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean update(NhanVien nv) {
        String sql = "UPDATE NhanVien SET tenNV=?,ngaySinh=?,soDienThoai=?,diaChi=?," +
                     "username=?,passwordHash=?,trangThai=?,vaiTro=? WHERE maNV=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, nv.getTenNV());
            ps.setDate(2, nv.getNgaySinh() != null ? Date.valueOf(nv.getNgaySinh()) : null);
            ps.setString(3, nv.getSoDienThoai());
            ps.setString(4, nv.getDiaChi());
            ps.setString(5, nv.getUsername());
            ps.setString(6, nv.getPasswordHash());
            ps.setString(7, nv.getTrangThai().name());
            ps.setString(8, nv.getVaiTro().name());
            ps.setString(9, nv.getMaNV());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("NhanVienDAO.update: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(String maNV) {
        String sql = "DELETE FROM NhanVien WHERE maNV=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maNV);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("NhanVienDAO.delete: " + e.getMessage());
            return false;
        }
    }

    @Override
    public NhanVien findById(String maNV) {
        String sql = "SELECT * FROM NhanVien WHERE maNV=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maNV);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("NhanVienDAO.findById: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<NhanVien> findAll() {
        List<NhanVien> list = new ArrayList<>();
        String sql = "SELECT * FROM NhanVien ORDER BY maNV";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("NhanVienDAO.findAll: " + e.getMessage());
        }
        return list;
    }

    @Override
    public NhanVien findByUsername(String username) {
        String sql = "SELECT * FROM NhanVien WHERE username=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("NhanVienDAO.findByUsername: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<NhanVien> findByTrangThai(TrangThaiNhanVien trangThai) {
        List<NhanVien> list = new ArrayList<>();
        String sql = "SELECT * FROM NhanVien WHERE trangThai=? ORDER BY tenNV";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, trangThai.name());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("NhanVienDAO.findByTrangThai: " + e.getMessage());
        }
        return list;
    }

    @Override
    public boolean updateTrangThai(String maNV, TrangThaiNhanVien trangThai) {
        String sql = "UPDATE NhanVien SET trangThai=? WHERE maNV=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, trangThai.name());
            ps.setString(2, maNV);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("NhanVienDAO.updateTrangThai: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<NhanVien> search(String keyword) {
        List<NhanVien> list = new ArrayList<>();
        String sql = "SELECT * FROM NhanVien WHERE maNV LIKE ? OR tenNV LIKE ? OR soDienThoai LIKE ? ORDER BY maNV";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("NhanVienDAO.search: " + e.getMessage());
        }
        return list;
    }
}
