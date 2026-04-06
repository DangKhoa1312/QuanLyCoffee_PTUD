package dao;

import dao.base.BaseDAO;
import entity.Mon;
import enums.LoaiMon;
import java.util.List;

public interface MonDAO extends BaseDAO<Mon, String> {
    List<Mon> findByLoai(LoaiMon loaiMon);
    List<Mon> findDangBan();
}
