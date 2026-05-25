package dao;

import entity.CauHinh;
import java.util.List;

public interface CauHinhDAO {
    boolean insert(CauHinh cauHinh);
    boolean update(CauHinh cauHinh);
    boolean delete(String maCauHinh);
    CauHinh findById(String maCauHinh);
    List<CauHinh> findAll();
}
