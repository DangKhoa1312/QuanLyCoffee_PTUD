package dao;

import dao.base.BaseDAO;
import entity.DinhMucNguyenLieu;
import java.util.List;

public interface DinhMucNguyenLieuDAO extends BaseDAO<DinhMucNguyenLieu, String> {
    List<DinhMucNguyenLieu> findByMon(String maMon);
}
