package entity;

public class Topping {
    private String  maTopping;
    private String  tenTopping;
    private double  giaTopping;
    private boolean trangThai; // true = đang cung cấp

    public Topping() {}

    public Topping(String maTopping, String tenTopping,
                   double giaTopping, boolean trangThai) {
        this.maTopping  = maTopping;
        this.tenTopping = tenTopping;
        this.giaTopping = giaTopping;
        this.trangThai  = trangThai;
    }

    public String getMaTopping()          { return maTopping; }
    public void   setMaTopping(String v)  { this.maTopping = v; }

    public String getTenTopping()          { return tenTopping; }
    public void   setTenTopping(String v)  { this.tenTopping = v; }

    public double getGiaTopping()          { return giaTopping; }
    public void   setGiaTopping(double v)  { this.giaTopping = v; }

    public boolean isTrangThai()           { return trangThai; }
    public void    setTrangThai(boolean v) { this.trangThai = v; }

    @Override
    public String toString() {
        return "Topping{" + maTopping + ", " + tenTopping + ", gia=" + giaTopping + "}";
    }
}
