package entity;

import enums.TrangThaiCa;
import java.time.LocalDate;
import java.time.LocalTime;

public class CaLamViec {
    private String      maCa;
    private LocalDate   ngayLam;
    private LocalTime   gioBatDau;
    private LocalTime   gioKetThuc;   // nullable
    private double      tongDoanhThu;
    private TrangThaiCa trangThai;
    private String      maNV;         // FK NhanVien
    private String      maKhuVuc;     // FK KhuVuc

    public CaLamViec() {}

    public CaLamViec(String maCa, LocalDate ngayLam,
                     LocalTime gioBatDau, LocalTime gioKetThuc,
                     double tongDoanhThu, TrangThaiCa trangThai,
                     String maNV, String maKhuVuc) {
        this.maCa         = maCa;
        this.ngayLam      = ngayLam;
        this.gioBatDau    = gioBatDau;
        this.gioKetThuc   = gioKetThuc;
        this.tongDoanhThu = tongDoanhThu;
        this.trangThai    = trangThai;
        this.maNV         = maNV;
        this.maKhuVuc     = maKhuVuc;
    }

    public String    getMaCa()              { return maCa; }
    public void      setMaCa(String v)      { this.maCa = v; }

    public LocalDate getNgayLam()              { return ngayLam; }
    public void      setNgayLam(LocalDate v)   { this.ngayLam = v; }

    public LocalTime getGioBatDau()              { return gioBatDau; }
    public void      setGioBatDau(LocalTime v)   { this.gioBatDau = v; }

    public LocalTime getGioKetThuc()             { return gioKetThuc; }
    public void      setGioKetThuc(LocalTime v)  { this.gioKetThuc = v; }

    public double getTongDoanhThu()          { return tongDoanhThu; }
    public void   setTongDoanhThu(double v)  { this.tongDoanhThu = v; }

    public TrangThaiCa getTrangThai()               { return trangThai; }
    public void        setTrangThai(TrangThaiCa v)  { this.trangThai = v; }

    public String getMaNV()          { return maNV; }
    public void   setMaNV(String v)  { this.maNV = v; }

    public String getMaKhuVuc()          { return maKhuVuc; }
    public void   setMaKhuVuc(String v)  { this.maKhuVuc = v; }

    @Override
    public String toString() {
        return "CaLamViec{" + maCa + ", " + ngayLam + ", NV=" + maNV + ", " + trangThai + "}";
    }
}
