package controller;

import dao.NhanVienDAO;
import dao.impl.NhanVienDAOImpl;
import entity.NhanVien;
import enums.TrangThaiNhanVien;
import exception.AppException;
import utils.PasswordUtils;

import java.util.List;

/**
 * Controller quản lý nghiệp vụ Nhân viên trong hệ thống Admin.
 */
public class NhanVienController {

    private final NhanVienDAO nhanVienDAO;

    public NhanVienController() {
        this.nhanVienDAO = new NhanVienDAOImpl();
    }

    /**
     * Lấy danh sách tất cả nhân viên.
     */
    public List<NhanVien> getAllEmployees() {
        return nhanVienDAO.findAll();
    }

    public String generateNextMaNV() {
        return utils.IDGenerator.newMaNhanVien();
    }

    /**
     * Lấy danh sách nhân viên theo trạng thái.
     */
    public List<NhanVien> getEmployeesByStatus(TrangThaiNhanVien trangThai) {
        return nhanVienDAO.findByTrangThai(trangThai);
    }

    /**
     * Thêm nhân viên mới.
     * @param nv Đối tượng nhân viên với mật khẩu chưa băm.
     */
    public boolean addEmployee(NhanVien nv) {
        validateEmployee(nv);
        
        // Kiểm tra trùng mã
        if (nhanVienDAO.findById(nv.getMaNV()) != null) {
            throw new AppException("Mã nhân viên " + nv.getMaNV() + " đã tồn tại!");
        }
        
        // Kiểm tra trùng username
        if (nhanVienDAO.findByUsername(nv.getUsername()) != null) {
            throw new AppException("Tên đăng nhập " + nv.getUsername() + " đã được sử dụng!");
        }

        // Băm mật khẩu trước khi lưu
        String hashedPassword = PasswordUtils.hash(nv.getPasswordHash());
        nv.setPasswordHash(hashedPassword);

        return nhanVienDAO.insert(nv);
    }

    /**
     * Cập nhật thông tin nhân viên.
     */
    public boolean updateEmployee(NhanVien nv) {
        validateEmployee(nv);
        
        NhanVien current = nhanVienDAO.findById(nv.getMaNV());
        if (current == null) {
            throw new AppException("Không tìm thấy nhân viên mã " + nv.getMaNV());
        }

        // Nếu người dùng không nhập mật khẩu mới (để trống), giữ nguyên mật khẩu cũ
        if (nv.getPasswordHash() == null || nv.getPasswordHash().trim().isEmpty()) {
            nv.setPasswordHash(current.getPasswordHash());
        } else {
            // Nếu có nhập mật khẩu mới, tiến hành băm
            nv.setPasswordHash(PasswordUtils.hash(nv.getPasswordHash()));
        }

        return nhanVienDAO.update(nv);
    }

    /**
     * "Xóa" nhân viên bằng cách chuyển trạng thái sang NGHI_VIEC.
     */
    public boolean deactivateEmployee(String maNV) {
        return nhanVienDAO.updateTrangThai(maNV, TrangThaiNhanVien.DA_NGHI);
    }

    /**
     * Tìm kiếm nhân viên linh hoạt.
     */
    public List<NhanVien> searchEmployees(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return nhanVienDAO.findAll();
        }
        return nhanVienDAO.search(keyword.trim());
    }

    /**
     * Đổi mật khẩu cho nhân viên.
     */
    public boolean changePassword(String maNV, String newPassword) {
        NhanVien nv = nhanVienDAO.findById(maNV);
        if (nv == null) return false;

        nv.setPasswordHash(PasswordUtils.hash(newPassword));
        return nhanVienDAO.update(nv);
    }

    /**
     * Kiểm tra tính hợp lệ cơ bản của dữ liệu nhân viên.
     */
    private void validateEmployee(NhanVien nv) {
        if (nv.getTenNV() == null || nv.getTenNV().trim().isEmpty()) {
            throw new AppException("Tên nhân viên không được để trống!");
        }
        if (nv.getUsername() == null || nv.getUsername().trim().isEmpty()) {
            throw new AppException("Tên đăng nhập không được để trống!");
        }
        if (nv.getSoDienThoai() != null && !nv.getSoDienThoai().matches("^[0-9]{10,11}$")) {
            throw new AppException("Số điện thoại không hợp lệ (10-11 chữ số)!");
        }
    }
}
