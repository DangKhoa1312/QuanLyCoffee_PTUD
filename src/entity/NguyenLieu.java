package entity;

import java.time.LocalDate;

public class NguyenLieu {
    private String    maNL;
    private String    tenNL;
    private String    donViTinh;
    private double    donGiaNhap;
    private LocalDate ngayHetHan; // nullable
    private String    loaiNL;     // "Chính" hoặc "Phụ"

    public NguyenLieu() {
        this.loaiNL = "Chính";
    }

    public NguyenLieu(String maNL, String tenNL, String donViTinh,
                      double donGiaNhap, LocalDate ngayHetHan, String loaiNL) {
        this.maNL       = maNL;
        this.tenNL      = tenNL;
        this.donViTinh  = donViTinh;
        this.donGiaNhap = donGiaNhap;
        this.ngayHetHan = ngayHetHan;
        this.loaiNL     = loaiNL != null ? loaiNL : "Chính";
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

    public String getLoaiNL()          { return loaiNL; }
    public void   setLoaiNL(String v)  { this.loaiNL = v; }

    @Override
    public String toString() {
        return "NguyenLieu{" + maNL + ", " + tenNL + ", " + donViTinh + ", " + loaiNL + "}";
    }
}
