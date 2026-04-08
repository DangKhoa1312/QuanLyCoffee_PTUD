package ui.dialog;

import entity.NhaCungCap;
import jiconfont.icons.FontAwesome;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Dialog thêm/sửa Nhà Cung Cấp — phong cách giống StaffDialog.
 * Constructor nhận (Frame, NhaCungCap, boolean isEdit).
 */
public class NhaCungCapDialog extends JDialog {

    private JTextField txtMaNCC, txtTenNCC, txtDiaChi, txtSDT, txtEmail;
    private boolean confirmed = false;
    private boolean deleted   = false;
    private final NhaCungCap current;
    private final boolean isEdit;

    private final Color PRIMARY_COLOR = new Color(41, 128, 185);

    public NhaCungCapDialog(Frame owner, NhaCungCap ncc, boolean isEdit) {
        super(owner, isEdit ? "Chi Tiết Nhà Cung Cấp" : "Thêm Nhà Cung Cấp Mới", true);
        this.current = ncc;
        this.isEdit = isEdit;
        setSize(700, 600);
        setLocationRelativeTo(owner);
        setResizable(false);

        initUI();
        populateData();
    }

    public boolean isConfirmed() { return confirmed; }
    public boolean isDeleted()   { return deleted; }

    private void initUI() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(245, 247, 250));

        // ── Header ──
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY_COLOR);
        header.setPreferredSize(new Dimension(0, 60));
        header.setBorder(new EmptyBorder(0, 25, 0, 25));

        JLabel title = new JLabel(" THÔNG TIN NHÀ CUNG CẤP");
        title.setFont(new Font("Roboto", Font.BOLD, 17));
        title.setForeground(Color.WHITE);
        title.setIcon(IconFontSwing.buildIcon(FontAwesome.TRUCK, 24, Color.WHITE));
        header.add(title, BorderLayout.WEST);

        JLabel breadcrumb = new JLabel("Kho > Nhà cung cấp > " + (isEdit ? "Chi tiết" : "Thêm mới"));
        breadcrumb.setForeground(new Color(236, 240, 241));
        breadcrumb.setFont(new Font("Roboto", Font.ITALIC, 12));
        header.add(breadcrumb, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // ── Body — Form Card ──
        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(20, 25, 10, 25));

        JPanel card = new JPanel(new BorderLayout(0, 15));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 230, 230)),
            new EmptyBorder(20, 25, 20, 25)
        ));

        JLabel lblCardTitle = new JLabel("THÔNG TIN CHI TIẾT");
        lblCardTitle.setFont(new Font("Roboto", Font.BOLD, 14));
        lblCardTitle.setForeground(PRIMARY_COLOR);
        lblCardTitle.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 240, 240)));
        card.add(lblCardTitle, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Mã NCC
        addLabelRow(form, gbc, "Mã Nhà Cung Cấp:", FontAwesome.BARCODE);
        txtMaNCC = new JTextField();
        txtMaNCC.setPreferredSize(new Dimension(0, 35));
        txtMaNCC.setEditable(false);
        txtMaNCC.setBackground(new Color(240, 240, 240));
        form.add(txtMaNCC, gbc); gbc.gridy++;

        // Tên NCC
        addLabelRow(form, gbc, "Tên Nhà Cung Cấp*:", FontAwesome.BUILDING);
        txtTenNCC = new JTextField();
        txtTenNCC.setPreferredSize(new Dimension(0, 35));
        form.add(txtTenNCC, gbc); gbc.gridy++;

        // Số điện thoại
        addLabelRow(form, gbc, "Số Điện Thoại:", FontAwesome.PHONE);
        txtSDT = new JTextField();
        txtSDT.setPreferredSize(new Dimension(0, 35));
        form.add(txtSDT, gbc); gbc.gridy++;

        // Email
        addLabelRow(form, gbc, "Email:", FontAwesome.ENVELOPE);
        txtEmail = new JTextField();
        txtEmail.setPreferredSize(new Dimension(0, 35));
        form.add(txtEmail, gbc); gbc.gridy++;

        // Địa chỉ
        addLabelRow(form, gbc, "Địa Chỉ:", FontAwesome.MAP_MARKER);
        txtDiaChi = new JTextField();
        txtDiaChi.setPreferredSize(new Dimension(0, 35));
        form.add(txtDiaChi, gbc);

        card.add(form, BorderLayout.CENTER);
        body.add(card, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);

        // ── Footer ──
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        footer.setOpaque(false);

        if (isEdit) {
            JButton btnDelete = new JButton("XÓA ");
            btnDelete.setPreferredSize(new Dimension(120, 40));
            btnDelete.setBackground(new Color(231, 76, 60));
            btnDelete.setForeground(Color.WHITE);
            btnDelete.setFont(new Font("Roboto", Font.BOLD, 13));
            btnDelete.setIcon(IconFontSwing.buildIcon(FontAwesome.TRASH, 16, Color.WHITE));
            btnDelete.addActionListener(e -> handleDelete());
            footer.add(btnDelete);
        }

        JButton btnCancel = new JButton("HỦY BỎ");
        btnCancel.setPreferredSize(new Dimension(100, 40));
        btnCancel.setFont(new Font("Roboto", Font.PLAIN, 13));
        btnCancel.addActionListener(e -> dispose());
        footer.add(btnCancel);

        JButton btnSave = new JButton(" LƯU DỮ LIỆU");
        btnSave.setPreferredSize(new Dimension(150, 40));
        btnSave.setBackground(new Color(46, 204, 113));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Roboto", Font.BOLD, 13));
        btnSave.setIcon(IconFontSwing.buildIcon(FontAwesome.FLOPPY_O, 16, Color.WHITE));
        btnSave.addActionListener(e -> handleSave());
        footer.add(btnSave);

        add(footer, BorderLayout.SOUTH);
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

    private void populateData() {
        if (current == null) return;
        txtMaNCC.setText(current.getMaNCC());
        if (isEdit) {
            txtTenNCC.setText(current.getTenNCC());
            txtDiaChi.setText(current.getDiaChi() != null ? current.getDiaChi() : "");
            txtSDT.setText(current.getSoDienThoai() != null ? current.getSoDienThoai() : "");
            txtEmail.setText(current.getEmail() != null ? current.getEmail() : "");
        }
    }

    private void handleSave() {
        String tenNCC = txtTenNCC.getText().trim();
        if (tenNCC.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên nhà cung cấp.");
            return;
        }

        current.setTenNCC(tenNCC);
        current.setDiaChi(txtDiaChi.getText().trim().isEmpty() ? null : txtDiaChi.getText().trim());
        current.setSoDienThoai(txtSDT.getText().trim().isEmpty() ? null : txtSDT.getText().trim());
        current.setEmail(txtEmail.getText().trim().isEmpty() ? null : txtEmail.getText().trim());

        confirmed = true;
        dispose();
    }

    private void handleDelete() {
        int c = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa nhà cung cấp \"" + current.getTenNCC() + "\"?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (c == JOptionPane.YES_OPTION) {
            deleted = true;
            confirmed = true;
            dispose();
        }
    }
}
