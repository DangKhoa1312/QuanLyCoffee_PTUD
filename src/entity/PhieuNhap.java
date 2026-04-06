package entity;

import java.time.LocalDateTime;

public class PhieuNhap {
    private String        maPN;
    private LocalDateTime ngayNhap;
    private double        tongTien;
    private String        maNV;   // FK NhanVien
    private String        maNCC;  // FK NhaCungCap
    private String        maKho;  // FK Kho

    public PhieuNhap() {}

    public PhieuNhap(String maPN, LocalDateTime ngayNhap, double tongTien,
                     String maNV, String maNCC, String maKho) {
        this.maPN     = maPN;
        this.ngayNhap = ngayNhap;
        this.tongTien = tongTien;
        this.maNV     = maNV;
        this.maNCC    = maNCC;
        this.maKho    = maKho;
    }

    public String getMaPN()          { return maPN; }
    public void   setMaPN(String v)  { this.maPN = v; }

    public LocalDateTime getNgayNhap()               { return ngayNhap; }
    public void          setNgayNhap(LocalDateTime v){ this.ngayNhap = v; }

    public double getTongTien()          { return tongTien; }
    public void   setTongTien(double v)  { this.tongTien = v; }

    public String getMaNV()          { return maNV; }
    public void   setMaNV(String v)  { this.maNV = v; }

    public String getMaNCC()          { return maNCC; }
    public void   setMaNCC(String v)  { this.maNCC = v; }

    public String getMaKho()          { return maKho; }
    public void   setMaKho(String v)  { this.maKho = v; }

    @Override
    public String toString() {
        return "PhieuNhap{" + maPN + ", " + ngayNhap + ", total=" + tongTien + "}";
    }
}
