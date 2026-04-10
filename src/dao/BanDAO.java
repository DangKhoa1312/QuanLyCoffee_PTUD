package dao;

import dao.base.BaseDAO;
import entity.Ban;
import enums.TrangThaiBan;
import java.util.List;

public interface BanDAO extends BaseDAO<Ban, String> {
    List<Ban> findByKhuVuc(String maKhuVuc);
    List<Ban> findByTrangThai(TrangThaiBan trangThai);
    boolean   updateTrangThai(String maBan, TrangThaiBan trangThai);
    int       countByKhuVuc(String maKhuVuc);
    int       countTrongByKhuVuc(String maKhuVuc);
}
