package ui.dialog;

import controller.KhoController;
import entity.*;
import jiconfont.icons.FontAwesome;
import jiconfont.swing.IconFontSwing;
import utils.CurrencyUtils;
import utils.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialog tạo phiếu nhập kho.
 * Cho phép chọn NCC, Kho, thêm từng dòng nguyên liệu và xác nhận nhập.
 */
public class PhieuNhapDialog extends JDialog {

    private final KhoController controller = new KhoController();

    private JComboBox<String> cbNCC, cbKho;
    private JComboBox<String> cbNguyenLieu;
    private JTextField txtSoLuong, txtDonGia;
    private JTable tableItems;
    private DefaultTableModel modelItems;
    private JLabel lblTongTien;

    private List<NhaCungCap> listNCC;
    private List<Kho> listKho;
    private List<NguyenLieu> listNL;
    private final List<ChiTietPhieuNhap> chiTietList = new ArrayList<>();

    private boolean confirmed = false;

    private final Color PRIMARY  = new Color(41, 128, 185);
    private final Color SUCCESS  = new Color(46, 204, 113);

    public PhieuNhapDialog(Frame owner) {
        super(owner, "Tạo Phiếu Nhập Kho", true);
        setSize(850, 620);
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
        cbNCC = new JComboBox<>();
        cbNCC.setPreferredSize(new Dimension(250, 32));
        pnlInfo.add(cbNCC, gbc);

        // Row 1 col 2: Kho
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        pnlInfo.add(createLabel("  Kho nhập:"), gbc);
        gbc.gridx = 3; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
        cbKho = new JComboBox<>();
        cbKho.setPreferredSize(new Dimension(200, 32));
        pnlInfo.add(cbKho, gbc);

        mainPanel.add(pnlInfo, BorderLayout.NORTH);

        // ===== CENTER: Thêm dòng + Bảng =====
        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setOpaque(false);

        // Dòng thêm nguyên liệu
        JPanel pnlAddRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        pnlAddRow.setBackground(Color.WHITE);
        pnlAddRow.setBorder(new LineBorder(new Color(230, 230, 230)));

        pnlAddRow.add(createLabel("Nguyên liệu:"));
        cbNguyenLieu = new JComboBox<>();
        cbNguyenLieu.setPreferredSize(new Dimension(200, 32));
        cbNguyenLieu.addActionListener(e -> autoFillDonGia());
        pnlAddRow.add(cbNguyenLieu);

        pnlAddRow.add(createLabel("  SL:"));
        txtSoLuong = new JTextField(6);
        txtSoLuong.setPreferredSize(new Dimension(0, 32));
        pnlAddRow.add(txtSoLuong);

        pnlAddRow.add(createLabel("  Đơn giá:"));
        txtDonGia = new JTextField(8);
        txtDonGia.setPreferredSize(new Dimension(0, 32));
        pnlAddRow.add(txtDonGia);

        JButton btnAddLine = new JButton("Thêm");
        btnAddLine.setIcon(IconFontSwing.buildIcon(FontAwesome.PLUS, 13, Color.WHITE));
        btnAddLine.setBackground(SUCCESS);
        btnAddLine.setForeground(Color.WHITE);
        btnAddLine.setFont(new Font("Roboto", Font.BOLD, 12));
        btnAddLine.setPreferredSize(new Dimension(90, 32));
        btnAddLine.setFocusable(false);
        btnAddLine.addActionListener(e -> addItemRow());
        pnlAddRow.add(btnAddLine);

        centerPanel.add(pnlAddRow, BorderLayout.NORTH);

        // Bảng chi tiết
        String[] cols = {"Mã NL", "Tên NL", "Số lượng", "Đơn giá", "Thành tiền", "Xoá"};
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
                if (col == 5 && row >= 0) {
                    chiTietList.remove(row);
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

        listKho = controller.getAllKho();
        for (Kho k : listKho) {
            cbKho.addItem(k.getMaKho() + " - " + k.getTenKho());
        }

        listNL = controller.getAllNguyenLieu();
        for (NguyenLieu nl : listNL) {
            cbNguyenLieu.addItem(nl.getMaNL() + " - " + nl.getTenNL());
        }
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

        NguyenLieu nl = listNL.get(nlIdx);

        // Kiểm tra trùng
        for (ChiTietPhieuNhap ct : chiTietList) {
            if (ct.getMaNL().equals(nl.getMaNL())) {
                JOptionPane.showMessageDialog(this,
                        "Nguyên liệu " + nl.getTenNL() + " đã có trong danh sách.\nVui lòng xoá dòng cũ trước khi thêm lại.");
                return;
            }
        }

        ChiTietPhieuNhap ct = new ChiTietPhieuNhap();
        ct.setMaCTPN(controller.generateNextMaCTPN());
        ct.setSoLuong(soLuong);
        ct.setDonGia(donGia);
        ct.setThanhTien(soLuong * donGia);
        ct.setMaNL(nl.getMaNL());
        chiTietList.add(ct);

        refreshTable();
        txtSoLuong.setText("");
        txtDonGia.setText("");
    }

    private void refreshTable() {
        modelItems.setRowCount(0);
        double tongTien = 0;
        for (ChiTietPhieuNhap ct : chiTietList) {
            NguyenLieu nl = controller.getNguyenLieuById(ct.getMaNL());
            String tenNL = nl != null ? nl.getTenNL() : ct.getMaNL();
            modelItems.addRow(new Object[]{
                ct.getMaNL(), tenNL,
                String.format("%.1f", ct.getSoLuong()),
                CurrencyUtils.formatNoUnit(ct.getDonGia()),
                CurrencyUtils.formatNoUnit(ct.getThanhTien()),
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
        if (cbNCC.getSelectedIndex() < 0 || cbKho.getSelectedIndex() < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn NCC và Kho.");
            return;
        }

        NhaCungCap ncc = listNCC.get(cbNCC.getSelectedIndex());
        Kho kho = listKho.get(cbKho.getSelectedIndex());

        double tongTien = 0;
        for (ChiTietPhieuNhap ct : chiTietList) tongTien += ct.getThanhTien();

        // Regenerate maCTPN for all items
        for (int i = 0; i < chiTietList.size(); i++) {
            chiTietList.get(i).setMaCTPN(controller.generateNextMaCTPN());
        }

        PhieuNhap pn = new PhieuNhap();
        pn.setMaPN(controller.generateNextMaPN());
        pn.setNgayNhap(LocalDateTime.now());
        pn.setTongTien(tongTien);
        pn.setMaNV(SessionManager.isLoggedIn() ? SessionManager.getCurrentUser().getMaNV() : "NV001");
        pn.setMaNCC(ncc.getMaNCC());
        pn.setMaKho(kho.getMaKho());

        int confirm = JOptionPane.showConfirmDialog(this,
                String.format("Xác nhận nhập kho?\n\nMã PN: %s\nNCC: %s\nKho: %s\nTổng tiền: %s đ\nSố dòng: %d",
                        pn.getMaPN(), ncc.getTenNCC(), kho.getTenKho(),
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
