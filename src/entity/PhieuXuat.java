package entity;

import java.time.LocalDateTime;

public class PhieuXuat {
    private String        maPX;
    private LocalDateTime ngayXuat;
    private String        lyDoXuat;
    private String        maNV;   // FK NhanVien
    private String        maKho;  // FK Kho

    public PhieuXuat() {}

    public PhieuXuat(String maPX, LocalDateTime ngayXuat, String lyDoXuat,
                     String maNV, String maKho) {
        this.maPX      = maPX;
        this.ngayXuat  = ngayXuat;
        this.lyDoXuat  = lyDoXuat;
        this.maNV      = maNV;
        this.maKho     = maKho;
    }

    public String getMaPX()          { return maPX; }
    public void   setMaPX(String v)  { this.maPX = v; }

    public LocalDateTime getNgayXuat()               { return ngayXuat; }
    public void          setNgayXuat(LocalDateTime v){ this.ngayXuat = v; }

    public String getLyDoXuat()          { return lyDoXuat; }
    public void   setLyDoXuat(String v)  { this.lyDoXuat = v; }

    public String getMaNV()          { return maNV; }
    public void   setMaNV(String v)  { this.maNV = v; }

    public String getMaKho()          { return maKho; }
    public void   setMaKho(String v)  { this.maKho = v; }

    @Override
    public String toString() {
        return "PhieuXuat{" + maPX + ", " + ngayXuat + ", lyDo=" + lyDoXuat + "}";
    }
}
