package ui.panel;

import com.toedter.calendar.JDateChooser;
import controller.StatisticController;
import jiconfont.icons.FontAwesome;
import jiconfont.swing.IconFontSwing;
import org.jfree.chart.*;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import utils.CurrencyUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.List;

/**
 * Panel Thống Kê – full-screen layout, không scroll.
 * Header + Filter | 4 KPI Cards | 4 Charts (2×2) lấp đầy màn hình.
 */
public class StatisticPanel extends JPanel {

    private final StatisticController statController;

    // ── Design tokens (đồng bộ DashboardPanel) ──────────────────
    private static final Color BG          = new Color(248, 250, 252);
    private static final Color CARD_BG     = new Color(255, 255, 255);
    private static final Color TEXT_MAIN   = new Color(15, 23, 42);
    private static final Color TEXT_SUB    = new Color(100, 116, 139);

    private static final Color P_GREEN_BG  = new Color(220, 252, 231);
    private static final Color P_GREEN_FG  = new Color(22, 163, 74);
    private static final Color P_BLUE_BG   = new Color(219, 234, 254);
    private static final Color P_BLUE_FG   = new Color(37, 99, 235);
    private static final Color P_PURPLE_BG = new Color(243, 232, 255);
    private static final Color P_PURPLE_FG = new Color(147, 51, 234);
    private static final Color P_ORANGE_BG = new Color(255, 237, 213);
    private static final Color P_ORANGE_FG = new Color(234, 88, 12);

    // ── JFreeChart palette ──────────────────────────────────────
    private static final Color CHART_LINE  = new Color(59, 130, 246);
    private static final Color CHART_BAR   = new Color(99, 102, 241);
    private static final Color[] PIE_COLORS = {
        new Color(59, 130, 246), new Color(16, 185, 129), new Color(245, 158, 11),
        new Color(239, 68, 68), new Color(139, 92, 246), new Color(236, 72, 153),
        new Color(20, 184, 166), new Color(249, 115, 22), new Color(99, 102, 241),
        new Color(107, 114, 128)
    };

    // ── UI components ───────────────────────────────────────────
    private JComboBox<String> cboRange;
    private JPanel pnlCustomDate;
    private JDateChooser dateChooserFrom, dateChooserTo;
    private JLabel lblRevenue, lblInvoices, lblAvgOrder, lblTopItem;

    private JPanel pnlChartDoanhThu, pnlChartTopMon, pnlChartLoaiMon, pnlChartGio;

    private LocalDate dateFrom, dateTo;

    // ═══════════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════════
    public StatisticPanel() {
        this.statController = new StatisticController();
        int days = utils.AppConfig.getInstance().getInt("THOI_GIAN_THONG_KE", 7);
        this.dateFrom = LocalDate.now().minusDays(days - 1);
        this.dateTo = LocalDate.now();

        setLayout(new BorderLayout(0, 0));
        setBackground(BG);
        setBorder(new EmptyBorder(16, 24, 16, 24));

        // ── TOP: Header + KPI ───────────────────────────────────
        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.setOpaque(false);
        topSection.add(buildHeader());
        topSection.add(Box.createRigidArea(new Dimension(0, 14)));
        topSection.add(buildKpiRow());
        topSection.add(Box.createRigidArea(new Dimension(0, 14)));
        add(topSection, BorderLayout.NORTH);

        // ── CENTER: 4 charts (2×2) – fills ALL remaining space ──
        add(buildChartsGrid(), BorderLayout.CENTER);

        loadCharts();
    }

    // ═══════════════════════════════════════════════════════════════
    // HEADER + FILTER
    // ═══════════════════════════════════════════════════════════════
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout(12, 0));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        // Title
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        JLabel ico = new JLabel(IconFontSwing.buildIcon(FontAwesome.BAR_CHART, 20, P_BLUE_FG));
        JLabel lblTitle = new JLabel("  Thống Kê Doanh Thu");
        lblTitle.setFont(new Font("Roboto", Font.BOLD, 22));
        lblTitle.setForeground(TEXT_MAIN);
        left.add(ico);
        left.add(lblTitle);
        p.add(left, BorderLayout.WEST);

        // Filter
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));
        filterPanel.setOpaque(false);

        int days = utils.AppConfig.getInstance().getInt("THOI_GIAN_THONG_KE", 7);
        cboRange = new JComboBox<>(new String[]{
            days + " ngày qua", "30 ngày qua", "Tháng này", "Tháng trước", "Tùy chỉnh"
        });
        cboRange.setFont(new Font("Roboto", Font.PLAIN, 13));
        cboRange.setPreferredSize(new Dimension(130, 30));
        cboRange.addActionListener(e -> onRangeChanged());

        pnlCustomDate = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        pnlCustomDate.setOpaque(false);
        pnlCustomDate.setVisible(false);

        dateChooserFrom = new JDateChooser();
        dateChooserFrom.setDate(java.sql.Date.valueOf(dateFrom));
        dateChooserFrom.setDateFormatString("dd/MM/yyyy");
        dateChooserFrom.setFont(new Font("Roboto", Font.PLAIN, 12));
        dateChooserFrom.setPreferredSize(new Dimension(110, 28));

        dateChooserTo = new JDateChooser();
        dateChooserTo.setDate(java.sql.Date.valueOf(dateTo));
        dateChooserTo.setDateFormatString("dd/MM/yyyy");
        dateChooserTo.setFont(new Font("Roboto", Font.PLAIN, 12));
        dateChooserTo.setPreferredSize(new Dimension(110, 28));

        JPanel btnApply = createSmallBtn("Áp Dụng", P_BLUE_FG, () -> {
            updateDateRange();
            loadCharts();
        });

        pnlCustomDate.add(smallLabel("Từ:"));
        pnlCustomDate.add(dateChooserFrom);
        pnlCustomDate.add(smallLabel("→"));
        pnlCustomDate.add(smallLabel("Đến:"));
        pnlCustomDate.add(dateChooserTo);
        pnlCustomDate.add(btnApply);

        JPanel btnRefresh = createSmallBtn("Làm Mới", TEXT_SUB, () -> {
            cboRange.setSelectedIndex(0); // Will trigger onRangeChanged
            dateChooserFrom.setDate(java.sql.Date.valueOf(dateFrom));
            dateChooserTo.setDate(java.sql.Date.valueOf(dateTo));
        });

        filterPanel.add(cboRange);
        filterPanel.add(pnlCustomDate);
        filterPanel.add(btnRefresh);
        p.add(filterPanel, BorderLayout.EAST);

        return p;
    }

    // ═══════════════════════════════════════════════════════════════
    // KPI CARDS (compact)
    // ═══════════════════════════════════════════════════════════════
    private JPanel buildKpiRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 12, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        row.setPreferredSize(new Dimension(0, 80));

        lblRevenue  = kpiValueLabel();
        lblInvoices = kpiValueLabel();
        lblAvgOrder = kpiValueLabel();
        lblTopItem  = kpiValueLabel();
        lblTopItem.setFont(new Font("Roboto", Font.BOLD, 16));

        row.add(buildKpiCard("Tổng doanh thu",   lblRevenue,  FontAwesome.USD,        P_GREEN_BG,  P_GREEN_FG));
        row.add(buildKpiCard("Số hóa đơn",       lblInvoices, FontAwesome.FILE_TEXT_O, P_BLUE_BG,   P_BLUE_FG));
        row.add(buildKpiCard("Trung bình / đơn",  lblAvgOrder, FontAwesome.LINE_CHART,  P_PURPLE_BG, P_PURPLE_FG));
        row.add(buildKpiCard("Món bán chạy nhất", lblTopItem,  FontAwesome.TROPHY,     P_ORANGE_BG, P_ORANGE_FG));

        return row;
    }

    // ═══════════════════════════════════════════════════════════════
    // CHARTS GRID (2×2) – fills CENTER
    // ═══════════════════════════════════════════════════════════════
    private JPanel buildChartsGrid() {
        JPanel grid = new JPanel(new GridLayout(2, 2, 12, 12));
        grid.setOpaque(false);

        pnlChartDoanhThu = createChartCard();
        pnlChartTopMon   = createChartCard();
        pnlChartLoaiMon  = createChartCard();
        pnlChartGio      = createChartCard();

        grid.add(pnlChartDoanhThu);
        grid.add(pnlChartTopMon);
        grid.add(pnlChartLoaiMon);
        grid.add(pnlChartGio);

        return grid;
    }

    // ═══════════════════════════════════════════════════════════════
    // LOAD DATA (async)
    // ═══════════════════════════════════════════════════════════════
    public void loadCharts() {
        lblRevenue.setText("...");
        lblInvoices.setText("...");
        lblAvgOrder.setText("...");
        lblTopItem.setText("...");

        new SwingWorker<Void, Void>() {
            double revenue;
            int invoiceCount;
            String topItemName = "—";
            Map<String, Double> mapDT;
            Map<String, Integer> mapMon;
            Map<String, Double> mapLoai;
            Map<Integer, Integer> mapGioDon;

            @Override
            protected Void doInBackground() {
                revenue = statController.getTongDoanhThu(dateFrom, dateTo);
                invoiceCount = statController.getSoHoaDon(dateFrom, dateTo);
                mapDT   = statController.getDoanhThuTheoNgay(dateFrom, dateTo);
                mapMon  = statController.getTopMonBanChay(10, dateFrom, dateTo);
                mapLoai = statController.getDoanhThuTheoLoaiMon(dateFrom, dateTo);
                mapGioDon = statController.getSoDonTheoGio(dateFrom, dateTo);

                if (!mapMon.isEmpty()) topItemName = mapMon.keySet().iterator().next();
                return null;
            }

            @Override
            protected void done() {
                // ── KPI ──
                lblRevenue.setText(CurrencyUtils.format(revenue));
                lblInvoices.setText(String.valueOf(invoiceCount));
                lblAvgOrder.setText(invoiceCount > 0
                    ? CurrencyUtils.format(revenue / invoiceCount) : "0 đ");
                lblTopItem.setText(topItemName);

                // ── Charts ──
                buildLineChart(mapDT);
                buildBarChartMon(mapMon);
                buildPieChart(mapLoai);
                buildPeakHoursPanel(mapGioDon);
            }
        }.execute();
    }

    // ═══════════════════════════════════════════════════════════════
    // CHART BUILDERS
    // ═══════════════════════════════════════════════════════════════

    private void buildLineChart(Map<String, Double> data) {
        pnlChartDoanhThu.removeAll();

        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        for (Map.Entry<String, Double> e : data.entrySet()) {
            ds.addValue(e.getValue(), "Doanh Thu", e.getKey());
        }

        JFreeChart chart = ChartFactory.createLineChart(
            "Doanh Thu Theo Ngày", null, null, ds,
            PlotOrientation.VERTICAL, false, true, false
        );
        styleChart(chart);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setRangeGridlinePaint(new Color(230, 230, 230));
        plot.setDomainGridlinesVisible(false);

        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, CHART_LINE);
        renderer.setSeriesStroke(0, new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        renderer.setSeriesShapesVisible(0, true);
        renderer.setSeriesShape(0, new java.awt.geom.Ellipse2D.Double(-3, -3, 6, 6));

        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        domainAxis.setTickLabelFont(new Font("Roboto", Font.PLAIN, 10));

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setTickLabelFont(new Font("Roboto", Font.PLAIN, 10));
        rangeAxis.setNumberFormatOverride(NumberFormat.getInstance(new Locale("vi", "VN")));

        ChartPanel cp = new ChartPanel(chart);
        cp.setMinimumDrawWidth(0);
        cp.setMinimumDrawHeight(0);
        cp.setMaximumDrawWidth(Integer.MAX_VALUE);
        cp.setMaximumDrawHeight(Integer.MAX_VALUE);
        pnlChartDoanhThu.add(cp, BorderLayout.CENTER);
        pnlChartDoanhThu.revalidate();
        pnlChartDoanhThu.repaint();
    }

    private void buildBarChartMon(Map<String, Integer> data) {
        pnlChartTopMon.removeAll();

        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        for (Map.Entry<String, Integer> e : data.entrySet()) {
            ds.addValue(e.getValue(), "Số lượng", e.getKey());
        }

        JFreeChart chart = ChartFactory.createBarChart(
            "Top Món Bán Chạy", null, "Số lượng", ds,
            PlotOrientation.VERTICAL, false, true, false
        );
        styleChart(chart);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setRangeGridlinePaint(new Color(230, 230, 230));
        plot.setDomainGridlinesVisible(false);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, CHART_BAR);
        renderer.setBarPainter(new org.jfree.chart.renderer.category.StandardBarPainter());
        renderer.setShadowVisible(false);
        renderer.setMaximumBarWidth(0.06);

        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        domainAxis.setTickLabelFont(new Font("Roboto", Font.PLAIN, 9));
        domainAxis.setMaximumCategoryLabelWidthRatio(0.8f);

        ChartPanel cp = new ChartPanel(chart);
        cp.setMinimumDrawWidth(0);
        cp.setMinimumDrawHeight(0);
        cp.setMaximumDrawWidth(Integer.MAX_VALUE);
        cp.setMaximumDrawHeight(Integer.MAX_VALUE);
        pnlChartTopMon.add(cp, BorderLayout.CENTER);
        pnlChartTopMon.revalidate();
        pnlChartTopMon.repaint();
    }

    @SuppressWarnings("unchecked")
    private void buildPieChart(Map<String, Double> data) {
        pnlChartLoaiMon.removeAll();

        DefaultPieDataset ds = new DefaultPieDataset();
        for (Map.Entry<String, Double> e : data.entrySet()) {
            ds.setValue(e.getKey(), e.getValue());
        }

        JFreeChart chart = ChartFactory.createPieChart(
            "Doanh Thu Theo Loại Món", ds, true, true, false
        );
        styleChart(chart);

        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(CARD_BG);
        plot.setOutlineVisible(false);
        plot.setShadowPaint(null);
        plot.setLabelFont(new Font("Roboto", Font.PLAIN, 10));
        plot.setLabelBackgroundPaint(new Color(255, 255, 255, 200));
        plot.setLabelOutlinePaint(null);
        plot.setLabelShadowPaint(null);
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}: {2}"));

        int i = 0;
        for (String key : data.keySet()) {
            plot.setSectionPaint(key, PIE_COLORS[i % PIE_COLORS.length]);
            i++;
        }

        LegendTitle legend = chart.getLegend();
        if (legend != null) {
            legend.setItemFont(new Font("Roboto", Font.PLAIN, 10));
            legend.setBackgroundPaint(CARD_BG);
            legend.setFrame(org.jfree.chart.block.BlockBorder.NONE);
        }

        ChartPanel cp = new ChartPanel(chart);
        cp.setMinimumDrawWidth(0);
        cp.setMinimumDrawHeight(0);
        cp.setMaximumDrawWidth(Integer.MAX_VALUE);
        cp.setMaximumDrawHeight(Integer.MAX_VALUE);
        pnlChartLoaiMon.add(cp, BorderLayout.CENTER);
        pnlChartLoaiMon.revalidate();
        pnlChartLoaiMon.repaint();
    }

    private void buildPeakHoursPanel(Map<Integer, Integer> data) {
        pnlChartGio.removeAll();
        pnlChartGio.setLayout(new BorderLayout());

        JLabel lblTitle = new JLabel("Thống Kê Giờ Cao Điểm");
        lblTitle.setFont(new Font("Roboto", Font.BOLD, 14));
        lblTitle.setForeground(TEXT_MAIN);
        lblTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
        pnlChartGio.add(lblTitle, BorderLayout.NORTH);

        JPanel pnlList = new JPanel();
        pnlList.setLayout(new BoxLayout(pnlList, BoxLayout.Y_AXIS));
        pnlList.setOpaque(false);

        // Sắp xếp map theo số đơn giảm dần
        List<Map.Entry<Integer, Integer>> list = new ArrayList<>(data.entrySet());
        list.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

        int rank = 1;
        for (Map.Entry<Integer, Integer> entry : list) {
            JPanel item = new JPanel(new BorderLayout());
            item.setOpaque(false);
            item.setBorder(new EmptyBorder(6, 4, 6, 4));
            
            JLabel lblHour = new JLabel(String.format("Hạng %d: %02dh - %02dh", rank, entry.getKey(), entry.getKey() + 1));
            lblHour.setFont(new Font("Roboto", Font.PLAIN, 13));
            lblHour.setForeground(TEXT_MAIN);
            
            JLabel lblCount = new JLabel(entry.getValue() + " đơn");
            lblCount.setFont(new Font("Roboto", Font.BOLD, 13));
            lblCount.setForeground(P_GREEN_FG);
            
            item.add(lblHour, BorderLayout.WEST);
            item.add(lblCount, BorderLayout.EAST);
            
            pnlList.add(item);
            pnlList.add(new JSeparator());
            
            rank++;
            if (rank > 10) break; // Chỉ hiện top 10
        }
        
        if (rank == 1) { // Không có dữ liệu
            JLabel lblEmpty = new JLabel("Chưa có dữ liệu đơn hàng");
            lblEmpty.setFont(new Font("Roboto", Font.ITALIC, 12));
            lblEmpty.setForeground(TEXT_SUB);
            pnlList.add(lblEmpty);
        }

        JScrollPane scroll = new JScrollPane(pnlList);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        pnlChartGio.add(scroll, BorderLayout.CENTER);

        pnlChartGio.revalidate();
        pnlChartGio.repaint();
    }

    // ═══════════════════════════════════════════════════════════════
    // CHART STYLING
    // ═══════════════════════════════════════════════════════════════
    private void styleChart(JFreeChart chart) {
        chart.setBackgroundPaint(CARD_BG);
        chart.getTitle().setFont(new Font("Roboto", Font.BOLD, 14));
        chart.getTitle().setPaint(TEXT_MAIN);

        Plot plot = chart.getPlot();
        plot.setBackgroundPaint(CARD_BG);
        plot.setOutlineVisible(false);
    }

    // ═══════════════════════════════════════════════════════════════
    // DATE RANGE LOGIC
    // ═══════════════════════════════════════════════════════════════
    private void onRangeChanged() {
        int idx = cboRange.getSelectedIndex();
        pnlCustomDate.setVisible(idx == 4);

        LocalDate now = LocalDate.now();
        switch (idx) {
            case 0 -> { 
                int days = utils.AppConfig.getInstance().getInt("THOI_GIAN_THONG_KE", 7);
                dateFrom = now.minusDays(days - 1);  dateTo = now; 
            }
            case 1 -> { dateFrom = now.minusDays(29); dateTo = now; }
            case 2 -> { dateFrom = now.withDayOfMonth(1); dateTo = now; }
            case 3 -> {
                LocalDate firstOfLast = now.minusMonths(1).withDayOfMonth(1);
                dateFrom = firstOfLast;
                dateTo = firstOfLast.plusMonths(1).minusDays(1);
            }
            case 4 -> { /* Custom – wait for Apply */ }
        }
        if (idx != 4) loadCharts();
    }

    private void updateDateRange() {
        try {
            java.util.Date dFrom = dateChooserFrom.getDate();
            java.util.Date dTo = dateChooserTo.getDate();
            dateFrom = dFrom.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            dateTo = dTo.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            if (dateFrom.isAfter(dateTo)) {
                LocalDate tmp = dateFrom; dateFrom = dateTo; dateTo = tmp;
            }
        } catch (Exception e) {
            int days = utils.AppConfig.getInstance().getInt("THOI_GIAN_THONG_KE", 7);
            dateFrom = LocalDate.now().minusDays(days - 1);
            dateTo = LocalDate.now();
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // UI HELPERS
    // ═══════════════════════════════════════════════════════════════

    private JLabel kpiValueLabel() {
        JLabel l = new JLabel("...");
        l.setFont(new Font("Roboto", Font.BOLD, 20));
        l.setForeground(TEXT_MAIN);
        return l;
    }

    private JPanel buildKpiCard(String title, JLabel valueLabel, FontAwesome icon, Color bgIcn, Color fgIcn) {
        JPanel card = new JPanel(new BorderLayout(10, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(12, 16, 12, 16));

        JPanel iconPnl = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgIcn);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        iconPnl.setOpaque(false);
        iconPnl.setPreferredSize(new Dimension(42, 42));
        iconPnl.add(new JLabel(IconFontSwing.buildIcon(icon, 18, fgIcn)), BorderLayout.CENTER);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Roboto", Font.PLAIN, 12));
        lblTitle.setForeground(TEXT_SUB);
        text.add(lblTitle);
        text.add(Box.createVerticalStrut(2));
        text.add(valueLabel);

        card.add(iconPnl, BorderLayout.WEST);
        card.add(text, BorderLayout.CENTER);
        return card;
    }

    private JPanel createChartCard() {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(8, 8, 8, 8));
        return card;
    }



    private JPanel createSmallBtn(String text, Color color, Runnable action) {
        JPanel btn = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 1)) {
            private boolean hovered = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
                    @Override public void mouseClicked(MouseEvent e) { action.run(); }
                });
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setOpaque(false);
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (hovered) {
                    g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 25));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setBorder(new EmptyBorder(2, 10, 2, 10));
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Roboto", Font.BOLD, 12));
        lbl.setForeground(color);
        btn.add(lbl);
        return btn;
    }

    private JLabel smallLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Roboto", Font.PLAIN, 12));
        l.setForeground(TEXT_SUB);
        return l;
    }
}
