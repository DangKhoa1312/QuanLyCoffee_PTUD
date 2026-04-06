package entity;

public class ChiTietHoaDonTopping {
    private String maID;
    private int    soLuong;
    private double giaTopping; // giá snapshot tại thời điểm thanh toán
    private String maCTHD;     // FK ChiTietHoaDon
    private String maTopping;  // FK Topping

    public ChiTietHoaDonTopping() {}

    public ChiTietHoaDonTopping(String maID, int soLuong, double giaTopping,
                                 String maCTHD, String maTopping) {
        this.maID       = maID;
        this.soLuong    = soLuong;
        this.giaTopping = giaTopping;
        this.maCTHD     = maCTHD;
        this.maTopping  = maTopping;
    }

    public String getMaID()          { return maID; }
    public void   setMaID(String v)  { this.maID = v; }

    public int  getSoLuong()       { return soLuong; }
    public void setSoLuong(int v)  { this.soLuong = v; }

    public double getGiaTopping()          { return giaTopping; }
    public void   setGiaTopping(double v)  { this.giaTopping = v; }

    public String getMaCTHD()          { return maCTHD; }
    public void   setMaCTHD(String v)  { this.maCTHD = v; }

    public String getMaTopping()          { return maTopping; }
    public void   setMaTopping(String v)  { this.maTopping = v; }

    public double getTongGiaTopping() {
        return soLuong * giaTopping;
    }

    @Override
    public String toString() {
        return "ChiTietHoaDonTopping{" + maID + ", topping=" + maTopping
                + ", sl=" + soLuong + ", gia=" + giaTopping + "}";
    }
}
