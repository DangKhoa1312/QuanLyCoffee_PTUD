package entity;

public class NhaCungCap {
    private String maNCC;
    private String tenNCC;
    private String diaChi;
    private String soDienThoai;
    private String email;

    public NhaCungCap() {}

    public NhaCungCap(String maNCC, String tenNCC, String diaChi,
                      String soDienThoai, String email) {
        this.maNCC       = maNCC;
        this.tenNCC      = tenNCC;
        this.diaChi      = diaChi;
        this.soDienThoai = soDienThoai;
        this.email       = email;
    }

    public String getMaNCC()          { return maNCC; }
    public void   setMaNCC(String v)  { this.maNCC = v; }

    public String getTenNCC()          { return tenNCC; }
    public void   setTenNCC(String v)  { this.tenNCC = v; }

    public String getDiaChi()          { return diaChi; }
    public void   setDiaChi(String v)  { this.diaChi = v; }

    public String getSoDienThoai()          { return soDienThoai; }
    public void   setSoDienThoai(String v)  { this.soDienThoai = v; }

    public String getEmail()          { return email; }
    public void   setEmail(String v)  { this.email = v; }

    @Override
    public String toString() {
        return "NhaCungCap{" + maNCC + ", " + tenNCC + "}";
    }
}
