package enums;

public enum LoaiDon {
    TAI_BAN("Tại Bàn"),
    MANG_VE("Mang Về");

    private final String label;
    LoaiDon(String label) { this.label = label; }
    public String getLabel() { return label; }

    @Override
    public String toString() { return label; }
}