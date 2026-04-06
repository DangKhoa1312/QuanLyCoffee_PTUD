package dao;

import dao.base.BaseDAO;
import entity.BangGiaChiTiet;

import java.util.List;

public interface BangGiaChiTietDAO extends BaseDAO<BangGiaChiTiet, String> {
    BangGiaChiTiet findGia(String maSize, String maBangGia);
    List<BangGiaChiTiet> findByBangGia(String maBangGia);
}
