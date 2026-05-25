package dao;

import entity.KhuyenMai;
import java.util.List;

public interface KhuyenMaiDAO {
    List<KhuyenMai> findAll();
    KhuyenMai findById(String maKhuyenMai);
    boolean insert(KhuyenMai khuyenMai);
    boolean update(KhuyenMai khuyenMai);
    boolean delete(String maKhuyenMai); // set trangThai = 'TAM_DUNG'
    List<KhuyenMai> findValidPromotions(double tongTien); // Tự động check điều kiện
}
