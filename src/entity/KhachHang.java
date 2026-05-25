package entity;

import java.time.LocalDateTime;

public class KhachHang {
    private String soDienThoai;
    private String tenKhachHang;
    private int diemTichLuy;
    private LocalDateTime ngayThamGia;
    private boolean hienThi;

    public KhachHang() {}

    public KhachHang(String soDienThoai, String tenKhachHang, int diemTichLuy, LocalDateTime ngayThamGia, boolean hienThi) {
        this.soDienThoai = soDienThoai;
        this.tenKhachHang = tenKhachHang;
        this.diemTichLuy = diemTichLuy;
        this.ngayThamGia = ngayThamGia;
        this.hienThi = hienThi;
    }

    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }

    public String getTenKhachHang() { return tenKhachHang; }
    public void setTenKhachHang(String tenKhachHang) { this.tenKhachHang = tenKhachHang; }

    public int getDiemTichLuy() { return diemTichLuy; }
    public void setDiemTichLuy(int diemTichLuy) { this.diemTichLuy = diemTichLuy; }

    public LocalDateTime getNgayThamGia() { return ngayThamGia; }
    public void setNgayThamGia(LocalDateTime ngayThamGia) { this.ngayThamGia = ngayThamGia; }

    public boolean isHienThi() { return hienThi; }
    public void setHienThi(boolean hienThi) { this.hienThi = hienThi; }

    @Override
    public String toString() {
        return tenKhachHang + " (" + soDienThoai + ")";
    }
}
