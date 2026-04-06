package entity;

public class ChiTietHoaDon {
    private String maCTHD;
    private int    soLuong;
    private double donGia;     // giá tại thời điểm thanh toán (snapshot)
    private double thanhTien;  // soLuong * donGia
    private String ghiChu;     // ghi chú pha chế: đường, đá,...
    private String maHD;       // FK HoaDon
    private String maMon;      // FK Mon
    private String maSize;     // FK Size

    public ChiTietHoaDon() {}

    public ChiTietHoaDon(String maCTHD, int soLuong, double donGia,
                          double thanhTien, String ghiChu,
                          String maHD, String maMon, String maSize) {
        this.maCTHD    = maCTHD;
        this.soLuong   = soLuong;
        this.donGia    = donGia;
        this.thanhTien = thanhTien;
        this.ghiChu    = ghiChu;
        this.maHD      = maHD;
        this.maMon     = maMon;
        this.maSize    = maSize;
    }

    public String getMaCTHD()          { return maCTHD; }
    public void   setMaCTHD(String v)  { this.maCTHD = v; }

    public int  getSoLuong()       { return soLuong; }
    public void setSoLuong(int v)  { this.soLuong = v; }

    public double getDonGia()          { return donGia; }
    public void   setDonGia(double v)  { this.donGia = v; }

    public double getThanhTien()          { return thanhTien; }
    public void   setThanhTien(double v)  { this.thanhTien = v; }

    public String getGhiChu()          { return ghiChu; }
    public void   setGhiChu(String v)  { this.ghiChu = v; }

    public String getMaHD()          { return maHD; }
    public void   setMaHD(String v)  { this.maHD = v; }

    public String getMaMon()          { return maMon; }
    public void   setMaMon(String v)  { this.maMon = v; }

    public String getMaSize()          { return maSize; }
    public void   setMaSize(String v)  { this.maSize = v; }

    public void tinhThanhTien() {
        this.thanhTien = this.soLuong * this.donGia;
    }

    @Override
    public String toString() {
        return "ChiTietHoaDon{" + maCTHD + ", mon=" + maMon
                + ", size=" + maSize + ", sl=" + soLuong
                + ", tien=" + thanhTien + "}";
    }
}
