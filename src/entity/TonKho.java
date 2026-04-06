package entity;

import java.time.LocalDateTime;

public class TonKho {
    private String        maTonKho;
    private double        soLuongTon;
    private double        mucToiThieu;
    private LocalDateTime ngayCapNhat;
    private String        maKho; // FK Kho
    private String        maNL;  // FK NguyenLieu

    public TonKho() {}

    public TonKho(String maTonKho, double soLuongTon, double mucToiThieu,
                  LocalDateTime ngayCapNhat, String maKho, String maNL) {
        this.maTonKho    = maTonKho;
        this.soLuongTon  = soLuongTon;
        this.mucToiThieu = mucToiThieu;
        this.ngayCapNhat = ngayCapNhat;
        this.maKho       = maKho;
        this.maNL        = maNL;
    }

    public String getMaTonKho()          { return maTonKho; }
    public void   setMaTonKho(String v)  { this.maTonKho = v; }

    public double getSoLuongTon()          { return soLuongTon; }
    public void   setSoLuongTon(double v)  { this.soLuongTon = v; }

    public double getMucToiThieu()          { return mucToiThieu; }
    public void   setMucToiThieu(double v)  { this.mucToiThieu = v; }

    public LocalDateTime getNgayCapNhat()               { return ngayCapNhat; }
    public void          setNgayCapNhat(LocalDateTime v){ this.ngayCapNhat = v; }

    public String getMaKho()          { return maKho; }
    public void   setMaKho(String v)  { this.maKho = v; }

    public String getMaNL()          { return maNL; }
    public void   setMaNL(String v)  { this.maNL = v; }

    /** Kiểm tra tồn kho có đang ở mức cảnh báo không */
    public boolean isSapHet() {
        return soLuongTon <= mucToiThieu;
    }

    @Override
    public String toString() {
        return "TonKho{" + maTonKho + ", nl=" + maNL
                + ", ton=" + soLuongTon + ", min=" + mucToiThieu + "}";
    }
}
