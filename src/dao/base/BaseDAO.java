package dao.base;

import java.util.List;

/**
 * Interface generic CRUD cho tat ca DAO
 * T: kieu entity, ID: kieu khoa chinh
 */
public interface BaseDAO<T, ID> {
    boolean insert(T entity);
    boolean update(T entity);
    boolean delete(ID id);
    T       findById(ID id);
    List<T> findAll();
}
