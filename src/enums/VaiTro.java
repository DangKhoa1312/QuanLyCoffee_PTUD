package enums;

public enum VaiTro {
    ADMIN("Quản Trị Viên"),
    QUAN_LY("Quản Lý"),
    NHAN_VIEN("Nhân Viên");

    private final String tenHienThi;

    VaiTro(String tenHienThi) {
        this.tenHienThi = tenHienThi;
    }

    public String getTenHienThi() {
        return tenHienThi;
    }

    @Override
    public String toString() {
        return tenHienThi;
    }
}