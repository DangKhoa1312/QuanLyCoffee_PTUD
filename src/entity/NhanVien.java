package entity;

import enums.TrangThaiNhanVien;
import enums.VaiTro;
import java.time.LocalDate;

public class NhanVien {
    private String            maNV;
    private String            tenNV;
    private LocalDate         ngaySinh;
    private String            soDienThoai;
    private String            diaChi;
    private String            username;
    private String            passwordHash;
    private TrangThaiNhanVien trangThai;
    private VaiTro            vaiTro;

    public NhanVien() {}

    public NhanVien(String maNV, String tenNV, LocalDate ngaySinh,
                    String soDienThoai, String diaChi,
                    String username, String passwordHash,
                    TrangThaiNhanVien trangThai, VaiTro vaiTro) {
        this.maNV         = maNV;
        this.tenNV        = tenNV;
        this.ngaySinh     = ngaySinh;
        this.soDienThoai  = soDienThoai;
        this.diaChi       = diaChi;
        this.username     = username;
        this.passwordHash = passwordHash;
        this.trangThai    = trangThai;
        this.vaiTro       = vaiTro;
    }

    public String getMaNV()               { return maNV; }
    public void   setMaNV(String v)       { this.maNV = v; }

    public String getTenNV()              { return tenNV; }
    public void   setTenNV(String v)      { this.tenNV = v; }

    public LocalDate getNgaySinh()              { return ngaySinh; }
    public void      setNgaySinh(LocalDate v)   { this.ngaySinh = v; }

    public String getSoDienThoai()          { return soDienThoai; }
    public void   setSoDienThoai(String v)  { this.soDienThoai = v; }

    public String getDiaChi()          { return diaChi; }
    public void   setDiaChi(String v)  { this.diaChi = v; }

    public String getUsername()          { return username; }
    public void   setUsername(String v)  { this.username = v; }

    public String getPasswordHash()          { return passwordHash; }
    public void   setPasswordHash(String v)  { this.passwordHash = v; }

    public TrangThaiNhanVien getTrangThai()               { return trangThai; }
    public void              setTrangThai(TrangThaiNhanVien v) { this.trangThai = v; }

    public VaiTro getVaiTro()          { return vaiTro; }
    public void   setVaiTro(VaiTro v)  { this.vaiTro = v; }

    @Override
    public String toString() {
        return "NhanVien{" + maNV + ", " + tenNV + ", " + vaiTro + ", " + trangThai + "}";
    }
}
