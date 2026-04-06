package dao;

import dao.base.BaseDAO;
import entity.ChiTietHoaDonTopping;
import java.util.List;

public interface ChiTietHoaDonToppingDAO extends BaseDAO<ChiTietHoaDonTopping, String> {
    List<ChiTietHoaDonTopping> findByCTHD(String maCTHD);
}
