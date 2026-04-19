package dao;

import dao.base.BaseDAO;
import entity.PhieuNhap;

public interface PhieuNhapDAO extends BaseDAO<PhieuNhap, String> {
    boolean insert(java.sql.Connection conn, PhieuNhap pn);
}
