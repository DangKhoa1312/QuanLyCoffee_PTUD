package entity;

public class ChiTietPhieuXuat {
    private String maCTPX;
    private double soLuong;
    private String maPX;      // FK PhieuXuat
    private String maNL;      // FK NguyenLieu

    public ChiTietPhieuXuat() {}

    public ChiTietPhieuXuat(String maCTPX, double soLuong, String maPX, String maNL) {
        this.maCTPX  = maCTPX;
        this.soLuong = soLuong;
        this.maPX    = maPX;
        this.maNL    = maNL;
    }

    public String getMaCTPX()          { return maCTPX; }
    public void   setMaCTPX(String v)  { this.maCTPX = v; }

    public double getSoLuong()          { return soLuong; }
    public void   setSoLuong(double v)  { this.soLuong = v; }

    public String getMaPX()          { return maPX; }
    public void   setMaPX(String v)  { this.maPX = v; }

    public String getMaNL()          { return maNL; }
    public void   setMaNL(String v)  { this.maNL = v; }

    @Override
    public String toString() {
        return "ChiTietPhieuXuat{" + maCTPX + ", nl=" + maNL + ", sl=" + soLuong + "}";
    }
}
