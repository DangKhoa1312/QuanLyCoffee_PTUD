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
        Connection conn = DatabaseConnection.getInstance().getConnection();
        if (conn == null) {
            System.err.println("NhanVienDAOImpl: Khong the lay ket noi Database!");
        }
        return conn;
    }

    private NhanVien mapRow(ResultSet rs) throws SQLException {
        NhanVien nv = new NhanVien();
        nv.setMaNV(rs.getString("maNV"));
        nv.setTenNV(rs.getString("tenNV"));
        
        Date ngaySinh = rs.getDate("ngaySinh");
        if (ngaySinh != null) {
            nv.setNgaySinh(ngaySinh.toLocalDate());
        }
        
        nv.setSoDienThoai(rs.getString("soDienThoai"));
        nv.setDiaChi(rs.getString("diaChi"));
        nv.setUsername(rs.getString("username"));
        nv.setPasswordHash(rs.getString("passwordHash"));
        
        try {
            String trangThaiStr = rs.getString("trangThai");
            if (trangThaiStr != null) {
                nv.setTrangThai(TrangThaiNhanVien.valueOf(trangThaiStr));
            }
        } catch (IllegalArgumentException e) {
            nv.setTrangThai(TrangThaiNhanVien.DANG_LAM_VIEC); // Default
        }

        try {
            String vaiTroStr = rs.getString("vaiTro");
            if (vaiTroStr != null) {
                nv.setVaiTro(VaiTro.valueOf(vaiTroStr));
            }
        } catch (IllegalArgumentException e) {
            nv.setVaiTro(VaiTro.NHAN_VIEN); // Default
        }

        try {
            nv.setDeleted(rs.getBoolean("isDeleted"));
        } catch (SQLException e) {
            // Ignored if column doesn't exist
        }
        
        return nv;
    }

    @Override
    public boolean insert(NhanVien nv) {
        String sql = "INSERT INTO NhanVien(maNV,tenNV,ngaySinh,soDienThoai,diaChi," +
                     "username,passwordHash,trangThai,vaiTro,isDeleted) VALUES(?,?,?,?,?,?,?,?,?,?)";
        Connection conn = getConn();
        if (conn == null) return false;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nv.getMaNV());
            ps.setString(2, nv.getTenNV());
            ps.setDate(3, nv.getNgaySinh() != null ? Date.valueOf(nv.getNgaySinh()) : null);
            ps.setString(4, nv.getSoDienThoai());
            ps.setString(5, nv.getDiaChi());
            ps.setString(6, nv.getUsername());
            ps.setString(7, nv.getPasswordHash());
            ps.setString(8, nv.getTrangThai() != null ? nv.getTrangThai().name() : TrangThaiNhanVien.DANG_LAM_VIEC.name());
            ps.setString(9, nv.getVaiTro() != null ? nv.getVaiTro().name() : VaiTro.NHAN_VIEN.name());
            ps.setBoolean(10, nv.isDeleted());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("NhanVienDAO.insert: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean update(NhanVien nv) {
        String sql = "UPDATE NhanVien SET tenNV=?,ngaySinh=?,soDienThoai=?,diaChi=?," +
                     "username=?,passwordHash=?,trangThai=?,vaiTro=?,isDeleted=? WHERE maNV=?";
        Connection conn = getConn();
        if (conn == null) return false;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nv.getTenNV());
            ps.setDate(2, nv.getNgaySinh() != null ? Date.valueOf(nv.getNgaySinh()) : null);
            ps.setString(3, nv.getSoDienThoai());
            ps.setString(4, nv.getDiaChi());
            ps.setString(5, nv.getUsername());
            ps.setString(6, nv.getPasswordHash());
            ps.setString(7, nv.getTrangThai() != null ? nv.getTrangThai().name() : TrangThaiNhanVien.DANG_LAM_VIEC.name());
            ps.setString(8, nv.getVaiTro() != null ? nv.getVaiTro().name() : VaiTro.NHAN_VIEN.name());
            ps.setBoolean(9, nv.isDeleted());
            ps.setString(10, nv.getMaNV());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("NhanVienDAO.update: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(String maNV) {
        String sql = "DELETE FROM NhanVien WHERE maNV=?";
        Connection conn = getConn();
        if (conn == null) return false;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNV);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("NhanVienDAO.delete: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean softDelete(String maNV) {
        String sql = "UPDATE NhanVien SET isDeleted=1 WHERE maNV=?";
        Connection conn = getConn();
        if (conn == null) return false;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNV);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("NhanVienDAO.softDelete: " + e.getMessage());
            return false;
        }
    }

    @Override
    public NhanVien findById(String maNV) {
        String sql = "SELECT * FROM NhanVien WHERE maNV=? AND isDeleted=0";
        Connection conn = getConn();
        if (conn == null) return null;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
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
        String sql = "SELECT * FROM NhanVien WHERE isDeleted=0 ORDER BY maNV";
        Connection conn = getConn();
        if (conn == null) return list;
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("NhanVienDAO.findAll: " + e.getMessage());
        }
        return list;
    }

    @Override
    public NhanVien findByUsername(String username) {
        String sql = "SELECT * FROM NhanVien WHERE username=? AND isDeleted=0";
        Connection conn = getConn();
        if (conn == null) return null;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("NhanVienDAO.findByUsername: " + e.getMessage());
        }
        return null;
    }

    @Override
    public NhanVien findBySoDienThoai(String soDienThoai) {
        String sql = "SELECT * FROM NhanVien WHERE soDienThoai=? AND isDeleted=0";
        Connection conn = getConn();
        if (conn == null) return null;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, soDienThoai);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("NhanVienDAO.findBySoDienThoai: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<NhanVien> findByTrangThai(TrangThaiNhanVien trangThai) {
        List<NhanVien> list = new ArrayList<>();
        String sql = "SELECT * FROM NhanVien WHERE trangThai=? AND isDeleted=0 ORDER BY tenNV";
        Connection conn = getConn();
        if (conn == null) return list;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trangThai != null ? trangThai.name() : "");
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
        Connection conn = getConn();
        if (conn == null) return false;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trangThai != null ? trangThai.name() : "");
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
        String sql = "SELECT * FROM NhanVien WHERE (maNV LIKE ? OR tenNV LIKE ? OR soDienThoai LIKE ?) AND isDeleted=0 ORDER BY maNV";
        Connection conn = getConn();
        if (conn == null) return list;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            String pattern = "%" + (keyword != null ? keyword : "") + "%";
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
