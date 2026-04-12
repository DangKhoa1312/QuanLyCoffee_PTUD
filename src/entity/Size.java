package entity;

public class Size implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private String  maSize;
    private String  tenSize;  // S, M, L, Thường,...
    private String  maMon;    // FK Mon
    private boolean trangThai = true; // true = size đang kinh doanh

    public Size() {}

    public Size(String maSize, String tenSize, String maMon) {
        this.maSize   = maSize;
        this.tenSize  = tenSize;
        this.maMon    = maMon;
        this.trangThai = true;
    }

    public Size(String maSize, String tenSize, String maMon, boolean trangThai) {
        this.maSize    = maSize;
        this.tenSize   = tenSize;
        this.maMon     = maMon;
        this.trangThai = trangThai;
    }

    public String getMaSize()          { return maSize; }
    public void   setMaSize(String v)  { this.maSize = v; }

    public String getTenSize()          { return tenSize; }
    public void   setTenSize(String v)  { this.tenSize = v; }

    public String getMaMon()          { return maMon; }
    public void   setMaMon(String v)  { this.maMon = v; }

    public boolean isTrangThai()           { return trangThai; }
    public void    setTrangThai(boolean v) { this.trangThai = v; }

    @Override
    public String toString() {
        return "Size{" + maSize + ", " + tenSize + ", mon=" + maMon + ", active=" + trangThai + "}";
    }
}
