package dao;

import java.util.Map;

public interface StatisticDAO {
    /** 
     * Lấy doanh thu trong 7 ngày gần nhất (tính từ hôm nay lui về trước).
     * @return Map<Chuỗi ngày (dd/MM), Tổng doanh thu>
     */
    Map<String, Double> getDoanhThu7NgayQua();

    /** 
     * Lấy Top N món bán chạy nhất mọi thời đại (nếu cần theo khoảng thời gian thì thêm tham số).
     * @return Map<Tên món, Số lượng bán>
     */
    Map<String, Integer> getTopMonBanChay(int top);
}
