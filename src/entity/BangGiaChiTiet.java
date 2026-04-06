package entity;

public class BangGiaChiTiet {
    private String maBGCT;
    private double giaBan;
    private String maSize;    // FK Size
    private String maBangGia; // FK BangGia

    public BangGiaChiTiet() {}

    public BangGiaChiTiet(String maBGCT, double giaBan,
                          String maSize, String maBangGia) {
        this.maBGCT    = maBGCT;
        this.giaBan    = giaBan;
        this.maSize    = maSize;
        this.maBangGia = maBangGia;
    }

    public String getMaBGCT()          { return maBGCT; }
    public void   setMaBGCT(String v)  { this.maBGCT = v; }

    public double getGiaBan()          { return giaBan; }
    public void   setGiaBan(double v)  { this.giaBan = v; }

    public String getMaSize()          { return maSize; }
    public void   setMaSize(String v)  { this.maSize = v; }

    public String getMaBangGia()          { return maBangGia; }
    public void   setMaBangGia(String v)  { this.maBangGia = v; }

    @Override
    public String toString() {
        return "BangGiaChiTiet{" + maBGCT + ", size=" + maSize
                + ", bangGia=" + maBangGia + ", gia=" + giaBan + "}";
    }
}
