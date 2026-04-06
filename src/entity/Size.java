package entity;

public class Size {
    private String maSize;
    private String tenSize; // S, M, L, Thường,...
    private String maMon;   // FK Mon

    public Size() {}

    public Size(String maSize, String tenSize, String maMon) {
        this.maSize  = maSize;
        this.tenSize = tenSize;
        this.maMon   = maMon;
    }

    public String getMaSize()          { return maSize; }
    public void   setMaSize(String v)  { this.maSize = v; }

    public String getTenSize()          { return tenSize; }
    public void   setTenSize(String v)  { this.tenSize = v; }

    public String getMaMon()          { return maMon; }
    public void   setMaMon(String v)  { this.maMon = v; }

    @Override
    public String toString() {
        return "Size{" + maSize + ", " + tenSize + ", mon=" + maMon + "}";
    }
}
