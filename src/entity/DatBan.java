package entity;

import enums.TrangThaiDatBan;
import java.time.LocalDateTime;

public class DatBan {
    private String          maDatBan;
    private String          tenKhach;
    private String          soDienThoai;
    private int             soLuongNguoi;
    private TrangThaiDatBan trangThai;
    private LocalDateTime   thoiGianDen;
    private LocalDateTime   thoiGianDat;
    private String          maBan;       // FK Ban
    private String          maHD;        // FK HoaDon (nullable - gán sau khi khách thanh toán)

    public DatBan() {}

    public DatBan(String maDatBan, String tenKhach, String soDienThoai,
                  int soLuongNguoi, TrangThaiDatBan trangThai,
                  LocalDateTime thoiGianDen, LocalDateTime thoiGianDat,
                  String maBan, String maHD) {
        this.maDatBan     = maDatBan;
        this.tenKhach     = tenKhach;
        this.soDienThoai  = soDienThoai;
        this.soLuongNguoi = soLuongNguoi;
        this.trangThai    = trangThai;
        this.thoiGianDen  = thoiGianDen;
        this.thoiGianDat  = thoiGianDat;
        this.maBan        = maBan;
        this.maHD         = maHD;
    }

    public String getMaDatBan()          { return maDatBan; }
    public void   setMaDatBan(String v)  { this.maDatBan = v; }

    public String getTenKhach()          { return tenKhach; }
    public void   setTenKhach(String v)  { this.tenKhach = v; }

    public String getSoDienThoai()          { return soDienThoai; }
    public void   setSoDienThoai(String v)  { this.soDienThoai = v; }

    public int  getSoLuongNguoi()       { return soLuongNguoi; }
    public void setSoLuongNguoi(int v)  { this.soLuongNguoi = v; }

    public TrangThaiDatBan getTrangThai()               { return trangThai; }
    public void            setTrangThai(TrangThaiDatBan v){ this.trangThai = v; }

    public LocalDateTime getThoiGianDen()               { return thoiGianDen; }
    public void          setThoiGianDen(LocalDateTime v){ this.thoiGianDen = v; }

    public LocalDateTime getThoiGianDat()               { return thoiGianDat; }
    public void          setThoiGianDat(LocalDateTime v){ this.thoiGianDat = v; }

    public String getMaBan()          { return maBan; }
    public void   setMaBan(String v)  { this.maBan = v; }

    public String getMaHD()          { return maHD; }
    public void   setMaHD(String v)  { this.maHD = v; }

    /** Kiểm tra đơn đặt bàn có bị quá hạn không (quá 15 phút so với giờ hẹn) */
    public boolean isQuaHan() {
        return LocalDateTime.now().isAfter(thoiGianDen.plusMinutes(15));
    }

    @Override
    public String toString() {
        return "DatBan{" + maDatBan + ", " + tenKhach + ", ban=" + maBan
                + ", den=" + thoiGianDen + ", " + trangThai + "}";
    }
}
