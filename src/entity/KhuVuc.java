package entity;

public class KhuVuc {
    private String maKhuVuc;
    private String tenKhuVuc;
    private String moTa;
    private boolean trangThai; // true = active

    public KhuVuc() {}

    public KhuVuc(String maKhuVuc, String tenKhuVuc, String moTa, boolean trangThai) {
        this.maKhuVuc  = maKhuVuc;
        this.tenKhuVuc = tenKhuVuc;
        this.moTa      = moTa;
        this.trangThai = trangThai;
    }

    public String getMaKhuVuc()          { return maKhuVuc; }
    public void   setMaKhuVuc(String v)  { this.maKhuVuc = v; }

    public String getTenKhuVuc()          { return tenKhuVuc; }
    public void   setTenKhuVuc(String v)  { this.tenKhuVuc = v; }

    public String getMoTa()          { return moTa; }
    public void   setMoTa(String v)  { this.moTa = v; }

    public boolean isTrangThai()          { return trangThai; }
    public void    setTrangThai(boolean v){ this.trangThai = v; }

    @Override
    public String toString() {
        return "KhuVuc{" + maKhuVuc + ", " + tenKhuVuc + ", active=" + trangThai + "}";
    }
}
