package controller;

import dao.StatisticDAO;
import dao.impl.StatisticDAOImpl;

import java.util.Map;

public class StatisticController {
    
    private final StatisticDAO statDAO;

    public StatisticController() {
        this.statDAO = new StatisticDAOImpl();
    }

    public Map<String, Double> getDoanhThu7NgayQua() {
        return statDAO.getDoanhThu7NgayQua();
    }

    public Map<String, Integer> getTopMonBanChay(int top) {
        return statDAO.getTopMonBanChay(top);
    }
}
