package ui;

import controller.AuthController;
import controller.ShiftController;
import ui.panel.admin.StaffManagementPanel;
import utils.SessionManager;
import jiconfont.icons.FontAwesome;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Cửa sổ chính của hệ thống COFFEE 11:01.
 * Bố cục: AccordionSidebar (trái) + Header (trên) + ContentPanel/CardLayout (giữa).
 */
public class MainFrame extends JFrame {

    private final AuthController  authController  = new AuthController();
    private final ShiftController shiftController = new ShiftController();

    // ── Sidebar ──────────────────────────────────────────────────────────────
    private AccordionSidebar sidebar;

    // ── Content ──────────────────────────────────────────────────────────────
    private JPanel contentPanel;

    // ── Panel references (để gọi refresh khi chuyển tab) ────────────────────
    private ui.panel.DashboardPanel dashboardPanel;
    private ui.panel.TablePanel     tablePanel;
    private ui.panel.OrderPanel     orderPanel;
    private ui.panel.InvoicePanel   invoicePanel;
    private ui.panel.StatisticPanel statisticPanel;

    // ── Header ───────────────────────────────────────────────────────────────
    private JLabel lblClock;
    private boolean sidebarVisible = true;

    // ══════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTOR
    // ══════════════════════════════════════════════════════════════════════════
    public MainFrame() {
        setTitle("COFFEE 11:01 - Hệ Thống Quản Lý");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 800);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        initUI();
        startClock();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  INIT
    // ══════════════════════════════════════════════════════════════════════════
    private void initUI() {
        setLayout(new BorderLayout());

        // 1. Sidebar (Full Height Left)
        sidebar = new AccordionSidebar();
        sidebar.setNavListener(this::handleNav);
        add(sidebar, BorderLayout.WEST);

        // 2. Right Container (Header + Content)
        JPanel rightContainer = new JPanel(new BorderLayout());
        rightContainer.add(buildHeader(), BorderLayout.NORTH);

        contentPanel = new JPanel(new CardLayout());
        contentPanel.setBackground(new Color(245, 247, 250));
        buildContentCards();
        rightContainer.add(contentPanel, BorderLayout.CENTER);

        add(rightContainer, BorderLayout.CENTER);

        // Màn hình khởi đầu
        showCard("HOME");
        sidebar.setActivePage("HOME");
        sidebar.refreshBadges();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  HEADER
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(225, 225, 225)),
                new EmptyBorder(8, 18, 8, 20)));

        // Toggle sidebar
        JButton btnToggle = new JButton("☰");
        btnToggle.setFont(new Font("Dialog", Font.PLAIN, 18));
        btnToggle.setFocusable(false);
        btnToggle.setContentAreaFilled(false);
        btnToggle.setBorderPainted(false);
        btnToggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnToggle.setForeground(new Color(70, 70, 70));
        btnToggle.addActionListener(e -> toggleSidebar());
        header.add(btnToggle, BorderLayout.WEST);

        // Phải: Đồng hồ và User Actions
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightPanel.setOpaque(false);

        lblClock = new JLabel();
        lblClock.setFont(new Font("Roboto", Font.PLAIN, 14));
        lblClock.setForeground(new Color(130, 130, 130));
        rightPanel.add(lblClock);

        JLabel lblDiv = new JLabel(" | ");
        lblDiv.setForeground(new Color(226, 232, 240));
        lblDiv.setFont(new Font("Roboto", Font.BOLD, 16));
        rightPanel.add(lblDiv);

        if (SessionManager.isCaDangMo()) {
            rightPanel.add(createHeaderBtn("Đóng Ca", FontAwesome.LOCK, new Color(220, 38, 38), new Color(254, 242, 242), () -> handleNav("ACTION_DONG_CA")));
        }
        rightPanel.add(createHeaderBtn("Đăng Xuất", FontAwesome.SIGN_OUT, new Color(71, 85, 105), new Color(241, 245, 249), () -> handleNav("ACTION_LOGOUT")));

        header.add(rightPanel, BorderLayout.EAST);

        return header;
    }

    private JComponent createHeaderBtn(String text, FontAwesome icon, Color color, Color hoverBg, Runnable action) {
        JPanel btn = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 2)) {
            private boolean hovered = false;
            {
                addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override public void mouseEntered(java.awt.event.MouseEvent e) { hovered = true; repaint(); }
                    @Override public void mouseExited(java.awt.event.MouseEvent e)  { hovered = false; repaint(); }
                    @Override public void mouseClicked(java.awt.event.MouseEvent e) { action.run(); }
                });
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setOpaque(false);
            }
            @Override protected void paintComponent(Graphics g) {
                if (hovered) {
                    Graphics2D g2 = (Graphics2D)g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(hoverBg);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        btn.setBorder(new EmptyBorder(4, 10, 4, 10));
        if (icon != null) btn.add(new JLabel(IconFontSwing.buildIcon(icon, 14, color)));
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Roboto", Font.BOLD, 13));
        lbl.setForeground(color);
        btn.add(lbl);
        return btn;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  CONTENT CARDS
    // ══════════════════════════════════════════════════════════════════════════
    private void buildContentCards() {
        // HOME → Dashboard
        dashboardPanel = new ui.panel.DashboardPanel();
        dashboardPanel.setNavCallback(this::handleNav);
        contentPanel.add(dashboardPanel, "HOME");

        // Bán Hàng: TablePanel ↔ OrderPanel (linked callbacks)
        tablePanel = new ui.panel.TablePanel();
        orderPanel = new ui.panel.OrderPanel();
        tablePanel.setTableClickListener((ban, dh) -> {
            orderPanel.loadDonHangForTable(ban, dh);
            showCard("GOI_MON");
        });
        orderPanel.setOnBackAction(() -> {
            tablePanel.refreshData();
            showCard("BAN_HANG");
        });
        contentPanel.add(tablePanel, "BAN_HANG");
        contentPanel.add(orderPanel, "GOI_MON");

        // Đặt Bàn (placeholder — sẽ được thay bằng ReservationPanel)
        contentPanel.add(buildPlaceholder("📅  Đặt Bàn", "Module đang được phát triển trong phiên bản tiếp theo."), "DAT_BAN");

        // Hoá Đơn
        invoicePanel = new ui.panel.InvoicePanel();
        contentPanel.add(invoicePanel, "HOA_DON");

        // Thống Kê
        statisticPanel = new ui.panel.StatisticPanel();
        contentPanel.add(statisticPanel, "THONG_KE");

        // Admin panels
        contentPanel.add(new ui.panel.admin.MenuManagementPanel(), "ADMIN_MON");
        contentPanel.add(new ui.panel.admin.PriceManagementPanel(), "ADMIN_GIA");
        contentPanel.add(new ui.panel.admin.TableManagementPanel(), "ADMIN_BAN");
        contentPanel.add(new StaffManagementPanel(),                "ADMIN_NHAN_VIEN");
        contentPanel.add(new ui.panel.admin.WarehouseManagementPanel(), "ADMIN_KHO");

        // Placeholders cho các module chưa build
        contentPanel.add(buildPlaceholder("🕐  Lịch Sử Ca Làm Việc",  "Module đang phát triển..."), "LICH_SU_CA");

        contentPanel.add(new ui.panel.admin.ToppingManagementPanel(), "ADMIN_TOPPING");
        contentPanel.add(new ui.panel.admin.RecipeManagementPanel(), "ADMIN_CONG_THUC");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  NAVIGATION HANDLER
    // ══════════════════════════════════════════════════════════════════════════
    private void handleNav(String key) {
        switch (key) {
            case "ACTION_DONG_CA":
                handleDongCa();
                return;
            case "ACTION_LOGOUT":
                handleLogout();
                return;
            case "HOME":
                dashboardPanel.refresh();
                break;
            case "BAN_HANG":
                tablePanel.refreshData();
                break;
            case "HOA_DON":
                invoicePanel.loadData();
                break;
            case "THONG_KE":
                statisticPanel.loadCharts();
                break;
            default:
                break;
        }
        showCard(key);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════════════════════════════
    private void showCard(String name) {
        ((CardLayout) contentPanel.getLayout()).show(contentPanel, name);
    }

    private void toggleSidebar() {
        sidebarVisible = !sidebarVisible;
        sidebar.setVisible(sidebarVisible);
        revalidate();
        repaint();
    }

    private void startClock() {
        new Timer(1000, e ->
            lblClock.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("  HH:mm:ss  ")))
        ).start();
    }

    private JPanel buildHomePage() {
        JPanel home = new JPanel(new GridBagLayout());
        home.setOpaque(false);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(new EmptyBorder(50, 70, 50, 70));

        JLabel lbl1 = new JLabel("☕  COFFEE 11:01");
        lbl1.setFont(new Font("Roboto", Font.BOLD, 30));
        lbl1.setForeground(new Color(44, 26, 14));
        lbl1.setAlignmentX(CENTER_ALIGNMENT);

        JLabel lbl2 = new JLabel("Chọn chức năng từ menu bên trái để bắt đầu.");
        lbl2.setFont(new Font("Roboto", Font.PLAIN, 16));
        lbl2.setForeground(new Color(160, 140, 120));
        lbl2.setAlignmentX(CENTER_ALIGNMENT);

        card.add(lbl1);
        card.add(Box.createVerticalStrut(14));
        card.add(lbl2);
        home.add(card);
        return home;
    }

    private JPanel buildPlaceholder(String title, String subtitle) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(new EmptyBorder(40, 60, 40, 60));

        JLabel t = new JLabel(title);
        t.setFont(new Font("Roboto", Font.BOLD, 22));
        t.setForeground(new Color(60, 36, 16));
        t.setAlignmentX(CENTER_ALIGNMENT);

        JLabel s = new JLabel(subtitle);
        s.setFont(new Font("Roboto", Font.ITALIC, 14));
        s.setForeground(new Color(160, 140, 120));
        s.setAlignmentX(CENTER_ALIGNMENT);

        card.add(t);
        card.add(Box.createVerticalStrut(10));
        card.add(s);
        p.add(card);
        return p;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  BUSINESS ACTIONS
    // ══════════════════════════════════════════════════════════════════════════
    private void handleDongCa() {
        if (!SessionManager.isCaDangMo()) {
            JOptionPane.showMessageDialog(this, "Hiện không có ca nào đang mở.");
            return;
        }
        ui.dialog.ShiftCloseDialog dlg = new ui.dialog.ShiftCloseDialog(this, shiftController);
        dlg.setVisible(true);
        if (dlg.isShiftClosed()) {
            authController.logout();
            new LoginForm().setVisible(true);
            dispose();
        }
    }

    private void handleLogout() {
        if (SessionManager.isCaDangMo()) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Bạn đang có ca làm việc đang mở!\nĐăng xuất sẽ yêu cầu ĐÓNG CA trước.\nTiếp tục?",
                    "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                handleDongCa();
            }
            return;
        }
        authController.logout();
        new LoginForm().setVisible(true);
        dispose();
    }
}
