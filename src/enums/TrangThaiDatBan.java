package enums;

public enum TrangThaiDatBan {
    CHO_XAC_NHAN,
    DA_XAC_NHAN,
    DA_DEN,
    HET_HAN,
    DA_HUY;

    /** Trả về tên tiếng Việt để hiển thị trên UI */
    public String displayName() {
        switch (this) {
            case CHO_XAC_NHAN: return "Chờ xác nhận";
            case DA_XAC_NHAN:  return "Đã xác nhận";
            case DA_DEN:       return "Đã đến";
            case HET_HAN:      return "Hết hạn";
            case DA_HUY:       return "Đã huỷ";
            default:           return name();
        }
    }

    @Override
    public String toString() { return displayName(); }
}