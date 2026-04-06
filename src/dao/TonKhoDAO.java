package dao;

import dao.base.BaseDAO;
import entity.TonKho;
import java.util.List;

public interface TonKhoDAO extends BaseDAO<TonKho, String> {
    List<TonKho> findSapHet(String maKho);
    boolean updateSoLuong(String maTonKho, double delta);
}
