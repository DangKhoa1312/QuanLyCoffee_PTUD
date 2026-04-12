package entity;

import java.time.LocalDate;

public class BangGia {
    private String    maBangGia;
    private String    tenBangGia;
    private LocalDate ngayBatDau;
    private LocalDate ngayKetThuc;
    private boolean   trangThai; // true = đang áp dụng (theo thời gian)
    private boolean   hoatDong;   // true = chưa bị xóa (Soft Delete)

    public BangGia() {
        this.hoatDong = true;
    }

    public BangGia(String maBangGia, String tenBangGia,
                   LocalDate ngayBatDau, LocalDate ngayKetThuc,
                   boolean trangThai, boolean hoatDong) {
        this.maBangGia   = maBangGia;
        this.tenBangGia  = tenBangGia;
        this.ngayBatDau  = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
        this.trangThai   = trangThai;
        this.hoatDong    = hoatDong;
    }

    public String getMaBangGia()          { return maBangGia; }
    public void   setMaBangGia(String v)  { this.maBangGia = v; }

    public String getTenBangGia()          { return tenBangGia; }
    public void   setTenBangGia(String v)  { this.tenBangGia = v; }

    public LocalDate getNgayBatDau()              { return ngayBatDau; }
    public void      setNgayBatDau(LocalDate v)   { this.ngayBatDau = v; }

    public LocalDate getNgayKetThuc()             { return ngayKetThuc; }
    public void      setNgayKetThuc(LocalDate v)  { this.ngayKetThuc = v; }

    public boolean isTrangThai()           { return trangThai; }
    public void    setTrangThai(boolean v) { this.trangThai = v; }

    public boolean isHoatDong()           { return hoatDong; }
    public void    setHoatDong(boolean v) { this.hoatDong = v; }

    @Override
    public String toString() {
        return "BangGia{" + maBangGia + ", " + tenBangGia
                + ", " + ngayBatDau + " -> " + ngayKetThuc + "}";
    }
}
