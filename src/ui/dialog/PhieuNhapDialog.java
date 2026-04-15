package ui.dialog;

import controller.KhoController;
import entity.*;
import jiconfont.icons.FontAwesome;
import jiconfont.swing.IconFontSwing;
import utils.CurrencyUtils;
import utils.SessionManager;

import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Dialog tạo phiếu nhập kho.
 * Cho phép chọn NCC, thêm nhiều dòng nguyên liệu (với ngày hết hạn) và xác nhận nhập.
 */
public class PhieuNhapDialog extends JDialog {

    private final KhoController controller = new KhoController();

    private JComboBox<String> cbNCC;
    private JComboBox<String> cbNguyenLieu;
    private JTextField txtSoLuong, txtDonGia;
    private JDateChooser dateChooserHetHan;
    private JTable tableItems;
    private DefaultTableModel modelItems;
    private JLabel lblTongTien;

    private List<NhaCungCap> listNCC;
    private List<NguyenLieu> listNL;
    private final List<ChiTietPhieuNhap> chiTietList = new ArrayList<>();
    private final List<LocalDate> ngayHetHanList = new ArrayList<>();

    private boolean confirmed = false;
    private int tempCTPNCounter = 0;

    private final Color PRIMARY  = new Color(41, 128, 185);
    private final Color SUCCESS  = new Color(46, 204, 113);

    public PhieuNhapDialog(Frame owner) {
        super(owner, "Tạo Phiếu Nhập Kho", true);
        setSize(900, 700);
        setLocationRelativeTo(owner);
        setResizable(false);

        initUI();
        loadComboData();
    }

    public boolean isConfirmed() { return confirmed; }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 12));
        mainPanel.setBackground(new Color(245, 247, 250));
        mainPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

        // ===== TOP: Thông tin phiếu nhập =====
        JPanel pnlInfo = new JPanel(new GridBagLayout());
        pnlInfo.setBackground(Color.WHITE);
        pnlInfo.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 230, 230)),
            new EmptyBorder(15, 20, 15, 20)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // Row 1: NCC
        gbc.gridx = 0; gbc.gridy = 0;
        pnlInfo.add(createLabel("Nhà cung cấp:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
        gbc.gridwidth = 3;
        cbNCC = new JComboBox<>();
        cbNCC.setPreferredSize(new Dimension(400, 32));
        pnlInfo.add(cbNCC, gbc);
        gbc.gridwidth = 1;

        mainPanel.add(pnlInfo, BorderLayout.NORTH);

        // ===== CENTER: Thêm dòng + Bảng =====
        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setOpaque(false);

        // Dòng thêm nguyên liệu (2 hàng)
        JPanel pnlAddRow = new JPanel();
        pnlAddRow.setLayout(new BoxLayout(pnlAddRow, BoxLayout.Y_AXIS));
        pnlAddRow.setBackground(Color.WHITE);
        pnlAddRow.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 230, 230)),
            new EmptyBorder(8, 10, 8, 10)
        ));

        // Row 1: Nguyên liệu + SL + Đơn giá
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        row1.setOpaque(false);

        row1.add(createLabel("Nguyên liệu:"));
        cbNguyenLieu = new JComboBox<>();
        cbNguyenLieu.setPreferredSize(new Dimension(220, 32));
        row1.add(cbNguyenLieu);

        row1.add(createLabel("  SL:"));
        txtSoLuong = new JTextField(6);
        txtSoLuong.setPreferredSize(new Dimension(0, 32));
        row1.add(txtSoLuong);

        row1.add(createLabel("  Đơn giá:"));
        txtDonGia = new JTextField(8);
        txtDonGia.setPreferredSize(new Dimension(0, 32));
        row1.add(txtDonGia);

        pnlAddRow.add(row1);

        // Row 2: Ngày hết hạn (JDateChooser) + Nút thêm
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        row2.setOpaque(false);

        row2.add(createLabel("Ngày hết hạn:"));
        dateChooserHetHan = new JDateChooser();
        dateChooserHetHan.setPreferredSize(new Dimension(180, 32));
        dateChooserHetHan.setDateFormatString("dd/MM/yyyy");
        dateChooserHetHan.setFont(new Font("Roboto", Font.PLAIN, 13));
        // Đặt ngày tối thiểu là ngày hiện tại
        dateChooserHetHan.setMinSelectableDate(new Date());
        row2.add(dateChooserHetHan);

        JButton btnAddLine = new JButton("Thêm dòng");
        btnAddLine.setIcon(IconFontSwing.buildIcon(FontAwesome.PLUS, 13, Color.WHITE));
        btnAddLine.setBackground(SUCCESS);
        btnAddLine.setForeground(Color.WHITE);
        btnAddLine.setFont(new Font("Roboto", Font.BOLD, 12));
        btnAddLine.setPreferredSize(new Dimension(130, 32));
        btnAddLine.setFocusable(false);
        btnAddLine.addActionListener(e -> addItemRow());
        row2.add(btnAddLine);

        pnlAddRow.add(row2);

        centerPanel.add(pnlAddRow, BorderLayout.NORTH);

        // Bảng chi tiết
        String[] cols = {"Mã NL", "Tên NL", "Số lượng", "Đơn giá", "Thành tiền", "Ngày hết hạn", "Xoá"};
        modelItems = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableItems = new JTable(modelItems);
        tableItems.setRowHeight(38);
        tableItems.setFont(new Font("Roboto", Font.PLAIN, 13));
        tableItems.getTableHeader().setFont(new Font("Roboto", Font.BOLD, 13));
        tableItems.getTableHeader().setBackground(new Color(236, 240, 241));

        // Nút xoá dòng
        tableItems.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int col = tableItems.columnAtPoint(e.getPoint());
                int row = tableItems.rowAtPoint(e.getPoint());
                if (col == 6 && row >= 0) {
                    chiTietList.remove(row);
                    ngayHetHanList.remove(row);
                    refreshTable();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tableItems);
        scroll.setBorder(new LineBorder(new Color(230, 230, 230)));
        scroll.getViewport().setBackground(Color.WHITE);
        centerPanel.add(scroll, BorderLayout.CENTER);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // ===== BOTTOM: Tổng tiền + Nút =====
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(10, 0, 0, 0));

        lblTongTien = new JLabel("Tổng tiền: 0 đ");
        lblTongTien.setFont(new Font("Roboto", Font.BOLD, 16));
        lblTongTien.setForeground(new Color(44, 62, 80));
        bottom.add(lblTongTien, BorderLayout.WEST);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        JButton btnCancel = new JButton("Huỷ");
        btnCancel.setFont(new Font("Roboto", Font.BOLD, 13));
        btnCancel.setPreferredSize(new Dimension(100, 38));
        btnCancel.addActionListener(e -> dispose());

        JButton btnConfirm = new JButton("  Xác nhận Nhập Kho");
        btnConfirm.setIcon(IconFontSwing.buildIcon(FontAwesome.CHECK, 14, Color.WHITE));
        btnConfirm.setBackground(PRIMARY);
        btnConfirm.setForeground(Color.WHITE);
        btnConfirm.setFont(new Font("Roboto", Font.BOLD, 14));
        btnConfirm.setPreferredSize(new Dimension(220, 38));
        btnConfirm.setFocusable(false);
        btnConfirm.addActionListener(e -> handleConfirm());

        btnPanel.add(btnCancel);
        btnPanel.add(btnConfirm);
        bottom.add(btnPanel, BorderLayout.EAST);

        mainPanel.add(bottom, BorderLayout.SOUTH);
        setContentPane(mainPanel);
    }

    private void loadComboData() {
        listNCC = controller.getAllNhaCungCap();
        for (NhaCungCap ncc : listNCC) {
            cbNCC.addItem(ncc.getMaNCC() + " - " + ncc.getTenNCC());
        }

        listNL = controller.getAllNguyenLieu();
        for (NguyenLieu nl : listNL) {
            cbNguyenLieu.addItem(nl.getMaNL() + " - " + nl.getTenNL());
        }

        // Auto-fill đơn giá khi chọn nguyên liệu
        cbNguyenLieu.addActionListener(e -> autoFillDonGia());
    }

    private void autoFillDonGia() {
        int idx = cbNguyenLieu.getSelectedIndex();
        if (idx >= 0 && idx < listNL.size()) {
            txtDonGia.setText(String.valueOf((long) listNL.get(idx).getDonGiaNhap()));
        }
    }

    private void addItemRow() {
        int nlIdx = cbNguyenLieu.getSelectedIndex();
        if (nlIdx < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nguyên liệu.");
            return;
        }

        double soLuong, donGia;
        try {
            soLuong = Double.parseDouble(txtSoLuong.getText().trim());
            donGia = Double.parseDouble(txtDonGia.getText().trim());
            if (soLuong <= 0 || donGia < 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Số lượng và đơn giá phải là số hợp lệ > 0.");
            return;
        }

        // Validate ngày hết hạn bằng JDateChooser
        Date selectedDate = dateChooserHetHan.getDate();
        if (selectedDate == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ngày hết hạn.");
            return;
        }
        LocalDate ngayHH = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        if (ngayHH.isBefore(LocalDate.now())) {
            JOptionPane.showMessageDialog(this, "Ngày hết hạn không được nhỏ hơn ngày hiện tại (" +
                    LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ").");
            return;
        }

        NguyenLieu nl = listNL.get(nlIdx);

        // Kiểm tra trùng
        for (ChiTietPhieuNhap ct : chiTietList) {
            if (ct.getMaNL().equals(nl.getMaNL())) {
                JOptionPane.showMessageDialog(this,
                        "Nguyên liệu " + nl.getTenNL() + " đã có trong danh sách.\nVui lòng xoá dòng cũ trước khi thêm lại.");
                return;
            }
        }

        // Dùng counter tạm thay vì gọi DB (tránh trùng mã khi chưa lưu)
        tempCTPNCounter++;
        ChiTietPhieuNhap ct = new ChiTietPhieuNhap();
        ct.setMaCTPN("TEMP_" + tempCTPNCounter);
        ct.setSoLuong(soLuong);
        ct.setDonGia(donGia);
        ct.setThanhTien(soLuong * donGia);
        ct.setMaNL(nl.getMaNL());
        chiTietList.add(ct);
        ngayHetHanList.add(ngayHH);

        refreshTable();
        txtSoLuong.setText("");
        txtDonGia.setText("");
        dateChooserHetHan.setDate(null);
    }

    private void refreshTable() {
        modelItems.setRowCount(0);
        double tongTien = 0;
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (int i = 0; i < chiTietList.size(); i++) {
            ChiTietPhieuNhap ct = chiTietList.get(i);
            NguyenLieu nl = controller.getNguyenLieuById(ct.getMaNL());
            String tenNL = nl != null ? nl.getTenNL() : ct.getMaNL();
            String ngayHH = i < ngayHetHanList.size() ? ngayHetHanList.get(i).format(fmt) : "";
            modelItems.addRow(new Object[]{
                ct.getMaNL(), tenNL,
                String.format("%.1f", ct.getSoLuong()),
                CurrencyUtils.formatNoUnit(ct.getDonGia()),
                CurrencyUtils.formatNoUnit(ct.getThanhTien()),
                ngayHH,
                "❌ Xoá"
            });
            tongTien += ct.getThanhTien();
        }
        lblTongTien.setText("Tổng tiền: " + CurrencyUtils.formatNoUnit(tongTien) + " đ");
    }

    private void handleConfirm() {
        if (chiTietList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chưa có nguyên liệu nào trong phiếu nhập!");
            return;
        }
        if (cbNCC.getSelectedIndex() < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn Nhà Cung Cấp.");
            return;
        }

        NhaCungCap ncc = listNCC.get(cbNCC.getSelectedIndex());

        // Lấy kho đầu tiên làm mặc định
        List<Kho> listKho = controller.getAllKho();
        Kho kho = listKho.isEmpty() ? null : listKho.get(0);

        if (kho == null) {
            JOptionPane.showMessageDialog(this, "Chưa có kho nào trong hệ thống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double tongTien = 0;
        for (ChiTietPhieuNhap ct : chiTietList) tongTien += ct.getThanhTien();

        // Gán mã CTPN thật từ DB cho tất cả items
        for (int i = 0; i < chiTietList.size(); i++) {
            String realMaCTPN = controller.generateNextMaCTPN();
            chiTietList.get(i).setMaCTPN(realMaCTPN);
        }

        // Cập nhật ngày hết hạn cho từng nguyên liệu
        for (int i = 0; i < chiTietList.size(); i++) {
            if (i < ngayHetHanList.size()) {
                NguyenLieu nl = controller.getNguyenLieuById(chiTietList.get(i).getMaNL());
                if (nl != null) {
                    nl.setNgayHetHan(ngayHetHanList.get(i));
                    controller.updateNguyenLieu(nl);
                }
            }
        }

        PhieuNhap pn = new PhieuNhap();
        pn.setMaPN(controller.generateNextMaPN());
        pn.setNgayNhap(LocalDateTime.now());
        pn.setTongTien(tongTien);
        pn.setMaNV(SessionManager.isLoggedIn() ? SessionManager.getCurrentUser().getMaNV() : "NV001");
        pn.setMaNCC(ncc.getMaNCC());
        pn.setMaKho(kho.getMaKho());

        int confirm = JOptionPane.showConfirmDialog(this,
                String.format("Xác nhận nhập kho?\n\nMã PN: %s\nNCC: %s\nTổng tiền: %s đ\nSố dòng: %d",
                        pn.getMaPN(), ncc.getTenNCC(),
                        CurrencyUtils.formatNoUnit(tongTien), chiTietList.size()),
                "Xác nhận", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (controller.processNhapKho(pn, chiTietList)) {
                confirmed = true;
                JOptionPane.showMessageDialog(this,
                        "✅ Nhập kho thành công!\nMã phiếu: " + pn.getMaPN(),
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "❌ Lỗi khi nhập kho! Kiểm tra lại dữ liệu.",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Roboto", Font.BOLD, 13));
        lbl.setForeground(new Color(44, 62, 80));
        return lbl;
    }
}
