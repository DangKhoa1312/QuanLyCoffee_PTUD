package ui.dialog;

import com.toedter.calendar.JDateChooser;
import controller.NhanVienController;
import entity.NhanVien;
import jiconfont.swing.IconFontSwing;
import utils.PasswordUtils;
import jiconfont.icons.FontAwesome;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class UserProfileDialog extends JDialog {

    private NhanVien currentUser;
    private NhanVienController controller;

    // Fields
    private JTextField txtMaNV;
    private JTextField txtUsername;
    private JTextField txtRole;
    private JTextField txtTen;
    private JDateChooser dcNgaySinh;
    private JTextField txtPhone;
    private JTextField txtDiaChi;

    private JPasswordField txtCurrentPwd;
    private JPasswordField txtNewPwd;
    private JPasswordField txtConfirmPwd;
    
    private boolean isUpdated = false;

    public UserProfileDialog(Frame parent, NhanVien nv) {
        super(parent, "Hồ Sơ Cá Nhân", true);
        this.currentUser = nv;
        this.controller = new NhanVienController();

        setSize(850, 680);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        initUI();
        loadData();
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 0, 0));
        mainPanel.setBackground(Color.WHITE);

        // ==========================
        // LEFT COLUMN: Personal Info
        // ==========================
        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        GridBagConstraints gbcL = new GridBagConstraints();
        gbcL.gridx = 0; gbcL.gridy = 0;
        gbcL.fill = GridBagConstraints.HORIZONTAL;
        gbcL.weightx = 1.0;
        gbcL.insets = new Insets(0, 0, 5, 0);

        // Avatar (Centered)
        JPanel avatarPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        avatarPanel.setBackground(Color.WHITE);
        JLabel lblAvatar = new JLabel(IconFontSwing.buildIcon(FontAwesome.USER_CIRCLE_O, 80, new Color(41, 128, 185)));
        avatarPanel.add(lblAvatar);
        leftPanel.add(avatarPanel, gbcL);

        // Title
        gbcL.gridy++;
        gbcL.insets = new Insets(5, 0, 15, 0);
        JLabel titleLeft = new JLabel("Thông tin chung", SwingConstants.CENTER);
        titleLeft.setFont(new Font("Roboto", Font.BOLD, 18));
        titleLeft.setForeground(new Color(44, 62, 80));
        leftPanel.add(titleLeft, gbcL);

        // Fields Definition Helper
        int smallGap = 5;
        int largeGap = 12;

        // Mã NV (Read-only)
        gbcL.insets = new Insets(0, 0, smallGap, 0);
        gbcL.gridy++; leftPanel.add(createLabel("Mã nhân viên"), gbcL);
        gbcL.insets = new Insets(0, 0, largeGap, 0);
        txtMaNV = createReadOnlyField();
        gbcL.gridy++; leftPanel.add(txtMaNV, gbcL);

        // Tên đăng nhập (Read-only)
        gbcL.insets = new Insets(0, 0, smallGap, 0);
        gbcL.gridy++; leftPanel.add(createLabel("Tên đăng nhập"), gbcL);
        gbcL.insets = new Insets(0, 0, largeGap, 0);
        txtUsername = createReadOnlyField();
        gbcL.gridy++; leftPanel.add(txtUsername, gbcL);

        // Chức vụ (Read-only)
        gbcL.insets = new Insets(0, 0, smallGap, 0);
        gbcL.gridy++; leftPanel.add(createLabel("Vai trò"), gbcL);
        gbcL.insets = new Insets(0, 0, largeGap, 0);
        txtRole = createReadOnlyField();
        gbcL.gridy++; leftPanel.add(txtRole, gbcL);

        // Họ và tên
        gbcL.insets = new Insets(0, 0, smallGap, 0);
        gbcL.gridy++; leftPanel.add(createLabel("Họ và tên *"), gbcL);
        gbcL.insets = new Insets(0, 0, largeGap, 0);
        txtTen = createTextField();
        gbcL.gridy++; leftPanel.add(txtTen, gbcL);

        // Ngày sinh
        gbcL.insets = new Insets(0, 0, smallGap, 0);
        gbcL.gridy++; leftPanel.add(createLabel("Ngày sinh"), gbcL);
        gbcL.insets = new Insets(0, 0, largeGap, 0);
        dcNgaySinh = new JDateChooser();
        dcNgaySinh.setDateFormatString("dd/MM/yyyy");
        dcNgaySinh.setFont(new Font("Roboto", Font.PLAIN, 14));
        dcNgaySinh.setPreferredSize(new Dimension(0, 35));
        gbcL.gridy++; leftPanel.add(dcNgaySinh, gbcL);

        // Số điện thoại
        gbcL.insets = new Insets(0, 0, smallGap, 0);
        gbcL.gridy++; leftPanel.add(createLabel("Số điện thoại"), gbcL);
        gbcL.insets = new Insets(0, 0, largeGap, 0);
        txtPhone = createTextField();
        gbcL.gridy++; leftPanel.add(txtPhone, gbcL);

        // Địa chỉ
        gbcL.insets = new Insets(0, 0, smallGap, 0);
        gbcL.gridy++; leftPanel.add(createLabel("Địa chỉ"), gbcL);
        gbcL.insets = new Insets(0, 0, 0, 0);
        txtDiaChi = createTextField();
        gbcL.gridy++; leftPanel.add(txtDiaChi, gbcL);


        // ==========================
        // RIGHT COLUMN: Security
        // ==========================
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(new Color(252, 252, 252));
        rightPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(230, 235, 240)),
            new EmptyBorder(30, 40, 30, 40)
        ));
        GridBagConstraints gbcR = new GridBagConstraints();
        gbcR.gridx = 0; gbcR.gridy = 0;
        gbcR.fill = GridBagConstraints.HORIZONTAL;
        gbcR.weightx = 1.0;

        // Title
        gbcR.insets = new Insets(0, 0, 5, 0);
        JLabel titleRight = new JLabel("Đổi mật khẩu", SwingConstants.LEFT);
        titleRight.setFont(new Font("Roboto", Font.BOLD, 18));
        titleRight.setForeground(new Color(44, 62, 80));
        rightPanel.add(titleRight, gbcR);

        // Notice
        gbcR.gridy++;
        gbcR.insets = new Insets(0, 0, 30, 0);
        JLabel lblNotice = new JLabel("Bỏ trống nếu bạn không muốn thay đổi");
        lblNotice.setFont(new Font("Roboto", Font.ITALIC, 13));
        lblNotice.setForeground(new Color(127, 140, 141));
        rightPanel.add(lblNotice, gbcR);

        // Fields
        gbcR.insets = new Insets(0, 0, 8, 0);
        
        gbcR.gridy++;
        rightPanel.add(createLabel("Mật khẩu hiện tại"), gbcR);
        gbcR.gridy++;
        gbcR.insets = new Insets(0, 0, 20, 0);
        txtCurrentPwd = createPasswordField();
        rightPanel.add(txtCurrentPwd, gbcR);

        gbcR.gridy++;
        gbcR.insets = new Insets(0, 0, 8, 0);
        rightPanel.add(createLabel("Mật khẩu mới"), gbcR);
        gbcR.gridy++;
        gbcR.insets = new Insets(0, 0, 20, 0);
        txtNewPwd = createPasswordField();
        rightPanel.add(txtNewPwd, gbcR);

        gbcR.gridy++;
        gbcR.insets = new Insets(0, 0, 8, 0);
        rightPanel.add(createLabel("Xác nhận mật khẩu mới"), gbcR);
        gbcR.gridy++;
        gbcR.insets = new Insets(0, 0, 0, 0);
        txtConfirmPwd = createPasswordField();
        rightPanel.add(txtConfirmPwd, gbcR);
        
        // Push everything to top
        gbcR.gridy++;
        gbcR.weighty = 1.0;
        rightPanel.add(Box.createGlue(), gbcR);

        mainPanel.add(leftPanel);
        mainPanel.add(rightPanel);
        add(mainPanel, BorderLayout.CENTER);

        // ==========================
        // FOOTER: Buttons
        // ==========================
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        footerPanel.setBackground(Color.WHITE);
        footerPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230, 235, 240)));

        JButton btnCancel = new JButton("Hủy bỏ");
        btnCancel.setPreferredSize(new Dimension(110, 38));
        btnCancel.setFont(new Font("Roboto", Font.PLAIN, 14));
        btnCancel.setForeground(new Color(90, 90, 90));
        btnCancel.setBackground(new Color(240, 240, 240));
        btnCancel.setFocusPainted(false);
        btnCancel.setBorder(BorderFactory.createEmptyBorder());
        btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = new JButton(" Lưu thay đổi ");
        btnSave.setPreferredSize(new Dimension(140, 38));
        btnSave.setFont(new Font("Roboto", Font.BOLD, 14));
        btnSave.setBackground(new Color(41, 128, 185));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.setBorder(BorderFactory.createEmptyBorder());
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSave.setIcon(IconFontSwing.buildIcon(FontAwesome.CHECK, 16, Color.WHITE));
        btnSave.addActionListener(e -> handleSave());

        footerPanel.add(btnCancel);
        footerPanel.add(btnSave);
        add(footerPanel, BorderLayout.SOUTH);
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Roboto", Font.BOLD, 13));
        lbl.setForeground(new Color(90, 100, 110));
        return lbl;
    }

    private JTextField createTextField() {
        JTextField tf = new JTextField();
        tf.setPreferredSize(new Dimension(0, 35));
        tf.setFont(new Font("Roboto", Font.PLAIN, 14));
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(210, 215, 220), 1, true),
            new EmptyBorder(0, 12, 0, 12)
        ));
        return tf;
    }

    private JTextField createReadOnlyField() {
        JTextField tf = createTextField();
        tf.setEditable(false);
        tf.setBackground(new Color(245, 247, 250));
        tf.setForeground(new Color(149, 165, 166));
        return tf;
    }

    private JPasswordField createPasswordField() {
        JPasswordField pf = new JPasswordField();
        pf.setPreferredSize(new Dimension(0, 35));
        pf.setFont(new Font("Roboto", Font.PLAIN, 14));
        pf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(210, 215, 220), 1, true),
            new EmptyBorder(0, 12, 0, 12)
        ));
        return pf;
    }

    private void loadData() {
        txtMaNV.setText(currentUser.getMaNV());
        txtUsername.setText(currentUser.getUsername());
        txtRole.setText(currentUser.getVaiTro() != null ? currentUser.getVaiTro().toString() : "");
        
        txtTen.setText(currentUser.getTenNV());
        if (currentUser.getNgaySinh() != null) {
            dcNgaySinh.setDate(Date.from(currentUser.getNgaySinh().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }
        txtPhone.setText(currentUser.getSoDienThoai() != null ? currentUser.getSoDienThoai() : "");
        txtDiaChi.setText(currentUser.getDiaChi() != null ? currentUser.getDiaChi() : "");
    }

    private void handleSave() {
        String newName = txtTen.getText().trim();
        String newPhone = txtPhone.getText().trim();
        String newAddress = txtDiaChi.getText().trim();

        if (newName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Họ và tên không được để trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtTen.requestFocus();
            return;
        }

        String curPwdStr = new String(txtCurrentPwd.getPassword());
        String newPwdStr = new String(txtNewPwd.getPassword());
        String confirmPwdStr = new String(txtConfirmPwd.getPassword());

        boolean isChangePassword = !newPwdStr.isEmpty() || !confirmPwdStr.isEmpty() || !curPwdStr.isEmpty();

        if (isChangePassword) {
            // Xác thực mật khẩu cũ
            if (!PasswordUtils.verify(curPwdStr, currentUser.getPasswordHash())) {
                JOptionPane.showMessageDialog(this, "Mật khẩu hiện tại không đúng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                txtCurrentPwd.requestFocus();
                return;
            }
            if (newPwdStr.length() < 6) {
                JOptionPane.showMessageDialog(this, "Mật khẩu mới phải từ 6 ký tự trở lên!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                txtNewPwd.requestFocus();
                return;
            }
            if (!newPwdStr.equals(confirmPwdStr)) {
                JOptionPane.showMessageDialog(this, "Xác nhận mật khẩu không khớp!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                txtConfirmPwd.requestFocus();
                return;
            }
        }

        // Cập nhật Entity
        currentUser.setTenNV(newName);
        currentUser.setSoDienThoai(newPhone);
        currentUser.setDiaChi(newAddress);
        
        if (dcNgaySinh.getDate() != null) {
            LocalDate birthDate = dcNgaySinh.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            currentUser.setNgaySinh(birthDate);
        } else {
            currentUser.setNgaySinh(null);
        }

        if (isChangePassword) {
            currentUser.setPasswordHash(newPwdStr); // Chưa băm, sẽ được băm ở Controller
        } else {
            currentUser.setPasswordHash(""); // Đánh dấu là không đổi mật khẩu
        }

        try {
            if (controller.updateSelfProfile(currentUser)) {
                JOptionPane.showMessageDialog(this, "Cập nhật hồ sơ thành công!");
                isUpdated = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Có lỗi xảy ra khi lưu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isUpdated() {
        return isUpdated;
    }
}
