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
public class CartItem {
    private Mon mon;
    private Size size;
    private int soLuong;
    private double donGiaSize;
    private String ghiChu;
    private List<CartTopping> toppings = new ArrayList<>();

    public static class CartTopping {
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
    
    public List<CartTopping> getToppings() { return toppings; }

    public void addTopping(Topping topping, int soLuong) {
        toppings.add(new CartTopping(topping, soLuong, topping.getGiaTopping()));
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
}
