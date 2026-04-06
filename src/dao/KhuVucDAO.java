package dao;

import dao.base.BaseDAO;
import entity.KhuVuc;
import java.util.List;

public interface KhuVucDAO extends BaseDAO<KhuVuc, String> {
    List<KhuVuc> findActive(); // trangThai = true
}
