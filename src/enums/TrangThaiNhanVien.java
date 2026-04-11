package enums;

public enum TrangThaiNhanVien {
    DANG_LAM_VIEC("Đang làm việc"),
    DA_NGHI("Đã nghỉ việc");

    private final String tenHienThi;

    TrangThaiNhanVien(String tenHienThi) {
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