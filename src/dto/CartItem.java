package dto;

import entity.Mon;
import entity.Size;
import entity.Topping;

import java.util.ArrayList;
import java.util.List;

/**
 * Lớp trung gian lưu dữ liệu giỏ hàng trên bộ nhớ giao diện
 * trước khi lưu xuống Database dưới dạng ChiTietDonHang.
 */
public class CartItem implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private Mon mon;
    private Size size;
    private int soLuong;
    private double donGiaSize;
    private String ghiChu;
    private boolean daPhucVu = false;
    private List<CartTopping> toppings = new ArrayList<>();

    public static class CartTopping implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        public Topping topping;
        public int soLuong;
        public double giaTopping;

        public CartTopping(Topping topping, int soLuong, double giaTopping) {
            this.topping = topping;
            this.soLuong = soLuong;
            this.giaTopping = giaTopping;
        }
    }

    public CartItem(Mon mon, Size size, int soLuong, double donGiaSize, String ghiChu) {
        this.mon = mon;
        this.size = size;
        this.soLuong = soLuong;
        this.donGiaSize = donGiaSize;
        this.ghiChu = ghiChu == null ? "" : ghiChu;
    }

    public Mon getMon() { return mon; }
    public Size getSize() { return size; }
    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }
    public double getDonGiaSize() { return donGiaSize; }
    public String getGhiChu() { return ghiChu; }
    public boolean isDaPhucVu() { return daPhucVu; }
    public void setDaPhucVu(boolean daPhucVu) { this.daPhucVu = daPhucVu; }
    
    public List<CartTopping> getToppings() { return toppings; }

    public void addTopping(Topping topping, int soLuong, double giaTopping) {
        toppings.add(new CartTopping(topping, soLuong, giaTopping));
    }

    /** Giá của 1 đơn vị món (đã bao gồm các topping trong đó) */
    public double getDonGiaMotPhu() {
        double topPrice = 0;
        for (CartTopping ctx : toppings) {
            topPrice += (ctx.giaTopping * ctx.soLuong);
        }
        return donGiaSize + topPrice;
    }

    /** Tổng giá của mục này = Giá 1 đơn vị * Số lượng tổng */
    public double getThanhTien() {
        return getDonGiaMotPhu() * soLuong;
    }

    /** Kiểm tra hai CartItem có giống hệt nhau về món, size, trạng thái, ghi chú và toppings hay không */
    public boolean isIdentical(CartItem other) {
        if (!this.mon.getMaMon().equals(other.getMon().getMaMon())) return false;
        if (!this.size.getMaSize().equals(other.getSize().getMaSize())) return false;
        if (this.daPhucVu != other.isDaPhucVu()) return false;
        if (!this.ghiChu.equals(other.getGhiChu())) return false;
        if (this.toppings.size() != other.getToppings().size()) return false;

        // So sánh Toppings (đơn giản, giả sử topping add theo cùng thứ tự hoặc số lượng giống nhau)
        // Cách an toàn hơn:
        for (CartTopping t1 : this.toppings) {
            boolean match = false;
            for (CartTopping t2 : other.getToppings()) {
                if (t1.topping.getMaTopping().equals(t2.topping.getMaTopping()) && t1.soLuong == t2.soLuong) {
                    match = true;
                    break;
                }
            }
            if (!match) return false;
        }
        return true;
    }
}
