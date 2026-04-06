package ui.dialog;

import entity.NhanVien;
import enums.TrangThaiNhanVien;
import enums.VaiTro;
import jiconfont.icons.FontAwesome;
import jiconfont.swing.IconFontSwing;
import com.toedter.calendar.JDateChooser;
import java.util.Date;
import java.time.ZoneId;
import java.time.Period;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.LocalDate;
import utils.ValidationUtils;
import utils.SessionManager;

/**
 * StaffDialog phiên bản Hybrid: Kết hợp thiết kế Card hiện đại & chuẩn Java UX.
 */
public class StaffDialog extends JDialog {

    private boolean confirmed = false;
    private final NhanVien employee;
    private final boolean isEditMode;

    private JTextField txtMaNV, txtTenNV, txtSDT, txtDiaChi, txtUsername;
    private JDateChooser txtNgaySinh;
    private JPasswordField txtPassword;
    private JComboBox<VaiTro> cbVaiTro;
    private JComboBox<TrangThaiNhanVien> cbTrangThai;
    private JButton btnSave;

    private final Color PRIMARY_COLOR = new Color(41, 128, 185); // Xanh chuyên nghiệp
    private final Color BG_COLOR = new Color(245, 247, 250); // Xám nhạt nền

    public StaffDialog(Frame parent, NhanVien nv, boolean isEditMode) {
        super(parent, isEditMode ? "Cập nhật Nhân Viên" : "Thêm Nhân Viên Mới", true);
        this.employee = nv;
        this.isEditMode = isEditMode;

        initUI();
        fillData();
    }

    private void initUI() {
        setSize(850, 600); // Rộng hơn để chia 2 cột
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_COLOR);

        // 1. Header
        add(createHeader(), BorderLayout.NORTH);

        // 2. Central Body (2 Cards)
        JPanel body = new JPanel(new GridLayout(1, 2, 20, 0));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(20, 20, 20, 20));

        body.add(createPersonalInfoCard());
        body.add(createAccountInfoCard());

        add(body, BorderLayout.CENTER);

        // 3. Footer Buttons
        add(createFooter(), BorderLayout.SOUTH);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY_COLOR);
        header.setPreferredSize(new Dimension(0, 70));
        header.setBorder(new EmptyBorder(0, 25, 0, 25));

        JLabel title = new JLabel(" THÔNG TIN CHI TIẾT NHÂN VIÊN");
        title.setFont(new Font("Roboto", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        title.setIcon(IconFontSwing.buildIcon(FontAwesome.USER_CIRCLE_O, 32, Color.WHITE));

        header.add(title, BorderLayout.WEST);

        // Breadcrumb giả lập ở góc phải
        JLabel breadcrumb = new JLabel("Admin > Nhân viên > " + (isEditMode ? "Cập nhật" : "Thêm mới"));
        breadcrumb.setForeground(new Color(236, 240, 241));
        breadcrumb.setFont(new Font("Roboto", Font.ITALIC, 13));
        header.add(breadcrumb, BorderLayout.EAST);

        return header;
    }

    private JPanel createPersonalInfoCard() {
        JPanel card = createCardPanel("THÔNG TIN CÁ NHÂN");
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = createGBC();

        txtTenNV = addInputRow(form, gbc, "Họ và Tên*:", FontAwesome.USER);

        addLabelRow(form, gbc, "Ngày Sinh*:", FontAwesome.CALENDAR);
        txtNgaySinh = new JDateChooser();
        txtNgaySinh.setDateFormatString("yyyy-MM-dd");
        txtNgaySinh.setPreferredSize(new Dimension(0, 35));
        form.add(txtNgaySinh, gbc);
        gbc.gridy++;

        txtSDT = addInputRow(form, gbc, "Số Điện Thoại*:", FontAwesome.PHONE);
        txtDiaChi = addInputRow(form, gbc, "Địa Chỉ:", FontAwesome.MAP_MARKER);

        card.add(form, BorderLayout.CENTER);
        return card;
    }

    private JPanel createAccountInfoCard() {
        JPanel card = createCardPanel("TÀI KHOẢN & HỆ THỐNG");
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = createGBC();

        txtMaNV = addInputRow(form, gbc, "M\u00E3 Nh\u00E2n Vi\u00EAn:", FontAwesome.ID_CARD_O);
        txtMaNV.setEditable(false); // Lu\u00F4n kh\u00F4ng cho ph\u00E9p s\u1EEDa m\u00E3

        txtUsername = addInputRow(form, gbc, "Tên Đăng Nhập*:", FontAwesome.AT);
        if (isEditMode)
            txtUsername.setEditable(false);

        if (!isEditMode) {
            txtPassword = (JPasswordField) addInputRow(form, gbc, "Mật Khẩu:", FontAwesome.LOCK, true);
        }

        addLabelRow(form, gbc, "Vai Trò:", FontAwesome.SHIELD);
        cbVaiTro = new JComboBox<>(VaiTro.values());
        form.add(cbVaiTro, gbc);
        gbc.gridy++;

        addLabelRow(form, gbc, "Trạng Thái:", FontAwesome.TOGGLE_ON);
        cbTrangThai = new JComboBox<>(TrangThaiNhanVien.values());
        form.add(cbTrangThai, gbc);

        // Nếu là chính mình đang đăng nhập thì không cho sửa Vai trò & Trạng thái
        if (employee.getMaNV() != null && employee.getMaNV().equals(SessionManager.getMaNVHienTai())) {
            cbVaiTro.setEnabled(false);
            cbTrangThai.setEnabled(false);
        }

        card.add(form, BorderLayout.CENTER);
        return card;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 15));
        footer.setOpaque(false);

        btnSave = new JButton(" LƯU DỮ LIỆU");
        btnSave.setPreferredSize(new Dimension(150, 40));
        btnSave.setBackground(new Color(46, 204, 113));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Roboto", Font.BOLD, 13));
        btnSave.setIcon(IconFontSwing.buildIcon(FontAwesome.FLOPPY_O, 16, Color.WHITE));
        btnSave.addActionListener(e -> handleSave());
        // Disable Save button initially in edit mode until changes are made
        if (isEditMode)
            btnSave.setEnabled(false);

        if (isEditMode) {
            // Không cho phép tự xóa bản thân
            if (!employee.getMaNV().equals(SessionManager.getMaNVHienTai())) {
                JButton btnDelete = new JButton("XÓA ");
                btnDelete.setPreferredSize(new Dimension(150, 40));
                btnDelete.setBackground(new Color(231, 76, 60));
                btnDelete.setForeground(Color.WHITE);
                btnDelete.setFont(new Font("Roboto", Font.BOLD, 13));
                btnDelete.setIcon(IconFontSwing.buildIcon(FontAwesome.TRASH, 16, Color.WHITE));
                btnDelete.addActionListener(e -> handleDelete());
                footer.add(btnDelete);
            }
        }

        JButton btnCancel = new JButton("HỦY BỎ");
        btnCancel.setPreferredSize(new Dimension(100, 40));
        btnCancel.setFont(new Font("Roboto", Font.PLAIN, 13));
        btnCancel.addActionListener(e -> dispose());

        footer.add(btnCancel);
        footer.add(btnSave);

        setupDirtyCheck();

        return footer;
    }

    private void handleDelete() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn cho nhân viên " + employee.getTenNV() + " nghỉ việc?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            employee.setTrangThai(TrangThaiNhanVien.DA_NGHI);
            confirmed = true;
            dispose();
        }
    }

    private void setupDirtyCheck() {
        java.awt.event.ActionListener checkListener = e -> checkDirty();
        javax.swing.event.DocumentListener docListener = new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                checkDirty();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                checkDirty();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                checkDirty();
            }
        };

        txtTenNV.getDocument().addDocumentListener(docListener);
        txtSDT.getDocument().addDocumentListener(docListener);
        txtDiaChi.getDocument().addDocumentListener(docListener);
        if (txtUsername != null)
            txtUsername.getDocument().addDocumentListener(docListener);
        if (txtPassword != null)
            txtPassword.getDocument().addDocumentListener(docListener);

        cbVaiTro.addActionListener(checkListener);
        cbTrangThai.addActionListener(checkListener);
        txtNgaySinh.addPropertyChangeListener("date", e -> checkDirty());
    }

    private void checkDirty() {
        if (!isEditMode) {
            btnSave.setEnabled(true);
            return;
        }

        boolean dirty = false;
        if (!txtTenNV.getText().equals(employee.getTenNV()))
            dirty = true;
        if (!txtSDT.getText().equals(employee.getSoDienThoai()))
            dirty = true;
        if (!txtDiaChi.getText().equals(employee.getDiaChi()))
            dirty = true;
        if (cbVaiTro.getSelectedItem() != employee.getVaiTro())
            dirty = true;
        if (cbTrangThai.getSelectedItem() != employee.getTrangThai())
            dirty = true;

        LocalDate currentBirth = null;
        if (txtNgaySinh.getDate() != null) {
            currentBirth = txtNgaySinh.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        if (currentBirth != null && !currentBirth.equals(employee.getNgaySinh()))
            dirty = true;
        if (currentBirth == null && employee.getNgaySinh() != null)
            dirty = true;

        btnSave.setEnabled(dirty);
    }

    // --- HELPER METHODS FOR CLEAN UI ---

    private JPanel createCardPanel(String title) {
        JPanel p = new JPanel(new BorderLayout(0, 15));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(230, 230, 230), 1),
                new EmptyBorder(20, 20, 20, 20)));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Roboto", Font.BOLD, 14));
        lblTitle.setForeground(PRIMARY_COLOR);
        lblTitle.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 240, 240)));
        p.add(lblTitle, BorderLayout.NORTH);

        return p;
    }

    private JTextField addInputRow(JPanel p, GridBagConstraints gbc, String labelText, FontAwesome icon) {
        return (JTextField) addInputRow(p, gbc, labelText, icon, false);
    }

    private JComponent addInputRow(JPanel p, GridBagConstraints gbc, String labelText, FontAwesome icon,
            boolean isPassword) {
        addLabelRow(p, gbc, labelText, icon);

        JComponent field = isPassword ? new JPasswordField("123456") : new JTextField();
        field.setPreferredSize(new Dimension(0, 35));
        p.add(field, gbc);
        gbc.gridy++;
        return field;
    }

    private void addLabelRow(JPanel p, GridBagConstraints gbc, String text, FontAwesome icon) {
        JLabel lbl = new JLabel(" " + text);
        lbl.setIcon(IconFontSwing.buildIcon(icon, 14, Color.GRAY));
        lbl.setFont(new Font("Roboto", Font.BOLD, 12));
        gbc.insets = new Insets(10, 0, 5, 0);
        p.add(lbl, gbc);
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 0, 0);
    }

    private GridBagConstraints createGBC() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        return gbc;
    }

    private void fillData() {
        txtMaNV.setText(employee.getMaNV());
        txtTenNV.setText(employee.getTenNV());
        if (employee.getNgaySinh() != null) {
            txtNgaySinh.setDate(Date.from(employee.getNgaySinh().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }
        txtSDT.setText(employee.getSoDienThoai());
        txtDiaChi.setText(employee.getDiaChi());
        txtUsername.setText(employee.getUsername());
        cbVaiTro.setSelectedItem(employee.getVaiTro());
        cbTrangThai.setSelectedItem(employee.getTrangThai());
    }

    private boolean validateInput() {
        String tenNV = txtTenNV.getText().trim();
        if (ValidationUtils.isEmpty(tenNV)) {
            JOptionPane.showMessageDialog(this, "Họ và tên không được để trống!", "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE);
            txtTenNV.requestFocus();
            return false;
        }

        Date birthDate = txtNgaySinh.getDate();
        if (birthDate == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ngày sinh!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            txtNgaySinh.requestFocus();
            return false;
        }

        LocalDate localBirth = birthDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        if (Period.between(localBirth, LocalDate.now()).getYears() < 18) {
            JOptionPane.showMessageDialog(this, "Nhân viên phải đủ 18 tuổi!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        String sdt = txtSDT.getText().trim();
        if (!sdt.matches("\\d{10}")) {
            JOptionPane.showMessageDialog(this, "Số điện thoại phải đủ 10 chữ số và không chứa ký tự khác!", "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE);
            txtSDT.requestFocus();
            return false;
        }

        if (ValidationUtils.isEmpty(txtUsername.getText())) {
            JOptionPane.showMessageDialog(this, "Tên đăng nhập không được để trống!", "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE);
            txtUsername.requestFocus();
            return false;
        }

        if (!isEditMode && ValidationUtils.isEmpty(new String(txtPassword.getPassword()))) {
            JOptionPane.showMessageDialog(this, "Mật khẩu không được để trống!", "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE);
            txtPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void handleSave() {
        if (!validateInput())
            return;

        try {
            employee.setMaNV(txtMaNV.getText().trim());
            employee.setTenNV(txtTenNV.getText().trim());
            if (txtNgaySinh.getDate() != null) {
                employee.setNgaySinh(txtNgaySinh.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            }
            employee.setSoDienThoai(txtSDT.getText().trim());
            employee.setDiaChi(txtDiaChi.getText().trim());
            employee.setUsername(txtUsername.getText().trim());
            employee.setVaiTro((VaiTro) cbVaiTro.getSelectedItem());
            employee.setTrangThai((TrangThaiNhanVien) cbTrangThai.getSelectedItem());

            if (!isEditMode && txtPassword != null) {
                employee.setPasswordHash(new String(txtPassword.getPassword()));
            }

            confirmed = true;
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi lưu dữ liệu: " + e.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}
