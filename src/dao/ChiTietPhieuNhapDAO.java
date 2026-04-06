package dao;

import dao.base.BaseDAO;
import entity.ChiTietPhieuNhap;
import java.util.List;

public interface ChiTietPhieuNhapDAO extends BaseDAO<ChiTietPhieuNhap, String> {
    List<ChiTietPhieuNhap> findByPhieuNhap(String maPN);
}
