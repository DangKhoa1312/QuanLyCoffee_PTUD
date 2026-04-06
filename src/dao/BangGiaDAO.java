package dao;

import dao.base.BaseDAO;
import entity.BangGia;
import java.time.LocalDate;

import java.util.List;

public interface BangGiaDAO extends BaseDAO<BangGia, String> {
    BangGia findHienHanh(LocalDate ngay);
    List<BangGia> findTatCaHienHanh(LocalDate ngay);
}
