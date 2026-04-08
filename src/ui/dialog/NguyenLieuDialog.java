package ui.dialog;

import entity.NguyenLieu;
import jiconfont.icons.FontAwesome;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Dialog thêm/sửa Nguyên Liệu — phong cách giống StaffDialog (2 card, header màu).
 * Constructor nhận (Frame, NguyenLieu, boolean isEdit).
 */
public class NguyenLieuDialog extends JDialog {

    private JTextField txtMaNL, txtTenNL, txtDonViTinh, txtDonGia, txtHanSD;
    private boolean confirmed = false;
    private boolean deleted   = false;
    private final NguyenLieu current;
    private final boolean isEdit;

    private final Color PRIMARY_COLOR = new Color(41, 128, 185);

    public NguyenLieuDialog(Frame owner, NguyenLieu nl, boolean isEdit) {
        super(owner, isEdit ? "Chi Tiết Nguyên Liệu" : "Thêm Nguyên Liệu Mới", true);
        this.current = nl;
        this.isEdit = isEdit;
        setSize(700, 580);
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

        JLabel title = new JLabel(" THÔNG TIN NGUYÊN LIỆU");
        title.setFont(new Font("Roboto", Font.BOLD, 17));
        title.setForeground(Color.WHITE);
        title.setIcon(IconFontSwing.buildIcon(FontAwesome.LEAF, 24, Color.WHITE));
        header.add(title, BorderLayout.WEST);

        JLabel breadcrumb = new JLabel("Kho > Nguyên liệu > " + (isEdit ? "Chi tiết" : "Thêm mới"));
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

        // Mã NL
        addLabelRow(form, gbc, "Mã Nguyên Liệu:", FontAwesome.BARCODE);
        txtMaNL = new JTextField(20);
        txtMaNL.setPreferredSize(new Dimension(0, 35));
        txtMaNL.setEditable(false);
        txtMaNL.setBackground(new Color(240, 240, 240));
        form.add(txtMaNL, gbc); gbc.gridy++;

        // Tên NL
        addLabelRow(form, gbc, "Tên Nguyên Liệu*:", FontAwesome.TAG);
        txtTenNL = new JTextField(20);
        txtTenNL.setPreferredSize(new Dimension(0, 35));
        form.add(txtTenNL, gbc); gbc.gridy++;

        // ĐV Tính
        addLabelRow(form, gbc, "Đơn Vị Tính*:", FontAwesome.BALANCE_SCALE);
        txtDonViTinh = new JTextField(20);
        txtDonViTinh.setPreferredSize(new Dimension(0, 35));
        form.add(txtDonViTinh, gbc); gbc.gridy++;

        // Đơn giá nhập
        addLabelRow(form, gbc, "Đơn Giá Nhập*:", FontAwesome.MONEY);
        txtDonGia = new JTextField(20);
        txtDonGia.setPreferredSize(new Dimension(0, 35));
        form.add(txtDonGia, gbc); gbc.gridy++;

        // Hạn sử dụng
        addLabelRow(form, gbc, "Hạn Sử Dụng (dd/MM/yyyy):", FontAwesome.CALENDAR);
        txtHanSD = new JTextField(20);
        txtHanSD.setPreferredSize(new Dimension(0, 35));
        txtHanSD.setToolTipText("Để trống nếu không có hạn sử dụng");
        form.add(txtHanSD, gbc);

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
        txtMaNL.setText(current.getMaNL());
        if (isEdit) {
            txtTenNL.setText(current.getTenNL());
            txtDonViTinh.setText(current.getDonViTinh());
            txtDonGia.setText(String.valueOf((long) current.getDonGiaNhap()));
            if (current.getNgayHetHan() != null) {
                txtHanSD.setText(current.getNgayHetHan().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }
        }
    }

    private void handleSave() {
        String tenNL = txtTenNL.getText().trim();
        String donViTinh = txtDonViTinh.getText().trim();
        String donGiaStr = txtDonGia.getText().trim();
        String hanSDStr = txtHanSD.getText().trim();

        if (tenNL.isEmpty() || donViTinh.isEmpty() || donGiaStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin (Tên, ĐV tính, Đơn giá).");
            return;
        }

        double donGia;
        try {
            donGia = Double.parseDouble(donGiaStr);
            if (donGia < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Đơn giá không hợp lệ.");
            return;
        }

        LocalDate hanSD = null;
        if (!hanSDStr.isEmpty()) {
            try {
                hanSD = LocalDate.parse(hanSDStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } catch (DateTimeParseException e) {
                JOptionPane.showMessageDialog(this, "Hạn sử dụng không hợp lệ. Định dạng: dd/MM/yyyy");
                return;
            }
        }

        current.setTenNL(tenNL);
        current.setDonViTinh(donViTinh);
        current.setDonGiaNhap(donGia);
        current.setNgayHetHan(hanSD);

        confirmed = true;
        dispose();
    }

    private void handleDelete() {
        int c = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa nguyên liệu \"" + current.getTenNL() + "\"?\n(Lưu ý: sẽ xóa cả định mức và tồn kho liên quan)",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (c == JOptionPane.YES_OPTION) {
            deleted = true;
            confirmed = true;
            dispose();
        }
    }
}
