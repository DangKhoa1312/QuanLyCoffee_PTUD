package ui.dialog;

import controller.KhoController;
import entity.*;
import jiconfont.icons.FontAwesome;
import jiconfont.swing.IconFontSwing;
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
 * Dialog tạo phiếu xuất kho.
 * Cho phép chọn nhiều nguyên liệu, nhập số lượng và lý do xuất.
 */
public class PhieuXuatDialog extends JDialog {

    private final KhoController controller = new KhoController();

    private JComboBox<String> cbNguyenLieu;
    private JTextField txtSoLuong;
    private JLabel lblDonViTinh;
    private JTextField txtLyDo;
    private JTable tableItems;
    private DefaultTableModel modelItems;

    private List<NguyenLieu> listNL;
    private final List<ChiTietPhieuXuat> chiTietList = new ArrayList<>();

    private boolean confirmed = false;
    private int tempCounter = 0;

    private final Color PRIMARY = new Color(41, 128, 185);
    private final Color DANGER  = new Color(231, 76, 60);

    public PhieuXuatDialog(Frame owner) {
        super(owner, "Tạo Phiếu Xuất Kho", true);
        setSize(950, 700);
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

        // ===== TOP: Lý do xuất =====
        JPanel pnlInfo = new JPanel(new GridBagLayout());
        pnlInfo.setBackground(Color.WHITE);
        pnlInfo.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 230, 230)),
            new EmptyBorder(15, 20, 15, 20)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        pnlInfo.add(createLabel("Lý do xuất:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
        gbc.gridwidth = 3;
        txtLyDo = new JTextField();
        txtLyDo.setPreferredSize(new Dimension(400, 32));
        txtLyDo.setFont(new Font("Roboto", Font.PLAIN, 13));
        txtLyDo.setText("Xuất kho thủ công");
        pnlInfo.add(txtLyDo, gbc);
        gbc.gridwidth = 1;

        mainPanel.add(pnlInfo, BorderLayout.NORTH);

        // ===== CENTER: Thêm dòng + Bảng =====
        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setOpaque(false);

        // Dòng thêm nguyên liệu
        JPanel pnlAddRow = new JPanel();
        pnlAddRow.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 8));
        pnlAddRow.setBackground(Color.WHITE);
        pnlAddRow.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 230, 230)),
            new EmptyBorder(8, 10, 8, 10)
        ));

        pnlAddRow.add(createLabel("Nguyên liệu:"));
        cbNguyenLieu = new JComboBox<>();
        cbNguyenLieu.setPreferredSize(new Dimension(250, 32));
        pnlAddRow.add(cbNguyenLieu);

        pnlAddRow.add(createLabel("  Số lượng:"));
        txtSoLuong = new JTextField(8);
        txtSoLuong.setPreferredSize(new Dimension(0, 32));
        pnlAddRow.add(txtSoLuong);

        lblDonViTinh = new JLabel("");
        lblDonViTinh.setFont(new Font("Roboto", Font.BOLD, 13));
        lblDonViTinh.setForeground(new Color(41, 128, 185));
        lblDonViTinh.setPreferredSize(new Dimension(80, 32));
        pnlAddRow.add(lblDonViTinh);

        JButton btnAddLine = new JButton("Thêm dòng");
        btnAddLine.setIcon(IconFontSwing.buildIcon(FontAwesome.PLUS, 13, Color.WHITE));
        btnAddLine.setBackground(new Color(46, 204, 113));
        btnAddLine.setForeground(Color.WHITE);
        btnAddLine.setFont(new Font("Roboto", Font.BOLD, 12));
        btnAddLine.setPreferredSize(new Dimension(130, 32));
        btnAddLine.setFocusable(false);
        btnAddLine.addActionListener(e -> addItemRow());
        pnlAddRow.add(btnAddLine);

        centerPanel.add(pnlAddRow, BorderLayout.NORTH);

        // Bảng chi tiết
        String[] cols = {"Mã Nguyên Liệu", "Tên Nguyên Liệu", "Đơn Vị Tính", "Tồn Kho", "Số Lượng Xuất", "Xóa"};
        modelItems = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableItems = new JTable(modelItems);
        tableItems.setRowHeight(38);
        tableItems.setFont(new Font("Roboto", Font.PLAIN, 13));
        tableItems.getTableHeader().setFont(new Font("Roboto", Font.BOLD, 13));
        tableItems.getTableHeader().setBackground(new Color(236, 240, 241));

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

        // ===== BOTTOM: Nút =====
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottom.setOpaque(false);

        JButton btnCancel = new JButton("Hủy");
        btnCancel.setFont(new Font("Roboto", Font.BOLD, 13));
        btnCancel.setPreferredSize(new Dimension(100, 38));
        btnCancel.addActionListener(e -> dispose());

        JButton btnConfirm = new JButton("  Xác nhận Xuất Kho");
        btnConfirm.setIcon(IconFontSwing.buildIcon(FontAwesome.CHECK, 14, Color.WHITE));
        btnConfirm.setBackground(DANGER);
        btnConfirm.setForeground(Color.WHITE);
        btnConfirm.setFont(new Font("Roboto", Font.BOLD, 14));
        btnConfirm.setPreferredSize(new Dimension(220, 38));
        btnConfirm.setFocusable(false);
        btnConfirm.addActionListener(e -> handleConfirm());

        bottom.add(btnCancel);
        bottom.add(btnConfirm);
        mainPanel.add(bottom, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void loadComboData() {
        listNL = controller.getAllNguyenLieu();
        for (NguyenLieu nl : listNL) {
            cbNguyenLieu.addItem(nl.getMaNL() + " - " + nl.getTenNL());
        }

        // Auto-update đơn vị tính khi chọn nguyên liệu
        cbNguyenLieu.addActionListener(e -> {
            int idx = cbNguyenLieu.getSelectedIndex();
            if (idx >= 0 && idx < listNL.size()) {
                String dvt = listNL.get(idx).getDonViTinh();
                lblDonViTinh.setText(dvt != null ? dvt : "");
            } else {
                lblDonViTinh.setText("");
            }
        });

        // Set initial value
        if (!listNL.isEmpty()) {
            String dvt = listNL.get(0).getDonViTinh();
            lblDonViTinh.setText(dvt != null ? dvt : "");
        }
    }

    private void addItemRow() {
        int nlIdx = cbNguyenLieu.getSelectedIndex();
        if (nlIdx < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nguyên liệu.");
            return;
        }

        double soLuong;
        try {
            soLuong = Double.parseDouble(txtSoLuong.getText().trim());
            if (soLuong <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Số lượng phải là số hợp lệ > 0.");
            return;
        }

        NguyenLieu nl = listNL.get(nlIdx);

        // Kiểm tra trùng
        for (ChiTietPhieuXuat ct : chiTietList) {
            if (ct.getMaNL().equals(nl.getMaNL())) {
                JOptionPane.showMessageDialog(this,
                        "Nguyên liệu " + nl.getTenNL() + " đã có trong danh sách.\nVui lòng xóa dòng cũ trước khi thêm lại.");
                return;
            }
        }

        // Kiểm tra tồn kho
        double tonKhoHienTai = 0;
        List<TonKho> allTK = controller.getAllTonKho();
        for (TonKho tk : allTK) {
            if (tk.getMaNL().equals(nl.getMaNL())) {
                tonKhoHienTai = tk.getSoLuongTon();
                break;
            }
        }

        if (soLuong > tonKhoHienTai) {
            JOptionPane.showMessageDialog(this,
                    "Số lượng xuất (" + soLuong + ") vượt quá tồn kho hiện tại (" + tonKhoHienTai + ")!",
                    "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        tempCounter++;
        ChiTietPhieuXuat ct = new ChiTietPhieuXuat();
        ct.setMaCTPX("TEMP_" + tempCounter);
        ct.setSoLuong(soLuong);
        ct.setMaNL(nl.getMaNL());
        chiTietList.add(ct);

        refreshTable();
        txtSoLuong.setText("");
    }

    private void refreshTable() {
        modelItems.setRowCount(0);
        for (ChiTietPhieuXuat ct : chiTietList) {
            NguyenLieu nl = controller.getNguyenLieuById(ct.getMaNL());
            String tenNL = nl != null ? nl.getTenNL() : ct.getMaNL();
            String dvt = nl != null ? nl.getDonViTinh() : "";

            double tonKho = 0;
            List<TonKho> allTK = controller.getAllTonKho();
            for (TonKho tk : allTK) {
                if (tk.getMaNL().equals(ct.getMaNL())) {
                    tonKho = tk.getSoLuongTon();
                    break;
                }
            }

            modelItems.addRow(new Object[]{
                ct.getMaNL(), tenNL, dvt,
                String.format("%.1f", tonKho),
                String.format("%.1f", ct.getSoLuong()),
                "\u274C Xóa"
            });
        }
    }

    private void handleConfirm() {
        if (chiTietList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chưa có nguyên liệu nào trong phiếu xuất!");
            return;
        }

        String lyDo = txtLyDo.getText().trim();
        if (lyDo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập lý do xuất.");
            return;
        }

        // Lấy kho đầu tiên
        List<Kho> listKho = controller.getAllKho();
        Kho kho = listKho.isEmpty() ? null : listKho.get(0);
        if (kho == null) {
            JOptionPane.showMessageDialog(this, "Chưa có kho nào trong hệ thống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        PhieuXuat px = new PhieuXuat();
        px.setMaPX(controller.generateNextMaPX());
        px.setNgayXuat(LocalDateTime.now());
        px.setLyDoXuat(lyDo);
        px.setMaNV(SessionManager.isLoggedIn() ? SessionManager.getCurrentUser().getMaNV() : "NV001");
        px.setMaKho(kho.getMaKho());

        int confirm = JOptionPane.showConfirmDialog(this,
                String.format("Xác nhận xuất kho?\n\nMã phiếu xuất: %s\nLý do: %s\nSố dòng: %d",
                        px.getMaPX(), lyDo, chiTietList.size()),
                "Xác nhận", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (controller.processXuatKho(px, chiTietList)) {
                confirmed = true;
                JOptionPane.showMessageDialog(this,
                        "Xuất kho thành công!\nMã phiếu: " + px.getMaPX(),
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Lỗi khi xuất kho! Kiểm tra lại dữ liệu.",
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
