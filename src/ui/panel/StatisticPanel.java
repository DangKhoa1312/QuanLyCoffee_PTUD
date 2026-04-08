package ui.panel;

import controller.StatisticController;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Map;

public class StatisticPanel extends JPanel {

    private final StatisticController statController;

    private JPanel pnlChartDoanhThu;
    private JPanel pnlChartMonBanChay;

    public StatisticPanel() {
        this.statController = new StatisticController();
        initUI();
        loadCharts();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header
        JLabel lblTitle = new JLabel("BIỂU ĐỒ THỐNG KÊ");
        lblTitle.setFont(new Font("Roboto", Font.BOLD, 24));
        lblTitle.setForeground(new Color(44, 62, 80));
        add(lblTitle, BorderLayout.NORTH);

        // Grid 2 charts (1 line, 1 bar)
        JPanel grid = new JPanel(new GridLayout(1, 2, 20, 0));
        grid.setOpaque(false);
        grid.setBorder(new EmptyBorder(20, 0, 0, 0));

        pnlChartDoanhThu = new JPanel(new BorderLayout());
        pnlChartDoanhThu.setBackground(Color.WHITE);
        pnlChartDoanhThu.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));

        pnlChartMonBanChay = new JPanel(new BorderLayout());
        pnlChartMonBanChay.setBackground(Color.WHITE);
        pnlChartMonBanChay.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));

        grid.add(pnlChartDoanhThu);
        grid.add(pnlChartMonBanChay);

        add(grid, BorderLayout.CENTER);
    }

    public void loadCharts() {
        pnlChartDoanhThu.removeAll();
        pnlChartMonBanChay.removeAll();

        // 1. Chart Doanh thu
        Map<String, Double> mapDT = statController.getDoanhThu7NgayQua();
        DefaultCategoryDataset dsDT = new DefaultCategoryDataset();
        for (Map.Entry<String, Double> entry : mapDT.entrySet()) {
            dsDT.addValue(entry.getValue(), "Doanh Thu", entry.getKey());
        }

        JFreeChart chartDT = ChartFactory.createLineChart(
            "Doanh Thu 7 Ngày Qua",
            "Ngày",
            "Số tiền (VNĐ)",
            dsDT,
            PlotOrientation.VERTICAL,
            false, true, false
        );
        chartDT.getTitle().setFont(new Font("Roboto", Font.BOLD, 16));
        ChartPanel cpDT = new ChartPanel(chartDT);
        pnlChartDoanhThu.add(cpDT, BorderLayout.CENTER);

        // 2. Chart Món bán chạy
        Map<String, Integer> mapMon = statController.getTopMonBanChay(5);
        DefaultCategoryDataset dsMon = new DefaultCategoryDataset();
        for (Map.Entry<String, Integer> entry : mapMon.entrySet()) {
            dsMon.addValue(entry.getValue(), "Số lượng", entry.getKey());
        }

        JFreeChart chartMon = ChartFactory.createBarChart(
            "Top 5 Món Bán Chạy",
            "Tên Món",
            "Ly/Cốc",
            dsMon,
            PlotOrientation.VERTICAL,
            false, true, false
        );
        chartMon.getTitle().setFont(new Font("Roboto", Font.BOLD, 16));
        ChartPanel cpMon = new ChartPanel(chartMon);
        pnlChartMonBanChay.add(cpMon, BorderLayout.CENTER);

        pnlChartDoanhThu.revalidate();
        pnlChartMonBanChay.revalidate();
    }
}
