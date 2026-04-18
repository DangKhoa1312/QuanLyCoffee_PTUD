package enums;

public enum TrangThaiHoaDon {
    CHUA_THANH_TOAN("Chưa Thanh Toán"),
    DA_THANH_TOAN("Đã Thanh Toán");

    private final String label;
    TrangThaiHoaDon(String label) { this.label = label; }
    public String getLabel() { return label; }

    @Override
    public String toString() { return label; }
}