package entity;

import enums.LoaiMon;

public class Mon {
    private String  maMon;
    private String  tenMon;
    private String  moTa;
    private String  hinhAnh;
    private LoaiMon loaiMon;
    private boolean trangThai; // true = đang bán

    public Mon() {}

    public Mon(String maMon, String tenMon, String moTa,
               String hinhAnh, LoaiMon loaiMon, boolean trangThai) {
        this.maMon    = maMon;
        this.tenMon   = tenMon;
        this.moTa     = moTa;
        this.hinhAnh  = hinhAnh;
        this.loaiMon  = loaiMon;
        this.trangThai = trangThai;
    }

    public String getMaMon()          { return maMon; }
    public void   setMaMon(String v)  { this.maMon = v; }

    public String getTenMon()          { return tenMon; }
    public void   setTenMon(String v)  { this.tenMon = v; }

    public String getMoTa()          { return moTa; }
    public void   setMoTa(String v)  { this.moTa = v; }

    public String getHinhAnh()          { return hinhAnh; }
    public void   setHinhAnh(String v)  { this.hinhAnh = v; }

    public LoaiMon getLoaiMon()          { return loaiMon; }
    public void    setLoaiMon(LoaiMon v) { this.loaiMon = v; }

    public boolean isTrangThai()          { return trangThai; }
    public void    setTrangThai(boolean v){ this.trangThai = v; }

    @Override
    public String toString() {
        return "Mon{" + maMon + ", " + tenMon + ", " + loaiMon + "}";
    }
}
