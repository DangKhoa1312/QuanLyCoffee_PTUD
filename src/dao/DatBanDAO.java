package dao;

import dao.base.BaseDAO;
import entity.DatBan;
import java.util.List;

public interface DatBanDAO extends BaseDAO<DatBan, String> {
    List<DatBan> findByBan(String maBan);
    List<DatBan> findConHieuLuc();
}
