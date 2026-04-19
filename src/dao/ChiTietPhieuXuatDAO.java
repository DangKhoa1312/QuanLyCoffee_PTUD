package dao;

import dao.base.BaseDAO;
import entity.ChiTietPhieuXuat;
import java.util.List;

public interface ChiTietPhieuXuatDAO extends BaseDAO<ChiTietPhieuXuat, String> {
    List<ChiTietPhieuXuat> findByPhieuXuat(String maPX);
    boolean insert(java.sql.Connection conn, ChiTietPhieuXuat ctpx);
}
