package entity;

public class ChiTietPhieuNhap {
    private String maCTPN;
    private double soLuong;
    private double donGia;
    private double thanhTien; // soLuong * donGia
    private String maPN;      // FK PhieuNhap
    private String maNL;      // FK NguyenLieu

    public ChiTietPhieuNhap() {}

    public ChiTietPhieuNhap(String maCTPN, double soLuong, double donGia,
                             double thanhTien, String maPN, String maNL) {
        this.maCTPN    = maCTPN;
        this.soLuong   = soLuong;
        this.donGia    = donGia;
        this.thanhTien = thanhTien;
        this.maPN      = maPN;
        this.maNL      = maNL;
    }

    public String getMaCTPN()          { return maCTPN; }
    public void   setMaCTPN(String v)  { this.maCTPN = v; }

    public double getSoLuong()          { return soLuong; }
    public void   setSoLuong(double v)  { this.soLuong = v; }

    public double getDonGia()          { return donGia; }
    public void   setDonGia(double v)  { this.donGia = v; }

    public double getThanhTien()          { return thanhTien; }
    public void   setThanhTien(double v)  { this.thanhTien = v; }

    public String getMaPN()          { return maPN; }
    public void   setMaPN(String v)  { this.maPN = v; }

    public String getMaNL()          { return maNL; }
    public void   setMaNL(String v)  { this.maNL = v; }

    /** Tính lại thanhTien từ soLuong * donGia */
    public void tinhThanhTien() {
        this.thanhTien = this.soLuong * this.donGia;
    }

    @Override
    public String toString() {
        return "ChiTietPhieuNhap{" + maCTPN + ", nl=" + maNL
                + ", sl=" + soLuong + ", tien=" + thanhTien + "}";
    }
}
