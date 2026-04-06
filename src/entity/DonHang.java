package entity;

import enums.LoaiDon;
import enums.TrangThaiDonHang;
import java.time.LocalDateTime;

public class DonHang {
    private String           maDonHang;
    private LocalDateTime    thoiGianMo;
    private LocalDateTime    thoiGianChot;     // nullable - khi chốt order
    private double           tongTienTamTinh;
    private String           ghiChu;
    private TrangThaiDonHang trangThai;
    private LoaiDon          loaiDon;
    private String           maBan;      // FK Ban (nullable nếu mang về)
    private String           maDatBan;   // FK DatBan (nullable nếu khách vãng lai)
    private String           maCa;       // FK CaLamViec
    private String           maNV;       // FK NhanVien

    public DonHang() {}

    public DonHang(String maDonHang, LocalDateTime thoiGianMo,
                   LocalDateTime thoiGianChot, double tongTienTamTinh,
                   String ghiChu, TrangThaiDonHang trangThai, LoaiDon loaiDon,
                   String maBan, String maDatBan, String maCa, String maNV) {
        this.maDonHang       = maDonHang;
        this.thoiGianMo      = thoiGianMo;
        this.thoiGianChot    = thoiGianChot;
        this.tongTienTamTinh = tongTienTamTinh;
        this.ghiChu          = ghiChu;
        this.trangThai       = trangThai;
        this.loaiDon         = loaiDon;
        this.maBan           = maBan;
        this.maDatBan        = maDatBan;
        this.maCa            = maCa;
        this.maNV            = maNV;
    }

    public String getMaDonHang()          { return maDonHang; }
    public void   setMaDonHang(String v)  { this.maDonHang = v; }

    public LocalDateTime getThoiGianMo()               { return thoiGianMo; }
    public void          setThoiGianMo(LocalDateTime v){ this.thoiGianMo = v; }

    public LocalDateTime getThoiGianChot()               { return thoiGianChot; }
    public void          setThoiGianChot(LocalDateTime v){ this.thoiGianChot = v; }

    public double getTongTienTamTinh()          { return tongTienTamTinh; }
    public void   setTongTienTamTinh(double v)  { this.tongTienTamTinh = v; }

    public String getGhiChu()          { return ghiChu; }
    public void   setGhiChu(String v)  { this.ghiChu = v; }

    public TrangThaiDonHang getTrangThai()               { return trangThai; }
    public void             setTrangThai(TrangThaiDonHang v){ this.trangThai = v; }

    public LoaiDon getLoaiDon()          { return loaiDon; }
    public void    setLoaiDon(LoaiDon v) { this.loaiDon = v; }

    public String getMaBan()          { return maBan; }
    public void   setMaBan(String v)  { this.maBan = v; }

    public String getMaDatBan()          { return maDatBan; }
    public void   setMaDatBan(String v)  { this.maDatBan = v; }

    public String getMaCa()          { return maCa; }
    public void   setMaCa(String v)  { this.maCa = v; }

    public String getMaNV()          { return maNV; }
    public void   setMaNV(String v)  { this.maNV = v; }

    @Override
    public String toString() {
        return "DonHang{" + maDonHang + ", ban=" + maBan
                + ", " + loaiDon + ", " + trangThai
                + ", tamTinh=" + tongTienTamTinh + "}";
    }
}
