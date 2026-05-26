package dao;

import dao.base.BaseDAO;
import entity.NhanVien;
import enums.TrangThaiNhanVien;
import java.util.List;

public interface NhanVienDAO extends BaseDAO<NhanVien, String> {
    NhanVien            findByUsername(String username);
    NhanVien            findBySoDienThoai(String soDienThoai);
    List<NhanVien>      findByTrangThai(TrangThaiNhanVien trangThai);
    boolean             updateTrangThai(String maNV, TrangThaiNhanVien trangThai);
    List<NhanVien>      search(String keyword);
    boolean             softDelete(String maNV);
}
