package dao;

import dao.base.BaseDAO;
import entity.HoaDon;
import java.time.LocalDate;
import java.util.List;

public interface HoaDonDAO extends BaseDAO<HoaDon, String> {
    List<HoaDon> findByCa(String maCa);
    List<HoaDon> findByNgay(LocalDate ngay);
    List<HoaDon> findByFilter(LocalDate tuNgay, LocalDate denNgay,
                               String hinhThuc, String maBan, String maNV);
    int countByCa(String maCa);
    int countHoanThanhByCa(String maCa);
}
