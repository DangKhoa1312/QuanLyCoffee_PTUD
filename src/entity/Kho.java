package entity;

public class Kho {
    private String maKho;
    private String tenKho;
    private String diaChi;
    private String maNV;   // FK NhanVien (nullable - quản lý kho)

    public Kho() {}

    public Kho(String maKho, String tenKho, String diaChi, String maNV) {
        this.maKho  = maKho;
        this.tenKho = tenKho;
        this.diaChi = diaChi;
        this.maNV   = maNV;
    }

    public String getMaKho()          { return maKho; }
    public void   setMaKho(String v)  { this.maKho = v; }

    public String getTenKho()          { return tenKho; }
    public void   setTenKho(String v)  { this.tenKho = v; }

    public String getDiaChi()          { return diaChi; }
    public void   setDiaChi(String v)  { this.diaChi = v; }

    public String getMaNV()          { return maNV; }
    public void   setMaNV(String v)  { this.maNV = v; }

    @Override
    public String toString() {
        return "Kho{" + maKho + ", " + tenKho + ", quanLy=" + maNV + "}";
    }
}
