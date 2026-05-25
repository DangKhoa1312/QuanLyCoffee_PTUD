package entity;

public class CauHinh {
    private String maCauHinh;
    private String tenCauHinh;
    private String giaTri;
    private String kieuDuLieu;
    private String moTa;

    public CauHinh() {
    }

    public CauHinh(String maCauHinh, String tenCauHinh, String giaTri, String kieuDuLieu, String moTa) {
        this.maCauHinh = maCauHinh;
        this.tenCauHinh = tenCauHinh;
        this.giaTri = giaTri;
        this.kieuDuLieu = kieuDuLieu;
        this.moTa = moTa;
    }

    public String getMaCauHinh() {
        return maCauHinh;
    }

    public void setMaCauHinh(String maCauHinh) {
        this.maCauHinh = maCauHinh;
    }

    public String getTenCauHinh() {
        return tenCauHinh;
    }

    public void setTenCauHinh(String tenCauHinh) {
        this.tenCauHinh = tenCauHinh;
    }

    public String getGiaTri() {
        return giaTri;
    }

    public void setGiaTri(String giaTri) {
        this.giaTri = giaTri;
    }

    public String getKieuDuLieu() {
        return kieuDuLieu;
    }

    public void setKieuDuLieu(String kieuDuLieu) {
        this.kieuDuLieu = kieuDuLieu;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    @Override
    public String toString() {
        return "CauHinh{" + "maCauHinh=" + maCauHinh + ", giaTri=" + giaTri + '}';
    }
}
