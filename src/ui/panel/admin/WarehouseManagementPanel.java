package ui.panel.admin;

import controller.KhoController;
import entity.*;
import jiconfont.icons.FontAwesome;
import jiconfont.swing.IconFontSwing;
import utils.CurrencyUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Panel Quản Lý Kho — Giao diện admin với 4 tab:
 *  1. Tồn Kho  — double-click mở chi tiết nguyên liệu
 *  2. Nguyên Liệu — double-click mở chi tiết
 *  3. Nhà Cung Cấp — double-click mở chi tiết
 *  4. Phiếu Nhập — double-click xem chi tiết phiếu
 */
public class WarehouseManagementPanel extends JPanel {

    private final KhoController controller = new KhoController();

    private final Color PRIMARY_COLOR = new Color(52, 73, 94);
    private final Color BG_COLOR      = new Color(245, 247, 250);
    private final Color WARN_COLOR    = new Color(231, 76, 60);
    private final Color OK_COLOR      = new Color(39, 174, 96);

    // ── Tab: Tồn Kho ──
    private DefaultTableModel tonKhoModel;
    private JTable tonKhoTable;
    private JComboBox<Kho> cbFilterKho;

    // ── Tab: Nguyên Liệu ──
    private DefaultTableModel nlModel;
    private JTable nlTable;
    private JTextField txtNLSearch;

    // ── Tab: Nhà Cung Cấp ──
    private DefaultTableModel nccModel;
    private JTable nccTable;
    private JTextField txtNCCSearch;

    // ── Tab: Phiếu Nhập ──
    private DefaultTableModel pnModel;
    private JTable pnTable;

    public WarehouseManagementPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG_COLOR);
        setBorder(new EmptyBorder(15, 25, 25, 25));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTabs(), BorderLayout.CENTER);

        loadAllData();
    }

    // ═══════════════════════════════════════════════════════════════
    // HEADER
    // ═══════════════════════════════════════════════════════════════
    private JPanel buildHeader() {
        JPanel pnl = new JPanel();
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
        pnl.setOpaque(false);

        JPanel bc = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bc.setOpaque(false);
        JLabel lbcGray = new JLabel("Admin / Quản Trị / ");
        lbcGray.setForeground(Color.GRAY);
        lbcGray.setFont(new Font("Roboto", Font.PLAIN, 13));
        JLabel lbcCurrent = new JLabel("Quản Lý Kho");
        lbcCurrent.setFont(new Font("Roboto", Font.BOLD, 13));
        lbcCurrent.setForeground(PRIMARY_COLOR);
        bc.add(lbcGray); bc.add(lbcCurrent);
        pnl.add(bc);
        pnl.add(Box.createVerticalStrut(8));

        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setOpaque(false);
        JLabel title = new JLabel("QUẢN LÝ KHO HÀNG");
        title.setFont(new Font("Roboto", Font.BOLD, 24));
        title.setForeground(new Color(44, 62, 80));
        title.setIcon(IconFontSwing.buildIcon(FontAwesome.ARCHIVE, 28, PRIMARY_COLOR));
        title.setIconTextGap(12);
        titleBar.add(title, BorderLayout.WEST);
        pnl.add(titleBar);
        pnl.add(Box.createVerticalStrut(10));
        return pnl;
    }

    // ═══════════════════════════════════════════════════════════════
    // TABS
    // ═══════════════════════════════════════════════════════════════
    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setFont(new Font("Roboto", Font.BOLD, 13));
        tabs.setBackground(Color.WHITE);
        tabs.addTab("  📦 Tồn Kho  ",      buildTonKhoTab());
        tabs.addTab("  🌿 Nguyên Liệu  ",  buildNguyenLieuTab());
        tabs.addTab("  🚚 Nhà Cung Cấp  ", buildNhaCungCapTab());
        tabs.addTab("  📋 Phiếu Nhập  ",   buildPhieuNhapTab());
        return tabs;
    }

    // ═══════════════════════════════════════════════════════════════
    // TAB 1: TỒN KHO — double-click mở chi tiết nguyên liệu
    // ═══════════════════════════════════════════════════════════════
    private JPanel buildTonKhoTab() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(12, 5, 5, 5));

        // Top bar
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 8));
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(new LineBorder(new Color(230, 230, 230)));
        topBar.add(new JLabel(IconFontSwing.buildIcon(FontAwesome.FILTER, 16, Color.GRAY)));
        topBar.add(new JLabel("Kho:"));
        cbFilterKho = new JComboBox<>();
        cbFilterKho.setPreferredSize(new Dimension(200, 35));
        cbFilterKho.setFont(new Font("Roboto", Font.PLAIN, 14));
        cbFilterKho.addActionListener(e -> loadTonKhoData());
        topBar.add(cbFilterKho);

        JButton btnRefresh = createSmallBtn("Làm mới", FontAwesome.REFRESH, new Color(220, 220, 220));
        btnRefresh.addActionListener(e -> loadAllData());
        topBar.add(btnRefresh);
        p.add(topBar, BorderLayout.NORTH);

        // Table — hidden col 9 giữ mã NL cho double-click
        String[] cols = {"Mã TK", "Kho", "Mã NL", "Tên Nguyên Liệu", "ĐVT",
                         "Tồn Kho", "Mức Tối Thiểu", "Ngày Cập Nhật", "Trạng Thái"};
        tonKhoModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tonKhoTable = buildStyledTable(tonKhoModel);
        tonKhoTable.setDefaultRenderer(Object.class, new TonKhoRenderer());
        tonKhoTable.getColumnModel().getColumn(8).setPreferredWidth(100);
        tonKhoTable.getColumnModel().getColumn(3).setPreferredWidth(180);

        // Double-click → mở chi tiết nguyên liệu
        tonKhoTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = tonKhoTable.getSelectedRow();
                    if (row >= 0) {
                        String maNL = (String) tonKhoModel.getValueAt(row, 2);
                        NguyenLieu nl = controller.getNguyenLieuById(maNL);
                        if (nl != null) handleEditNL(nl);
                    }
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tonKhoTable);
        scroll.setBorder(new LineBorder(new Color(230, 230, 230)));
        p.add(scroll, BorderLayout.CENTER);

        JLabel hint = new JLabel("  Nhấp đúp vào hàng để xem chi tiết nguyên liệu");
        hint.setFont(new Font("Roboto", Font.ITALIC, 12));
        hint.setForeground(Color.GRAY);
        hint.setBorder(new EmptyBorder(5, 5, 0, 0));
        p.add(hint, BorderLayout.SOUTH);

        return p;
    }

    // ═══════════════════════════════════════════════════════════════
    // TAB 2: NGUYÊN LIỆU — double-click mở chi tiết
    // ═══════════════════════════════════════════════════════════════
    private JPanel buildNguyenLieuTab() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(12, 5, 5, 5));

        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 230, 230)),
            new EmptyBorder(8, 15, 8, 15)
        ));
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        left.add(new JLabel(IconFontSwing.buildIcon(FontAwesome.SEARCH, 16, Color.GRAY)));
        txtNLSearch = new JTextField(22);
        txtNLSearch.setPreferredSize(new Dimension(0, 35));
        txtNLSearch.setFont(new Font("Roboto", Font.PLAIN, 14));
        txtNLSearch.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { searchNguyenLieu(); }
        });
        left.add(txtNLSearch);
        bar.add(left, BorderLayout.WEST);

        JButton btnAdd = createSmallBtn("  Thêm Nguyên Liệu", FontAwesome.PLUS, new Color(46, 204, 113));
        btnAdd.addActionListener(e -> handleAddNL());
        bar.add(btnAdd, BorderLayout.EAST);
        p.add(bar, BorderLayout.NORTH);

        // Table — hidden col 5 giữ NguyenLieu object
        String[] cols = {"Mã NL", "Tên Nguyên Liệu", "Đơn Vị Tính", "Đơn Giá Nhập", "Ngày Hết Hạn", "Object"};
        nlModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        nlTable = buildStyledTable(nlModel);
        nlTable.setDefaultRenderer(Object.class, new ZebraRenderer());
        nlTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        nlTable.removeColumn(nlTable.getColumnModel().getColumn(5));

        nlTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = nlTable.getSelectedRow();
                    if (row >= 0) {
                        NguyenLieu nl = (NguyenLieu) nlModel.getValueAt(row, 5);
                        handleEditNL(nl);
                    }
                }
            }
        });

        JScrollPane scroll = new JScrollPane(nlTable);
        scroll.setBorder(new LineBorder(new Color(230, 230, 230)));
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    // ═══════════════════════════════════════════════════════════════
    // TAB 3: NHÀ CUNG CẤP — double-click mở chi tiết
    // ═══════════════════════════════════════════════════════════════
    private JPanel buildNhaCungCapTab() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(12, 5, 5, 5));

        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 230, 230)),
            new EmptyBorder(8, 15, 8, 15)
        ));
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        left.add(new JLabel(IconFontSwing.buildIcon(FontAwesome.SEARCH, 16, Color.GRAY)));
        txtNCCSearch = new JTextField(22);
        txtNCCSearch.setPreferredSize(new Dimension(0, 35));
        txtNCCSearch.setFont(new Font("Roboto", Font.PLAIN, 14));
        txtNCCSearch.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { searchNhaCungCap(); }
        });
        left.add(txtNCCSearch);
        bar.add(left, BorderLayout.WEST);

        JButton btnAdd = createSmallBtn("  Thêm Nhà Cung Cấp", FontAwesome.PLUS, new Color(46, 204, 113));
        btnAdd.addActionListener(e -> handleAddNCC());
        bar.add(btnAdd, BorderLayout.EAST);
        p.add(bar, BorderLayout.NORTH);

        // Table — hidden col 5 giữ NhaCungCap object
        String[] cols = {"Mã NCC", "Tên Nhà Cung Cấp", "Số Điện Thoại", "Email", "Địa Chỉ", "Object"};
        nccModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        nccTable = buildStyledTable(nccModel);
        nccTable.setDefaultRenderer(Object.class, new ZebraRenderer());
        nccTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        nccTable.removeColumn(nccTable.getColumnModel().getColumn(5));

        nccTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = nccTable.getSelectedRow();
                    if (row >= 0) {
                        NhaCungCap ncc = (NhaCungCap) nccModel.getValueAt(row, 5);
                        handleEditNCC(ncc);
                    }
                }
            }
        });

        JScrollPane scroll = new JScrollPane(nccTable);
        scroll.setBorder(new LineBorder(new Color(230, 230, 230)));
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    // ═══════════════════════════════════════════════════════════════
    // TAB 4: PHIẾU NHẬP — double-click xem chi tiết
    // ═══════════════════════════════════════════════════════════════
    private JPanel buildPhieuNhapTab() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(12, 5, 5, 5));

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 8));
        bar.setBackground(Color.WHITE);
        bar.setBorder(new LineBorder(new Color(230, 230, 230)));

        JButton btnNhapHang = createSmallBtn("  Tạo Phiếu Nhập", FontAwesome.DOWNLOAD, new Color(46, 204, 113));
        btnNhapHang.setPreferredSize(new Dimension(180, 35));
        btnNhapHang.addActionListener(e -> handleTaoPhieuNhap());

        JButton btnRefresh = createSmallBtn("Làm mới", FontAwesome.REFRESH, new Color(220, 220, 220));
        btnRefresh.addActionListener(e -> loadPhieuNhapData());
        bar.add(btnRefresh);
        bar.add(btnNhapHang);
        p.add(bar, BorderLayout.NORTH);

        String[] cols = {"Mã Phiếu", "Ngày Nhập", "Kho", "Nhà Cung Cấp", "Nhân Viên", "Tổng Tiền"};
        pnModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        pnTable = buildStyledTable(pnModel);
        pnTable.setDefaultRenderer(Object.class, new ZebraRenderer());
        pnTable.getColumnModel().getColumn(1).setPreferredWidth(155);
        pnTable.getColumnModel().getColumn(5).setPreferredWidth(130);

        pnTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) viewPhieuNhapDetail();
            }
        });

        JScrollPane scroll = new JScrollPane(pnTable);
        scroll.setBorder(new LineBorder(new Color(230, 230, 230)));
        p.add(scroll, BorderLayout.CENTER);

        JLabel hint = new JLabel("  Nhấp đúp vào phiếu để xem chi tiết");
        hint.setFont(new Font("Roboto", Font.ITALIC, 12));
        hint.setForeground(Color.GRAY);
        hint.setBorder(new EmptyBorder(5, 5, 0, 0));
        p.add(hint, BorderLayout.SOUTH);
        return p;
    }

    // ═══════════════════════════════════════════════════════════════
    // LOAD DATA
    // ═══════════════════════════════════════════════════════════════
    private void loadAllData() {
        loadKhoCombo();
        loadTonKhoData();
        loadNguyenLieuData();
        loadNhaCungCapData();
        loadPhieuNhapData();
    }

    private void loadKhoCombo() {
        cbFilterKho.removeAllItems();
        cbFilterKho.addItem(new Kho("", "Tất cả Kho", null, null));
        for (Kho kho : controller.getAllKho()) cbFilterKho.addItem(kho);
    }

    private void loadTonKhoData() {
        tonKhoModel.setRowCount(0);
        List<TonKho> list = controller.getAllTonKho();
        Kho filterKho = (Kho) cbFilterKho.getSelectedItem();
        String filterMaKho = (filterKho != null && !filterKho.getMaKho().isEmpty()) ? filterKho.getMaKho() : null;

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for (TonKho tk : list) {
            if (filterMaKho != null && !tk.getMaKho().equals(filterMaKho)) continue;
            NguyenLieu nl = controller.getNguyenLieuById(tk.getMaNL());
            Kho kho       = controller.getKhoById(tk.getMaKho());
            String tenNL  = nl != null ? nl.getTenNL() : tk.getMaNL();
            String dvt    = nl != null ? nl.getDonViTinh() : "";
            String tenKho = kho != null ? kho.getTenKho() : tk.getMaKho();
            String ngayCN = tk.getNgayCapNhat() != null ? tk.getNgayCapNhat().format(fmt) : "";
            String status = tk.isSapHet() ? "⚠ Sắp hết" : "✓ Bình thường";

            tonKhoModel.addRow(new Object[]{
                tk.getMaTonKho(), tenKho, tk.getMaNL(), tenNL, dvt,
                tk.getSoLuongTon(), tk.getMucToiThieu(), ngayCN, status
            });
        }
    }

    private void loadNguyenLieuData() {
        nlModel.setRowCount(0);
        for (NguyenLieu nl : controller.getAllNguyenLieu()) {
            nlModel.addRow(new Object[]{
                nl.getMaNL(), nl.getTenNL(), nl.getDonViTinh(),
                CurrencyUtils.formatNoUnit(nl.getDonGiaNhap()) + " đ",
                nl.getNgayHetHan() != null ? nl.getNgayHetHan().toString() : "Không",
                nl
            });
        }
    }

    private void searchNguyenLieu() {
        String kw = txtNLSearch.getText().toLowerCase().trim();
        nlModel.setRowCount(0);
        for (NguyenLieu nl : controller.getAllNguyenLieu()) {
            if (nl.getTenNL().toLowerCase().contains(kw) || nl.getMaNL().toLowerCase().contains(kw)) {
                nlModel.addRow(new Object[]{
                    nl.getMaNL(), nl.getTenNL(), nl.getDonViTinh(),
                    CurrencyUtils.formatNoUnit(nl.getDonGiaNhap()) + " đ",
                    nl.getNgayHetHan() != null ? nl.getNgayHetHan().toString() : "Không",
                    nl
                });
            }
        }
    }

    private void loadNhaCungCapData() {
        nccModel.setRowCount(0);
        for (NhaCungCap ncc : controller.getAllNhaCungCap()) {
            nccModel.addRow(new Object[]{
                ncc.getMaNCC(), ncc.getTenNCC(),
                ncc.getSoDienThoai() != null ? ncc.getSoDienThoai() : "",
                ncc.getEmail() != null ? ncc.getEmail() : "",
                ncc.getDiaChi() != null ? ncc.getDiaChi() : "",
                ncc
            });
        }
    }

    private void searchNhaCungCap() {
        String kw = txtNCCSearch.getText().toLowerCase().trim();
        nccModel.setRowCount(0);
        for (NhaCungCap ncc : controller.getAllNhaCungCap()) {
            if (ncc.getTenNCC().toLowerCase().contains(kw) || ncc.getMaNCC().toLowerCase().contains(kw)) {
                nccModel.addRow(new Object[]{
                    ncc.getMaNCC(), ncc.getTenNCC(),
                    ncc.getSoDienThoai() != null ? ncc.getSoDienThoai() : "",
                    ncc.getEmail() != null ? ncc.getEmail() : "",
                    ncc.getDiaChi() != null ? ncc.getDiaChi() : "",
                    ncc
                });
            }
        }
    }

    private void loadPhieuNhapData() {
        pnModel.setRowCount(0);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for (PhieuNhap pn : controller.getAllPhieuNhap()) {
            Kho kho   = controller.getKhoById(pn.getMaKho());
            NhaCungCap ncc = controller.getNhaCungCapById(pn.getMaNCC());
            pnModel.addRow(new Object[]{
                pn.getMaPN(),
                pn.getNgayNhap() != null ? pn.getNgayNhap().format(fmt) : "",
                kho  != null ? kho.getTenKho()  : pn.getMaKho(),
                ncc  != null ? ncc.getTenNCC()  : pn.getMaNCC(),
                pn.getMaNV(),
                CurrencyUtils.formatNoUnit(pn.getTongTien()) + " đ"
            });
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // HANDLERS — NGUYÊN LIỆU
    // ═══════════════════════════════════════════════════════════════
    private void handleAddNL() {
        NguyenLieu nl = new NguyenLieu();
        nl.setMaNL(controller.generateNextMaNL());
        ui.dialog.NguyenLieuDialog dlg =
            new ui.dialog.NguyenLieuDialog((Frame) SwingUtilities.getWindowAncestor(this), nl, false);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            try {
                if (controller.addNguyenLieu(nl)) {
                    loadNguyenLieuData();
                    JOptionPane.showMessageDialog(this, "Thêm nguyên liệu thành công!");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleEditNL(NguyenLieu nl) {
        ui.dialog.NguyenLieuDialog dlg =
            new ui.dialog.NguyenLieuDialog((Frame) SwingUtilities.getWindowAncestor(this), nl, true);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            if (dlg.isDeleted()) {
                if (controller.deleteNguyenLieu(nl.getMaNL())) {
                    loadNguyenLieuData();
                    loadTonKhoData();
                    JOptionPane.showMessageDialog(this, "Đã xóa nguyên liệu!");
                } else {
                    JOptionPane.showMessageDialog(this, "Không thể xóa (có thể đang được sử dụng)!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                try {
                    if (controller.updateNguyenLieu(nl)) {
                        loadNguyenLieuData();
                        loadTonKhoData();
                        JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // HANDLERS — NHÀ CUNG CẤP
    // ═══════════════════════════════════════════════════════════════
    private void handleAddNCC() {
        NhaCungCap ncc = new NhaCungCap();
        ncc.setMaNCC(controller.generateNextMaNCC());
        ui.dialog.NhaCungCapDialog dlg =
            new ui.dialog.NhaCungCapDialog((Frame) SwingUtilities.getWindowAncestor(this), ncc, false);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            try {
                if (controller.addNhaCungCap(ncc)) {
                    loadNhaCungCapData();
                    JOptionPane.showMessageDialog(this, "Thêm nhà cung cấp thành công!");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleEditNCC(NhaCungCap ncc) {
        ui.dialog.NhaCungCapDialog dlg =
            new ui.dialog.NhaCungCapDialog((Frame) SwingUtilities.getWindowAncestor(this), ncc, true);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            if (dlg.isDeleted()) {
                if (controller.deleteNhaCungCap(ncc.getMaNCC())) {
                    loadNhaCungCapData();
                    JOptionPane.showMessageDialog(this, "Đã xóa nhà cung cấp!");
                } else {
                    JOptionPane.showMessageDialog(this, "Không thể xóa (có phiếu nhập liên quan)!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                try {
                    if (controller.updateNhaCungCap(ncc)) {
                        loadNhaCungCapData();
                        JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // HANDLERS — PHIẾU NHẬP
    // ═══════════════════════════════════════════════════════════════
    private void handleTaoPhieuNhap() {
        ui.dialog.PhieuNhapDialog dlg = new ui.dialog.PhieuNhapDialog((Frame) SwingUtilities.getWindowAncestor(this));
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            loadPhieuNhapData();
            loadTonKhoData();
        }
    }

    private void viewPhieuNhapDetail() {
        int row = pnTable.getSelectedRow();
        if (row < 0) return;
        String maPN = (String) pnModel.getValueAt(row, 0);
        List<ChiTietPhieuNhap> chiTiet = controller.getChiTietByPhieuNhap(maPN);

        StringBuilder sb = new StringBuilder("CHI TIẾT PHIẾU NHẬP: " + maPN + "\n");
        sb.append("─────────────────────────────────────────\n");
        sb.append(String.format("%-6s %-25s %8s %12s %14s\n", "Mã NL", "Tên NL", "SL", "Đơn Giá", "Thành Tiền"));
        sb.append("─────────────────────────────────────────\n");
        for (ChiTietPhieuNhap ct : chiTiet) {
            NguyenLieu nl = controller.getNguyenLieuById(ct.getMaNL());
            String tenNL = nl != null ? nl.getTenNL() : ct.getMaNL();
            sb.append(String.format("%-6s %-25s %8.2f %12s %14s\n",
                ct.getMaNL(), tenNL, ct.getSoLuong(),
                CurrencyUtils.format(ct.getDonGia()),
                CurrencyUtils.format(ct.getThanhTien())
            ));
        }
        JTextArea ta = new JTextArea(sb.toString());
        ta.setFont(new Font("Monospaced", Font.PLAIN, 13));
        ta.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(ta), "Chi tiết phiếu nhập", JOptionPane.INFORMATION_MESSAGE);
    }

    // ═══════════════════════════════════════════════════════════════
    // HELPERS — TABLE & BUTTON
    // ═══════════════════════════════════════════════════════════════
    private JTable buildStyledTable(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setRowHeight(55);
        t.setFont(new Font("Roboto", Font.PLAIN, 14));
        t.getTableHeader().setFont(new Font("Roboto", Font.BOLD, 14));
        t.getTableHeader().setBackground(new Color(236, 240, 241));
        t.setShowVerticalLines(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        return t;
    }

    private JButton createSmallBtn(String text, FontAwesome icon, Color bg) {
        JButton b = new JButton(text);
        Color fg = (bg.equals(new Color(220, 220, 220))) ? Color.BLACK : Color.WHITE;
        b.setIcon(IconFontSwing.buildIcon(icon, 14, fg));
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFont(new Font("Roboto", Font.BOLD, 13));
        b.setPreferredSize(new Dimension(160, 35));
        b.setFocusable(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    // ═══════════════════════════════════════════════════════════════
    // RENDERERS
    // ═══════════════════════════════════════════════════════════════
    class ZebraRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
            Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
            if (!sel) c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(252, 253, 255));
            return c;
        }
    }

    class TonKhoRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
            Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
            Object statusVal = tonKhoModel.getValueAt(row, 8);
            boolean warn = statusVal != null && statusVal.toString().contains("Sắp hết");
            if (!sel) {
                if (warn) {
                    c.setBackground(new Color(254, 243, 242));
                    c.setForeground(col == 8 ? WARN_COLOR : Color.BLACK);
                } else {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(252, 253, 255));
                    c.setForeground(col == 8 ? OK_COLOR : Color.BLACK);
                }
            }
            return c;
        }
    }
}
