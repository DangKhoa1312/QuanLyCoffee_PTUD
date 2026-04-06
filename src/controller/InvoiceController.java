package controller;

import dao.ChiTietHoaDonDAO;
import dao.ChiTietHoaDonToppingDAO;
import dao.HoaDonDAO;
import dao.impl.ChiTietHoaDonDAOImpl;
import dao.impl.ChiTietHoaDonToppingDAOImpl;
import dao.impl.HoaDonDAOImpl;
import dto.CartItem;
import entity.ChiTietHoaDon;
import entity.ChiTietHoaDonTopping;
import entity.HoaDon;

import java.util.ArrayList;
import java.util.List;

public class InvoiceController {

    private final HoaDonDAO hoaDonDAO;
    private final ChiTietHoaDonDAO ctHoaDonDAO;
    private final ChiTietHoaDonToppingDAO ctToppingDAO;
    private final MenuController menuController;

    public InvoiceController() {
        this.hoaDonDAO = new HoaDonDAOImpl();
        this.ctHoaDonDAO = new ChiTietHoaDonDAOImpl();
        this.ctToppingDAO = new ChiTietHoaDonToppingDAOImpl();
        this.menuController = new MenuController();
    }

    public List<HoaDon> getAllHoaDon() {
        return hoaDonDAO.findAll();
    }

    /**
     * Lấy danh sách chi tiết hóa đơn dưới dạng CartItem
     * (để hiển thị và in hóa đơn).
     */
    public List<CartItem> getChiTietHoaDon(String maHD) {
        List<CartItem> cart = new ArrayList<>();
        List<ChiTietHoaDon> cthdList = ctHoaDonDAO.findByHoaDon(maHD);

        for (ChiTietHoaDon ct : cthdList) {
            CartItem item = new CartItem(
                menuController.getMonById(ct.getMaMon()),
                menuController.getSizeById(ct.getMaSize()),
                ct.getSoLuong(),
                ct.getDonGia(),
                ct.getGhiChu()
            );

            // Load toppings
            List<ChiTietHoaDonTopping> topList = ctToppingDAO.findByCTHD(ct.getMaCTHD());
            for (ChiTietHoaDonTopping top : topList) {
                item.addTopping(menuController.getToppingById(top.getMaTopping()), top.getSoLuong());
                // Cập nhật giá topping theo snapshot lưu trong DB
                item.getToppings().get(item.getToppings().size() - 1).giaTopping = top.getGiaTopping();
            }
            cart.add(item);
        }
        return cart;
    }
}
