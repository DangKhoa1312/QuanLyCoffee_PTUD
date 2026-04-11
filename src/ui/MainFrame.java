package ui;

import controller.AuthController;
import controller.ShiftController;
import ui.panel.admin.StaffManagementPanel;
import utils.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import jiconfont.icons.FontAwesome;
import jiconfont.swing.IconFontSwing;

/**
 * Cửa sổ chính của hệ thống COFFEE 11:01.
 * Bố cục: Sidebar (trái) + Header (trên) + Content (CardLayout giữa).
 */
public class MainFrame extends JFrame {

    private final AuthController authController = new AuthController();
    private final ShiftController shiftController = new ShiftController();

    private JLabel lblCaInfo;
    private JLabel lblClock;
    private JPanel contentPanel;

    // Sidebar toggle components
    private boolean isSidebarExpanded = true;
    private JPanel sidebar;
    private JLabel lblAvatar;
    private JLabel lblRole;
    private JLabel lblAdmin;
    private final List<JButton> sidebarButtons = new ArrayList<>();
    private String tenNVFull = "";
    private String vaiTroFull = "";

    public MainFrame() {
        setTitle("COFFEE 11:01 - Hệ Thống Quản Lý");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 800);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        initUI();
        startClock();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        IconFontSwing.register(FontAwesome.getIconFont());

        // ═══════════════════ HEADER ═══════════════════
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)),
                new EmptyBorder(10, 20, 10, 20)));

        // Trái: thông tin ca và nút toggle
        JPanel pnlWest = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        pnlWest.setOpaque(false);

        JButton btnToggle = new JButton(IconFontSwing.buildIcon(FontAwesome.BARS, 20, new Color(44, 62, 80)));
        btnToggle.setFocusable(false);
        btnToggle.setContentAreaFilled(false);
        btnToggle.setBorderPainted(false);
        btnToggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnToggle.addActionListener(e -> toggleSidebar());
        pnlWest.add(btnToggle);

        lblCaInfo = new JLabel();
        lblCaInfo.setFont(new Font("Roboto", Font.BOLD, 14));
        updateCaInfo();
        pnlWest.add(lblCaInfo);

        header.add(pnlWest, BorderLayout.WEST);

        // Phải: đồng hồ
        lblClock = new JLabel();
        lblClock.setFont(new Font("Roboto", Font.PLAIN, 14));
        lblClock.setForeground(new Color(100, 100, 100));
        header.add(lblClock, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // ═══════════════════ SIDEBAR ═══════════════════
        sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(74, 54, 40)); // Coffee brown
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(new EmptyBorder(20, 12, 20, 12));

        // User info
        tenNVFull = SessionManager.isLoggedIn() ? SessionManager.getCurrentUser().getTenNV() : "";
        vaiTroFull = SessionManager.isLoggedIn() ? SessionManager.getCurrentUser().getVaiTro().toString() : "";

        lblAvatar = new JLabel("  " + tenNVFull);
        lblAvatar.setIcon(IconFontSwing.buildIcon(FontAwesome.USER_CIRCLE_O, 22, Color.WHITE));
        lblAvatar.setFont(new Font("Roboto", Font.BOLD, 14));
        lblAvatar.setForeground(Color.WHITE);
        lblAvatar.setAlignmentX(LEFT_ALIGNMENT);
        lblAvatar.setBorder(new EmptyBorder(0, 8, 0, 0));

        lblRole = new JLabel("         " + vaiTroFull);
        lblRole.setFont(new Font("Roboto", Font.PLAIN, 11));
        lblRole.setForeground(new Color(150, 150, 170));
        lblRole.setAlignmentX(LEFT_ALIGNMENT);

        sidebar.add(lblAvatar);
        sidebar.add(Box.createVerticalStrut(2));
        sidebar.add(lblRole);
        sidebar.add(Box.createVerticalStrut(25));

        // --- NHÓM 1: VẬN HÀNH ---
        addMenuHeader(sidebar, "VẬN HÀNH");
        JButton btnBanHang = createSidebarBtn("Bán Hàng", FontAwesome.SHOPPING_CART, new Color(243, 156, 18));
        JButton btnDatBan = createSidebarBtn("Đặt Bàn", FontAwesome.CALENDAR_CHECK_O, new Color(46, 204, 113));
        JButton btnHoaDon = createSidebarBtn("Hoá Đơn", FontAwesome.FILE_TEXT_O, new Color(52, 152, 219));

        sidebar.add(btnBanHang);
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(btnDatBan);
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(btnHoaDon);

        // --- NHÓM 2: THIẾT LẬP (Quản lý) ---
        if (SessionManager.isQuanLy()) {
            sidebar.add(Box.createVerticalStrut(25));
            addMenuHeader(sidebar, "THIẾT LẬP");

            JButton btnMon = createSidebarBtn("Thực Đơn", FontAwesome.CUTLERY, new Color(230, 126, 34));
            JButton btnGia = createSidebarBtn("Bảng Giá", FontAwesome.MONEY, new Color(46, 204, 113));
            JButton btnSoDo = createSidebarBtn("Sơ đồ Bàn", FontAwesome.TH_LARGE, new Color(52, 152, 219));

            sidebar.add(btnMon);
            sidebar.add(Box.createVerticalStrut(6));
            sidebar.add(btnGia);
            sidebar.add(Box.createVerticalStrut(6));
            sidebar.add(btnSoDo);

            // --- NHÓM 3: QUẢN TRỊ ---
            sidebar.add(Box.createVerticalStrut(25));
            addMenuHeader(sidebar, "QUẢN TRỊ");

            JButton btnNV = createSidebarBtn("Nhân Viên", FontAwesome.USERS, new Color(149, 165, 166));
            JButton btnKho = createSidebarBtn("Quản Lý Kho", FontAwesome.ARCHIVE, new Color(52, 73, 94));
            JButton btnThongKe = createSidebarBtn("Thống Kê", FontAwesome.BAR_CHART, new Color(155, 89, 182));

            sidebar.add(btnNV);
            sidebar.add(Box.createVerticalStrut(6));
            sidebar.add(btnKho);
            sidebar.add(Box.createVerticalStrut(6));
            sidebar.add(btnThongKe);

            // Hook ActionListeners cho Admin Buttons
            btnMon.addActionListener(e -> showCard("ADMIN_MON"));
            btnGia.addActionListener(e -> showCard("ADMIN_GIA"));
            btnSoDo.addActionListener(e -> showCard("ADMIN_BAN"));
            btnNV.addActionListener(e -> showCard("ADMIN_NHAN_VIEN"));
            btnKho.addActionListener(e -> showCard("ADMIN_KHO"));
            btnThongKe.addActionListener(e -> showCard("THONG_KE"));
        }

        // Spacer đẩy nút đóng ca xuống đáy
        sidebar.add(Box.createVerticalGlue());

        // Nút đóng ca
        if (SessionManager.isCaDangMo()) {
            JButton btnDongCa = createSidebarBtn("Đóng Ca", FontAwesome.STOP_CIRCLE, Color.WHITE);
            btnDongCa.setBackground(new Color(180, 40, 40));
            btnDongCa.addActionListener(e -> handleDongCa());
            sidebar.add(btnDongCa);
            sidebar.add(Box.createVerticalStrut(8));
        }

        // Nút đăng xuất
        JButton btnLogout = createSidebarBtn("Đăng Xuất", FontAwesome.SIGN_OUT, Color.WHITE);
        btnLogout.setBackground(new Color(90, 70, 60));
        btnLogout.addActionListener(e -> handleLogout());
        sidebar.add(btnLogout);

        add(sidebar, BorderLayout.WEST);

        // ═══════════════════ CONTENT (CardLayout) ═══════════════════
        contentPanel = new JPanel(new CardLayout());
        contentPanel.setBackground(new Color(245, 247, 250));

        // Home card mặc định
        JPanel homeCard = new JPanel(new GridBagLayout());
        homeCard.setOpaque(false);

        JPanel welcomeBox = new JPanel();
        welcomeBox.setLayout(new BoxLayout(welcomeBox, BoxLayout.Y_AXIS));
        welcomeBox.setBackground(Color.WHITE);
        welcomeBox.setBorder(new EmptyBorder(50, 60, 50, 60));

        JLabel lblW1 = new JLabel("☕ COFFEE 11:01");
        lblW1.setFont(new Font("Roboto", Font.BOLD, 28));
        lblW1.setForeground(new Color(26, 26, 46));
        lblW1.setAlignmentX(CENTER_ALIGNMENT);

        JLabel lblW2 = new JLabel("Chọn chức năng từ menu bên trái để bắt đầu.");
        lblW2.setFont(new Font("Roboto", Font.PLAIN, 16));
        lblW2.setForeground(new Color(150, 150, 150));
        lblW2.setAlignmentX(CENTER_ALIGNMENT);

        welcomeBox.add(lblW1);
        welcomeBox.add(Box.createVerticalStrut(12));
        welcomeBox.add(lblW2);

        homeCard.add(welcomeBox);
        contentPanel.add(homeCard, "HOME");

        // Tấm thẻ 2: Sơ đồ bàn
        ui.panel.TablePanel tablePanel = new ui.panel.TablePanel();
        contentPanel.add(tablePanel, "BAN_HANG");

        // Tấm thẻ 3: Gọi món
        ui.panel.OrderPanel orderPanel = new ui.panel.OrderPanel();
        contentPanel.add(orderPanel, "GOI_MON");

        // Hook Table -> Order
        tablePanel.setTableClickListener((ban, dh) -> {
            orderPanel.loadDonHangForTable(ban, dh);
            showCard("GOI_MON");
        });

        // Hook Order -> Table
        orderPanel.setOnBackAction(() -> {
            tablePanel.refreshData();
            showCard("BAN_HANG");
        });

        // Tấm thẻ 4: Quản lý Hoá Đơn
        ui.panel.InvoicePanel invoicePanel = new ui.panel.InvoicePanel();
        contentPanel.add(invoicePanel, "HOA_DON");

        // Tấm thẻ 5: Thống Kê (Dashboard)
        ui.panel.StatisticPanel statisticPanel = new ui.panel.StatisticPanel();
        contentPanel.add(statisticPanel, "THONG_KE");

        // --- PLACEHOLDERS CHO CÁC MODULE ADMIN ---
        contentPanel.add(createPlaceholderPanel("QUẢN LÝ ĐẶT BÀN"), "DAT_BAN");
        contentPanel.add(new ui.panel.admin.MenuManagementPanel(), "ADMIN_MON");
        contentPanel.add(new ui.panel.admin.PriceManagementPanel(), "ADMIN_GIA");
        contentPanel.add(new ui.panel.admin.TableManagementPanel(), "ADMIN_BAN");
        contentPanel.add(new StaffManagementPanel(), "ADMIN_NHAN_VIEN");
        contentPanel.add(new ui.panel.admin.WarehouseManagementPanel(), "ADMIN_KHO");

        add(contentPanel, BorderLayout.CENTER);

        // ═══════════════════ SỰ KIỆN MENU ═══════════════════
        btnBanHang.addActionListener(e -> {
            tablePanel.refreshData();
            showCard("BAN_HANG");
        });
        btnDatBan.addActionListener(e -> {
            showCard("DAT_BAN");
        });
        btnHoaDon.addActionListener(e -> {
            invoicePanel.loadData();
            showCard("HOA_DON");
        });
    }

    private JButton createSidebarBtn(String text, jiconfont.IconCode iconCode, Color iconColor) {
        JButton btn = new JButton(text);
        if (iconCode != null) {
            Icon icon = IconFontSwing.buildIcon(iconCode, 18, iconColor != null ? iconColor : Color.WHITE);
            btn.setIcon(icon);
            btn.setIconTextGap(15);
        }
        btn.setFont(new Font("Roboto", Font.BOLD, 13));
        btn.setForeground(new Color(230, 230, 230));
        btn.setBackground(new Color(90, 68, 52));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btn.setFocusable(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(0, 16, 0, 0));
        btn.putClientProperty("FlatLaf.style", "arc: 10; borderWidth: 0; focusWidth: 0; hoverBackground: #714c34");

        btn.putClientProperty("full_text", text);
        sidebarButtons.add(btn);

        return btn;
    }

    private void toggleSidebar() {
        isSidebarExpanded = !isSidebarExpanded;
        if (isSidebarExpanded) {
            sidebar.setPreferredSize(new Dimension(220, 0));
            lblAvatar.setText("  " + tenNVFull);
            lblRole.setVisible(true);

            for (JButton btn : sidebarButtons) {
                btn.setText((String) btn.getClientProperty("full_text"));
                btn.setHorizontalAlignment(SwingConstants.LEFT);
                btn.setBorder(new EmptyBorder(0, 16, 0, 0));
            }
        } else {
            sidebar.setPreferredSize(new Dimension(65, 0));
            lblAvatar.setText("");
            lblRole.setVisible(false);

            for (JButton btn : sidebarButtons) {
                btn.setText("");
                btn.setHorizontalAlignment(SwingConstants.CENTER);
                btn.setBorder(new EmptyBorder(0, 0, 0, 0));
            }
        }
        sidebar.revalidate();
        sidebar.repaint();
    }

    private void addMenuHeader(JPanel container, String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Roboto", Font.BOLD, 10));
        lbl.setForeground(new Color(180, 160, 150));
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        lbl.setBorder(new EmptyBorder(0, 8, 4, 0));
        container.add(lbl);
    }

    private JPanel createPlaceholderPanel(String title) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        JLabel lbl = new JLabel(title + " (Sẽ có trong bản cập nhật sau)");
        lbl.setFont(new Font("Roboto", Font.ITALIC, 20));
        lbl.setForeground(Color.GRAY);
        p.add(lbl);
        return p;
    }

    private void showCard(String name) {
        CardLayout cl = (CardLayout) contentPanel.getLayout();
        cl.show(contentPanel, name);
    }

    private void updateCaInfo() {
        if (SessionManager.isCaDangMo()) {
            String maCa = SessionManager.getCurrentCa().getMaCa();
            String gio = SessionManager.getCurrentCa().getGioBatDau().format(DateTimeFormatter.ofPattern("HH:mm"));
            lblCaInfo.setText("☕ COFFEE 11:01   |   Ca: " + maCa + "  (từ " + gio + ")");
            lblCaInfo.setForeground(new Color(39, 174, 96));
        } else {
            lblCaInfo.setText("☕ COFFEE 11:01   |   Chưa vào ca");
            lblCaInfo.setForeground(new Color(180, 180, 180));
        }
    }

    private void startClock() {
        Timer timer = new Timer(1000, e -> {
            lblClock.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        });
        timer.start();
    }

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
            // Có ca đang mở -> mở báo cáo đóng ca
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Bạn đang có ca làm việc đang mở!\nĐăng xuất sẽ yêu cầu ĐÓNG CA trước.\nTiếp tục?",
                    "Xác nhận",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                handleDongCa(); // Mở ShiftCloseDialog
            }
            return;
        }
        authController.logout();
        new LoginForm().setVisible(true);
        dispose();
    }
}
