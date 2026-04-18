package enums;

public enum HinhThucThanhToan {
    TIEN_MAT("Tiền Mặt"),
    CHUYEN_KHOAN("Chuyển Khoản");

    private final String label;
    HinhThucThanhToan(String label) { this.label = label; }
    public String getLabel() { return label; }

    @Override
    public String toString() { return label; }
}