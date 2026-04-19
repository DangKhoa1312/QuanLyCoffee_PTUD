package ui.panel.admin;

import controller.KhoController;
import entity.ChiTietPhieuXuat;
import entity.Kho;
import entity.NguyenLieu;
import entity.PhieuXuat;
import utils.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CreatePhieuXuatPanel extends JPanel {

    private final KhoController controller = new KhoController();
    private Runnable onBackAction;

    // Left Panel (Menu)
    private JTable nlTable;
    private DefaultTableModel nlModel;
    private JTextField txtSearch;
    
    // Left Form
    private JLabel lblSelectedNL;
    private JTextField txtSoLuong;
    private NguyenLieu currentSelectedNL;
    private double currentTonKho = 0;

    // Right Panel (Cart)
    private JTable cartTable;
    private DefaultTableModel cartModel;
    private JTextField txtLyDo;

    private List<NguyenLieu> listNL;
    private final List<ChiTietPhieuXuat> chiTietList = new ArrayList<>();
    private int tempCounter = 0;

    public CreatePhieuXuatPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));
        initUI();
        loadData();
    }

    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);
        if (aFlag) {
            refresh();
        }
    }

    public void setOnBackAction(Runnable onBackAction) {
        this.onBackAction = onBackAction;
    }

    public void refresh() {
        chiTietList.clear();
        refreshCartTable();
        loadData();
        clearForm();
    }

    private void initUI() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(15, 20, 15, 20));

        JButton btnBack = new JButton("⬅ Quay lại");
        btnBack.setFont(new Font("Roboto", Font.BOLD, 15));
        btnBack.setBackground(null);
        btnBack.setBorderPainted(false);
        btnBack.setFocusPainted(false);
        btnBack.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> {
            if (onBackAction != null) onBackAction.run();
        });
        header.add(btnBack, BorderLayout.WEST);

        JLabel lblTitle = new JLabel("TẠO PHIẾU XUẤT KHO", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Roboto", Font.BOLD, 22));
        lblTitle.setForeground(new Color(231, 76, 60)); // Red for export
        header.add(lblTitle, BorderLayout.CENTER);

        // Spacer
        JLabel spacer = new JLabel();
        spacer.setPreferredSize(new Dimension(100, 10));
        header.add(spacer, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // Split Pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(500);
        splitPane.setResizeWeight(0.5);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);

        splitPane.setLeftComponent(createLeftPanel());
        splitPane.setRightComponent(createRightPanel());

        JPanel centerContainer = new JPanel(new BorderLayout());
        centerContainer.setBorder(new EmptyBorder(0, 20, 20, 20));
        centerContainer.setOpaque(false);
        centerContainer.add(splitPane, BorderLayout.CENTER);
        
        add(centerContainer, BorderLayout.CENTER);
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 0, 10));

        // Search Bar
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.setOpaque(false);
        searchPanel.add(new JLabel("Tìm kiếm NL:"), BorderLayout.WEST);
        txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(0, 35));
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterNL(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterNL(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterNL(); }
        });
        searchPanel.add(txtSearch, BorderLayout.CENTER);
        panel.add(searchPanel, BorderLayout.NORTH);

        // Table
        nlModel = new DefaultTableModel(new String[]{"Mã", "Tên NL", "ĐV Tính", "Tồn Hiện Tại"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        nlTable = new JTable(nlModel);
        nlTable.setRowHeight(35);
        nlTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && nlTable.getSelectedRow() != -1) {
                selectNL(nlTable.getSelectedRow());
            }
        });
        JScrollPane scroll = new JScrollPane(nlTable);
        scroll.getViewport().setBackground(Color.WHITE);
        panel.add(scroll, BorderLayout.CENTER);

        // Form Add to Cart
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 230, 230)),
            new EmptyBorder(15, 15, 15, 15)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        formPanel.add(new JLabel("Đang chọn:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        lblSelectedNL = new JLabel("-- Chưa chọn --");
        lblSelectedNL.setFont(new Font("Roboto", Font.BOLD, 14));
        lblSelectedNL.setForeground(new Color(231, 76, 60));
        formPanel.add(lblSelectedNL, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Số lượng xuất (Theo ĐV Tính):"), gbc);
        gbc.gridx = 1;
        txtSoLuong = new JTextField();
        txtSoLuong.setPreferredSize(new Dimension(0, 30));
        formPanel.add(txtSoLuong, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 5, 5, 5);
        JButton btnAdd = new JButton("Thêm vào phiếu \u2794");
        btnAdd.setBackground(new Color(230, 126, 34)); // Orange
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFont(new Font("Roboto", Font.BOLD, 14));
        btnAdd.setPreferredSize(new Dimension(0, 40));
        btnAdd.addActionListener(e -> addToCart());
        formPanel.add(btnAdd, gbc);

        panel.add(formPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 10, 0, 0));

        // Info Header
        JPanel infoPanel = new JPanel(new BorderLayout(5, 5));
        infoPanel.setOpaque(false);
        infoPanel.add(new JLabel("Lý do xuất:"), BorderLayout.WEST);
        txtLyDo = new JTextField("Xuất kho thủ công");
        txtLyDo.setPreferredSize(new Dimension(0, 35));
        infoPanel.add(txtLyDo, BorderLayout.CENTER);
        panel.add(infoPanel, BorderLayout.NORTH);

        // Cart Table
        cartModel = new DefaultTableModel(new String[]{"Mã", "Tên", "ĐV Tính", "SL Xuất", "Xóa"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        cartTable = new JTable(cartModel);
        cartTable.setRowHeight(40);
        cartTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int col = cartTable.columnAtPoint(e.getPoint());
                int row = cartTable.rowAtPoint(e.getPoint());
                if (col == 4 && row >= 0) {
                    chiTietList.remove(row);
                    refreshCartTable();
                }
            }
        });
        JScrollPane scroll = new JScrollPane(cartTable);
        scroll.getViewport().setBackground(Color.WHITE);
        panel.add(scroll, BorderLayout.CENTER);

        // Footer
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        footerPanel.setBackground(Color.WHITE);
        footerPanel.setBorder(new LineBorder(new Color(230, 230, 230)));

        JButton btnConfirm = new JButton("Xác nhận Xuất Kho");
        btnConfirm.setBackground(new Color(231, 76, 60));
        btnConfirm.setForeground(Color.WHITE);
        btnConfirm.setFont(new Font("Roboto", Font.BOLD, 15));
        btnConfirm.setPreferredSize(new Dimension(180, 45));
        btnConfirm.addActionListener(e -> savePhieuXuat());
        footerPanel.add(btnConfirm);

        panel.add(footerPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void loadData() {
        listNL = controller.getAllNguyenLieu();
        filterNL();
    }

    private void filterNL() {
        nlModel.setRowCount(0);
        String kw = txtSearch.getText().toLowerCase().trim();
        List<entity.TonKho> allTK = controller.getAllTonKho();
        
        for (NguyenLieu nl : listNL) {
            if (kw.isEmpty() || nl.getTenNL().toLowerCase().contains(kw) || nl.getMaNL().toLowerCase().contains(kw)) {
                double ton = 0;
                for (entity.TonKho tk : allTK) {
                    if (tk.getMaNL().equals(nl.getMaNL())) {
                        ton = tk.getSoLuongTon();
                        break;
                    }
                }
                nlModel.addRow(new Object[]{
                    nl.getMaNL(), nl.getTenNL(), nl.getDonViTinh(), String.format("%.1f", ton)
                });
            }
        }
    }

    private void selectNL(int row) {
        String maNL = (String) nlModel.getValueAt(row, 0);
        String tonStr = (String) nlModel.getValueAt(row, 3);
        currentTonKho = Double.parseDouble(tonStr.replace(",", "."));
        
        for (NguyenLieu nl : listNL) {
            if (nl.getMaNL().equals(maNL)) {
                currentSelectedNL = nl;
                String dvt = nl.getDonViTinh() != null ? nl.getDonViTinh() : "";
                lblSelectedNL.setText(nl.getTenNL() + " (Tồn: " + currentTonKho + " " + dvt + ")");
                txtSoLuong.setText("1");
                txtSoLuong.requestFocus();
                txtSoLuong.selectAll();
                break;
            }
        }
    }

    private void clearForm() {
        currentSelectedNL = null;
        currentTonKho = 0;
        lblSelectedNL.setText("-- Chưa chọn --");
        txtSoLuong.setText("");
        nlTable.clearSelection();
    }

    private void addToCart() {
        if (currentSelectedNL == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nguyên liệu từ bảng bên trên.");
            return;
        }
        
        double soLuong;
        try {
            soLuong = Double.parseDouble(txtSoLuong.getText().trim());
            if (soLuong <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Số lượng xuất phải là số hợp lệ > 0.");
            return;
        }

        if (soLuong > currentTonKho) {
            JOptionPane.showMessageDialog(this, "Số lượng xuất (" + soLuong + ") vượt quá tồn kho hiện tại (" + currentTonKho + ")!");
            return;
        }

        for (ChiTietPhieuXuat ct : chiTietList) {
            if (ct.getMaNL().equals(currentSelectedNL.getMaNL())) {
                JOptionPane.showMessageDialog(this, "Nguyên liệu đã có trong phiếu. Vui lòng xóa dòng cũ để thêm lại.");
                return;
            }
        }

        tempCounter++;
        ChiTietPhieuXuat ct = new ChiTietPhieuXuat();
        ct.setMaCTPX("TEMP_" + tempCounter);
        ct.setSoLuong(soLuong);
        ct.setMaNL(currentSelectedNL.getMaNL());
        
        chiTietList.add(ct);
        refreshCartTable();
        clearForm();
    }

    private void refreshCartTable() {
        cartModel.setRowCount(0);
        for (ChiTietPhieuXuat ct : chiTietList) {
            NguyenLieu nl = controller.getNguyenLieuById(ct.getMaNL());
            String tenNL = nl != null ? nl.getTenNL() : ct.getMaNL();
            String dvt = nl != null ? nl.getDonViTinh() : "";
            cartModel.addRow(new Object[]{
                ct.getMaNL(), tenNL, dvt,
                String.format("%.1f", ct.getSoLuong()),
                "\u274C Xóa"
            });
        }
    }

    private void savePhieuXuat() {
        if (chiTietList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Phiếu xuất trống!");
            return;
        }

        String lyDo = txtLyDo.getText().trim();
        if (lyDo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập lý do xuất.");
            return;
        }

        List<Kho> listKho = controller.getAllKho();
        if (listKho.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chưa có kho nào trong hệ thống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        for (ChiTietPhieuXuat ct : chiTietList) {
            ct.setMaCTPX(controller.generateNextMaCTPX());
        }

        PhieuXuat px = new PhieuXuat();
        px.setMaPX(controller.generateNextMaPX());
        px.setNgayXuat(LocalDateTime.now());
        px.setLyDoXuat(lyDo);
        px.setMaNV(SessionManager.isLoggedIn() ? SessionManager.getCurrentUser().getMaNV() : "NV001");
        px.setMaKho(listKho.get(0).getMaKho());

        if (controller.processXuatKho(px, chiTietList)) {
            JOptionPane.showMessageDialog(this, "✅ Xuất kho thành công!\nMã phiếu: " + px.getMaPX());
            if (onBackAction != null) onBackAction.run();
        } else {
            JOptionPane.showMessageDialog(this, "❌ Lỗi khi lưu phiếu xuất!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
