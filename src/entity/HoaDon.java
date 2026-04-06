package entity;

import enums.HinhThucThanhToan;
import enums.LoaiDon;
import enums.TrangThaiHoaDon;
import java.time.LocalDateTime;

public class HoaDon {
    private String            maHD;
    private LocalDateTime     thoiGianXuat;       // thời điểm tạo hóa đơn
    private LocalDateTime     thoiGianThanhToan;   // nullable - khi khách TT xong
    private double            tongTienPhaiTra;
    private TrangThaiHoaDon   trangThai;
    private HinhThucThanhToan hinhThucThanhToan;   // nullable
    private String            maBan;               // FK Ban (null nếu mang về)
    private String            maCa;                // FK CaLamViec
    private LoaiDon           loaiDon;             // TAI_BAN hoặc MANG_VE
    private String            ghiChu;              // ghi chú đơn hàng
    private String            maNV;                // FK NhanVien (thu ngân thực hiện TT)

    public HoaDon() {}

    public HoaDon(String maHD, LocalDateTime thoiGianXuat,
                  LocalDateTime thoiGianThanhToan, double tongTienPhaiTra,
                  TrangThaiHoaDon trangThai, HinhThucThanhToan hinhThucThanhToan,
                  String maBan, String maCa, LoaiDon loaiDon, String ghiChu,
                  String maNV) {
        this.maHD               = maHD;
        this.thoiGianXuat       = thoiGianXuat;
        this.thoiGianThanhToan  = thoiGianThanhToan;
        this.tongTienPhaiTra    = tongTienPhaiTra;
        this.trangThai          = trangThai;
        this.hinhThucThanhToan  = hinhThucThanhToan;
        this.maBan              = maBan;
        this.maCa               = maCa;
        this.loaiDon            = loaiDon;
        this.ghiChu             = ghiChu;
        this.maNV               = maNV;
    }

    public String getMaHD()          { return maHD; }
    public void   setMaHD(String v)  { this.maHD = v; }

    public LocalDateTime getThoiGianXuat()               { return thoiGianXuat; }
    public void          setThoiGianXuat(LocalDateTime v){ this.thoiGianXuat = v; }

    public LocalDateTime getThoiGianThanhToan()               { return thoiGianThanhToan; }
    public void          setThoiGianThanhToan(LocalDateTime v){ this.thoiGianThanhToan = v; }

    public double getTongTienPhaiTra()          { return tongTienPhaiTra; }
    public void   setTongTienPhaiTra(double v)  { this.tongTienPhaiTra = v; }

    public TrangThaiHoaDon getTrangThai()               { return trangThai; }
    public void            setTrangThai(TrangThaiHoaDon v){ this.trangThai = v; }

    public HinhThucThanhToan getHinhThucThanhToan()               { return hinhThucThanhToan; }
    public void              setHinhThucThanhToan(HinhThucThanhToan v){ this.hinhThucThanhToan = v; }

    public String getMaBan()          { return maBan; }
    public void   setMaBan(String v)  { this.maBan = v; }

    public String getMaCa()          { return maCa; }
    public void   setMaCa(String v)  { this.maCa = v; }

    public LoaiDon getLoaiDon()          { return loaiDon; }
    public void    setLoaiDon(LoaiDon v) { this.loaiDon = v; }

    public String getGhiChu()          { return ghiChu; }
    public void   setGhiChu(String v)  { this.ghiChu = v; }

    public String getMaNV()          { return maNV; }
    public void   setMaNV(String v)  { this.maNV = v; }

    public boolean isDaThanhToan() {
        return TrangThaiHoaDon.DA_THANH_TOAN.equals(trangThai);
    }

    @Override
    public String toString() {
        return "HoaDon{" + maHD + ", ban=" + maBan
                + ", tien=" + tongTienPhaiTra
                + ", " + trangThai + ", " + hinhThucThanhToan + "}";
    }
}
