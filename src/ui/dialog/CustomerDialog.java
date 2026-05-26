package ui.dialog;

import dao.KhachHangDAO;
import dao.impl.KhachHangDAOImpl;
import entity.KhachHang;
import jiconfont.icons.FontAwesome;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.LocalDateTime;

public class CustomerDialog extends JDialog {

    private final KhachHangDAO khachHangDAO = new KhachHangDAOImpl();
    private boolean isConfirmed = false;
    private final boolean isEditMode;
    private KhachHang currentKhachHang;

    private JTextField txtSdt;
    private JTextField txtTenKH;
    private JTextField txtDiem;

    private final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private final Color BG_COLOR = new Color(245, 247, 250);

    public CustomerDialog(Frame parent, KhachHang kh, boolean isEditMode) {
        super(parent, isEditMode ? "Cập nhật Khách hàng" : "Thêm Khách hàng mới", true);
        this.currentKhachHang = kh;
        this.isEditMode = isEditMode;

        setSize(450, 450);
        setLocationRelativeTo(parent);
        setResizable(false);

        initUI();
        if (isEditMode) {
            loadData();
        }
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(BG_COLOR);

        // ── Header ──
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(PRIMARY_COLOR);
        pnlHeader.setPreferredSize(new Dimension(0, 62));
        pnlHeader.setBorder(new EmptyBorder(0, 24, 0, 24));
        JLabel lblTitle = new JLabel("  " + (isEditMode ? "CẬP NHẬT KHÁCH HÀNG" : "THÊM KHÁCH HÀNG MỚI"));
        lblTitle.setFont(new Font("Roboto", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setIcon(IconFontSwing.buildIcon(FontAwesome.USERS, 24, Color.WHITE));
        pnlHeader.add(lblTitle, BorderLayout.WEST);
        mainPanel.add(pnlHeader, BorderLayout.NORTH);

        // ── Form ──
        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBackground(Color.WHITE);
        pnlForm.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(15, 20, 15, 20),
                new LineBorder(new Color(230, 230, 230), 1, true)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;

        // Số điện thoại
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        pnlForm.add(new JLabel("Số điện thoại (*):"), gbc);
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        txtSdt = new JTextField();
        txtSdt.setPreferredSize(new Dimension(150, 35));
        pnlForm.add(txtSdt, gbc);

        // Tên khách hàng
        gbc.gridy = 2;
        gbc.weightx = 0;
        pnlForm.add(new JLabel("Tên khách hàng (*):"), gbc);
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        txtTenKH = new JTextField();
        txtTenKH.setPreferredSize(new Dimension(150, 35));
        pnlForm.add(txtTenKH, gbc);

        // Điểm tích lũy (Ẩn khi tạo mới)
        JLabel lblDiem = new JLabel("Điểm tích luỹ:");
        gbc.gridy = 4;
        gbc.weightx = 0;
        pnlForm.add(lblDiem, gbc);
        gbc.gridy = 5;
        gbc.weightx = 1.0;
        txtDiem = new JTextField("0");
        txtDiem.setPreferredSize(new Dimension(150, 35));

        // Khóa không cho phép sửa điểm thủ công
        txtDiem.setEditable(false);
        txtDiem.setBackground(new Color(240, 240, 240));
        txtDiem.setToolTipText("Điểm tích lũy tự động cộng khi mua hàng, không được tự nhập");

        if (!isEditMode) {
            lblDiem.setVisible(false);
            txtDiem.setVisible(false);
        }

        pnlForm.add(txtDiem, gbc);

        mainPanel.add(pnlForm, BorderLayout.CENTER);

        // ── Footer ──
        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        pnlFooter.setBackground(new Color(250, 251, 252));
        pnlFooter.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(218, 224, 232)));

        JButton btnCancel = new JButton("HỦY BỎ");
        btnCancel.setFont(new Font("Roboto", Font.PLAIN, 13));
        btnCancel.setForeground(new Color(231, 76, 60));
        btnCancel.setBackground(Color.WHITE);
        btnCancel.setFocusPainted(false);
        btnCancel.setPreferredSize(new Dimension(110, 40));
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = new JButton(isEditMode ? "  CẬP NHẬT" : "  LƯU DỮ LIỆU");
        btnSave.setIcon(IconFontSwing.buildIcon(FontAwesome.FLOPPY_O, 16, Color.WHITE));
        btnSave.setFont(new Font("Roboto", Font.BOLD, 13));
        btnSave.setForeground(Color.WHITE);
        btnSave.setBackground(isEditMode ? PRIMARY_COLOR : new Color(46, 204, 113));
        btnSave.setFocusPainted(false);
        btnSave.setPreferredSize(new Dimension(155, 40));
        btnSave.addActionListener(e -> handleSave());

        pnlFooter.add(btnCancel);
        pnlFooter.add(btnSave);
        mainPanel.add(pnlFooter, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void loadData() {
        if (currentKhachHang != null) {
            txtSdt.setText(currentKhachHang.getSoDienThoai());
            txtSdt.setEditable(false); // Không cho sửa SĐT
            txtSdt.setBackground(new Color(240, 240, 240));
            txtTenKH.setText(currentKhachHang.getTenKhachHang());
            txtDiem.setText(String.valueOf(currentKhachHang.getDiemTichLuy()));
        }
    }

    private void handleSave() {
        String sdt = txtSdt.getText().trim();
        String ten = txtTenKH.getText().trim();
        String diemStr = txtDiem.getText().trim();

        if (sdt.isEmpty() || ten.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập SĐT và Tên khách hàng!", "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Lấy lại điểm hiện tại, tránh bị ghi đè bằng thao tác nhập bậy
        int diem = 0;
        if (isEditMode && currentKhachHang != null) {
            diem = currentKhachHang.getDiemTichLuy();
        }

        if (!isEditMode) {
            if (khachHangDAO.findById(sdt) != null) {
                JOptionPane.showMessageDialog(this, "Số điện thoại đã tồn tại trong hệ thống!", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            KhachHang newKH = new KhachHang(sdt, ten, diem, LocalDateTime.now(), true);
            if (khachHangDAO.insert(newKH)) {
                isConfirmed = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi thêm dữ liệu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            currentKhachHang.setTenKhachHang(ten);
            currentKhachHang.setDiemTichLuy(diem);
            if (khachHangDAO.update(currentKhachHang)) {
                isConfirmed = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi cập nhật dữ liệu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public boolean isConfirmed() {
        return isConfirmed;
    }
}
