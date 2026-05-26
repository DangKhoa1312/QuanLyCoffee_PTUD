package ui.panel;

import controller.KhoController;
import controller.ShiftController;
import dao.BanDAO;
import dao.HoaDonDAO;
import dao.impl.BanDAOImpl;
import dao.impl.HoaDonDAOImpl;
import entity.Ban;
import entity.HoaDon;
import entity.TonKho;
import enums.TrangThaiBan;
import enums.TrangThaiHoaDon;
import jiconfont.icons.FontAwesome;
import jiconfont.swing.IconFontSwing;
import utils.CurrencyUtils;
import utils.OrderManager;
import utils.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Modern Light Minimalist Dashboard
 */
public class DashboardPanel extends JPanel {

    public interface NavCallback {
        void navigate(String key);
    }

    private NavCallback navCallback;

    private static final Color BG = new Color(248, 250, 252); // slate-50
    private static final Color CARD_BG = new Color(255, 255, 255);
    private static final Color TEXT_MAIN = new Color(15, 23, 42); // slate-900
    private static final Color TEXT_SUB = new Color(100, 116, 139); // slate-500

    // Soft pastel branding colors
    private static final Color P_GREEN_BG = new Color(220, 252, 231); // green-100
    private static final Color P_GREEN_FG = new Color(22, 163, 74); // green-600

    private static final Color P_BLUE_BG = new Color(219, 234, 254); // blue-100
    private static final Color P_BLUE_FG = new Color(37, 99, 235); // blue-600

    private static final Color P_RED_BG = new Color(254, 226, 226); // red-100
    private static final Color P_RED_FG = new Color(220, 38, 38); // red-600

    private static final Color P_PURPLE_BG = new Color(243, 232, 255); // purple-100
    private static final Color P_PURPLE_FG = new Color(147, 51, 234); // purple-600

    private static final Color P_AMBER_BG = new Color(254, 243, 199); // amber-100
    private static final Color P_AMBER_FG = new Color(217, 119, 6); // amber-600

    private JLabel lblRevenue, lblInvoices, lblAlerts, lblBanDangPV;
    private JLabel lblSubMangVe, lblSubAvg;
    private DefaultTableModel recentModel, alertModel;

    public DashboardPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG);

        JScrollPane scroll = new JScrollPane(buildContent());
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);

        loadData();
    }

    public void setNavCallback(NavCallback cb) {
        this.navCallback = cb;
    }

    public void refresh() {
        loadData();
    }

    private JPanel buildContent() {
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(32, 40, 32, 40));

        root.add(buildTopBar());
        root.add(vgap(28));
        root.add(buildKpiRow());
        root.add(vgap(16));
        root.add(buildSubInfoBar());
        root.add(vgap(32));
        root.add(buildSectionLabel("Truy cập nhanh"));
        root.add(vgap(12));
        root.add(buildQuickAccess());
        root.add(vgap(32));
        root.add(buildSectionLabel("Tổng quan hôm nay"));
        root.add(vgap(12));
        root.add(buildBottomRow());

        return root;
    }

    private JPanel buildTopBar() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        String name = SessionManager.getCurrentUser() != null ? SessionManager.getCurrentUser().getTenNV() : "Bạn";
        JLabel lblGreeting = new JLabel("Chào mừng trở lại, " + name + " \uD83D\uDC4B"); // vẫy tay
        lblGreeting.setFont(new Font("Roboto", Font.BOLD, 24));
        lblGreeting.setForeground(TEXT_MAIN);

        String today = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", new java.util.Locale("vi", "VN")));
        JLabel lblDate = new JLabel(today);
        lblDate.setFont(new Font("Roboto", Font.PLAIN, 14));
        lblDate.setForeground(TEXT_SUB);

        left.add(lblGreeting);
        left.add(Box.createVerticalStrut(4));
        left.add(lblDate);
        p.add(left, BorderLayout.WEST);

        // Nút refresh bo tròn
        JPanel pnlRefresh = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        pnlRefresh.setOpaque(false);
        pnlRefresh.setPreferredSize(new Dimension(50, 50));
        pnlRefresh.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        JLabel lblRefresh = new JLabel(IconFontSwing.buildIcon(FontAwesome.REFRESH, 18, TEXT_SUB),
                SwingConstants.CENTER);
        pnlRefresh.add(lblRefresh);
        pnlRefresh.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                loadData();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                lblRefresh.setIcon(IconFontSwing.buildIcon(FontAwesome.REFRESH, 18, TEXT_MAIN));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                lblRefresh.setIcon(IconFontSwing.buildIcon(FontAwesome.REFRESH, 18, TEXT_SUB));
            }
        });

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 5));
        rightPanel.setOpaque(false);
        rightPanel.add(pnlRefresh);
        p.add(rightPanel, BorderLayout.EAST);

        return p;
    }

    private JPanel buildKpiRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 20, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        lblRevenue = kpiValueLabel();
        lblInvoices = kpiValueLabel();
        lblBanDangPV = kpiValueLabel();
        lblAlerts = kpiValueLabel();

        row.add(buildKpiCard("Doanh thu hôm nay", lblRevenue, FontAwesome.USD, P_GREEN_BG, P_GREEN_FG));
        row.add(buildKpiCard("Hóa đơn hôm nay", lblInvoices, FontAwesome.FILE_TEXT_O, P_BLUE_BG, P_BLUE_FG));
        row.add(buildKpiCard("Bàn đang phục vụ", lblBanDangPV, FontAwesome.CUTLERY, P_AMBER_BG, P_AMBER_FG));
        row.add(buildKpiCard("Cảnh báo kho", lblAlerts, FontAwesome.EXCLAMATION_TRIANGLE, P_RED_BG, P_RED_FG));

        return row;
    }

    private JPanel buildSubInfoBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        bar.setBorder(new EmptyBorder(10, 24, 10, 24));

        lblSubMangVe = subInfoLabel("Mang về đang chờ: 0", FontAwesome.MOTORCYCLE, P_PURPLE_FG);
        lblSubAvg = subInfoLabel("TB/đơn: 0 đ", FontAwesome.LINE_CHART, P_BLUE_FG);

        bar.add(lblSubMangVe);
        bar.add(subDivider());
        bar.add(lblSubAvg);

        return bar;
    }

    private JLabel subInfoLabel(String text, FontAwesome icon, Color color) {
        JLabel lbl = new JLabel("  " + text);
        lbl.setFont(new Font("Roboto", Font.BOLD, 13));
        lbl.setForeground(color);
        lbl.setIcon(IconFontSwing.buildIcon(icon, 14, color));
        return lbl;
    }

    private JLabel subDivider() {
        JLabel d = new JLabel("   |   ");
        d.setForeground(new Color(226, 232, 240));
        d.setFont(new Font("Roboto", Font.PLAIN, 14));
        return d;
    }

    private JLabel kpiValueLabel() {
        JLabel l = new JLabel("...");
        l.setFont(new Font("Roboto", Font.BOLD, 26));
        l.setForeground(TEXT_MAIN);
        return l;
    }

    private JPanel buildKpiCard(String title, JLabel valueLabel, FontAwesome icon, Color bgIcn, Color fgIcn) {
        JPanel card = new JPanel(new BorderLayout(16, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24); // Bo tròn sâu
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Icon circle mềm mại Pastel
        JPanel iconPnl = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgIcn);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        iconPnl.setOpaque(false);
        iconPnl.setPreferredSize(new Dimension(56, 56));
        iconPnl.add(new JLabel(IconFontSwing.buildIcon(icon, 22, fgIcn)), BorderLayout.CENTER);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Roboto", Font.PLAIN, 14));
        lblTitle.setForeground(TEXT_SUB);
        text.add(lblTitle);
        text.add(Box.createVerticalStrut(6));
        text.add(valueLabel);

        card.add(iconPnl, BorderLayout.WEST);
        card.add(text, BorderLayout.CENTER);
        return card;
    }

    // ─── Quick access
    private JPanel buildQuickAccess() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        row.add(quickBtn("Bán Hàng", FontAwesome.SHOPPING_BAG, "BAN_HANG"));
        row.add(quickBtn("Đặt Bàn", FontAwesome.CALENDAR, "DAT_BAN"));
        row.add(quickBtn("Hoá Đơn", FontAwesome.FILE_TEXT, "HOA_DON"));
        if (SessionManager.isQuanLy()) {
            row.add(quickBtn("Thống Kê", FontAwesome.PIE_CHART, "THONG_KE"));
            row.add(quickBtn("Thực Đơn", FontAwesome.COFFEE, "ADMIN_MON"));
            row.add(quickBtn("Kho Hàng", FontAwesome.ARCHIVE, "ADMIN_KHO"));
            row.add(quickBtn("Công Thức", FontAwesome.FLASK, "ADMIN_CONG_THUC"));
        }

        return row;
    }

    private JPanel quickBtn(String text, FontAwesome icon, String key) {
        JPanel card = new JPanel() {
            private boolean hovered = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hovered = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        hovered = false;
                        repaint();
                    }

                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (navCallback != null)
                            navCallback.navigate(key);
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                if (hovered) {
                    g2.setColor(new Color(0, 0, 0, 10)); // hover shadow overlay
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                }
                g2.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(16, 20, 16, 20));
        card.setPreferredSize(new Dimension(110, 92));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel ico = new JLabel(IconFontSwing.buildIcon(icon, 24, P_BLUE_FG));
        ico.setAlignmentX(CENTER_ALIGNMENT);

        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Roboto", Font.BOLD, 12));
        lbl.setForeground(TEXT_MAIN);
        lbl.setAlignmentX(CENTER_ALIGNMENT);

        card.add(Box.createVerticalGlue());
        card.add(ico);
        card.add(Box.createVerticalStrut(10));
        card.add(lbl);
        card.add(Box.createVerticalGlue());

        return card;
    }

    private JPanel buildBottomRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 24, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400)); // Cao rộng thoải mái
        row.add(buildTableCard("Hóa đơn gần đây",
                () -> recentModel = new DefaultTableModel(new String[] { "Mã HD", "Giờ", "Bàn", "Tiền" }, 0) {
                    @Override
                    public boolean isCellEditable(int r, int c) {
                        return false;
                    }
                }, true));
        row.add(buildTableCard("Cảnh báo nguyên liệu",
                () -> alertModel = new DefaultTableModel(new String[] { "Nguyên Liệu", "Tồn", "Tối Thiểu" }, 0) {
                    @Override
                    public boolean isCellEditable(int r, int c) {
                        return false;
                    }
                }, false));
        return row;
    }

    private JPanel buildTableCard(String titleStr, java.util.function.Supplier<DefaultTableModel> modelSupplier,
            boolean isInvoice) {
        JPanel card = new JPanel(new BorderLayout(0, 14)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(24, 24, 24, 24));

        JLabel title = new JLabel(titleStr);
        title.setFont(new Font("Roboto", Font.BOLD, 16));
        title.setForeground(TEXT_MAIN);
        card.add(title, BorderLayout.NORTH);

        DefaultTableModel md = modelSupplier.get();
        JTable table = new JTable(md);
        table.setRowHeight(42);
        table.setFont(new Font("Roboto", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Roboto", Font.BOLD, 12));
        table.getTableHeader().setBackground(CARD_BG);
        table.getTableHeader().setForeground(TEXT_SUB);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(241, 245, 249))); // viền
                                                                                                                 // mỏng
        table.setShowGrid(false); // Xóa sạch border grid xấu xí
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(BG);

        if (isInvoice) {
            DefaultTableCellRenderer right = new DefaultTableCellRenderer();
            right.setHorizontalAlignment(SwingConstants.RIGHT);
            table.getColumnModel().getColumn(3).setCellRenderer(right);
        } else {
            table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row,
                        int col) {
                    Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                    if (col == 0 && String.valueOf(v).contains("✓"))
                        c.setForeground(P_GREEN_FG);
                    else if (col == 0)
                        c.setForeground(P_RED_FG);
                    else
                        c.setForeground(TEXT_MAIN);
                    return c;
                }
            });
        }

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(null);
        sp.getViewport().setBackground(CARD_BG);
        card.add(sp, BorderLayout.CENTER);

        return card;
    }

    public void loadData() {
        lblRevenue.setText("...");
        lblInvoices.setText("...");
        lblAlerts.setText("...");
        lblBanDangPV.setText("...");

        new SwingWorker<Void, Void>() {
            double revenue = 0;
            int invoiceCount = 0;
            int banCoKhach = 0;
            int donDangPV = 0;
            int donMangVe = 0;
            List<HoaDon> recentHD = new ArrayList<>();
            List<TonKho> alertList = new ArrayList<>();

            @Override
            protected Void doInBackground() {
                try {
                    HoaDonDAO dao = new HoaDonDAOImpl();
                    LocalDate today = LocalDate.now();

                    // Lấy tất cả hóa đơn hôm nay (đã thanh toán)
                    List<HoaDon> todayHD = dao.findByNgay(today).stream()
                            .filter(h -> TrangThaiHoaDon.DA_THANH_TOAN.equals(h.getTrangThai()))
                            .collect(Collectors.toList());

                    revenue = todayHD.stream().mapToDouble(HoaDon::getTongTienPhaiTra).sum();
                    invoiceCount = todayHD.size();

                    // 8 hóa đơn gần nhất
                    recentHD = todayHD.stream()
                            .sorted((a, b) -> {
                                if (b.getThoiGianXuat() == null)
                                    return -1;
                                if (a.getThoiGianXuat() == null)
                                    return 1;
                                return b.getThoiGianXuat().compareTo(a.getThoiGianXuat());
                            })
                            .limit(8).collect(Collectors.toList());
                } catch (Exception ignored) {
                }

                // Đếm bàn đang có khách từ DB
                try {
                    BanDAO banDAO = new BanDAOImpl();
                    List<Ban> dsBan = banDAO.findByTrangThai(TrangThaiBan.CO_KHACH);
                    banCoKhach = dsBan.size();
                } catch (Exception ignored) {
                }

                // Đếm đơn đang phục vụ & mang về từ OrderManager (RAM)
                try {
                    OrderManager om = OrderManager.getInstance();
                    List<entity.DonHang> allOpen = new ArrayList<>();
                    // Use getOpenTakeawayOrders for takeaway count
                    donMangVe = om.getOpenTakeawayOrders().size();
                    // Total active orders = all DANG_PHUC_VU in OrderManager
                    donDangPV = banCoKhach + donMangVe;
                } catch (Exception ignored) {
                }

                // Cảnh báo tồn kho
                try {
                    KhoController khoController = new KhoController();
                    List<TonKho> allTK = khoController.getAllTonKho();

                    alertList = allTK.stream()
                            .filter(TonKho::isSapHet)
                            .limit(10)
                            .collect(Collectors.toList());
                } catch (Exception ignored) {
                }
                return null;
            }

            @Override
            protected void done() {
                // KPI Cards
                lblRevenue.setText(CurrencyUtils.formatNoUnit(revenue));
                lblInvoices.setText(String.valueOf(invoiceCount));

                lblBanDangPV.setText(String.valueOf(banCoKhach));
                if (banCoKhach > 0) {
                    lblBanDangPV.setForeground(P_AMBER_FG);
                } else {
                    lblBanDangPV.setForeground(TEXT_MAIN);
                }

                if (alertList.isEmpty()) {
                    lblAlerts.setText("An toàn ✓");
                    lblAlerts.setForeground(P_GREEN_FG);
                } else {
                    lblAlerts.setText(alertList.size() + " cảnh báo");
                    lblAlerts.setForeground(P_RED_FG);
                }

                // Sub-info bar
                lblSubMangVe.setText("  Mang về đang chờ: " + donMangVe);
                double avg = invoiceCount > 0 ? revenue / invoiceCount : 0;
                lblSubAvg.setText("  TB/đơn: " + CurrencyUtils.format(avg));

                // Recent invoices table
                recentModel.setRowCount(0);
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
                for (HoaDon h : recentHD) {
                    String ban = h.getMaBan() != null ? h.getMaBan() : "Mang về";
                    recentModel.addRow(new Object[] {
                            h.getMaHD(),
                            h.getThoiGianXuat() != null ? h.getThoiGianXuat().format(fmt) : "—",
                            ban,
                            CurrencyUtils.format(h.getTongTienPhaiTra())
                    });
                }
                if (recentHD.isEmpty())
                    recentModel.addRow(new Object[] { "Chưa có", "dữ liệu", "hôm nay", "" });

                // Alert table
                alertModel.setRowCount(0);
                KhoController khoController = new KhoController();

                for (TonKho tk : alertList) {
                    entity.NguyenLieu nl = khoController.getNguyenLieuById(tk.getMaNL());
                    String name = (nl != null) ? nl.getTenNL() : tk.getMaNL();
                    String reason = "";

                    if (tk.getSoLuongTon() <= 0)
                        reason = " [Hết hàng]";
                    else if (tk.isSapHet())
                        reason = " [Sắp hết]";

                    alertModel.addRow(new Object[] { name + reason, String.format("%.0f", tk.getSoLuongTon()),
                            String.format("%.0f", tk.getMucToiThieu()) });
                }
                if (alertList.isEmpty())
                    alertModel.addRow(new Object[] { "Mọi thứ ổn ✓", "", "" });
            }
        }.execute();
    }

    private JPanel buildSectionLabel(String txt) {
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        wrapper.setOpaque(false);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        JLabel l = new JLabel(txt);
        l.setFont(new Font("Roboto", Font.BOLD, 18));
        l.setForeground(TEXT_MAIN);
        wrapper.add(l);
        return wrapper;
    }

    private Component vgap(int h) {
        return Box.createRigidArea(new Dimension(0, h));
    }
}
