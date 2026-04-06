package dao;

import dao.base.BaseDAO;
import entity.Topping;
import java.util.List;

public interface ToppingDAO extends BaseDAO<Topping, String> {
    List<Topping> findDangCungCap();
}
