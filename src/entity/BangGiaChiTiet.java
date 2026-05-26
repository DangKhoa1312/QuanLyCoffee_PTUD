package entity;

public class BangGiaChiTiet {
    private String maBGCT;
    private double giaBan;
    private String maSize;    // FK Size (Có thể null)
    private String maBangGia; // FK BangGia
    private String maTopping; // FK Topping (Có thể null)

    public BangGiaChiTiet() {}

    // Constructor cho Size
    public BangGiaChiTiet(String maBGCT, double giaBan,
                          String maSize, String maBangGia) {
        this.maBGCT    = maBGCT;
        this.giaBan    = giaBan;
        this.maSize    = maSize;
        this.maBangGia = maBangGia;
        this.maTopping = null;
    }

    // Constructor đầy đủ
    public BangGiaChiTiet(String maBGCT, double giaBan, String maSize, String maBangGia, String maTopping) {
        this.maBGCT = maBGCT;
        this.giaBan = giaBan;
        this.maSize = maSize;
        this.maBangGia = maBangGia;
        this.maTopping = maTopping;
    }

    public String getMaBGCT()          { return maBGCT; }
    public void   setMaBGCT(String v)  { this.maBGCT = v; }

    public double getGiaBan()          { return giaBan; }
    public void   setGiaBan(double v)  { this.giaBan = v; }

    public String getMaSize()          { return maSize; }
    public void   setMaSize(String v)  { this.maSize = v; }

    public String getMaBangGia()          { return maBangGia; }
    public void   setMaBangGia(String v)  { this.maBangGia = v; }

    public String getMaTopping() { return maTopping; }
    public void setMaTopping(String maTopping) { this.maTopping = maTopping; }

    @Override
    public String toString() {
        return "BangGiaChiTiet{" + maBGCT + ", size=" + maSize + ", topping=" + maTopping
                + ", bangGia=" + maBangGia + ", gia=" + giaBan + "}";
    }
}
