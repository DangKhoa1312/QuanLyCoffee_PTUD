package entity;

public class DinhMucNguyenLieu {
    private String maDinhMuc;
    private double soLuong;  // định mức tiêu hao / 1 đơn vị món (size M)
    private String maMon;    // FK Mon
    private String maNL;     // FK NguyenLieu

    public DinhMucNguyenLieu() {}

    public DinhMucNguyenLieu(String maDinhMuc, double soLuong,
                              String maMon, String maNL) {
        this.maDinhMuc = maDinhMuc;
        this.soLuong   = soLuong;
        this.maMon     = maMon;
        this.maNL      = maNL;
    }

    public String getMaDinhMuc()          { return maDinhMuc; }
    public void   setMaDinhMuc(String v)  { this.maDinhMuc = v; }

    public double getSoLuong()          { return soLuong; }
    public void   setSoLuong(double v)  { this.soLuong = v; }

    public String getMaMon()          { return maMon; }
    public void   setMaMon(String v)  { this.maMon = v; }

    public String getMaNL()          { return maNL; }
    public void   setMaNL(String v)  { this.maNL = v; }

    @Override
    public String toString() {
        return "DinhMucNguyenLieu{" + maDinhMuc + ", mon=" + maMon
                + ", nl=" + maNL + ", sl=" + soLuong + "}";
    }
}
