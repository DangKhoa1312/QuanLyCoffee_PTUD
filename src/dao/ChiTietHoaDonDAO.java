package dao;

import dao.base.BaseDAO;
import entity.ChiTietHoaDon;
import java.util.List;

public interface ChiTietHoaDonDAO extends BaseDAO<ChiTietHoaDon, String> {
    List<ChiTietHoaDon> findByHoaDon(String maHD);
    boolean deleteByHoaDon(String maHD);
}
