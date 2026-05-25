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
        if (!utils.SessionManager.isQuanLy()) {
            throw new AppException("Bạn không có quyền thêm nhân viên!");
        }
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
        if (!utils.SessionManager.isQuanLy()) {
            throw new AppException("Bạn không có quyền sửa nhân viên!");
        }
        validateEmployee(nv);
        
        NhanVien current = nhanVienDAO.findById(nv.getMaNV());
        if (current == null) {
            throw new AppException("Không tìm thấy nhân viên mã " + nv.getMaNV());
        }

        // --- BẢO VỆ TÀI KHOẢN ROOT (KIM BÀI MIỄN TỬ) ---
        if ("admin".equals(current.getUsername())) {
            String loggedInUsername = utils.SessionManager.getCurrentUser() != null ? utils.SessionManager.getCurrentUser().getUsername() : "";
            // Người khác không được phép sửa Root
            if (!"admin".equals(loggedInUsername)) {
                throw new AppException("Tài khoản 'admin' là tài khoản tối cao (Root), không thể bị tác động bởi người khác!");
            }
            // Root tự sửa thông tin của mình: Không cho phép tự đổi username, tự hạ quyền hoặc tự khóa
            if (!"admin".equals(nv.getUsername())) {
                throw new AppException("Không thể thay đổi tên đăng nhập của tài khoản Root!");
            }
            if (!enums.VaiTro.ADMIN.equals(nv.getVaiTro())) {
                throw new AppException("Không thể hạ quyền của tài khoản Root!");
            }
            if (!enums.TrangThaiNhanVien.DANG_LAM_VIEC.equals(nv.getTrangThai())) {
                throw new AppException("Không thể thay đổi trạng thái của tài khoản Root!");
            }
        }
        // -------------------------------------------------

        // Quản lý không được sửa tài khoản ADMIN
        if (enums.VaiTro.ADMIN.equals(current.getVaiTro()) && !utils.SessionManager.isAdmin()) {
            throw new AppException("Bạn không có quyền sửa tài khoản Quản trị viên (ADMIN)!");
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
        if (!utils.SessionManager.isQuanLy()) {
            throw new AppException("Bạn không có quyền vô hiệu hóa nhân viên!");
        }

        NhanVien current = nhanVienDAO.findById(maNV);
        
        // --- BẢO VỆ TÀI KHOẢN ROOT (KIM BÀI MIỄN TỬ) ---
        if (current != null && "admin".equals(current.getUsername())) {
            throw new AppException("Tài khoản 'admin' là tài khoản tối cao (Root), không thể bị khóa!");
        }
        // -------------------------------------------------

        if (current != null && enums.VaiTro.ADMIN.equals(current.getVaiTro())) {
            throw new AppException("Không thể khóa tài khoản Quản trị viên (ADMIN)!");
        }

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
     * Tự cập nhật hồ sơ cá nhân (không yêu cầu quyền Quản lý).
     * Chỉ cho phép đổi: Tên, SĐT, Địa chỉ, Ngày sinh, Mật khẩu.
     */
    public boolean updateSelfProfile(NhanVien nv) {
        validateEmployee(nv);
        NhanVien current = nhanVienDAO.findById(nv.getMaNV());
        if (current == null) {
            throw new AppException("Không tìm thấy nhân viên mã " + nv.getMaNV());
        }

        // Nếu có nhập mật khẩu mới, tiến hành băm
        if (nv.getPasswordHash() != null && !nv.getPasswordHash().trim().isEmpty()) {
            nv.setPasswordHash(PasswordUtils.hash(nv.getPasswordHash()));
        } else {
            nv.setPasswordHash(current.getPasswordHash());
        }

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
