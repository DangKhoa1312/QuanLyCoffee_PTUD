package enums;

public enum LoaiMon {
    COFFEE("Coffee"),
    COLD_BREW("Cold Brew"),
    MATCHA_CACAO("Matcha - Cacao"),
    TRA("Trà"),
    TRA_SUA("Trà Sữa"),
    DA_XAY("Đá Xay"),
    NUOC_EP("Nước Ép"),
    SODA("Soda"),
    YAOURT("Yaourt"),
    DO_AN_NHE("Đồ Ăn Nhẹ");

    private final String tenLoai;

    LoaiMon(String tenLoai) {
        this.tenLoai = tenLoai;
    }

    public String getTenLoai() {
        return tenLoai;
    }
}
