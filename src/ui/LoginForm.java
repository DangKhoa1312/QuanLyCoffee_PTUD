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

public class LoginForm extends JFrame {

    private final AuthController authController = new AuthController();
    private final ShiftController shiftController = new ShiftController();

    private JTextField txtUsername;
    private JPasswordField txtPassword;

    public LoginForm() {
        setTitle("COFFEE 11:01 - Đăng Nhập");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(460, 560);
        setLocationRelativeTo(null);
        setResizable(false);

        initUI();
    }

    private void initUI() {
        // Nền toàn frame: tối navy
        JPanel bg = new JPanel(new GridBagLayout());
        bg.setBackground(new Color(26, 26, 46));
        setContentPane(bg);

        // Card trung tâm
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(new EmptyBorder(45, 45, 45, 45));
        card.setPreferredSize(new Dimension(380, 440));

        // Logo
        JLabel lblIcon = new JLabel("\u2615", SwingConstants.CENTER);
        lblIcon.setFont(new Font("Roboto", Font.PLAIN, 52));
        lblIcon.setAlignmentX(CENTER_ALIGNMENT);

        JLabel lblTitle = new JLabel("COFFEE 11:01", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Roboto", Font.BOLD, 26));
        lblTitle.setForeground(new Color(26, 26, 46));
        lblTitle.setAlignmentX(CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel("Hệ thống quản lý bán hàng", SwingConstants.CENTER);
        lblSub.setFont(new Font("Roboto", Font.PLAIN, 13));
        lblSub.setForeground(new Color(160, 160, 160));
        lblSub.setAlignmentX(CENTER_ALIGNMENT);

        // Username field
        JLabel lblUser = new JLabel("Tên đăng nhập", SwingConstants.LEFT);
        lblUser.setFont(new Font("Roboto", Font.BOLD, 12));
        lblUser.setForeground(new Color(100, 100, 100));
        lblUser.setAlignmentX(CENTER_ALIGNMENT);
        lblUser.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

        txtUsername = new JTextField();
        txtUsername.setAlignmentX(CENTER_ALIGNMENT);
        txtUsername.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        txtUsername.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Username");

        // Password field
        JLabel lblPass = new JLabel("Mật khẩu", SwingConstants.LEFT);
        lblPass.setFont(new Font("Roboto", Font.BOLD, 12));
        lblPass.setForeground(new Color(100, 100, 100));
        lblPass.setAlignmentX(CENTER_ALIGNMENT);
        lblPass.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

        txtPassword = new JPasswordField();
        txtPassword.setAlignmentX(CENTER_ALIGNMENT);
        txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        txtPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập mật khẩu");
        txtPassword.putClientProperty(FlatClientProperties.STYLE, "showRevealButton: true");

        // Login button
        JButton btnLogin = new JButton("ĐĂNG NHẬP");
        btnLogin.setFont(new Font("Roboto", Font.BOLD, 15));
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btnLogin.setAlignmentX(CENTER_ALIGNMENT);
        btnLogin.setBackground(new Color(41, 128, 185));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusable(false);
        btnLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnLogin.addActionListener(e -> handleLogin());
        getRootPane().setDefaultButton(btnLogin);

        // Ghép card
        card.add(lblIcon);
        card.add(Box.createVerticalStrut(8));
        card.add(lblTitle);
        card.add(Box.createVerticalStrut(4));
        card.add(lblSub);
        card.add(Box.createVerticalStrut(30));
        card.add(lblUser);
        card.add(Box.createVerticalStrut(6));
        card.add(txtUsername);
        card.add(Box.createVerticalStrut(16));
        card.add(lblPass);
        card.add(Box.createVerticalStrut(6));
        card.add(txtPassword);
        card.add(Box.createVerticalStrut(28));
        card.add(btnLogin);

        bg.add(card);
    }

    private void handleLogin() {
        try {
            NhanVien nv = authController.login(txtUsername.getText(), new String(txtPassword.getPassword()));

            // Kiểm tra ca hiện tại
            CaLamViec caHienTai = shiftController.kiemTraCaHienTai();

            if (caHienTai != null) {
                // Đã có ca đang mở → vào thẳng MainFrame
                openMainFrame();
            } else if (VaiTro.NHAN_VIEN.equals(nv.getVaiTro())) {

                boolean daChapNhanMoCa = showShiftOpenDialog();
                if (!daChapNhanMoCa) {
                    authController.logout();
                    return; // Ở lại LoginForm
                }
                openMainFrame();
            } else {
                // QUAN_LY: hỏi có muốn mở ca không
                int choice = JOptionPane.showOptionDialog(this,
                        "Bạn có muốn mở ca làm việc không?\n(Bỏ qua nếu chỉ cần xem báo cáo/quản trị)",
                        "Mở ca làm việc",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        new String[] { "Mở Ca", "Bỏ Qua" },
                        "Bỏ Qua");
                if (choice == 0) {
                    showShiftOpenDialog();
                }
                openMainFrame();
            }

        } catch (AppException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi đăng nhập", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi kết nối cơ sở dữ liệu!", "Lỗi hệ thống",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Hiện dialog mở ca. Trả về true nếu ca được mở thành công.
     */
    private boolean showShiftOpenDialog() {
        ui.dialog.ShiftOpenDialog dlg = new ui.dialog.ShiftOpenDialog(this, shiftController);
        dlg.setVisible(true); // Modal, chặn ở đây
        return dlg.isShiftOpened();
    }

    private void openMainFrame() {
        SwingUtilities.invokeLater(() -> {
            new MainFrame().setVisible(true);
            this.dispose();
        });
    }

    // ── Entry point ──────────────────────────────────────────────────────
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
            UIManager.put("defaultFont", new Font("Roboto", Font.PLAIN, 14));
        } catch (Exception e) {
            System.err.println("FlatLaf init failed");
        }
        SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
    }
}