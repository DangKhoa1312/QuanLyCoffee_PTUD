package controller;

import dao.CaLamViecDAO;
import dao.HoaDonDAO;
import dao.KhuVucDAO;
import dao.impl.CaLamViecDAOImpl;
import dao.impl.HoaDonDAOImpl;
import dao.impl.KhuVucDAOImpl;
import entity.CaLamViec;
import entity.HoaDon;
import entity.KhuVuc;
import enums.TrangThaiCa;
import enums.TrangThaiHoaDon;
import exception.AppException;
import utils.IDGenerator;
import utils.SessionManager;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Xử lý nghiệp vụ ca làm việc: mở ca, đóng ca, kiểm tra ca hiện tại.
 * Báo cáo cuối ca dùng HoaDonDAO (không còn DonHangDAO).
 */
public class ShiftController {

    private final CaLamViecDAO caDAO;
    private final KhuVucDAO khuVucDAO;
    private final HoaDonDAO hoaDonDAO;

    public ShiftController() {
        this.caDAO = new CaLamViecDAOImpl();
        this.khuVucDAO = new KhuVucDAOImpl();
        this.hoaDonDAO = new HoaDonDAOImpl();
    }

    public List<KhuVuc> getDanhSachKhuVuc() {
        return khuVucDAO.findActive();
    }

    public CaLamViec kiemTraCaHienTai() {
        if (!SessionManager.isLoggedIn()) return null;
        CaLamViec ca = caDAO.findCaHienTai(SessionManager.getMaNVHienTai());
        SessionManager.setCurrentCa(ca);
        return ca;
    }

    /**
     * Mở ca làm việc mới.
     * @param tienDauCa số tiền mặt đầu ca
     * @param maKhuVuc khu vực phụ trách (có thể null)
     */
    public CaLamViec moCa(double tienDauCa, String maKhuVuc) {
        if (!SessionManager.isLoggedIn()) {
            throw new AppException("Chưa đăng nhập!");
        }
        if (maKhuVuc != null && maKhuVuc.trim().isEmpty()) {
            maKhuVuc = null;
        }

        String maNV = SessionManager.getMaNVHienTai();

        CaLamViec caHienTai = caDAO.findCaHienTai(maNV);
        if (caHienTai != null) {
            throw new AppException("Bạn đang có ca chưa đóng (Mã: " + caHienTai.getMaCa() + ").");
        }

        String maCa = IDGenerator.newMaCaLamViec();
        CaLamViec caMoi = new CaLamViec(
            maCa,
            LocalDate.now(),
            LocalTime.now(),
            null,
            tienDauCa,
            TrangThaiCa.DANG_LAM,
            maNV,
            maKhuVuc
        );

        boolean ok = caDAO.insert(caMoi);
        if (!ok) {
            throw new AppException("Lỗi hệ thống khi mở ca!");
        }

        SessionManager.setCurrentCa(caMoi);
        return caMoi;
    }

    public void dongCa(double tienThucTe) {
        CaLamViec ca = SessionManager.getCurrentCa();
        if (ca == null || !TrangThaiCa.DANG_LAM.equals(ca.getTrangThai())) {
            throw new AppException("Không có ca nào đang mở để đóng.");
        }

        caDAO.updateTongDoanhThu(ca.getMaCa(), tienThucTe);
        boolean ok = caDAO.dongCa(ca.getMaCa());
        if (!ok) {
            throw new AppException("Lỗi hệ thống khi đóng ca!");
        }

        SessionManager.setCurrentCa(null);
    }

    // ── Báo cáo cuối ca ── (dùng HoaDonDAO thay vì DonHangDAO)

    /** Đếm số hoá đơn hoàn thành trong ca */
    public int getSoDonHoanThanh(String maCa) {
        return hoaDonDAO.countHoanThanhByCa(maCa);
    }

    /** Tính tổng doanh thu thực tế (từ hoá đơn đã thanh toán trong ca) */
    public double getTongDoanhThuCa(String maCa) {
        List<HoaDon> dsHD = hoaDonDAO.findByCa(maCa);
        double total = 0;
        for (HoaDon hd : dsHD) {
            if (TrangThaiHoaDon.DA_THANH_TOAN.equals(hd.getTrangThai())) {
                total += hd.getTongTienPhaiTra();
            }
        }
        return total;
    }

    /** Đếm tổng số hoá đơn trong ca */
    public int getTongSoDon(String maCa) {
        return hoaDonDAO.countByCa(maCa);
    }
}