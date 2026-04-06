package dao;

import dao.base.BaseDAO;
import entity.Size;
import java.util.List;

public interface SizeDAO extends BaseDAO<Size, String> {
    List<Size> findByMon(String maMon);
}
