package ui.panel.admin;

import controller.TableController;
import entity.Ban;
import entity.KhuVuc;
import enums.TrangThaiBan;
import utils.IDGenerator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel quản trị Sơ đồ bàn theo luồng Master-Detail:
 * - Master View: Danh sách Khu Vực. Double click -> Detail View.
 * - Detail View: Thông tin Khu Vực & Danh sách các Bàn của Khu Vực đó.
 */
public class TableManagementPanel extends JPanel {

    private final TableController controller = new TableController();

    private CardLayout cardLayout;
    private JPanel mainContent;

    // --- Master View (Khu Vực) ---
    private DefaultTableModel kvModel;
    private JTable kvTable;

    // --- Detail View (Bàn) ---
    private KhuVuc currentKhuVuc;
    
    // Labels for Area Info in Detail logic
    private JLabel lblDetailBreadcrumb;
    private JLabel lblDetailName;
    private JLabel lblDetailDesc;
    private JLabel lblDetailStatus;

    private DefaultTableModel banModel;
    private JTable banTable;

    private final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private final Color BG_COLOR = new Color(245, 247, 250);

    public TableManagementPanel() {
        setLayout(new BorderLayout());
        setBackground(BG_COLOR);
        setBorder(new EmptyBorder(15, 25, 25, 25));
        initUI();
    }

    private void initUI() {
        cardLayout = new CardLayout();
        mainContent = new JPanel(cardLayout);
        mainContent.setOpaque(false);

        mainContent.add(createMasterView(), "MASTER");
        mainContent.add(createDetailView(), "DETAIL");

        add(mainContent, BorderLayout.CENTER);
        
        loadKhuVucData();
    }

    // ═══════════════════════════════════════════════════════
    // MASTER VIEW: DANH SÁCH KHU VỰC
    // ═══════════════════════════════════════════════════════

    private JPanel createMasterView() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setOpaque(false);

        // --- Header ---
        JPanel pnlHeader = new JPanel();
        pnlHeader.setLayout(new BoxLayout(pnlHeader, BoxLayout.Y_AXIS));
        pnlHeader.setOpaque(false);

        JPanel pnlTitle = new JPanel(new BorderLayout());
        pnlTitle.setOpaque(false);
        JLabel lblTitle = new JLabel("DANH SÁCH KHU VỰC");
        lblTitle.setFont(new Font("Roboto", Font.BOLD, 22));
        lblTitle.setForeground(new Color(44, 62, 80));
        pnlTitle.add(lblTitle, BorderLayout.WEST);

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlButtons.setOpaque(false);
        
        JButton btnAdd = createToolbarButton("+ Tạo Khu Vực Mới", new Color(46, 204, 113));
        JButton btnRefresh = createToolbarButton("🔄 Làm mới", new Color(100, 100, 100));
        
        pnlButtons.add(btnAdd);
        pnlButtons.add(btnRefresh);
        pnlTitle.add(pnlButtons, BorderLayout.EAST);

        pnlHeader.add(pnlTitle);
        panel.add(pnlHeader, BorderLayout.NORTH);

        // --- Table ---
        kvModel = new DefaultTableModel(
            new String[]{"Mã KV", "Tên Khu Vực", "Mô Tả", "Số Bàn", "Trạng Thái"}, 0
        ) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        kvTable = new JTable(kvModel);
        kvTable.setFont(new Font("Roboto", Font.PLAIN, 14));
        kvTable.setRowHeight(45);
        kvTable.getTableHeader().setFont(new Font("Roboto", Font.BOLD, 14));
        kvTable.getTableHeader().setBackground(new Color(236, 240, 241));
        kvTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        kvTable.setShowVerticalLines(false);

        kvTable.getColumnModel().getColumn(4).setCellRenderer(new StatusRenderer());
        
        // Hide Mã KV
        kvTable.getColumnModel().getColumn(0).setMinWidth(0);
        kvTable.getColumnModel().getColumn(0).setMaxWidth(0);
        kvTable.getColumnModel().getColumn(0).setPreferredWidth(0);

        JScrollPane scroll = new JScrollPane(kvTable);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        panel.add(scroll, BorderLayout.CENTER);

        // --- Events ---
        btnAdd.addActionListener(e -> handleAddKhuVuc());
        btnRefresh.addActionListener(e -> loadKhuVucData());

        kvTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) goDetailView();
            }
        });

        return panel;
    }

    private void loadKhuVucData() {
        kvModel.setRowCount(0);
        List<KhuVuc> list = controller.getAllKhuVuc();
        for (KhuVuc kv : list) {
            int count = controller.countBanByKhuVuc(kv.getMaKhuVuc());
            kvModel.addRow(new Object[]{
                kv.getMaKhuVuc(),
                kv.getTenKhuVuc(),
                kv.getMoTa() != null ? kv.getMoTa() : "",
                count,
                kv.isTrangThai() ? "Đang hoạt động" : "Tạm ngưng"
            });
        }
    }

    private void goDetailView() {
        int row = kvTable.getSelectedRow();
        if (row < 0) return;
        String maKV = (String) kvModel.getValueAt(row, 0);
        
        // Fetch fresh
        currentKhuVuc = null;
        for(KhuVuc k : controller.getAllKhuVuc()) {
            if(k.getMaKhuVuc().equals(maKV)) {
                currentKhuVuc = k; break;
            }
        }
        if(currentKhuVuc == null) return;
        
        refreshDetailViewData();
        cardLayout.show(mainContent, "DETAIL");
    }

    // ═══════════════════════════════════════════════════════
    // DETAIL VIEW: THÔNG TIN KHU VỰC VÀ DANH SÁCH BÀN
    // ═══════════════════════════════════════════════════════

    private JPanel createDetailView() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setOpaque(false);

        // --- Header Khu Vực ---
        JPanel pnlHeader = new JPanel();
        pnlHeader.setLayout(new BoxLayout(pnlHeader, BoxLayout.Y_AXIS));
        pnlHeader.setOpaque(false);

        // Breadcrumb
        JPanel pnlBreadcrumb = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlBreadcrumb.setOpaque(false);
        
        JButton btnBack = new JButton("⬅ Trở lại");
        btnBack.setFont(new Font("Roboto", Font.BOLD, 13));
        btnBack.setForeground(PRIMARY_COLOR);
        btnBack.setContentAreaFilled(false);
        btnBack.setBorderPainted(false);
        btnBack.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> {
            loadKhuVucData();
            cardLayout.show(mainContent, "MASTER");
        });
        
        lblDetailBreadcrumb = new JLabel(" / Khu vực chi tiết");
        lblDetailBreadcrumb.setFont(new Font("Roboto", Font.BOLD, 13));
        lblDetailBreadcrumb.setForeground(Color.GRAY);
        
        pnlBreadcrumb.add(btnBack);
        pnlBreadcrumb.add(lblDetailBreadcrumb);
        pnlHeader.add(pnlBreadcrumb);
        pnlHeader.add(Box.createVerticalStrut(10));

        // Info Card
        JPanel cardInfo = new JPanel(new BorderLayout());
        cardInfo.setBackground(Color.WHITE);
        cardInfo.putClientProperty("FlatLaf.style", "arc: 10");
        cardInfo.setBorder(new EmptyBorder(15, 20, 15, 20));

        JPanel pnlInfoText = new JPanel(new GridLayout(3, 1, 0, 8));
        pnlInfoText.setOpaque(false);
        lblDetailName = new JLabel();
        lblDetailName.setFont(new Font("Roboto", Font.BOLD, 18));
        lblDetailDesc = new JLabel();
        lblDetailDesc.setFont(new Font("Roboto", Font.PLAIN, 14));
        lblDetailDesc.setForeground(Color.GRAY);
        lblDetailStatus = new JLabel();
        lblDetailStatus.setFont(new Font("Roboto", Font.BOLD, 13));
        
        pnlInfoText.add(lblDetailName);
        pnlInfoText.add(lblDetailDesc);
        pnlInfoText.add(lblDetailStatus);
        cardInfo.add(pnlInfoText, BorderLayout.CENTER);

        // Actions Khu Vực
        JPanel pnlAreaActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlAreaActions.setOpaque(false);
        JButton btnEditKV = createToolbarButton("✏ Cập nhật Khu Vực", new Color(52, 152, 219));
        JButton btnToggleKV = createToolbarButton("⏸ Tạm ngưng/Kích hoạt Khu Vực", new Color(243, 156, 18));
        JButton btnXoaKV = createToolbarButton("🗑 Xóa Khu Vực", new Color(231, 76, 60));

        btnEditKV.addActionListener(e -> handleEditKhuVuc());
        btnToggleKV.addActionListener(e -> handleToggleKhuVuc());
        btnXoaKV.addActionListener(e -> handleDeleteKhuVuc());

        pnlAreaActions.add(btnEditKV);
        pnlAreaActions.add(btnToggleKV);
        pnlAreaActions.add(btnXoaKV);
        cardInfo.add(pnlAreaActions, BorderLayout.EAST);

        pnlHeader.add(cardInfo);
        panel.add(pnlHeader, BorderLayout.NORTH);

        // --- Danh sách Bàn Section ---
        JPanel pnlBan = new JPanel(new BorderLayout(0, 10));
        pnlBan.setOpaque(false);

        // Toolbar Bàn
        JPanel pnlBanToolbar = new JPanel(new BorderLayout());
        pnlBanToolbar.setOpaque(false);
        JLabel lblListTbTitle = new JLabel("Danh sách Bàn thuộc Khu Vực");
        lblListTbTitle.setFont(new Font("Roboto", Font.BOLD, 16));
        lblListTbTitle.setForeground(new Color(26, 26, 46));
        
        JPanel pnlBanActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlBanActions.setOpaque(false);
        JButton btnAddBan = createToolbarButton("+ Thêm Bàn", new Color(39, 174, 96));
        JButton btnEditBan = createToolbarButton("✏ Sửa Bàn", new Color(52, 152, 219));
        JButton btnRefreshBan = createToolbarButton("🔄 Làm mới", new Color(100, 100, 100));

        btnAddBan.addActionListener(e -> handleAddBan());
        btnEditBan.addActionListener(e -> handleEditBan());
        btnRefreshBan.addActionListener(e -> loadBanDataForCurrentKhuVuc());
        
        pnlBanActions.add(btnAddBan);
        pnlBanActions.add(btnEditBan);
        pnlBanActions.add(btnRefreshBan);

        pnlBanToolbar.add(lblListTbTitle, BorderLayout.WEST);
        pnlBanToolbar.add(pnlBanActions, BorderLayout.EAST);
        pnlBan.add(pnlBanToolbar, BorderLayout.NORTH);

        // Table Bàn
        banModel = new DefaultTableModel(
            new String[]{"Mã Bàn", "Số Bàn", "Sức Chứa", "Trạng Thái"}, 0
        ) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        banTable = new JTable(banModel);
        banTable.setFont(new Font("Roboto", Font.PLAIN, 14));
        banTable.setRowHeight(40);
        banTable.getTableHeader().setFont(new Font("Roboto", Font.BOLD, 14));
        banTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        banTable.getColumnModel().getColumn(3).setCellRenderer(new BanStatusRenderer());
        
        banTable.getColumnModel().getColumn(0).setMinWidth(0);
        banTable.getColumnModel().getColumn(0).setMaxWidth(0);
        banTable.getColumnModel().getColumn(0).setPreferredWidth(0);

        JScrollPane scrollBan = new JScrollPane(banTable);
        scrollBan.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        pnlBan.add(scrollBan, BorderLayout.CENTER);

        banTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) handleEditBan();
            }
        });

        panel.add(pnlBan, BorderLayout.CENTER);
        return panel;
    }

    private void refreshDetailViewData() {
        if(currentKhuVuc == null) return;
        lblDetailBreadcrumb.setText(" / " + currentKhuVuc.getTenKhuVuc());
        lblDetailName.setText("Khu vực: " + currentKhuVuc.getTenKhuVuc());
        lblDetailDesc.setText("Mô tả: " + (currentKhuVuc.getMoTa() == null || currentKhuVuc.getMoTa().isEmpty() ? "(Không có)" : currentKhuVuc.getMoTa()));
        
        if (currentKhuVuc.isTrangThai()) {
            lblDetailStatus.setText("● Đang hoạt động");
            lblDetailStatus.setForeground(new Color(39, 174, 96));
        } else {
            lblDetailStatus.setText("○ Tạm ngưng");
            lblDetailStatus.setForeground(new Color(231, 76, 60));
        }
        
        loadBanDataForCurrentKhuVuc();
    }

    private void loadBanDataForCurrentKhuVuc() {
        banModel.setRowCount(0);
        if(currentKhuVuc == null) return;

        List<Ban> list = controller.getBanByKhuVuc(currentKhuVuc.getMaKhuVuc());

        for (Ban b : list) {
            String trangThai;
            switch (b.getTrangThai()) {
                case TRONG: trangThai = "Trống"; break;
                case CO_KHACH: trangThai = "Có khách"; break;
                case DA_DAT_TRUOC: trangThai = "Đã đặt"; break;
                case TAM_NGUNG: trangThai = "Tạm ngưng"; break;
                default: trangThai = b.getTrangThai().name();
            }

            banModel.addRow(new Object[]{
                b.getMaBan(),
                b.getSoBan(),
                b.getSucChua() + " người",
                trangThai
            });
        }
    }

    // ═══════════════════════════════════════════════════════
    // HANDLERS KHU VỰC
    // ═══════════════════════════════════════════════════════

    private void handleAddKhuVuc() {
        JTextField txtTen = new JTextField(20);
        JTextField txtMoTa = new JTextField(20);

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 8));
        form.add(new JLabel("Tên khu vực (*):"));
        form.add(txtTen);
        form.add(new JLabel("Mô tả:"));
        form.add(txtMoTa);

        while (true) {
            int result = JOptionPane.showConfirmDialog(this, form, "Thêm Khu Vực Mới",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                // Bug 8: Validate tên rỗng — phải hiển thị lỗi
                String tenKV = txtTen.getText().trim();
                if (tenKV.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "Tên khu vực không được để trống!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                    continue;
                }
                // Bug 3: Validate trùng tên
                if (controller.isTenKhuVucTrung(tenKV, null)) {
                    JOptionPane.showMessageDialog(this,
                            "Tên khu vực \"" + tenKV + "\" đã tồn tại! Vui lòng chọn tên khác.",
                            "Trùng tên", JOptionPane.ERROR_MESSAGE);
                    continue;
                }
                String maKV = IDGenerator.newMaKhuVuc();
                KhuVuc kv = new KhuVuc(maKV, tenKV, txtMoTa.getText().trim(), true);
                if (controller.addKhuVuc(kv)) {
                    loadKhuVucData();
                    JOptionPane.showMessageDialog(this, "Thêm khu vực thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    break;
                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi khi thêm khu vực!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    continue;
                }
            } else {
                break;
            }
        }
    }

    private void handleEditKhuVuc() {
        if(currentKhuVuc == null) return;
        
        JTextField txtTen = new JTextField(currentKhuVuc.getTenKhuVuc(), 20);
        JTextField txtMoTa = new JTextField(currentKhuVuc.getMoTa(), 20);

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 8));
        form.add(new JLabel("Mã KV:"));
        form.add(new JLabel(currentKhuVuc.getMaKhuVuc()));
        form.add(new JLabel("Tên khu vực (*):"));
        form.add(txtTen);
        form.add(new JLabel("Mô tả:"));
        form.add(txtMoTa);

        while (true) {
            int result = JOptionPane.showConfirmDialog(this, form, "Cập Nhật Khu Vực",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                String tenKV = txtTen.getText().trim();
                if (tenKV.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "Tên khu vực không được để trống!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                    continue;
                }
                // Bug 3: Validate trùng tên (bỏ qua chính nó)
                if (controller.isTenKhuVucTrung(tenKV, currentKhuVuc.getMaKhuVuc())) {
                    JOptionPane.showMessageDialog(this,
                            "Tên khu vực \"" + tenKV + "\" đã tồn tại! Vui lòng chọn tên khác.",
                            "Trùng tên", JOptionPane.ERROR_MESSAGE);
                    continue;
                }
                currentKhuVuc.setTenKhuVuc(tenKV);
                currentKhuVuc.setMoTa(txtMoTa.getText().trim());
                
                if (controller.updateKhuVuc(currentKhuVuc)) {
                    refreshDetailViewData();
                    JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                    break;
                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    continue;
                }
            } else {
                break;
            }
        }
    }

    private void handleToggleKhuVuc() {
        if(currentKhuVuc == null) return;

        // Bug 4: Chặn tạm ngưng nếu còn bàn đang có khách
        if (currentKhuVuc.isTrangThai()) {
            int banCoKhach = controller.countBanCoKhachByKhuVuc(currentKhuVuc.getMaKhuVuc());
            if (banCoKhach > 0) {
                JOptionPane.showMessageDialog(this,
                        "Không thể tạm ngưng khu vực \"" + currentKhuVuc.getTenKhuVuc() +
                        "\" vì còn " + banCoKhach + " bàn đang có khách!\n" +
                        "Vui lòng hoàn tất tất cả đơn hàng trước.",
                        "Không thể tạm ngưng", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        String action = currentKhuVuc.isTrangThai() ? "TẠM NGƯNG" : "KÍCH HOẠT";
        int confirm = JOptionPane.showConfirmDialog(this,
                action + " khu vực \"" + currentKhuVuc.getTenKhuVuc() + "\"?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (controller.toggleKhuVuc(currentKhuVuc)) {
                refreshDetailViewData();
            }
        }
    }

    private void handleDeleteKhuVuc() {
        if(currentKhuVuc == null) return;

        // Bug 4: Kiểm tra còn bàn có khách không
        int banCoKhach = controller.countBanCoKhachByKhuVuc(currentKhuVuc.getMaKhuVuc());
        if (banCoKhach > 0) {
            JOptionPane.showMessageDialog(this,
                    "Không thể xóa khu vực \"" + currentKhuVuc.getTenKhuVuc() +
                    "\" vì còn " + banCoKhach + " bàn đang có khách!\n" +
                    "Vui lòng hoàn tất tất cả đơn hàng trước.",
                    "Không thể xóa", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int count = controller.countBanByKhuVuc(currentKhuVuc.getMaKhuVuc());
        if (count > 0) {
            JOptionPane.showMessageDialog(this,
                    "Không thể xóa khu vực \"" + currentKhuVuc.getTenKhuVuc() +
                    "\" vì còn " + count + " bàn!\nHãy xóa hoặc chuyển hết bàn trước.",
                    "Không thể xóa", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn chắc chắn muốn xóa khu vực \"" + currentKhuVuc.getTenKhuVuc() + "\"?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (controller.deleteKhuVuc(currentKhuVuc.getMaKhuVuc())) {
                JOptionPane.showMessageDialog(this, "Xóa thành công!");
                loadKhuVucData();
                cardLayout.show(mainContent, "MASTER");
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi xóa!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ═══════════════════════════════════════════════════════
    // HANDLERS BÀN
    // ═══════════════════════════════════════════════════════

    private void handleAddBan() {
        if(currentKhuVuc == null) return;
        
        List<Ban> listBan = controller.getBanByKhuVuc(currentKhuVuc.getMaKhuVuc());
        int max = 0;
        for (Ban b : listBan) {
            String strNum = b.getSoBan().replaceAll("[^0-9]", "");
            if (!strNum.isEmpty()) {
                try {
                    int num = Integer.parseInt(strNum);
                    if (num > max) max = num;
                } catch (Exception ignored) {}
            }
        }
        String soBanGoiY = String.format("%02d", max + 1);
        
        JTextField txtSoBan = new JTextField(soBanGoiY, 15);
        JSpinner spnSucChua = new JSpinner(new SpinnerNumberModel(4, 1, 50, 1));
        JComboBox<String> cbStatus = new JComboBox<>(new String[]{"Trống (Hoạt động)", "Tạm ngưng"});
        
        JPanel form = new JPanel(new GridLayout(0, 2, 10, 8));
        form.add(new JLabel("Số bàn:"));
        form.add(txtSoBan);
        form.add(new JLabel("Sức chứa (người):"));
        form.add(spnSucChua);
        form.add(new JLabel("Trạng thái mới:"));
        form.add(cbStatus);

        Object[] options = {"Lưu lại", "Thoát"};
        int result = JOptionPane.showOptionDialog(this, form, "Thêm Bàn Mới - " + currentKhuVuc.getTenKhuVuc(),
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        if (result == 0 && !txtSoBan.getText().trim().isEmpty()) {
            String maBan = IDGenerator.newMaBan();
            TrangThaiBan tt = cbStatus.getSelectedIndex() == 0 ? TrangThaiBan.TRONG : TrangThaiBan.TAM_NGUNG;
            Ban ban = new Ban(maBan, txtSoBan.getText().trim(), currentKhuVuc.getMaKhuVuc(),
                    (int) spnSucChua.getValue(), tt);

            if (controller.addBan(ban)) {
                loadBanDataForCurrentKhuVuc();
                JOptionPane.showMessageDialog(this, "Thêm bàn thành công!");
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi thêm bàn! (Có thể trùng số bàn)", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleEditBan() {
        int row = banTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn bàn cần sửa.");
            return;
        }

        String maBan = (String) banModel.getValueAt(row, 0);
        String soBan = (String) banModel.getValueAt(row, 1);
        String sucChuaStr = ((String) banModel.getValueAt(row, 2)).replaceAll("[^0-9]", "");
        
        JTextField txtSoBan = new JTextField(soBan, 15);
        JSpinner spnSucChua = new JSpinner(new SpinnerNumberModel(
                Integer.parseInt(sucChuaStr), 1, 50, 1));
        
        // ComboBox to allow moving table to another area if needed
        List<KhuVuc> dsKV = controller.getAllKhuVuc();
        JComboBox<String> cbKV = new JComboBox<>();
        int selectedIdx = 0;
        for (int i = 0; i < dsKV.size(); i++) {
            KhuVuc kv = dsKV.get(i);
            cbKV.addItem(kv.getMaKhuVuc() + " - " + kv.getTenKhuVuc());
            if (kv.getMaKhuVuc().equals(currentKhuVuc.getMaKhuVuc())) {
                selectedIdx = i;
            }
        }
        cbKV.setSelectedIndex(selectedIdx);
        
        String ttStr = (String) banModel.getValueAt(row, 3);
        JComboBox<String> cbStatus = new JComboBox<>(new String[]{"Trống", "Có khách", "Đã đặt", "Tạm ngưng"});
        cbStatus.setSelectedItem(ttStr);

        int sucChuaGoc = Integer.parseInt(sucChuaStr);
        String maKVGoc = currentKhuVuc.getMaKhuVuc();

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 8));
        form.add(new JLabel("Mã bàn:"));
        form.add(new JLabel(maBan));
        form.add(new JLabel("Số bàn:"));
        form.add(txtSoBan);
        form.add(new JLabel("Sức chứa (người):"));
        form.add(spnSucChua);
        form.add(new JLabel("Đổi khu vực:"));
        form.add(cbKV);
        form.add(new JLabel("Trạng thái:"));
        form.add(cbStatus);

        Object[] options = {"Lưu", "Thoát"};
        int result = JOptionPane.showOptionDialog(this, form, "Cập Nhật Thông Tin Bàn",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        if (result == 0 && !txtSoBan.getText().trim().isEmpty()) {
            String maKVMoi = ((String) cbKV.getSelectedItem()).split(" - ")[0];
            String ttMoi = (String) cbStatus.getSelectedItem();
            String soBanMoi = txtSoBan.getText().trim();
            int sucChuaMoi = (int) spnSucChua.getValue();

            // Check if nothing changed
            if (soBanMoi.equals(soBan) && sucChuaMoi == sucChuaGoc &&
                maKVMoi.equals(maKVGoc) && ttMoi.equals(ttStr)) {
                JOptionPane.showMessageDialog(this, "Bạn chưa thay đổi thông tin gì cho bàn này.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            if ("Có khách".equals(ttStr) && !ttMoi.equals("Có khách")) {
                JOptionPane.showMessageDialog(this, "Không thể chuyển trạng thái bàn đang có khách bằng tay. Hãy hoàn tất Đơn hàng.");
                return;
            }
            if (ttMoi.equals("Có khách") && !"Có khách".equals(ttStr)) {
                JOptionPane.showMessageDialog(this, "Trạng thái Có khách tự động được thiết lập khi Gọi món, không thể gán tay.");
                return;
            }

            TrangThaiBan tt = TrangThaiBan.TRONG;
            switch (ttMoi) {
                case "Có khách": tt = TrangThaiBan.CO_KHACH; break;
                case "Đã đặt": tt = TrangThaiBan.DA_DAT_TRUOC; break;
                case "Tạm ngưng": tt = TrangThaiBan.TAM_NGUNG; break;
            }

            Ban ban = new Ban(maBan, soBanMoi, maKVMoi, sucChuaMoi, tt);

            if (controller.updateBan(ban)) {
                loadBanDataForCurrentKhuVuc();
                JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                // Nếu đổi khu vực, thì bàn sẽ biến mất khỏi bảng danh sách của Area hiện tại.
                if (!maKVMoi.equals(maKVGoc)) {
                    refreshDetailViewData();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật! (Số bàn có thể bị trùng)", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ═══════════════ UTILS ═══════════════

    private JButton createToolbarButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Roboto", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bgColor);
        btn.setFocusable(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bgColor.darker(), 1),
            new EmptyBorder(6, 14, 6, 14)
        ));

        Color hoverColor = bgColor.darker();
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(hoverColor); }
            public void mouseExited(java.awt.event.MouseEvent e) { btn.setBackground(bgColor); }
        });

        return btn;
    }

    private static class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            lbl.setHorizontalAlignment(CENTER);
            String text = value != null ? value.toString() : "";
            if ("Đang hoạt động".equals(text)) {
                if (!isSelected) { lbl.setForeground(new Color(39, 174, 96)); }
                lbl.setText("● " + text);
            } else {
                if (!isSelected) { lbl.setForeground(new Color(231, 76, 60)); }
                lbl.setText("○ " + text);
            }
            lbl.setFont(new Font("Roboto", Font.BOLD, 12));
            return lbl;
        }
    }

    private static class BanStatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            lbl.setHorizontalAlignment(CENTER);
            lbl.setFont(new Font("Roboto", Font.BOLD, 12));
            String text = value != null ? value.toString() : "";
            if (!isSelected) {
                switch (text) {
                    case "Trống": lbl.setForeground(new Color(39, 174, 96)); break;
                    case "Có khách": lbl.setForeground(new Color(231, 76, 60)); break;
                    case "Đã đặt": lbl.setForeground(new Color(243, 156, 18)); break;
                    case "Tạm ngưng": lbl.setForeground(new Color(149, 165, 166)); break;
                    default: lbl.setForeground(Color.BLACK);
                }
            }
            lbl.setText("● " + text);
            return lbl;
        }
    }
}
