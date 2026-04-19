package entity;

import java.time.LocalDate;

public class NguyenLieu {
    private String    maNL;
    private String    tenNL;
    private String    donViTinh;
    private double    donGiaNhap;
    private LocalDate ngayHetHan;     // nullable
    private String    donViDongGoi;   // "Hộp", "Bịch", "Chai", ...
    private double    khoiLuongDongGoi; // KL mỗi đơn vị đóng gói (vd: 1 Hộp = 500g → 500)

    public NguyenLieu() {}

    public NguyenLieu(String maNL, String tenNL, String donViTinh,
                      double donGiaNhap, LocalDate ngayHetHan, String donViDongGoi,
                      double khoiLuongDongGoi) {
        this.maNL              = maNL;
        this.tenNL             = tenNL;
        this.donViTinh         = donViTinh;
        this.donGiaNhap        = donGiaNhap;
        this.ngayHetHan        = ngayHetHan;
        this.donViDongGoi      = donViDongGoi;
        this.khoiLuongDongGoi  = khoiLuongDongGoi;
    }

    public String getMaNL()          { return maNL; }
    public void   setMaNL(String v)  { this.maNL = v; }

    public String getTenNL()          { return tenNL; }
    public void   setTenNL(String v)  { this.tenNL = v; }

    public String getDonViTinh()          { return donViTinh; }
    public void   setDonViTinh(String v)  { this.donViTinh = v; }

    public double getDonGiaNhap()          { return donGiaNhap; }
    public void   setDonGiaNhap(double v)  { this.donGiaNhap = v; }

    public LocalDate getNgayHetHan()             { return ngayHetHan; }
    public void      setNgayHetHan(LocalDate v)  { this.ngayHetHan = v; }

    public String getDonViDongGoi()          { return donViDongGoi; }
    public void   setDonViDongGoi(String v)  { this.donViDongGoi = v; }

    public double getKhoiLuongDongGoi()          { return khoiLuongDongGoi; }
    public void   setKhoiLuongDongGoi(double v)  { this.khoiLuongDongGoi = v; }

    // Giữ lại getLoaiNL/setLoaiNL để tương thích ngược (trả về null)
    public String getLoaiNL()          { return null; }
    public void   setLoaiNL(String v)  { /* không dùng nữa */ }

    @Override
    public String toString() {
        return "NguyenLieu{" + maNL + ", " + tenNL + ", " + donViTinh + ", dongGoi=" + donViDongGoi + "}";
    }
}
