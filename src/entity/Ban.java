package entity;

import enums.TrangThaiBan;

public class Ban {
    private String       maBan;
    private String       soBan;
    private String       maKhuVuc;  // FK KhuVuc
    private int          sucChua;
    private TrangThaiBan trangThai;

    public Ban() {}

    public Ban(String maBan, String soBan, String maKhuVuc,
               int sucChua, TrangThaiBan trangThai) {
        this.maBan    = maBan;
        this.soBan    = soBan;
        this.maKhuVuc = maKhuVuc;
        this.sucChua  = sucChua;
        this.trangThai = trangThai;
    }

    public String getMaBan()          { return maBan; }
    public void   setMaBan(String v)  { this.maBan = v; }

    public String getSoBan()          { return soBan; }
    public void   setSoBan(String v)  { this.soBan = v; }

    public String getMaKhuVuc()          { return maKhuVuc; }
    public void   setMaKhuVuc(String v)  { this.maKhuVuc = v; }

    public int  getSucChua()        { return sucChua; }
    public void setSucChua(int v)   { this.sucChua = v; }

    public TrangThaiBan getTrangThai()              { return trangThai; }
    public void         setTrangThai(TrangThaiBan v){ this.trangThai = v; }

    @Override
    public String toString() {
        return "Ban{" + maBan + ", " + soBan + ", kv=" + maKhuVuc + ", " + trangThai + "}";
    }
}
