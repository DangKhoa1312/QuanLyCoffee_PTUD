package ui;

import com.formdev.flatlaf.FlatClientProperties;
import controller.AuthController;
import controller.ShiftController;
import entity.CaLamViec;
import entity.NhanVien;
import enums.VaiTro;
import exception.AppException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

public class LoginForm extends JFrame {

    private final AuthController authController = new AuthController();
    private final ShiftController shiftController = new ShiftController();

    private JTextField txtUsername;
    private JPasswordField txtPassword;

    // ── Color palette ──────────────────────────────────────────────────────
    private static final Color COLOR_DARK      = new Color(28, 20, 14);     // nền tối cà phê
    private static final Color COLOR_BROWN     = new Color(98, 60, 30);     // nâu đậm
    private static final Color COLOR_CARAMEL   = new Color(188, 120, 60);   // caramel
    private static final Color COLOR_CREAM     = new Color(255, 248, 235);  // kem nhạt
    private static final Color COLOR_WARM_GRAY = new Color(120, 110, 100);  // xám ấm
    private static final Color COLOR_ACCENT    = new Color(210, 140, 50);   // vàng cà phê
    private static final Color COLOR_PANEL_BG  = new Color(250, 245, 238);  // nền form
    private static final Color COLOR_FIELD_BG  = new Color(255, 252, 245);  // nền input
    private static final Color COLOR_BORDER    = new Color(220, 200, 175);  // viền input

    public LoginForm() {
        setTitle("COFFEE 11:01 — Đăng Nhập");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(820, 540);
        setLocationRelativeTo(null);
        setResizable(false);
        setUndecorated(true);
        setShape(new RoundRectangle2D.Double(0, 0, 820, 540, 20, 20));

        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // bo góc ngoài
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };
        root.setBackground(COLOR_PANEL_BG);
        setContentPane(root);

        root.add(buildLeftPanel(), BorderLayout.WEST);
        root.add(buildRightPanel(), BorderLayout.CENTER);
    }

    // ── LEFT: Branding panel ───────────────────────────────────────────────
    private JPanel buildLeftPanel() {
        JPanel left = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Gradient nền tối
                GradientPaint grad = new GradientPaint(
                        0, 0, new Color(15, 10, 5),
                        getWidth(), getHeight(), new Color(60, 35, 15));
                g2.setPaint(grad);
                g2.fillRoundRect(0, 0, getWidth() + 20, getHeight(), 20, 20);

                // Vòng tròn trang trí lớn (blur-like)
                drawCircleDecor(g2, -40, -40, 260, new Color(188, 120, 60, 35));
                drawCircleDecor(g2, getWidth() - 80, getHeight() - 80, 220, new Color(98, 60, 30, 45));
                drawCircleDecor(g2, 60, getHeight() - 120, 180, new Color(210, 140, 50, 20));

                // Hạt cà phê trang trí nhỏ
                g2.setColor(new Color(210, 140, 50, 60));
                int[][] dots = {{40,180},{280,80},{300,340},{80,380},{200,440}};
                for (int[] d : dots) g2.fillOval(d[0]-5, d[1]-5, 10, 10);

                g2.dispose();
            }
            private void drawCircleDecor(Graphics2D g2, int x, int y, int size, Color c) {
                g2.setColor(c);
                g2.fillOval(x, y, size, size);
            }
        };
        left.setPreferredSize(new Dimension(340, 540));
        left.setOpaque(false);

        // Logo icon (giả lập bằng JLabel text lớn với nền tròn)
        JPanel iconWrap = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, COLOR_CARAMEL, getWidth(), getHeight(), COLOR_ACCENT);
                g2.setPaint(gp);
                g2.fill(new Ellipse2D.Double(0, 0, getWidth(), getHeight()));
                g2.dispose();
            }
        };
        iconWrap.setOpaque(false);
        iconWrap.setPreferredSize(new Dimension(90, 90));
        JLabel iconLbl = new JLabel("☕");
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        iconWrap.add(iconLbl);

        JLabel lblBrand = new JLabel("COFFEE 11:01");
        lblBrand.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblBrand.setForeground(COLOR_CREAM);
        lblBrand.setAlignmentX(CENTER_ALIGNMENT);

        JLabel lblTagline = new JLabel("<html><div style='text-align:center;'>Hệ thống quản lý<br>quán cà phê</div></html>");
        lblTagline.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lblTagline.setForeground(new Color(200, 175, 140));
        lblTagline.setHorizontalAlignment(SwingConstants.CENTER);
        lblTagline.setAlignmentX(CENTER_ALIGNMENT);

        // Đường kẻ trang trí
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(210, 140, 50, 120));
        sep.setMaximumSize(new Dimension(100, 1));

        // Feature bullets
        String[] features = {"☑  Quản lý bàn & đặt chỗ", "☑  Quản lý đơn hàng", "☑  Báo cáo doanh thu"};
        JPanel featuresPanel = new JPanel();
        featuresPanel.setLayout(new BoxLayout(featuresPanel, BoxLayout.Y_AXIS));
        featuresPanel.setOpaque(false);
        for (String f : features) {
            JLabel lbl = new JLabel(f);
            lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
            lbl.setForeground(new Color(170, 145, 110));
            lbl.setAlignmentX(CENTER_ALIGNMENT);
            lbl.setBorder(new EmptyBorder(4, 0, 4, 0));
            featuresPanel.add(lbl);
        }

        // Assemble left content
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(60, 40, 60, 30));

        iconWrap.setAlignmentX(CENTER_ALIGNMENT);
        content.add(iconWrap);
        content.add(Box.createVerticalStrut(20));
        content.add(lblBrand);
        content.add(Box.createVerticalStrut(8));
        content.add(lblTagline);
        content.add(Box.createVerticalStrut(24));
        content.add(sep);
        content.add(Box.createVerticalStrut(20));
        content.add(featuresPanel);

        left.setLayout(new BorderLayout());
        left.add(content, BorderLayout.CENTER);

        // Copyright bottom
        JLabel copy = new JLabel("© 2024 Coffee 11:01", SwingConstants.CENTER);
        copy.setFont(new Font("SansSerif", Font.PLAIN, 11));
        copy.setForeground(new Color(120, 95, 65));
        copy.setBorder(new EmptyBorder(0, 0, 18, 0));
        left.add(copy, BorderLayout.SOUTH);

        return left;
    }

    // ── RIGHT: Form panel ─────────────────────────────────────────────────
    private JPanel buildRightPanel() {
        JPanel right = new JPanel(new GridBagLayout());
        right.setBackground(COLOR_PANEL_BG);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setPreferredSize(new Dimension(340, 420));

        // Tiêu đề form
        JLabel lblWelcome = new JLabel("Chào mừng trở lại!");
        lblWelcome.setFont(new Font("SansSerif", Font.BOLD, 24));
        lblWelcome.setForeground(COLOR_DARK);
        lblWelcome.setAlignmentX(CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel("Vui lòng đăng nhập để tiếp tục");
        lblSub.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lblSub.setForeground(COLOR_WARM_GRAY);
        lblSub.setAlignmentX(CENTER_ALIGNMENT);

        // Username
        JLabel lblUser = makeFieldLabel("Tên đăng nhập");
        txtUsername = new JTextField();
        styleField(txtUsername);
        txtUsername.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập tên đăng nhập...");

        // Password
        JLabel lblPass = makeFieldLabel("Mật khẩu");
        txtPassword = new JPasswordField();
        styleField(txtPassword);
        txtPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập mật khẩu...");
        txtPassword.putClientProperty(FlatClientProperties.STYLE, "showRevealButton: true");

        // Login button
        JButton btnLogin = makeLoginButton();
        btnLogin.addActionListener(e -> handleLogin());
        getRootPane().setDefaultButton(btnLogin);

        // Version tag
        JLabel lblVersion = new JLabel("v1.0.0", SwingConstants.CENTER);
        lblVersion.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblVersion.setForeground(new Color(200, 185, 165));
        lblVersion.setAlignmentX(CENTER_ALIGNMENT);

        // Ghép form
        form.add(lblWelcome);
        form.add(Box.createVerticalStrut(6));
        form.add(lblSub);
        form.add(Box.createVerticalStrut(36));
        form.add(lblUser);
        form.add(Box.createVerticalStrut(7));
        form.add(txtUsername);
        form.add(Box.createVerticalStrut(18));
        form.add(lblPass);
        form.add(Box.createVerticalStrut(7));
        form.add(txtPassword);
        form.add(Box.createVerticalStrut(30));
        form.add(btnLogin);
        form.add(Box.createVerticalStrut(16));
        form.add(lblVersion);

        right.add(form);

        // ── Wrapper = BorderLayout: topBar (nút ✕) + form ────────────────
        JPanel rightWrapper = new JPanel(new BorderLayout());
        rightWrapper.setBackground(COLOR_PANEL_BG);

        // Top bar chứa nút đóng
        JButton btnClose = new JButton("✕");
        btnClose.setFont(new Font("SansSerif", Font.PLAIN, 14));
        btnClose.setForeground(COLOR_WARM_GRAY);
        btnClose.setContentAreaFilled(false);
        btnClose.setBorderPainted(false);
        btnClose.setFocusable(false);
        btnClose.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> System.exit(0));
        btnClose.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btnClose.setForeground(new Color(180, 60, 40)); }
            public void mouseExited(MouseEvent e) { btnClose.setForeground(COLOR_WARM_GRAY); }
        });

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.TRAILING, 10, 6));
        topBar.setOpaque(false);
        topBar.add(btnClose);

        rightWrapper.add(topBar, BorderLayout.NORTH);
        rightWrapper.add(right, BorderLayout.CENTER);

        // Make draggable
        addDragSupport(right);

        return rightWrapper;
    }

    private JLabel makeFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        lbl.setForeground(new Color(80, 60, 40));
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        return lbl;
    }

    private void styleField(JTextField field) {
        field.setAlignmentX(CENTER_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        field.setBackground(COLOR_FIELD_BG);
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setForeground(COLOR_DARK);
        field.putClientProperty(FlatClientProperties.STYLE,
                "arc: 10; borderColor: #DCC8AF; focusedBorderColor: #BC783C;");
    }

    private JButton makeLoginButton() {
        JButton btn = new JButton("ĐĂNG NHẬP") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(new Color(80, 50, 20));
                } else if (getModel().isRollover()) {
                    GradientPaint gp = new GradientPaint(0, 0, new Color(215, 148, 62),
                            getWidth(), getHeight(), new Color(120, 75, 35));
                    g2.setPaint(gp);
                } else {
                    GradientPaint gp = new GradientPaint(0, 0, COLOR_CARAMEL,
                            getWidth(), getHeight(), COLOR_BROWN);
                    g2.setPaint(gp);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                // Shadow line
                g2.setColor(new Color(0, 0, 0, 30));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        btn.setAlignmentX(CENTER_ALIGNMENT);
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusable(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setText("ĐĂNG NHẬP");
        return btn;
    }

    /** Cho phép kéo cửa sổ khi undecorated */
    private final int[] dragOffset = {0, 0};
    private void addDragSupport(JPanel panel) {
        panel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                dragOffset[0] = e.getX();
                dragOffset[1] = e.getY();
            }
        });
        panel.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                Point loc = getLocation();
                setLocation(loc.x + e.getX() - dragOffset[0], loc.y + e.getY() - dragOffset[1]);
            }
        });
    }

    // ── Business logic (giữ nguyên) ────────────────────────────────────────
    private void handleLogin() {
        try {
            NhanVien nv = authController.login(txtUsername.getText(), new String(txtPassword.getPassword()));

            CaLamViec caHienTai = shiftController.kiemTraCaHienTai();

            if (caHienTai != null) {
                openMainFrame();
            } else if (VaiTro.NHAN_VIEN.equals(nv.getVaiTro())) {
                boolean daChapNhanMoCa = showShiftOpenDialog();
                if (!daChapNhanMoCa) {
                    authController.logout();
                    return;
                }
                openMainFrame();
            } else {
                int choice = JOptionPane.showOptionDialog(this,
                        "Bạn có muốn mở ca làm việc không?\n(Bỏ qua nếu chỉ cần xem báo cáo/quản trị)",
                        "Mở ca làm việc",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        new String[]{"Mở Ca", "Bỏ Qua"},
                        "Bỏ Qua");
                if (choice == 0) showShiftOpenDialog();
                openMainFrame();
            }

        } catch (AppException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi đăng nhập", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi kết nối cơ sở dữ liệu!", "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean showShiftOpenDialog() {
        ui.dialog.ShiftOpenDialog dlg = new ui.dialog.ShiftOpenDialog(this, shiftController);
        dlg.setVisible(true);
        return dlg.isShiftOpened();
    }

    private void openMainFrame() {
        SwingUtilities.invokeLater(() -> {
            new MainFrame().setVisible(true);
            this.dispose();
        });
    }

    // ── Entry point ────────────────────────────────────────────────────────
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
            UIManager.put("defaultFont", new Font("SansSerif", Font.PLAIN, 14));
        } catch (Exception e) {
            System.err.println("FlatLaf init failed");
        }
        jiconfont.swing.IconFontSwing.register(jiconfont.icons.FontAwesome.getIconFont());
        SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
    }

}