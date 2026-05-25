package ui.dialog;

import dao.KhuyenMaiDAO;
import dao.impl.KhuyenMaiDAOImpl;
import entity.KhuyenMai;
import jiconfont.icons.FontAwesome;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import com.toedter.calendar.JDateChooser;
import java.util.Date;
import java.time.ZoneId;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class PromotionDialog extends JDialog {

    private final KhuyenMaiDAO khuyenMaiDAO = new KhuyenMaiDAOImpl();
    private boolean isConfirmed = false;
    private final boolean isEditMode;
    private KhuyenMai currentKhuyenMai;

    private JTextField txtMaKM, txtTenKM, txtGiaTri, txtDonHangTT, txtGiamToiDa;
    private JDateChooser dpNgayBD, dpNgayKT;
    private JComboBox<String> cbxLoaiKM, cbxTrangThai;

    private final Color C_PRIMARY = new Color(41, 128, 185);
    private final Color C_SUCCESS = new Color(46, 204, 113);
    private final Color C_DANGER = new Color(231, 76, 60);
    private final Color C_BG = new Color(245, 247, 250);
    private final Color C_CARD = Color.WHITE;
    private final Color C_BORDER = new Color(218, 224, 232);
    private final Color C_LABEL = new Color(55, 68, 82);
    private final Color C_FIELD_BG = Color.WHITE;
    private final Color C_READONLY = new Color(242, 244, 246);
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public PromotionDialog(Frame parent, KhuyenMai km, boolean isEditMode) {
        super(parent, isEditMode ? "Cập nhật Khuyến mãi" : "Thêm Khuyến mãi mới", true);
        this.currentKhuyenMai = km;
        this.isEditMode = isEditMode;

        setSize(750, 620);
        setLocationRelativeTo(parent);
        setResizable(false);

        initUI();
        if (isEditMode) {
            loadData();
        }
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(C_BG);

        // ── Header ──
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(C_PRIMARY);
        pnlHeader.setPreferredSize(new Dimension(0, 62));
        pnlHeader.setBorder(new EmptyBorder(0, 24, 0, 24));
        JLabel lblTitle = new JLabel("  " + (isEditMode ? "CẬP NHẬT KHUYẾN MÃI" : "THÊM KHUYẾN MÃI MỚI"));
        lblTitle.setFont(new Font("Roboto", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setIcon(IconFontSwing.buildIcon(FontAwesome.GIFT, 24, Color.WHITE));
        pnlHeader.add(lblTitle, BorderLayout.WEST);
        mainPanel.add(pnlHeader, BorderLayout.NORTH);

        // ── Body ──
        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(20, 25, 20, 25));

        JPanel card = new JPanel(new BorderLayout(0, 15));
        card.setBackground(C_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(C_BORDER, 1),
                new EmptyBorder(20, 24, 25, 24)));

        JLabel lblCardTitle = new JLabel("THÔNG TIN KHUYẾN MÃI");
        lblCardTitle.setFont(new Font("Roboto", Font.BOLD, 13));
        lblCardTitle.setForeground(C_PRIMARY);
        lblCardTitle.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(232, 236, 242)));
        lblCardTitle.setPreferredSize(new Dimension(0, 28));
        card.add(lblCardTitle, BorderLayout.NORTH);

        JPanel formGrid = new JPanel(new GridLayout(5, 2, 25, 15));
        formGrid.setOpaque(false);

        txtMaKM = field();
        formGrid.add(createFormGroup("Mã khuyến mãi (*):", txtMaKM));

        txtTenKM = field();
        formGrid.add(createFormGroup("Tên chương trình (*):", txtTenKM));

        cbxLoaiKM = combo(new String[] { "Phần trăm", "Tiền mặt" });
        formGrid.add(createFormGroup("Loại khuyến mãi:", cbxLoaiKM));

        txtGiaTri = field();
        formGrid.add(createFormGroup("Giá trị giảm:", txtGiaTri));

        txtDonHangTT = field();
        txtDonHangTT.setText("0");
        formGrid.add(createFormGroup("Đơn hàng tối thiểu:", txtDonHangTT));

        txtGiamToiDa = field();
        txtGiamToiDa.setText("0");
        formGrid.add(createFormGroup("Giảm tối đa (VNĐ):", txtGiamToiDa));

        dpNgayBD = datePicker();
        formGrid.add(createFormGroup("Ngày bắt đầu:", dpNgayBD));

        dpNgayKT = datePicker();
        formGrid.add(createFormGroup("Ngày kết thúc:", dpNgayKT));

        cbxTrangThai = combo(new String[] { "Đang hoạt động", "Tạm dừng" });
        formGrid.add(createFormGroup("Trạng thái:", cbxTrangThai));

        card.add(formGrid, BorderLayout.CENTER);
        body.add(card, BorderLayout.CENTER);
        mainPanel.add(body, BorderLayout.CENTER);

        // ── Footer ──
        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        pnlFooter.setBackground(new Color(250, 251, 252));
        pnlFooter.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, C_BORDER));

        JButton btnCancel = new JButton("HỦY BỎ");
        btnCancel.setFont(new Font("Roboto", Font.PLAIN, 13));
        btnCancel.setForeground(C_DANGER);
        btnCancel.setBackground(Color.WHITE);
        btnCancel.setFocusPainted(false);
        btnCancel.setPreferredSize(new Dimension(110, 40));
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = new JButton(isEditMode ? "  CẬP NHẬT" : "  LƯU DỮ LIỆU");
        btnSave.setIcon(IconFontSwing.buildIcon(FontAwesome.FLOPPY_O, 16, Color.WHITE));
        btnSave.setFont(new Font("Roboto", Font.BOLD, 13));
        btnSave.setForeground(Color.WHITE);
        btnSave.setBackground(isEditMode ? C_PRIMARY : C_SUCCESS);
        btnSave.setFocusPainted(false);
        btnSave.setPreferredSize(new Dimension(155, 40));
        btnSave.addActionListener(e -> handleSave());

        pnlFooter.add(btnCancel);
        pnlFooter.add(btnSave);
        mainPanel.add(pnlFooter, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private JPanel createFormGroup(String labelText, JComponent field) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        JLabel l = new JLabel(labelText);
        l.setFont(new Font("Roboto", Font.BOLD, 12));
        l.setForeground(C_LABEL);
        l.setBorder(new EmptyBorder(0, 0, 5, 0));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(l);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        p.add(field);
        return p;
    }

    private JDateChooser datePicker() {
        JDateChooser dc = new JDateChooser();
        dc.setDateFormatString("dd/MM/yyyy");
        dc.setFont(new Font("Roboto", Font.PLAIN, 14));
        dc.setPreferredSize(new Dimension(0, 38));
        dc.setBackground(C_FIELD_BG);
        return dc;
    }

    private JTextField field() {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Roboto", Font.PLAIN, 14));
        tf.setBackground(C_FIELD_BG);
        tf.setPreferredSize(new Dimension(0, 38));
        tf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(C_BORDER), new EmptyBorder(2, 10, 2, 10)));
        return tf;
    }

    private <T> JComboBox<T> combo(T[] items) {
        JComboBox<T> cb = new JComboBox<>(items);
        cb.setFont(new Font("Roboto", Font.PLAIN, 14));
        cb.setBackground(C_FIELD_BG);
        cb.setPreferredSize(new Dimension(0, 38));
        return cb;
    }

    private void loadData() {
        if (currentKhuyenMai != null) {
            txtMaKM.setText(currentKhuyenMai.getMaKhuyenMai());
            txtMaKM.setEditable(false);
            txtMaKM.setBackground(new Color(240, 240, 240));
            txtTenKM.setText(currentKhuyenMai.getTenKhuyenMai());
            cbxLoaiKM.setSelectedItem("TIEN_MAT".equals(currentKhuyenMai.getLoaiKhuyenMai()) ? "Tiền mặt" : "Phần trăm");
            txtGiaTri.setText(String.valueOf(currentKhuyenMai.getGiaTri()));
            txtDonHangTT.setText(String.valueOf(currentKhuyenMai.getDonHangToiThieu()));
            txtGiamToiDa.setText(String.valueOf(currentKhuyenMai.getGiamToiDa()));
            if (currentKhuyenMai.getNgayBatDau() != null)
                dpNgayBD.setDate(
                        Date.from(currentKhuyenMai.getNgayBatDau().atZone(ZoneId.systemDefault()).toInstant()));
            if (currentKhuyenMai.getNgayKetThuc() != null)
                dpNgayKT.setDate(
                        Date.from(currentKhuyenMai.getNgayKetThuc().atZone(ZoneId.systemDefault()).toInstant()));
            cbxTrangThai.setSelectedItem("TAM_DUNG".equals(currentKhuyenMai.getTrangThai()) ? "Tạm dừng" : "Đang hoạt động");
        }
    }

    private void handleSave() {
        String ma = txtMaKM.getText().trim();
        String ten = txtTenKM.getText().trim();
        if (ma.isEmpty() || ten.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập Mã và Tên KM!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double giaTri, dhMin, giamMax;
        try {
            giaTri = Double.parseDouble(txtGiaTri.getText().trim());
            dhMin = Double.parseDouble(txtDonHangTT.getText().trim());
            giamMax = Double.parseDouble(txtGiamToiDa.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Giá trị giảm, Đơn hàng TT, Giảm tối đa phải là số!", "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        LocalDateTime nbd = null, nkt = null;
        if (dpNgayBD.getDate() != null) {
            nbd = dpNgayBD.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay();
        }
        if (dpNgayKT.getDate() != null) {
            nkt = dpNgayKT.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atTime(23, 59, 59);
        }

        if (!isEditMode) {
            if (khuyenMaiDAO.findById(ma) != null) {
                JOptionPane.showMessageDialog(this, "Mã KM đã tồn tại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String loaiKM = cbxLoaiKM.getSelectedItem().toString().equals("Tiền mặt") ? "TIEN_MAT" : "PHAN_TRAM";
            String tThai = cbxTrangThai.getSelectedItem().toString().equals("Tạm dừng") ? "TAM_DUNG" : "DANG_HOAT_DONG";
            KhuyenMai km = new KhuyenMai(ma, ten, loaiKM, giaTri, dhMin, giamMax, nbd, nkt, tThai);
            if (khuyenMaiDAO.insert(km)) {
                isConfirmed = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi thêm dữ liệu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            currentKhuyenMai.setTenKhuyenMai(ten);
            currentKhuyenMai.setLoaiKhuyenMai(cbxLoaiKM.getSelectedItem().toString().equals("Tiền mặt") ? "TIEN_MAT" : "PHAN_TRAM");
            currentKhuyenMai.setGiaTri(giaTri);
            currentKhuyenMai.setDonHangToiThieu(dhMin);
            currentKhuyenMai.setGiamToiDa(giamMax);
            currentKhuyenMai.setNgayBatDau(nbd);
            currentKhuyenMai.setNgayKetThuc(nkt);
            currentKhuyenMai.setTrangThai(cbxTrangThai.getSelectedItem().toString().equals("Tạm dừng") ? "TAM_DUNG" : "DANG_HOAT_DONG");

            if (khuyenMaiDAO.update(currentKhuyenMai)) {
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
