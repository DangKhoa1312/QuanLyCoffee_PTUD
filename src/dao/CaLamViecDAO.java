package dao;

import dao.base.BaseDAO;
import entity.CaLamViec;
import java.time.LocalDate;
import java.util.List;

public interface CaLamViecDAO extends BaseDAO<CaLamViec, String> {
    CaLamViec      findCaHienTai(String maNV);               // Ca DANG_LAM của NV
    List<CaLamViec> findByNgay(LocalDate ngay);
    List<CaLamViec> findByNhanVien(String maNV);
    boolean         updateTongDoanhThu(String maCa, double tongDoanhThu);
    boolean         dongCa(String maCa);                      // set DA_DONG + gioKetThuc
}
