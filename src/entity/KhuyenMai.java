package entity;

import java.time.LocalDateTime;

public class KhuyenMai {
    private String maKhuyenMai;
    private String tenKhuyenMai;
    private String loaiKhuyenMai; // "PHAN_TRAM", "TIEN_MAT"
    private double giaTri;
    private double donHangToiThieu;
    private double giamToiDa;
    private LocalDateTime ngayBatDau;
    private LocalDateTime ngayKetThuc;
    private String trangThai; // "DANG_HOAT_DONG", "TAM_DUNG", "HET_HAN"

    public KhuyenMai() {}

    public KhuyenMai(String maKhuyenMai, String tenKhuyenMai, String loaiKhuyenMai, double giaTri,
                     double donHangToiThieu, double giamToiDa, LocalDateTime ngayBatDau, LocalDateTime ngayKetThuc,
                     String trangThai) {
        this.maKhuyenMai = maKhuyenMai;
        this.tenKhuyenMai = tenKhuyenMai;
        this.loaiKhuyenMai = loaiKhuyenMai;
        this.giaTri = giaTri;
        this.donHangToiThieu = donHangToiThieu;
        this.giamToiDa = giamToiDa;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
        this.trangThai = trangThai;
    }

    public String getMaKhuyenMai() { return maKhuyenMai; }
    public void setMaKhuyenMai(String maKhuyenMai) { this.maKhuyenMai = maKhuyenMai; }

    public String getTenKhuyenMai() { return tenKhuyenMai; }
    public void setTenKhuyenMai(String tenKhuyenMai) { this.tenKhuyenMai = tenKhuyenMai; }

    public String getLoaiKhuyenMai() { return loaiKhuyenMai; }
    public void setLoaiKhuyenMai(String loaiKhuyenMai) { this.loaiKhuyenMai = loaiKhuyenMai; }

    public double getGiaTri() { return giaTri; }
    public void setGiaTri(double giaTri) { this.giaTri = giaTri; }

    public double getDonHangToiThieu() { return donHangToiThieu; }
    public void setDonHangToiThieu(double donHangToiThieu) { this.donHangToiThieu = donHangToiThieu; }

    public double getGiamToiDa() { return giamToiDa; }
    public void setGiamToiDa(double giamToiDa) { this.giamToiDa = giamToiDa; }

    public LocalDateTime getNgayBatDau() { return ngayBatDau; }
    public void setNgayBatDau(LocalDateTime ngayBatDau) { this.ngayBatDau = ngayBatDau; }

    public LocalDateTime getNgayKetThuc() { return ngayKetThuc; }
    public void setNgayKetThuc(LocalDateTime ngayKetThuc) { this.ngayKetThuc = ngayKetThuc; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    /**
     * Tính toán số tiền được giảm cho một hóa đơn nhất định
     * @param tongTien Tổng tiền món của hóa đơn
     * @return Số tiền được giảm
     */
    public double tinhTienGiam(double tongTien) {
        if (!isHopLe(tongTien)) return 0;

        double tienGiam = 0;
        if ("PHAN_TRAM".equalsIgnoreCase(loaiKhuyenMai)) {
            tienGiam = tongTien * (giaTri / 100.0);
            if (giamToiDa > 0 && tienGiam > giamToiDa) {
                tienGiam = giamToiDa;
            }
        } else if ("TIEN_MAT".equalsIgnoreCase(loaiKhuyenMai)) {
            tienGiam = giaTri;
        }

        return Math.min(tienGiam, tongTien); // Không giảm quá tổng tiền
    }

    /**
     * Kiểm tra xem khuyến mãi có hợp lệ tại thời điểm hiện tại và với tổng tiền này không
     */
    public boolean isHopLe(double tongTien) {
        if (!"DANG_HOAT_DONG".equalsIgnoreCase(trangThai)) return false;
        if (tongTien < donHangToiThieu) return false;

        LocalDateTime now = LocalDateTime.now();
        if (ngayBatDau != null && now.isBefore(ngayBatDau)) return false;
        if (ngayKetThuc != null && now.isAfter(ngayKetThuc)) return false;

        return true;
    }

    @Override
    public String toString() {
        return tenKhuyenMai; // Trả về tên để hiển thị trên UI Combobox
    }
}
