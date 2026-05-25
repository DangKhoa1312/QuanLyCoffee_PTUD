package dao;

import entity.KhachHang;
import java.util.List;

public interface KhachHangDAO {
    List<KhachHang> findAll();
    KhachHang findById(String soDienThoai);
    boolean insert(KhachHang khachHang);
    boolean update(KhachHang khachHang);
    boolean updateDiem(String soDienThoai, int diemThayDoi); // + hoặc -
    boolean delete(String soDienThoai); // set hienThi = 0
}
